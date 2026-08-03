const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
}

type CachedToken = { value: string; expiresAt: number }
let cachedToken: CachedToken | undefined

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  })
}

async function getApplicationToken(clientId: string, clientSecret: string) {
  if (cachedToken && cachedToken.expiresAt > Date.now() + 60_000) return cachedToken.value

  const credentials = btoa(`${clientId}:${clientSecret}`)
  const response = await fetch("https://api.ebay.com/identity/v1/oauth2/token", {
    method: "POST",
    headers: {
      Authorization: `Basic ${credentials}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      scope: "https://api.ebay.com/oauth/api_scope",
    }),
  })

  if (!response.ok) {
    const details = await response.text()
    console.error("eBay token request failed", response.status, details)
    throw new Error("eBay authentication failed")
  }

  const token = await response.json()
  cachedToken = {
    value: token.access_token,
    expiresAt: Date.now() + Number(token.expires_in ?? 7200) * 1000,
  }
  return cachedToken.value
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers: corsHeaders })
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405)

  try {
    const clientId = Deno.env.get("EBAY_CLIENT_ID")
    const clientSecret = Deno.env.get("EBAY_CLIENT_SECRET")
    const marketplaceId = Deno.env.get("EBAY_MARKETPLACE_ID") ?? "EBAY_US"
    if (!clientId || !clientSecret) return json({ error: "eBay is not configured" }, 503)

    const payload = await request.json()
    const query = String(payload.query ?? "").trim().slice(0, 200)
    if (query.length < 2) return json({ error: "Enter a barcode or product name" }, 400)

    const token = await getApplicationToken(clientId, clientSecret)
    const params = new URLSearchParams({ limit: "20" })
    if (/^\d{8,14}$/.test(query)) params.set("gtin", query)
    else params.set("q", query)

    const response = await fetch(`https://api.ebay.com/buy/browse/v1/item_summary/search?${params}`, {
      headers: {
        Authorization: `Bearer ${token}`,
        "X-EBAY-C-MARKETPLACE-ID": marketplaceId,
        "X-EBAY-C-ENDUSERCTX": "contextualLocation=country%3DUS%2Czip%3D10001",
      },
    })

    if (!response.ok) {
      const details = await response.text()
      console.error("eBay search failed", response.status, details)
      return json({ error: "eBay search is temporarily unavailable" }, 502)
    }

    const data = await response.json()
    const items = (data.itemSummaries ?? []).map((item: Record<string, any>) => ({
      id: item.itemId,
      title: item.title,
      price: Number(item.price?.value ?? 0),
      currency: item.price?.currency ?? "USD",
      condition: item.condition ?? null,
      imageUrl: item.image?.imageUrl ?? null,
      itemUrl: item.itemWebUrl ?? null,
      shippingPrice: Number(item.shippingOptions?.[0]?.shippingCost?.value ?? 0),
    })).filter((item: { price: number }) => item.price > 0)

    const prices = items.map((item: { price: number }) => item.price)
    const average = prices.length ? prices.reduce((sum: number, price: number) => sum + price, 0) / prices.length : 0
    return json({
      provider: "ebay",
      query,
      total: Number(data.total ?? items.length),
      items,
      summary: {
        lowest: prices.length ? Math.min(...prices) : 0,
        highest: prices.length ? Math.max(...prices) : 0,
        average,
      },
    })
  } catch (error) {
    console.error("Unhandled eBay search error", error)
    return json({ error: "Unable to complete eBay search" }, 500)
  }
})


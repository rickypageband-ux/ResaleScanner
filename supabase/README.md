# Resale Scanner backend

The `ebay-search` Edge Function exchanges the server-only eBay credentials for an application token and queries eBay's Browse API. It returns normalized active-listing prices and never returns credentials or access tokens.

Required Supabase Edge Function secrets:

- `EBAY_CLIENT_ID`
- `EBAY_CLIENT_SECRET`
- `EBAY_MARKETPLACE_ID=EBAY_US`
- `EBAY_ENVIRONMENT=production`

Deploy from a linked Supabase CLI session:

```shell
supabase functions deploy ebay-search --project-ref kqraflpgcrpcioyuroxv
```

Calls require the project's publishable Supabase key. Before a public app release, add user authentication, per-user quotas, and abuse protection.

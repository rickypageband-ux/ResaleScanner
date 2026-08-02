package com.resalescanner.app

import android.app.Application

class ResaleScannerApplication : Application() {
    val container: AppContainer by lazy { DefaultAppContainer(this) }
}


package com.quash.bugger

import android.app.Application
import com.quash.bugs.Quash


class BuggerApplication : Application() {

    companion object {
        lateinit var instance: BuggerApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Quash.initialize(
            context = this,
            applicationKey = "0UNFLdbg4a5bf36da03bcfa3687f9e965544afc67888ee21d20b6c1a8dc476a9e4e90abd",
            enableNetworkLogging = true,
            sessionLength = 60
        )
    }
}
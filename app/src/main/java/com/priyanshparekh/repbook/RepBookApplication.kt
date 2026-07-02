package com.priyanshparekh.repbook

import android.app.Application

class RepBookApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

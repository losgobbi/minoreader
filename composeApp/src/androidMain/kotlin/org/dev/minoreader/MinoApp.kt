package org.dev.minoreader

import android.app.Application
import org.dev.minoreader.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MinoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MinoApp)
            modules(appModules())
        }
    }
}

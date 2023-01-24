package ru.unlucky.bodyposition

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.kpfu.health.di.healthModule
import ru.unlucky.bodyposition.di.presenterModule

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(listOf(healthModule, presenterModule))
        }

        RxJavaPlugins.setErrorHandler { it.printStackTrace() }
        initPython()
    }

    private fun initPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

}
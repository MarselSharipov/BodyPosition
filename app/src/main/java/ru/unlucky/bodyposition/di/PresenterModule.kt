package ru.unlucky.bodyposition.di

import ru.unlucky.bodyposition.ui.MainPresenter
import org.koin.dsl.module
import ru.kpfu.health.repo.HealthRepository

val presenterModule = module {

//    factory { MainPresenter() }
    factory { MainPresenter(get()) }

    single { HealthRepository() }
}
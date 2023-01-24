package ru.kpfu.health.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.kpfu.health.body_position.HARClassifier
import ru.kpfu.health.body_position.HumanActivityDetection
import ru.kpfu.health.movement.MovementDetection
import ru.kpfu.health.utils.HealthUtils

val healthModule = module {
    single { HealthUtils(androidContext()) }
    single { HARClassifier(androidContext()) }
    single { HumanActivityDetection(androidContext(), get(), get()) }
    single { MovementDetection(androidContext(), get()) }
}
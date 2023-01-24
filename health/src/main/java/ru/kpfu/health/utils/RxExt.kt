package ru.kpfu.health.utils

import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.observeOnIo(): Observable<T> = this.observeOn(Schedulers.io())
fun <T> Observable<T>.subscribeOnIo(): Observable<T> = this.subscribeOn(Schedulers.io())

fun Disposable.putIn(disposables: CompositeDisposable) {
    disposables.add(this)
}
package ru.unlucky.bodyposition.ui

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpPresenter
import ru.kpfu.health.body_position.HumanActivityDetection
import ru.kpfu.health.body_position.HumanActivityEnum
import ru.kpfu.health.repo.HealthRepository
import ru.kpfu.health.utils.putIn

class MainPresenter(private val healthRepository: HealthRepository): MvpPresenter<IMainActivity>() {
//class MainPresenter(private val humanActivityDetection: HumanActivityDetection): MvpPresenter<IMainActivity>() {

    private val disposables = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        initBodyPosition()
        viewState.startAppService()

    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun initBodyPosition() {
        healthRepository.bodyPositionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onBodyPositionChanged)
            .putIn(disposables)
    }

    private fun onBodyPositionChanged(position: HumanActivityEnum) {
        Log.d("MainPresenter", "onBodyPositionChanged: $position")
        viewState.showBodyPosition(position)
    }
}
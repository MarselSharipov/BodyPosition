package ru.unlucky.bodyposition.ui

import moxy.MvpView
import ru.kpfu.health.body_position.HumanActivityEnum

interface IMainActivity: MvpView {

    fun showBodyPosition(position: HumanActivityEnum)

    fun startAppService()

}
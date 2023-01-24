package ru.kpfu.health.repo

import io.reactivex.subjects.PublishSubject
import ru.kpfu.health.body_position.HumanActivityEnum
import ru.kpfu.health.movement.MovementEnum

// Класс, где будут содержаться сабжекты, которые будут слушаться в app модуле
class HealthRepository {

    val bodyPositionSubject = PublishSubject.create<HumanActivityEnum>()

    val movementSubject = PublishSubject.create<MovementEnum>()

}
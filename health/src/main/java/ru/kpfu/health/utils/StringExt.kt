package ru.kpfu.health.utils

fun ArrayList<String>.arrayToString(): String {
    return this.joinToString(separator = "\n")
}
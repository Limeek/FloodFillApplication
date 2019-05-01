package ru.limeek.floodfillapp.model

enum class Algorithm(var string: String) {
    ONE("Алгоритм №1"),
    TWO("Алгоритм №2"),
    THREE("Алгоритм №3"),
    FOUR("Алгоритм №4");

    override fun toString(): String {
        return string
    }
}
package com.example.focuslearn.questionset

data class Question(
    val id: Int,
    val question: String,
    val option_one: String,
    val option_two: String,
    val option_three: String,
    val option_four: String,
    val correct_answer: Int
)
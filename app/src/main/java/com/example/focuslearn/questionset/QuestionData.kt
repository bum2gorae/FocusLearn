package com.example.focuslearn.questionset

object QuestionData {
    fun getQuestions(): List<Question> {
        return listOf(
            Question(
                1, "중대 재해 처벌법에 따라 중대 재해로 정의되는 사고는 무엇입니까?",
                "1. 단순 근로자 상해",
                "2. 1인 이상의 사망자가 발생하는 사고",
                "3. 근로자의 일시적 업무 중단",
                "4. 업무와 무관한 사고",
                2
            ),
            Question(
                2, "중대 재해 처벌법의 주요 목적은 무엇입니까?",
                "1. 근로자의 생산성 향상",
                "2. 사업주의 법적 책임 강화",
                "3. 근로자의 건강과 안전 보장",
                "4. 근로자의 근로시간 단축",
                3
            ),
            Question(
                3, "중대 재해 처벌법에 의해 처벌받을 수 있는 주체는 누구입니까?",
                "1. 근로자",
                "2. 사업주 및 경영책임자",
                "3. 정부 공무원",
                "4. 일반 시민",
                2
            )
        )
    }
}
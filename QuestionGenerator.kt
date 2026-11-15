package com.brainrush.app.domain.generator

import com.brainrush.app.domain.models.Difficulty
import com.brainrush.app.domain.models.MathOperation
import com.brainrush.app.domain.models.MathQuestion
import kotlin.random.Random

class QuestionGenerator(private val random: Random = Random.Default) {
    private var sequenceId = 0L
    private var lastSignature: String? = null

    fun resetHistory() { lastSignature = null }

    fun generate(selectedOperations: Set<MathOperation>, difficulty: Difficulty): MathQuestion {
        val availableOps = resolveConcreteOperations(selectedOperations)
        var attempts = 0
        var candidate: MathQuestion

        do {
            val operation = availableOps[random.nextInt(availableOps.size)]
            candidate = generateForOperation(operation, difficulty)
            val signature = "${candidate.leftOperand}:${candidate.operation}:${candidate.rightOperand}"
            attempts++
            if (signature != lastSignature || attempts >= 12) {
                lastSignature = signature
                break
            }
        } while (true)

        check(validateQuestion(candidate)) { "Generated invalid question: $candidate" }
        return candidate
    }

    fun validateQuestion(question: MathQuestion): Boolean {
        return when (question.operation) {
            MathOperation.ADDITION -> {
                val expected = question.leftOperand.toLong() + question.rightOperand.toLong()
                expected in Int.MIN_VALUE..Int.MAX_VALUE && expected.toInt() == question.correctAnswer
            }
            MathOperation.SUBTRACTION -> {
                val expected = question.leftOperand.toLong() - question.rightOperand.toLong()
                expected in Int.MIN_VALUE..Int.MAX_VALUE && question.correctAnswer >= 0 && expected.toInt() == question.correctAnswer
            }
            MathOperation.MULTIPLICATION -> {
                val expected = question.leftOperand.toLong() * question.rightOperand.toLong()
                expected in Int.MIN_VALUE..Int.MAX_VALUE && expected.toInt() == question.correctAnswer
            }
            MathOperation.DIVISION -> {
                question.rightOperand != 0 && question.leftOperand % question.rightOperand == 0 && question.leftOperand / question.rightOperand == question.correctAnswer
            }
            MathOperation.MIXED -> false
        }
    }

    private fun resolveConcreteOperations(selected: Set<MathOperation>): List<MathOperation> {
        if (selected.isEmpty() || selected.contains(MathOperation.MIXED)) return MathOperation.concreteOperations()
        val filtered = selected.filter { it != MathOperation.MIXED }
        return filtered.ifEmpty { MathOperation.concreteOperations() }
    }

    private fun generateForOperation(operation: MathOperation, difficulty: Difficulty): MathQuestion {
        sequenceId++
        return when (operation) {
            MathOperation.ADDITION -> generateAddition(sequenceId, difficulty)
            MathOperation.SUBTRACTION -> generateSubtraction(sequenceId, difficulty)
            MathOperation.MULTIPLICATION -> generateMultiplication(sequenceId, difficulty)
            MathOperation.DIVISION -> generateDivision(sequenceId, difficulty)
            MathOperation.MIXED -> generateAddition(sequenceId, difficulty)
        }
    }

    private fun generateAddition(id: Long, difficulty: Difficulty): MathQuestion {
        val (a, b) = when (difficulty) {
            Difficulty.EASY -> random.nextInt(2, 21) to random.nextInt(2, 21)
            Difficulty.MEDIUM -> random.nextInt(15, 99) to random.nextInt(12, 99)
            Difficulty.HARD -> random.nextInt(110, 499) to random.nextInt(105, 499)
            Difficulty.EXPERT -> random.nextInt(450, 1950) to random.nextInt(350, 1950)
        }
        return MathQuestion(id, a, b, MathOperation.ADDITION, a + b)
    }

    private fun generateSubtraction(id: Long, difficulty: Difficulty): MathQuestion {
        val (rawA, rawB) = when (difficulty) {
            Difficulty.EASY -> random.nextInt(5, 25) to random.nextInt(2, 20)
            Difficulty.MEDIUM -> random.nextInt(25, 99) to random.nextInt(11, 89)
            Difficulty.HARD -> random.nextInt(200, 899) to random.nextInt(101, 699)
            Difficulty.EXPERT -> random.nextInt(900, 2999) to random.nextInt(350, 2499)
        }
        val a = maxOf(rawA, rawB)
        val b = minOf(rawA, rawB)
        return MathQuestion(id, a, b, MathOperation.SUBTRACTION, a - b)
    }

    private fun generateMultiplication(id: Long, difficulty: Difficulty): MathQuestion {
        val (a, b) = when (difficulty) {
            Difficulty.EASY -> random.nextInt(2, 10) to random.nextInt(2, 10)
            Difficulty.MEDIUM -> random.nextInt(4, 15) to random.nextInt(6, 19)
            Difficulty.HARD -> random.nextInt(12, 32) to random.nextInt(7, 19)
            Difficulty.EXPERT -> random.nextInt(18, 55) to random.nextInt(12, 35)
        }
        return MathQuestion(id, a, b, MathOperation.MULTIPLICATION, a * b)
    }

    private fun generateDivision(id: Long, difficulty: Difficulty): MathQuestion {
        val (divisor, quotient) = when (difficulty) {
            Difficulty.EASY -> random.nextInt(2, 10) to random.nextInt(2, 10)
            Difficulty.MEDIUM -> random.nextInt(4, 14) to random.nextInt(5, 18)
            Difficulty.HARD -> random.nextInt(7, 21) to random.nextInt(11, 29)
            Difficulty.EXPERT -> random.nextInt(12, 32) to random.nextInt(15, 45)
        }
        return MathQuestion(id, divisor * quotient, divisor, MathOperation.DIVISION, quotient)
    }
}

package com.example.calculatrice_tp1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private val operators = setOf('+', '-', 'x', '/', '%')
    private var expression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.displayText)
        restoreState(savedInstanceState)
        updateDisplay()

        setupNumberButtons()
        setupOperatorButtons()
        setupActionButtons()
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            expression = savedInstanceState.getString("expr", "") ?: ""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("expr", expression)
    }

    private fun updateDisplay() {
        display.text = expression.ifEmpty { "0" }
    }

    /** Number buttons 0-9 + negation + modulo */
    private fun setupNumberButtons() {
        val numberIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in numberIds) {
            findViewById<Button>(id)?.setOnClickListener {
                val digit = (it as Button).text.toString()
                onDigitPressed(digit)
            }
        }

        findViewById<Button>(R.id.btnNeg)?.setOnClickListener { onNegationPressed() }
        findViewById<Button>(R.id.btnModulo)?.setOnClickListener { onOperatorPressed('%') }
    }

    /** Operator buttons + - x / */
    private fun setupOperatorButtons() {
        findViewById<Button>(R.id.btnPlus)?.setOnClickListener { onOperatorPressed('+') }
        findViewById<Button>(R.id.btnMinus)?.setOnClickListener { onOperatorPressed('-') }
        findViewById<Button>(R.id.btnMultiply)?.setOnClickListener { onOperatorPressed('x') }
        findViewById<Button>(R.id.btnDivide)?.setOnClickListener { onOperatorPressed('/') }
    }

    /** Action buttons AC, del, = */
    private fun setupActionButtons() {
        findViewById<Button>(R.id.btnAC)?.setOnClickListener { onResetPressed() }
        findViewById<Button>(R.id.btnDelete)?.setOnClickListener { onBackPressedCustom() }
        findViewById<Button>(R.id.btnEqual)?.setOnClickListener { onEqualsPressed() }
    }

    private fun onDigitPressed(digit: String) {
        expression += digit
        updateDisplay()
    }

    private fun onOperatorPressed(op: Char) {
        if (expression.isEmpty()) return

        val lastChar = expression.last()
        if (operators.contains(lastChar)) {
            expression = expression.dropLast(1) + op
        } else {
            val idx = findOperatorIndex(expression)
            if (idx != -1) {
                val left = expression.substring(0, idx)
                val opOld = expression[idx]
                val right = expression.substring(idx + 1)
                if (right.isNotEmpty()) {
                    val computed = computeOperation(left, opOld, right)
                    if (computed != null) expression = computed + op
                    else {
                        expression = ""
                        Toast.makeText(this, "Erreur mathématique", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    expression = left + op
                }
            } else {
                expression += op
            }
        }
        updateDisplay()
    }

    private fun onEqualsPressed() {
        val idx = findOperatorIndex(expression)
        if (idx == -1) return
        val left = expression.substring(0, idx)
        val op = expression[idx]
        val right = expression.substring(idx + 1)
        if (right.isEmpty()) return
        val computed = computeOperation(left, op, right)
        if (computed != null) expression = computed
        else {
            expression = ""
            Toast.makeText(this, "Erreur mathématique", Toast.LENGTH_SHORT).show()
        }
        updateDisplay()
    }

    private fun onResetPressed() {
        expression = ""
        updateDisplay()
    }

    private fun onBackPressedCustom() {
        if (expression.isEmpty()) return
        expression = expression.dropLast(1)
        updateDisplay()
    }

    private fun onNegationPressed() {
        if (expression.isEmpty()) return

        val idx = findOperatorIndex(expression)
        if (idx == -1) {
            expression = toggleSign(expression)
        } else {
            val left = expression.substring(0, idx + 1)
            var right = expression.substring(idx + 1)
            if (right.isEmpty()) return
            right = toggleSign(right)
            expression = left + right
        }
        updateDisplay()
    }

    private fun toggleSign(num: String): String {
        return if (num.startsWith("-")) num.substring(1) else "-$num"
    }

    private fun findOperatorIndex(expr: String): Int {
        for (i in expr.indices) {
            val ch = expr[i]
            if (operators.contains(ch) && !(i == 0 && ch == '-')) return i
        }
        return -1
    }

    private fun computeOperation(leftStr: String, op: Char, rightStr: String): String? {
        return try {
            val l = leftStr.toInt()
            val r = rightStr.toInt()
            val res = when (op) {
                '+' -> l + r
                '-' -> l - r
                'x' -> l * r
                '/' -> if (r == 0) return null else l / r
                '%' -> if (r == 0) return null else l % r
                else -> return null
            }
            res.toString()
        } catch (e: Exception) {
            null
        }
    }
}

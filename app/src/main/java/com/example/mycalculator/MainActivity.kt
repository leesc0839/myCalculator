package com.example.mycalculator

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.mycalculator.model.History
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import java.lang.NumberFormatException
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private val inputTextView: TextView by lazy {
        findViewById(R.id.inputTextView)
    }

    private val outputTextView: TextView by lazy {
        findViewById(R.id.outputTextView)
    }

    private val historyConstraintLayout: ConstraintLayout by lazy {
        findViewById(R.id.historyConstraintLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById(R.id.historyLinearLayout)
    }

    lateinit var db: AppDataBase
    private var isOperator = false
    private var hasOperator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDb()
    }

    private fun initDb() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,
            "historyDB"
        ).build()
    }

    fun clickedButton(view: View) {
        when (view.id) {
            R.id.multiButton -> clickedOperatorButton("*")
            R.id.plusButton -> clickedOperatorButton("+")
            R.id.minusButton -> clickedOperatorButton("-")
            R.id.modularButton -> clickedOperatorButton("%")
            R.id.dividerButton -> clickedOperatorButton("/")
            R.id.dotButton -> clickedDotButton(".")
            R.id.button0 -> clickedNumberButton("0")
            R.id.button1 -> clickedNumberButton("1")
            R.id.button2 -> clickedNumberButton("2")
            R.id.button3 -> clickedNumberButton("3")
            R.id.button4 -> clickedNumberButton("4")
            R.id.button5 -> clickedNumberButton("5")
            R.id.button6 -> clickedNumberButton("6")
            R.id.button7 -> clickedNumberButton("7")
            R.id.button8 -> clickedNumberButton("8")
            R.id.button9 -> clickedNumberButton("9")
        }
    }

    private fun clickedDotButton(dot: String) {
        when {
            inputTextView.text.isEmpty() -> return
            inputTextView.text.last().code in 40..47 -> return
            inputTextView.text.last().code in 48..57 -> inputTextView.append(".")
        }
    }

    private fun clickedNumberButton(number: String) {
        if (isOperator) {
            inputTextView.append(" ")
            isOperator = false
        }

        val expression = inputTextView.text.split(" ")
        if (expression.isNotEmpty() && expression.last().length >= 15) {
            Snackbar.make(inputTextView, "15자리 이상 사용불가", Snackbar.LENGTH_SHORT).show()
            return
        } else if (expression.last().isNotEmpty() && expression.last().last() == '0' && number == "0")
            return
        inputTextView.append(number)

        outputTextView.text = calculateExpression()
    }

    @SuppressLint("SetTextI18n")
    private fun clickedOperatorButton(operator: String) {
        if (inputTextView.text.isEmpty()) {
            if (operator == "-") {
                inputTextView.append(operator)
                return
            } else {
                return
            }
        }

        when {
            isOperator -> {
                val text = inputTextView.text.toString()
                inputTextView.text = text.dropLast(1) + operator
            }
            hasOperator -> {
                Snackbar.make(inputTextView, "연산자는 한 개만 사용 가능 합니다.", Snackbar.LENGTH_SHORT).show()
                return
            }
            else -> {
                inputTextView.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(inputTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.greenForeground)),
            inputTextView.text.length - 1,
            inputTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        inputTextView.text = ssb

        isOperator = true
        hasOperator = true
    }

    fun clickedResultButton(view: View) {
        val expression = inputTextView.text.split(" ")
        if (expression.isEmpty() || expression.size != 3)
            return
        else if (expression[0].isNumber().not() || expression[2].isNumber().not())
            return
        val expressionText =
            inputTextView.text.toString() // 메인 스레드, dao insert 스레드 간의 실행순서는 모른다. 따라서 미리 저장해두는 것
        val resultText = calculateExpression()

        // db에 넣어주는 부분 , 디비에 넣는 과정은 모두 새로운 스레드로 생성 해야한다.
        Thread(Runnable {
            db.historyDao().insertHistory(
                History(
                    null,
                    expressionText,
                    resultText
                )
            ) // primaryKey 는 들어갈 때 자동으로 +1 이 된다.
        }).start()

        inputTextView.text = resultText
        outputTextView.text = ""

        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        val expression = inputTextView.text.split(" ")
        if (hasOperator.not() || expression.size != 3)
            return ""
        else if (expression[0].isNumber().not() || expression[2].isNumber().not()) {
            return ""
        }

        val first = expression[0].toDouble()
        val second = expression[2].toDouble()


        val result = when (expression[1]) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> first / second
            "%" -> first % second
            else -> 0.0
        }

        return if (result % 1 > 0) { // 소수점 존재
            result.toString()
        } else
            result.toInt().toString() // 소수점 존재 x
    }


    fun clickedClearButton(view: View) {
        isOperator = false
        hasOperator = false
        inputTextView.text = ""
        outputTextView.text = ""
    }

    fun clickedBackButton(view: View) {
        if (inputTextView.text.isEmpty())
            return

        when {
            inputTextView.text.length == 1 -> {
                inputTextView.text = ""
                return
            }
            inputTextView.text.last().code in 40..47 -> { // 연산자
                inputTextView.text = inputTextView.text.dropLast(2)
                isOperator = false
                hasOperator = false
            }
            else -> {
                inputTextView.text = inputTextView.text.dropLast(1)
                if ((inputTextView.text.last().code in 40..57).not())
                    inputTextView.text = inputTextView.text.dropLast(1)
            }
        }
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    fun clickedHistoryButton(view: View) {
        historyConstraintLayout.isVisible = true
        historyLinearLayout.removeAllViews()
        //room db 에서 데이터 가져오기 및 view 에 데이터 할당
        Thread(Runnable {
            db.historyDao().getAllHistory().reversed().forEach {
                runOnUiThread { // mainThread 의 handler 에 post 하는 함수 , 따라서 ui에 접근 가능하다.
                    val historyView =
                        LayoutInflater.from(this).inflate(R.layout.activity_history, null, false)
                    historyView.findViewById<TextView>(R.id.inputTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.outputTextView).text = " = ${it.result}"
                    historyLinearLayout.addView(historyView)

                }
            }
        }).start()

    }

    fun clickedHistoryClearButton(view: View) {
        //room db 에서 모든 기록 삭제 , view 에서 모든 기록 삭제
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().deleteAllHistory()
        }).start()
    }

    fun clickedHistoryCloseButton(view: View) {
        historyConstraintLayout.isVisible = false
    }

    private fun String.isNumber(): Boolean {
        return try {
            this.toBigDecimal()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

}
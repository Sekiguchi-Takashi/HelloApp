package com.sekiguchi.helloapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

// 画面2: 新規登録(日付・メモ・削除日 + 登録/クリア)
class EntryActivity : Activity() {

    private lateinit var dateField: EditText
    private lateinit var memoField: EditText
    private lateinit var deleteField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#FFF8E1"))
        }

        root.addView(TextView(this).apply {
            text = "新規登録"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#F06292"))
        })

        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.parseColor("#5D4037"))
            setPadding(0, 32, 0, 8)
        }

        // タップでカレンダーが開く日付入力欄
        fun dateInput(hint: String) = EditText(this).apply {
            this.hint = hint
            isFocusable = false
            isClickable = true
            textSize = 17f
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
            setOnClickListener { openPicker(this) }
        }

        root.addView(label("日付"))
        dateField = dateInput("タップして選択")
        dateField.setText(Store.today())
        root.addView(dateField)

        root.addView(label("メモ"))
        memoField = EditText(this).apply {
            hint = "内容を入力"
            textSize = 17f
            minLines = 3
            gravity = android.view.Gravity.TOP
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
        }
        root.addView(memoField)

        root.addView(label("削除日(この日まで表示)"))
        deleteField = dateInput("タップして選択")
        root.addView(deleteField)

        fun button(text: String, color: String, onClick: () -> Unit) = Button(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(color))
            setOnClickListener { onClick() }
        }

        fun lp() = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 32 }

        // 登録 → 保存して画面3へ
        root.addView(button("登録", "#F06292") {
            val date = dateField.text.toString()
            val memo = memoField.text.toString().trim()
            val del = deleteField.text.toString()
            when {
                memo.isEmpty() -> toast("メモを入力してください")
                del.isEmpty() -> toast("削除日を選択してください")
                del < Store.today() -> toast("削除日は今日以降にしてください")
                else -> {
                    Store.add(this, Entry(System.currentTimeMillis(), date, memo, del))
                    toast("登録しました")
                    startActivity(Intent(this, ListActivity::class.java))
                    finish()
                }
            }
        }, lp())

        // クリア → 入力を消して画面1へ戻る
        root.addView(button("クリア", "#90A4AE") {
            dateField.setText(Store.today())
            memoField.setText("")
            deleteField.setText("")
            finish()
        }, lp())

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun openPicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            target.setText(String.format(Locale.JAPAN, "%04d-%02d-%02d", y, m + 1, d))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

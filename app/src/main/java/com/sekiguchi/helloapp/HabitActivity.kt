package com.sekiguchi.helloapp

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

// 画面5: 習慣。土曜日〜金曜日を縦に並べ、各曜日の下に習慣を表示する。
class HabitActivity : Activity() {

    private lateinit var listArea: LinearLayout
    private val checks = mutableMapOf<Long, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#E8F5E9"))
        }

        root.addView(TextView(this).apply {
            text = "習慣"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#43A047"))
        })

        listArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(listArea) },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

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
        ).apply { topMargin = 24 }

        root.addView(button("追加", "#66BB6A") { showAddDialog() }, lp())

        root.addView(button("チェックした項目を削除", "#EF5350") {
            val ids = checks.filterValues { it.isChecked }.keys
            if (ids.isEmpty()) {
                Toast.makeText(this, "削除する項目にチェックを入れてください", Toast.LENGTH_SHORT).show()
            } else {
                HabitStore.removeIds(this, ids)
                Toast.makeText(this, "${ids.size}件削除しました", Toast.LENGTH_SHORT).show()
                refresh()
            }
        }, lp())

        root.addView(button("戻る", "#90A4AE") { finish() }, lp())

        setContentView(root)
        refresh()
    }

    private fun refresh() {
        checks.clear()
        listArea.removeAllViews()

        val habits = HabitStore.load(this)

        for (day in HabitStore.DAYS.indices) {
            // 曜日見出し(土曜=青系、日曜=赤系、平日=緑系)
            val headerColor = when (day) {
                0 -> "#1E88E5"
                1 -> "#E53935"
                else -> "#43A047"
            }
            listArea.addView(TextView(this).apply {
                text = HabitStore.DAYS[day]
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor(headerColor))
                setPadding(24, 14, 24, 14)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 })

            // その曜日の習慣を下に並べる
            val todays = habits.filter { it.day == day }
            if (todays.isEmpty()) {
                listArea.addView(TextView(this).apply {
                    text = "(なし)"
                    textSize = 13f
                    setTextColor(Color.GRAY)
                    setPadding(32, 8, 0, 0)
                })
            } else {
                for (h in todays) {
                    val cb = CheckBox(this).apply {
                        text = h.text
                        textSize = 15f
                        setTextColor(Color.parseColor("#37474F"))
                        setBackgroundColor(Color.WHITE)
                        setPadding(16, 16, 24, 16)
                    }
                    checks[h.id] = cb
                    listArea.addView(cb, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 8 })
                }
            }
        }
    }

    // 追加ボタン → 曜日と内容をポップアップで入力
    private fun showAddDialog() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@HabitActivity,
                android.R.layout.simple_spinner_dropdown_item, HabitStore.DAYS)
        }
        box.addView(TextView(this).apply { text = "曜日"; textSize = 14f })
        box.addView(spinner)

        val input = EditText(this).apply {
            hint = "習慣の内容(例: ランニング)"
            textSize = 16f
        }
        box.addView(TextView(this).apply {
            text = "内容"; textSize = 14f; setPadding(0, 24, 0, 0)
        })
        box.addView(input)

        AlertDialog.Builder(this)
            .setTitle("習慣を追加")
            .setView(box)
            .setPositiveButton("登録") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    Toast.makeText(this, "内容を入力してください", Toast.LENGTH_SHORT).show()
                } else {
                    HabitStore.add(this,
                        Habit(System.currentTimeMillis(), spinner.selectedItemPosition, text))
                    refresh()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
}

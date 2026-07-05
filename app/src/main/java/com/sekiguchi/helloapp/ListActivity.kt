package com.sekiguchi.helloapp

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

// 画面3: 登録一覧(日付が新しい順)。チェックした項目を削除できる
class ListActivity : Activity() {

    private lateinit var listArea: LinearLayout
    private val checks = mutableMapOf<Long, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#FFF8E1"))
        }

        root.addView(TextView(this).apply {
            text = "一覧(日付が新しい順)"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#F06292"))
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

        root.addView(button("チェックした項目を削除", "#EF5350") {
            val ids = checks.filterValues { it.isChecked }.keys
            if (ids.isEmpty()) {
                Toast.makeText(this, "削除する項目にチェックを入れてください", Toast.LENGTH_SHORT).show()
            } else {
                Store.removeIds(this, ids)
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

        // 画面2の「日付」で新しい順に並べる
        val sorted = Store.load(this).sortedByDescending { it.date }

        if (sorted.isEmpty()) {
            listArea.addView(TextView(this).apply {
                text = "登録はありません"
                textSize = 15f
                setTextColor(Color.GRAY)
                setPadding(0, 24, 0, 0)
            })
            return
        }

        for (e in sorted) {
            val cb = CheckBox(this).apply {
                text = "📅 ${e.date}  ${e.memo}\n　 削除日: ${e.deleteDate}"
                textSize = 15f
                setTextColor(Color.parseColor("#37474F"))
                setBackgroundColor(Color.WHITE)
                setPadding(16, 20, 24, 20)
            }
            checks[e.id] = cb
            listArea.addView(cb, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 })
        }
    }
}

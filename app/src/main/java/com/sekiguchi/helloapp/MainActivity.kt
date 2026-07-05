package com.sekiguchi.helloapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// 画面1: メニュー + ステータス表示
class MainActivity : Activity() {

    // false = 削除日が近い順(デフォルト) / true = 日付が新しい順
    private var sortByDate = false
    private lateinit var statusHeader: TextView
    private lateinit var listArea: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#FFF8E1"))
        }

        root.addView(TextView(this).apply {
            text = "Mycode"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#F06292"))
            gravity = Gravity.CENTER
        })

        fun button(label: String, color: String, onClick: () -> Unit) = Button(this).apply {
            text = label
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(color))
            setOnClickListener { onClick() }
        }

        fun lp() = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 24 }

        // ボタン1: 新規 → 画面2
        root.addView(button("新規", "#F06292") {
            startActivity(Intent(this, EntryActivity::class.java))
        }, lp())

        // ボタン2: 一覧へ → 画面3
        root.addView(button("一覧へ", "#4FC3F7") {
            startActivity(Intent(this, ListActivity::class.java))
        }, lp())

        // ボタン3: 並び替え(削除日順 ⇔ 日付順のトグル)
        root.addView(button("並び替え", "#FFB300") {
            sortByDate = !sortByDate
            refresh()
        }, lp())

        // ボタン4: メモリー → 画面4
        root.addView(button("メモリー", "#AB47BC") {
            startActivity(Intent(this, MemoryActivity::class.java))
        }, lp())

        statusHeader = TextView(this).apply {
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#5D4037"))
            setPadding(0, 40, 0, 12)
        }
        root.addView(statusHeader)

        listArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(listArea) },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        // メモリー(画面4で登録)はステータスに表示しない
        val entries = Store.load(this).filter { it.type != "memory" }
        val sorted = if (sortByDate)
            entries.sortedByDescending { it.date }   // 日付が新しい順
        else
            entries.sortedBy { it.deleteDate }       // 削除日(期限)が近い順

        statusHeader.text =
            if (sortByDate) "ステータス:日付が新しい順" else "ステータス:削除日が近い順"

        listArea.removeAllViews()
        if (sorted.isEmpty()) {
            listArea.addView(TextView(this).apply {
                text = "登録はありません"
                textSize = 15f
                setTextColor(Color.GRAY)
            })
            return
        }
        for (e in sorted) {
            listArea.addView(TextView(this).apply {
                text = "📅 ${e.date}  ${e.memo}\n　 削除日: ${e.deleteDate}"
                textSize = 15f
                setTextColor(Color.parseColor("#37474F"))
                setBackgroundColor(Color.WHITE)
                setPadding(24, 20, 24, 20)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 })
        }
    }
}

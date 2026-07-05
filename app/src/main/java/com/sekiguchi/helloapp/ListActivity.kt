package com.sekiguchi.helloapp

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File

// 画面3: 登録一覧(日付が新しい順)。
// 通常メモとメモリー(写真付き)を両方表示。写真(小)をタップで拡大表示。
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
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(8, 12, 16, 12)
            }

            val cb = CheckBox(this).apply {
                text = if (e.type == "memory")
                    "📷 ${e.date}  ${e.memo}"
                else
                    "📅 ${e.date}  ${e.memo}\n　 削除日: ${e.deleteDate}"
                textSize = 15f
                setTextColor(Color.parseColor("#37474F"))
            }
            checks[e.id] = cb
            row.addView(cb, LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            // 写真付きならサムネイルを表示。タップで拡大。
            if (e.photo.isNotEmpty() && File(e.photo).exists()) {
                val thumb = ImageView(this).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(decodeScaled(e.photo, 200))
                    setOnClickListener { showLarge(e.photo) }
                }
                row.addView(thumb, LinearLayout.LayoutParams(200, 200))
            }

            listArea.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 })
        }
    }

    // タップで写真を大きく表示(もう一度タップで閉じる)
    private fun showLarge(path: String) {
        val img = ImageView(this).apply {
            adjustViewBounds = true
            setImageBitmap(decodeScaled(path, 1200))
            setBackgroundColor(Color.BLACK)
        }
        val dlg = Dialog(this)
        dlg.setContentView(img)
        dlg.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        img.setOnClickListener { dlg.dismiss() }
        dlg.show()
    }

    // メモリ不足を避けるため縮小して読み込む
    private fun decodeScaled(path: String, req: Int): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        var sample = 1
        while (opts.outWidth / (sample * 2) >= req && opts.outHeight / (sample * 2) >= req) sample *= 2
        return BitmapFactory.decodeFile(path,
            BitmapFactory.Options().apply { inSampleSize = sample })
    }
}

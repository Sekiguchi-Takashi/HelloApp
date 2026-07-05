package com.sekiguchi.helloapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.Calendar
import java.util.Locale

// 画面4: メモリー登録(日付・メモ・写真)。
// ここで登録したものは画面1のステータスには出ず、自動削除もされない。
class MemoryActivity : Activity() {

    private val pickCode = 1
    private var photoUri: Uri? = null
    private lateinit var dateField: EditText
    private lateinit var memoField: EditText
    private lateinit var preview: ImageView
    private lateinit var previewLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#F3E5F5"))
        }

        root.addView(TextView(this).apply {
            text = "メモリー登録"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#AB47BC"))
        })

        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.parseColor("#5D4037"))
            setPadding(0, 32, 0, 8)
        }

        root.addView(label("日付"))
        dateField = EditText(this).apply {
            isFocusable = false
            isClickable = true
            textSize = 17f
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
            setText(Store.today())
            setOnClickListener { openPicker(this) }
        }
        root.addView(dateField)

        root.addView(label("メモ"))
        memoField = EditText(this).apply {
            hint = "思い出・記録を入力"
            textSize = 17f
            minLines = 3
            gravity = Gravity.TOP
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
        }
        root.addView(memoField)

        fun button(text: String, color: String, onClick: () -> Unit) = Button(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(color))
            setOnClickListener { onClick() }
        }

        fun lp(top: Int = 32) = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = top }

        // 写真選択
        root.addView(button("写真をアップロード", "#7E57C2") {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(Intent.createChooser(intent, "写真を選択"), pickCode)
        }, lp())

        previewLabel = TextView(this).apply {
            text = "写真: 未選択"
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 0)
        }
        root.addView(previewLabel)

        preview = ImageView(this).apply { adjustViewBounds = true }
        root.addView(preview, lp(8))

        // 登録 → 保存して画面3へ
        root.addView(button("登録", "#F06292") {
            val memo = memoField.text.toString().trim()
            if (memo.isEmpty()) {
                toast("メモを入力してください")
                return@button
            }
            val id = System.currentTimeMillis()
            var photoPath = ""
            photoUri?.let { uri ->
                runCatching {
                    val f = File(filesDir, "mem_$id.jpg")
                    contentResolver.openInputStream(uri)?.use { ins ->
                        f.outputStream().use { ins.copyTo(it) }
                    }
                    photoPath = f.absolutePath
                }.onFailure { toast("写真の保存に失敗しました(写真なしで登録します)") }
            }
            Store.add(this, Entry(id, dateField.text.toString(), memo, "", "memory", photoPath))
            toast("登録しました")
            startActivity(Intent(this, ListActivity::class.java))
            finish()
        }, lp())

        // クリア → 入力を消して画面1へ戻る
        root.addView(button("クリア", "#90A4AE") {
            dateField.setText(Store.today())
            memoField.setText("")
            photoUri = null
            preview.setImageBitmap(null)
            finish()
        }, lp())

        setContentView(ScrollView(this).apply { addView(root) })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickCode && resultCode == RESULT_OK) {
            photoUri = data?.data
            photoUri?.let {
                preview.setImageBitmap(decodeUriScaled(it, 600))
                previewLabel.text = "写真: 選択済み"
            }
        }
    }

    // 大きい写真をそのまま読むとメモリ不足になるため縮小して読み込む
    private fun decodeUriScaled(uri: Uri, req: Int): Bitmap? {
        contentResolver.openInputStream(uri)?.use { ins ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(ins, null, opts)
            var sample = 1
            while (opts.outWidth / (sample * 2) >= req && opts.outHeight / (sample * 2) >= req) sample *= 2
            contentResolver.openInputStream(uri)?.use { ins2 ->
                return BitmapFactory.decodeStream(ins2, null,
                    BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }
        return null
    }

    private fun openPicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            target.setText(String.format(Locale.JAPAN, "%04d-%02d-%02d", y, m + 1, d))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

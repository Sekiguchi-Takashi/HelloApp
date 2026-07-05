package com.sekiguchi.helloapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1件のメモ。
// type: "normal"(画面2) / "memory"(画面4)
// deleteDate: メモリーは空文字(自動削除なし)
// photo: メモリーの写真ファイルパス(なければ空文字)
data class Entry(
    val id: Long,
    val date: String,
    val memo: String,
    val deleteDate: String,
    val type: String = "normal",
    val photo: String = ""
)

object Store {
    private const val PREF = "mycode"
    private const val KEY = "entries"
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)

    fun today(): String = fmt.format(Date())

    // 読み込み時に「削除日を過ぎた通常メモ」を自動で消す。
    // 削除日が空(メモリー)のものは自動削除の対象外。
    fun load(context: Context): MutableList<Entry> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Entry>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Entry(
                    o.getLong("id"),
                    o.getString("date"),
                    o.getString("memo"),
                    o.optString("deleteDate", ""),
                    o.optString("type", "normal"),
                    o.optString("photo", "")
                )
            )
        }
        val t = today()
        val kept = list.filter { it.deleteDate.isEmpty() || it.deleteDate >= t }.toMutableList()
        if (kept.size != list.size) save(context, kept)
        return kept
    }

    fun save(context: Context, list: List<Entry>) {
        val arr = JSONArray()
        for (e in list) {
            arr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("date", e.date)
                    .put("memo", e.memo)
                    .put("deleteDate", e.deleteDate)
                    .put("type", e.type)
                    .put("photo", e.photo)
            )
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, e: Entry) {
        val list = load(context)
        list.add(e)
        save(context, list)
    }

    // 削除時、写真ファイルも一緒に消してストレージを圧迫しないようにする
    fun removeIds(context: Context, ids: Set<Long>) {
        val list = load(context)
        list.filter { it.id in ids && it.photo.isNotEmpty() }
            .forEach { runCatching { File(it.photo).delete() } }
        save(context, list.filter { it.id !in ids })
    }
}

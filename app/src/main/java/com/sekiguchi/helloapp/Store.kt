package com.sekiguchi.helloapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1件のメモ。dateは画面2の「日付」、deleteDateは「削除日」(いずれも yyyy-MM-dd)
data class Entry(
    val id: Long,
    val date: String,
    val memo: String,
    val deleteDate: String
)

object Store {
    private const val PREF = "mycode"
    private const val KEY = "entries"
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)

    fun today(): String = fmt.format(Date())

    // 読み込み時に「削除日を過ぎたもの」(=削除日の翌日以降)を自動で消す。
    // yyyy-MM-dd 形式は文字列比較がそのまま日付比較になる。
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
                    o.getString("deleteDate")
                )
            )
        }
        val t = today()
        val kept = list.filter { it.deleteDate >= t }.toMutableList()
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

    fun removeIds(context: Context, ids: Set<Long>) {
        save(context, load(context).filter { it.id !in ids })
    }
}

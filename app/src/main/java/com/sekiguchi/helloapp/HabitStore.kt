package com.sekiguchi.helloapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// 習慣1件。day: 0=土曜 〜 6=金曜
data class Habit(val id: Long, val day: Int, val text: String)

object HabitStore {
    private const val PREF = "mycode"
    private const val KEY = "habits"

    // 表示順は土曜始まり〜金曜
    val DAYS = listOf("土曜日", "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日")

    fun load(context: Context): MutableList<Habit> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Habit>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(Habit(o.getLong("id"), o.getInt("day"), o.getString("text")))
        }
        return list
    }

    fun save(context: Context, list: List<Habit>) {
        val arr = JSONArray()
        for (h in list) {
            arr.put(JSONObject().put("id", h.id).put("day", h.day).put("text", h.text))
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, h: Habit) {
        val list = load(context)
        list.add(h)
        save(context, list)
    }

    fun removeIds(context: Context, ids: Set<Long>) {
        save(context, load(context).filter { it.id !in ids })
    }
}

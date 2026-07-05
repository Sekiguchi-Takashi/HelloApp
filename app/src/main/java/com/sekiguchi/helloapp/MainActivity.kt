package com.sekiguchi.helloapp

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Hello World!\nGitHub Actionsビルド成功 🎉"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#1A237E"))
        }

        textView.setBackgroundColor(Color.parseColor("#E8EAF6"))
        setContentView(textView)
    }
}

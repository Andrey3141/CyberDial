package com.printer.cyberdial

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

class UpdateDialog(
    context: Context,
    private val currentVersion: String,
    private val latestVersion: String,
    private val changelog: String? = null,
    private val onUpdate: () -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val root = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#CC000000"))
            gravity = android.view.Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }

        val card = CardView(context).apply {
            val width = if (isLandscape) {
                (context.resources.displayMetrics.widthPixels * 0.7).toInt()
            } else {
                (context.resources.displayMetrics.widthPixels * 0.85).toInt()
            }
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            radius = 32f
            cardElevation = 20f
            setCardBackgroundColor(Color.parseColor("#17212B")) // Цвет как в Telegram
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        val icon = TextView(context).apply {
            text = "📱"
            textSize = 64f
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val title = TextView(context).apply {
            text = "Доступно обновление CyberDial!"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val version = TextView(context).apply {
            text = "Текущая: v$currentVersion → Новая: v$latestVersion"
            textSize = 16f
            setTextColor(Color.parseColor("#4CAF50"))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }

        val line = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply { bottomMargin = 24 }
            setBackgroundColor(Color.parseColor("#40FFFFFF"))
        }

        // Секция с changelog
        if (!changelog.isNullOrBlank()) {
            val changelogTitle = TextView(context).apply {
                text = "Что нового:"
                textSize = 16f
                setTextColor(Color.parseColor("#FFD700"))
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }

            val changelogText = TextView(context).apply {
                text = changelog.take(200) + if (changelog.length > 200) "..." else ""
                textSize = 14f
                setTextColor(Color.parseColor("#CCCCCC"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
                maxLines = 5
            }
            content.addView(changelogTitle)
            content.addView(changelogText)
        }

        val buttons = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val later = Button(context).apply {
            text = "Позже"
            textSize = 16f
            setTextColor(Color.parseColor("#8D8D93"))
            setBackgroundResource(R.drawable.button_rounded_gray)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 20 }
            setOnClickListener { dismiss() }
        }

        val update = Button(context).apply {
            text = "Обновить"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.button_rounded)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                dismiss()
                onUpdate()
            }
        }

        buttons.addView(later)
        buttons.addView(update)

        content.addView(icon)
        content.addView(title)
        content.addView(version)
        content.addView(line)
        content.addView(buttons)

        card.addView(content)
        root.addView(card)
        setContentView(root)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
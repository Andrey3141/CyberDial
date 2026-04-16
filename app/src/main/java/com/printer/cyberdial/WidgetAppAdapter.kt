package com.printer.cyberdial

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder

class WidgetAppAdapter(
    private val context: Context,
    private val apps: List<AppModel>,
    private val columns: Int = 4
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_WIDGET = 0
        private const val TYPE_APP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_WIDGET else TYPE_APP
    }

    override fun getItemCount(): Int = apps.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_WIDGET) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.widgets_header, parent, false)
            WidgetViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_icon, parent, false)
            AppViewHolder(view)
        }
    }

    private fun applyIconState(iconView: ImageView, overlay: View, lockIcon: ImageView, isActive: Boolean) {
        if (!isActive) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            matrix.postConcat(ColorMatrix().apply { setScale(0.5f, 0.5f, 0.5f, 1f) })
            iconView.colorFilter = ColorMatrixColorFilter(matrix)
            iconView.alpha = 0.6f
            overlay.visibility = View.VISIBLE
            lockIcon.visibility = View.VISIBLE
        } else {
            iconView.colorFilter = null
            iconView.alpha = 1f
            overlay.visibility = View.GONE
            lockIcon.visibility = View.GONE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppViewHolder) {
            val appPosition = position - 1
            if (appPosition < apps.size) {
                val app = apps[appPosition]
                holder.icon.setImageResource(app.iconResId)
                holder.name.text = app.name

                applyIconState(holder.icon, holder.inactiveOverlay, holder.lockIcon, app.isActive)

                if (app.isFolder && app.folderApps != null) {
                    holder.itemView.setOnClickListener {
                        val dialogView = LayoutInflater.from(holder.itemView.context)
                            .inflate(R.layout.dialog_folder, null)
                        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.folderAppsRecyclerView)

                        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(holder.itemView.context, 3)
                        recyclerView.adapter = AppIconAdapter(app.folderApps ?: emptyList(), 3)

                        androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                            .setTitle(app.name)
                            .setView(dialogView)
                            .setPositiveButton("Закрыть", null)
                            .show()
                    }
                } else {
                    holder.itemView.setOnClickListener { view ->
                        val pressAnim = ObjectAnimator.ofPropertyValuesHolder(
                            view,
                            PropertyValuesHolder.ofFloat("scaleX", 0.92f),
                            PropertyValuesHolder.ofFloat("scaleY", 0.92f)
                        )
                        pressAnim.duration = 80
                        pressAnim.start()

                        if (!app.isActive) {
                            ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                            Toast.makeText(view.context, "🔒 Приложение заблокировано", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        Toast.makeText(view.context, "📱 Открытие ${app.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    class WidgetViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val inactiveOverlay: View = view.findViewById(R.id.inactiveOverlay)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }
}
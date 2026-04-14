package com.printer.cyberdial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppViewHolder) {
            val appPosition = position - 1
            if (appPosition < apps.size) {
                val app = apps[appPosition]
                holder.icon.setImageResource(app.iconResId)
                holder.name.text = app.name

                if (app.isFolder && app.folderApps != null) {
                    holder.cardView.setCardBackgroundColor(0x33FFFFFF)
                    holder.itemView.setOnClickListener {
                        // Показываем диалог с папкой
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
                    holder.cardView.setCardBackgroundColor(0x4DFFFFFF)
                    holder.itemView.setOnClickListener {
                        Toast.makeText(holder.itemView.context, "Открытие ${app.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    class WidgetViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.appIconContainer)
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }
}
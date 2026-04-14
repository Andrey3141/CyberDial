package com.printer.cyberdial

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppIconAdapter(
    private var apps: List<AppModel>,
    private val columns: Int = 4
) : RecyclerView.Adapter<AppIconAdapter.ViewHolder>() {

    private var totalUnreadCount: Int = 0

    fun updateUnreadCount(count: Int) {
        totalUnreadCount = count
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIconContainer: CardView = view.findViewById(R.id.appIconContainer)
        val normalIconContainer: CardView = view.findViewById(R.id.normalIconContainer)
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val miniGridContainer: GridLayout = view.findViewById(R.id.miniGridContainer)
        val badgeContainer: FrameLayout = view.findViewById(R.id.badgeContainer)
        val badgeText: TextView = view.findViewById(R.id.badgeText)

        val miniIcon1: ImageView = view.findViewById(R.id.miniIcon1)
        val miniIcon2: ImageView = view.findViewById(R.id.miniIcon2)
        val miniIcon3: ImageView = view.findViewById(R.id.miniIcon3)
        val miniIcon4: ImageView = view.findViewById(R.id.miniIcon4)
        val miniIcon5: ImageView = view.findViewById(R.id.miniIcon5)
        val miniIcon6: ImageView = view.findViewById(R.id.miniIcon6)
        val miniIcon7: ImageView = view.findViewById(R.id.miniIcon7)
        val miniIcon8: ImageView = view.findViewById(R.id.miniIcon8)
        val miniIcon9: ImageView = view.findViewById(R.id.miniIcon9)

        val miniIconsList: List<ImageView> by lazy {
            listOf(miniIcon1, miniIcon2, miniIcon3, miniIcon4, miniIcon5,
                miniIcon6, miniIcon7, miniIcon8, miniIcon9)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.name.text = app.name

        if (app.isFolder && app.folderApps != null) {
            holder.normalIconContainer.visibility = View.GONE
            holder.appIconContainer.visibility = View.INVISIBLE
            holder.miniGridContainer.visibility = View.VISIBLE
            holder.badgeContainer.visibility = View.GONE

            val folderApps = app.folderApps!!
            val count = folderApps.size.coerceAtMost(9)

            for (i in 0 until 9) {
                val miniIcon = holder.miniIconsList[i]
                if (i < count) {
                    miniIcon.visibility = View.VISIBLE
                    try {
                        miniIcon.setImageResource(folderApps[i].iconResId)
                    } catch (e: Exception) {
                        miniIcon.setImageResource(R.drawable.ic_telegram_color)
                    }
                } else {
                    miniIcon.visibility = View.INVISIBLE
                }
            }

            holder.itemView.setOnClickListener {
                showFolderDialog(holder.itemView, app)
            }
        } else {
            holder.normalIconContainer.visibility = View.VISIBLE
            holder.appIconContainer.visibility = View.VISIBLE
            holder.miniGridContainer.visibility = View.GONE
            holder.icon.setImageResource(app.iconResId)

            if (app.name == "Telegram" && totalUnreadCount > 0) {
                holder.badgeContainer.visibility = View.VISIBLE
                holder.badgeText.text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString()
            } else {
                holder.badgeContainer.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                val scaleDown = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.fade_in)
                scaleDown.duration = 100
                holder.itemView.startAnimation(scaleDown)

                if (app.name == "Telegram") {
                    val intent = Intent(holder.itemView.context, TelegramSplashActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Открытие ${app.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showFolderDialog(view: View, folder: AppModel) {
        val dialogView = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_folder, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.folderAppsRecyclerView)

        val columnCount = 3
        recyclerView.layoutManager = GridLayoutManager(view.context, columnCount)
        recyclerView.adapter = AppIconAdapter(folder.folderApps ?: emptyList(), columnCount)

        AlertDialog.Builder(view.context)
            .setTitle(folder.name)
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    fun updateApps(newApps: List<AppModel>) {
        this.apps = newApps
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = apps.size
}
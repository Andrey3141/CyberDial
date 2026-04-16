package com.printer.cyberdial

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder

class AppIconAdapter(
    private var apps: List<AppModel>,
    private val columns: Int = 5
) : RecyclerView.Adapter<AppIconAdapter.ViewHolder>() {
    private var totalUnreadCount: Int = 0

    fun updateUnreadCount(count: Int) {
        totalUnreadCount = count
        notifyDataSetChanged()
    }

    private fun applyIconState(iconView: ImageView, overlay: View, lockIcon: ImageView, isActive: Boolean) {
        if (!isActive) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            matrix.postConcat(ColorMatrix().apply { setScale(0.4f, 0.4f, 0.4f, 1f) })
            iconView.colorFilter = ColorMatrixColorFilter(matrix)
            iconView.alpha = 0.7f
            overlay.visibility = View.VISIBLE
            lockIcon.visibility = View.VISIBLE
        } else {
            iconView.colorFilter = null
            iconView.alpha = 1f
            overlay.visibility = View.GONE
            lockIcon.visibility = View.GONE
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val inactiveOverlay: View = view.findViewById(R.id.inactiveOverlay)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
        val badgeContainer: FrameLayout = view.findViewById(R.id.badgeContainer)
        val badgeText: TextView = view.findViewById(R.id.badgeText)
        val miniGridContainer: GridLayout = view.findViewById(R.id.miniGridContainer)

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
            holder.icon.visibility = View.GONE
            holder.inactiveOverlay.visibility = View.GONE
            holder.lockIcon.visibility = View.GONE
            holder.miniGridContainer.visibility = View.VISIBLE
            holder.badgeContainer.visibility = View.GONE

            val folderApps = app.folderApps!!
            val count = folderApps.size.coerceAtMost(9)

            for (i in 0 until 9) {
                val miniIcon = holder.miniIconsList[i]
                if (i < count) {
                    miniIcon.visibility = View.VISIBLE
                    miniIcon.setImageResource(folderApps[i].iconResId)
                    // Не применяем белый фильтр к Telegram, иначе теряется голубой фон
                    if (folderApps[i].iconResId != R.drawable.ic_telegram_color) {
                        miniIcon.setColorFilter(Color.parseColor("#CCFFFFFF"), PorterDuff.Mode.SRC_IN)
                    } else {
                        miniIcon.colorFilter = null
                    }
                } else {
                    miniIcon.visibility = View.INVISIBLE
                }
            }

            holder.itemView.setOnClickListener {
                showFolderDialog(holder.itemView, app)
            }
        } else {
            holder.icon.visibility = View.VISIBLE
            holder.miniGridContainer.visibility = View.GONE
            holder.icon.setImageResource(app.iconResId)

            applyIconState(holder.icon, holder.inactiveOverlay, holder.lockIcon, app.isActive)

            if (app.name == "Telegram" && totalUnreadCount > 0) {
                holder.badgeContainer.visibility = View.VISIBLE
                holder.badgeText.text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString()
            } else {
                holder.badgeContainer.visibility = View.GONE
            }

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

                if (app.name == "Telegram") {
                    val intent = Intent(view.context, TelegramSplashActivity::class.java)
                    view.context.startActivity(intent)
                } else {
                    Toast.makeText(view.context, "📱 Открытие ${app.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFolderDialog(view: View, folder: AppModel) {
        val dialogView = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_folder, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.folderAppsRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(view.context, 3)
        recyclerView.adapter = AppIconAdapter(folder.folderApps ?: emptyList(), 3)

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
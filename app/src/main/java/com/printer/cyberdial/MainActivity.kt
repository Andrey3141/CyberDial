package com.printer.cyberdial

import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pageIndicator: LinearLayout
    private lateinit var clockText: TextView
    private lateinit var batteryIcon: ImageView
    private lateinit var batteryPercent: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    private lateinit var batteryRunnable: Runnable
    private lateinit var messageRepository: MessageRepository
    private lateinit var homeScreenAdapter: HomeScreenPagerAdapter
    private var unreadUpdateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        messageRepository = MessageRepository(this)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        setupWindowInsets()
        setupViewPager()
        setupDockBar()
        setupClock()
        setupBattery()
        startUnreadCountUpdater()
        checkForUpdates()
    }

    private fun checkForUpdates() {
        val updateChecker = UpdateChecker(this) { isAvailable, latestVersion, releaseUrl, apkUrl, changelog ->
            if (isAvailable && latestVersion != null) {
                val currentVersion = BuildConfig.VERSION_NAME
                val dialog = UpdateDialog(
                    this,
                    currentVersion,
                    latestVersion,
                    changelog
                ) {
                    // Открыть ссылку на скачивание APK
                    val url = if (apkUrl != null) apkUrl else releaseUrl
                    if (url != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Не удалось получить ссылку на обновление", Toast.LENGTH_LONG).show()
                    }
                }
                dialog.show()
            }
        }
        updateChecker.checkForUpdates()
    }

    private fun setupBattery() {
        batteryIcon = findViewById(R.id.batteryIcon)
        batteryPercent = findViewById(R.id.batteryPercent)

        batteryRunnable = object : Runnable {
            override fun run() {
                updateBattery()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(batteryRunnable)
    }

    private fun updateBattery() {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = isCharging(batteryManager)

        batteryPercent.text = "$batteryLevel%"
        updateBatteryIcon(batteryLevel, isCharging)
    }

    private fun isCharging(batteryManager: BatteryManager): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun updateBatteryIcon(level: Int, isCharging: Boolean) {
        val iconRes = when {
            isCharging -> R.drawable.stat_sys_battery_charging
            level >= 100 -> R.drawable.stat_sys_battery_100
            level >= 75 -> R.drawable.stat_sys_battery_75
            level >= 50 -> R.drawable.stat_sys_battery_50
            level >= 25 -> R.drawable.stat_sys_battery_25
            else -> R.drawable.stat_sys_battery_0
        }
        batteryIcon.setImageResource(iconRes)
    }

    private fun setupClock() {
        clockText = findViewById(R.id.clockText)
        timeRunnable = object : Runnable {
            override fun run() {
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                clockText.text = dateFormat.format(Date())
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(timeRunnable)
    }

    private fun startUnreadCountUpdater() {
        unreadUpdateRunnable = object : Runnable {
            override fun run() {
                updateTelegramBadge()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(unreadUpdateRunnable!!)
    }

    private fun updateTelegramBadge() {
        val totalUnread = messageRepository.getTotalUnreadCount()
        val fragment = supportFragmentManager.findFragmentByTag("f0")
        if (fragment is HomeScreenFragment) {
            fragment.updateTelegramBadge(totalUnread)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
        handler.removeCallbacks(batteryRunnable)
        unreadUpdateRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        pageIndicator = findViewById(R.id.pageIndicator)

        val totalPages = 2
        homeScreenAdapter = HomeScreenPagerAdapter(this, totalPages)
        viewPager.adapter = homeScreenAdapter

        setupPageIndicator(totalPages)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
            }
        })
    }

    private fun setupPageIndicator(totalPages: Int) {
        for (i in 0 until totalPages) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    24,
                    4
                ).apply {
                    marginStart = 6
                    marginEnd = 6
                }
                setBackgroundResource(R.drawable.page_indicator_inactive)
            }
            pageIndicator.addView(dot)
        }
        updatePageIndicator(0)
    }

    private fun updatePageIndicator(position: Int) {
        for (i in 0 until pageIndicator.childCount) {
            val dot = pageIndicator.getChildAt(i)
            dot.setBackgroundResource(
                if (i == position) R.drawable.page_indicator_active
                else R.drawable.page_indicator_inactive
            )
        }
    }

    private fun setupDockBar() {
        val dockPhone = findViewById<ImageView>(R.id.dockPhone)
        val dockMessages = findViewById<ImageView>(R.id.dockMessages)
        val dockBrowser = findViewById<ImageView>(R.id.dockBrowser)
        val dockCamera = findViewById<ImageView>(R.id.dockCamera)

        val dockItems = listOf(dockPhone, dockMessages, dockBrowser, dockCamera)

        dockItems.forEach { item ->
            item.setOnClickListener {
                it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                when (it.id) {
                    R.id.dockPhone -> showToast("Телефон")
                    R.id.dockMessages -> {
                        val intent = Intent(this, TelegramSplashActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.dockBrowser -> showToast("Браузер")
                    R.id.dockCamera -> showToast("Камера")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, "Открытие $message", Toast.LENGTH_SHORT).show()
    }
}
package com.printer.cyberdial

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class TelegramSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_splash)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Отключаем анимацию появления этого окна
        overridePendingTransition(0, 0)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, TelegramActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }, 1000)
    }
}
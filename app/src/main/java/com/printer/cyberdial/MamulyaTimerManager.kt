package com.printer.cyberdial

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

class MamulyaTimerManager private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: MamulyaTimerManager? = null

        fun getInstance(context: Context): MamulyaTimerManager {
            return instance ?: synchronized(this) {
                instance ?: MamulyaTimerManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("mamulya_timer", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerRunnable: Runnable? = null

    private var onTimerUpdateListener: ((secondsLeft: Int) -> Unit)? = null
    private var onTimerFinishListener: (() -> Unit)? = null

    init {
        // Если таймер не был запущен, но сохранено состояние - восстанавливаем
        if (isTimerStarted() && !isTimerFinished()) {
            val remainingTime = getRemainingTime()
            if (remainingTime > 0) {
                startTimer(remainingTime, fromResume = true)
            }
        }
    }

    fun startGlobalTimer(durationSeconds: Long = 60) {
        if (isTimerStarted() && !isTimerFinished()) {
            return // Таймер уже запущен
        }

        clearTimerState()
        saveTimerStartTime(System.currentTimeMillis())
        saveTimerDuration(durationSeconds)
        saveTimerFinished(false)
        startTimer(durationSeconds, fromResume = false)
    }

    private fun startTimer(durationSeconds: Long, fromResume: Boolean = false) {
        stopTimer()

        timerRunnable = object : Runnable {
            override fun run() {
                val remaining = getRemainingTime()

                if (remaining <= 0) {
                    onTimerComplete()
                } else {
                    onTimerUpdateListener?.invoke(remaining.toInt())
                    handler.postDelayed(this, 1000)
                }
            }
        }

        if (!fromResume) {
            onTimerUpdateListener?.invoke(durationSeconds.toInt())
        } else {
            onTimerUpdateListener?.invoke(getRemainingTime().toInt())
        }

        handler.post(timerRunnable!!)
    }

    private fun onTimerComplete() {
        saveTimerFinished(true)
        stopTimer()
        onTimerFinishListener?.invoke()
    }

    fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }

    fun getRemainingTime(): Long {
        if (!isTimerStarted()) return 60
        if (isTimerFinished()) return 0

        val startTime = prefs.getLong("timer_start_time", 0)
        val duration = prefs.getLong("timer_duration", 60)
        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        val remaining = duration - elapsed

        return if (remaining < 0) 0 else remaining
    }

    fun isTimerStarted(): Boolean {
        return prefs.getBoolean("timer_started", false)
    }

    fun isTimerFinished(): Boolean {
        return prefs.getBoolean("timer_finished", false)
    }

    fun setOnTimerUpdateListener(listener: (secondsLeft: Int) -> Unit) {
        this.onTimerUpdateListener = listener
        // Сразу отдаем текущее состояние
        if (isTimerStarted() && !isTimerFinished()) {
            listener.invoke(getRemainingTime().toInt())
        } else if (!isTimerStarted()) {
            listener.invoke(60)
        } else if (isTimerFinished()) {
            listener.invoke(0)
        }
    }

    fun setOnTimerFinishListener(listener: () -> Unit) {
        this.onTimerFinishListener = listener
    }

    private fun saveTimerStartTime(time: Long) {
        prefs.edit().putLong("timer_start_time", time).apply()
        prefs.edit().putBoolean("timer_started", true).apply()
    }

    private fun saveTimerDuration(duration: Long) {
        prefs.edit().putLong("timer_duration", duration).apply()
    }

    private fun saveTimerFinished(finished: Boolean) {
        prefs.edit().putBoolean("timer_finished", finished).apply()
    }

    private fun clearTimerState() {
        prefs.edit().remove("timer_start_time").apply()
        prefs.edit().remove("timer_duration").apply()
        prefs.edit().remove("timer_started").apply()
        prefs.edit().remove("timer_finished").apply()
    }

    fun resetTimer() {
        stopTimer()
        clearTimerState()
    }
}
package com.example.keyboardlauncher

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class FocusFragment : Fragment() {

    private lateinit var timeText: TextView
    private lateinit var todayText: TextView
    private lateinit var totalText: TextView

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var isPaused = false

    private var totalTimeMs = 25 * 60 * 1000L
    private var remainingMs = totalTimeMs
    private var elapsedMs = 0L

    private val PREFS = "focus_prefs"
    private val TODAY_MIN_KEY = "today_minutes"
    private val TOTAL_MIN_KEY = "total_minutes"
    private val TODAY_DATE_KEY = "today_date"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_focus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeText = view.findViewById(R.id.timeText)
        todayText = view.findViewById(R.id.todayText)
        totalText = view.findViewById(R.id.totalText)

        updateTimeText(remainingMs)
        updateStats()

        // TAP: start / pause / resume
        timeText.setOnClickListener {
            when {
                isRunning -> pauseTimer()
                isPaused -> resumeTimer()
                else -> startTimer()
            }
        }

        // LONG PRESS: stop & reset
        timeText.setOnLongClickListener {
            stopTimer()
            true
        }
    }

    // ───────── TIMER LOGIC ─────────

    private fun startTimer() {
        isRunning = true
        isPaused = false

        timer?.cancel()
        timer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(ms: Long) {
                remainingMs = ms
                elapsedMs += 1000
                updateTimeText(ms)
            }

            override fun onFinish() {
                finishSession()
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        isPaused = true
    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        isPaused = false

        saveElapsedMinutes()
        remainingMs = totalTimeMs
        elapsedMs = 0L
        updateTimeText(totalTimeMs)
        updateStats()
    }

    private fun finishSession() {
        isRunning = false
        isPaused = false
        timeText.text = "DONE"

        saveElapsedMinutes()
        remainingMs = totalTimeMs
        elapsedMs = 0L
        updateStats()
    }

    // ───────── STATS ─────────

    private fun saveElapsedMinutes() {
        val minutes = (elapsedMs / 60000).toInt()
        if (minutes <= 0) return

        val prefs = requireContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        resetIfNewDay(prefs)

        prefs.edit()
            .putInt(TODAY_MIN_KEY, prefs.getInt(TODAY_MIN_KEY, 0) + minutes)
            .putInt(TOTAL_MIN_KEY, prefs.getInt(TOTAL_MIN_KEY, 0) + minutes)
            .apply()
    }

    private fun resetIfNewDay(prefs: android.content.SharedPreferences) {
        val today = currentDate()
        if (prefs.getString(TODAY_DATE_KEY, null) != today) {
            prefs.edit()
                .putString(TODAY_DATE_KEY, today)
                .putInt(TODAY_MIN_KEY, 0)
                .apply()
        }
    }

    private fun updateStats() {
        val prefs = requireContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val today = prefs.getInt(TODAY_MIN_KEY, 0)
        val total = prefs.getInt(TOTAL_MIN_KEY, 0)

        todayText.text = "Today: $today min"
        totalText.text = "Total: ${total / 60} hr ${total % 60} min"
    }

    private fun updateTimeText(ms: Long) {
        val m = (ms / 1000) / 60
        val s = (ms / 1000) % 60
        timeText.text = String.format("%02d:%02d", m, s)
    }

    private fun currentDate(): String =
        java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
            .format(java.util.Date())

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}

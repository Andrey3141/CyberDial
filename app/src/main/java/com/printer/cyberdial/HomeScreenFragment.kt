package com.printer.cyberdial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeScreenFragment : Fragment() {
    companion object {
        private const val ARG_PAGE = "page"

        fun newInstance(page: Int): HomeScreenFragment {
            val fragment = HomeScreenFragment()
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private var currentPage = 0
    private var adapter: AppIconAdapter? = null
    private var layoutManager: GridLayoutManager? = null
    private lateinit var mamulyaTimerManager: MamulyaTimerManager
    private var timerTextView: TextView? = null

    // Состояния активности для виджетов
    private val widgetStates = mapOf(
        R.id.weatherWidget to true,   // Погода активна
        R.id.musicWidget to false,    // Музыка неактивна
        R.id.calendarWidget to false  // Календарь неактивен
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentPage = arguments?.getInt(ARG_PAGE, 0) ?: 0
        recyclerView = view.findViewById(R.id.appsRecyclerView)
        val widgetsContainer = view.findViewById<LinearLayout>(R.id.widgetsContainer)

        // Инициализируем глобальный менеджер таймера
        mamulyaTimerManager = MamulyaTimerManager.getInstance(requireContext())

        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null

        if (currentPage == 0) {
            widgetsContainer.visibility = View.VISIBLE
            setupWidgetClickListeners(view)
            setupWidgetsVisualState(view)
            setupMamulyaWidget(view)
        } else {
            widgetsContainer.visibility = View.GONE
        }

        setupAppsForPage()

        if (currentPage == 0) {
            val messageRepository = MessageRepository(requireContext())
            val totalUnread = messageRepository.getTotalUnreadCount()
            updateTelegramBadge(totalUnread)
        }
    }

    private fun setupWidgetsVisualState(view: View) {
        val weatherWidget = view.findViewById<LinearLayout>(R.id.weatherWidget)
        val musicWidget = view.findViewById<LinearLayout>(R.id.musicWidget)
        val calendarWidget = view.findViewById<LinearLayout>(R.id.calendarWidget)

        applyWidgetState(weatherWidget, widgetStates[R.id.weatherWidget] ?: true)
        applyWidgetState(musicWidget, widgetStates[R.id.musicWidget] ?: false)
        applyWidgetState(calendarWidget, widgetStates[R.id.calendarWidget] ?: false)
    }

    private fun applyWidgetState(widget: LinearLayout, isActive: Boolean) {
        if (!isActive) {
            widget.alpha = 0.6f
            widget.isClickable = false
            widget.isFocusable = false
        } else {
            widget.alpha = 1f
            widget.isClickable = true
            widget.isFocusable = true
        }
    }

    private fun setupMamulyaWidget(view: View) {
        val mamulyaWidget = view.findViewById<LinearLayout>(R.id.mamulyaWidget)
        timerTextView = view.findViewById(R.id.mamulyaTimer)

        mamulyaWidget.setOnClickListener {
            val remaining = mamulyaTimerManager.getRemainingTime()
            Toast.makeText(requireContext(), "🕒 Мамуля появится через ${remaining} секунд", Toast.LENGTH_SHORT).show()
        }

        // Подписываемся на обновления таймера
        mamulyaTimerManager.setOnTimerUpdateListener { secondsLeft ->
            timerTextView?.text = "${secondsLeft}c"
            if (secondsLeft <= 10 && secondsLeft > 0) {
                timerTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            } else if (secondsLeft == 0) {
                timerTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            } else {
                timerTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        }

        // Если таймер уже завершен, показываем 0
        if (mamulyaTimerManager.isTimerFinished()) {
            timerTextView?.text = "0c"
            timerTextView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
        }
    }

    fun updateTelegramBadge(unreadCount: Int) {
        adapter?.updateUnreadCount(unreadCount)
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
        if (currentPage == 0) {
            val messageRepository = MessageRepository(requireContext())
            val totalUnread = messageRepository.getTotalUnreadCount()
            updateTelegramBadge(totalUnread)

            // Обновляем таймер при возвращении на экран
            timerTextView?.let {
                val remaining = mamulyaTimerManager.getRemainingTime()
                it.text = "${remaining}c"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
        // Не отписываемся от таймера, т.к. виджет может быть пересоздан
    }

    private fun setupWidgetClickListeners(view: View) {
        val weatherWidget = view.findViewById<LinearLayout>(R.id.weatherWidget)
        val musicWidget = view.findViewById<LinearLayout>(R.id.musicWidget)
        val calendarWidget = view.findViewById<LinearLayout>(R.id.calendarWidget)

        weatherWidget.setOnClickListener {
            if (widgetStates[R.id.weatherWidget] == true) {
                Toast.makeText(requireContext(), "⛅ Погода: Солнечно, +24°C", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "📴 Погода недоступна", Toast.LENGTH_SHORT).show()
            }
        }

        musicWidget.setOnClickListener {
            if (widgetStates[R.id.musicWidget] == true) {
                Toast.makeText(requireContext(), "🎵 Музыка: плейлист 'Диалог с Мамулей'", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "📴 Музыка недоступна", Toast.LENGTH_SHORT).show()
            }
        }

        calendarWidget.setOnClickListener {
            if (widgetStates[R.id.calendarWidget] == true) {
                Toast.makeText(requireContext(), "📅 Сегодня важный день... Мамуля напишет!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "📴 Календарь недоступен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAppsForPage() {
        val allApps = getAllApps()
        val appsPerPage = 12
        val startIndex = currentPage * appsPerPage
        val endIndex = minOf(startIndex + appsPerPage, allApps.size)

        val pageApps = if (startIndex < allApps.size) {
            allApps.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        if (layoutManager == null) {
            layoutManager = GridLayoutManager(requireContext(), 5).apply {
                isAutoMeasureEnabled = false
            }
            recyclerView.layoutManager = layoutManager
        }

        if (adapter == null) {
            adapter = AppIconAdapter(pageApps, 5)
            recyclerView.adapter = adapter
        } else {
            adapter?.updateApps(pageApps)
        }

        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        recyclerView.addItemDecoration(GridSpacingItemDecoration(5, 12, true))
    }

    private fun getAllApps(): List<AppModel> {
        val socialApps = listOf(
            AppModel("WhatsApp", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("Telegram", R.drawable.ic_telegram_color, isActive = true),
            AppModel("Instagram", R.drawable.ic_instagram, isActive = false),
            AppModel("Facebook", android.R.drawable.ic_menu_manage, isActive = false),
            AppModel("Twitter", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("TikTok", android.R.drawable.ic_menu_week, isActive = false)
        )

        val googleApps = listOf(
            AppModel("Google", android.R.drawable.ic_menu_search, isActive = false),
            AppModel("Gmail", android.R.drawable.ic_menu_send, isActive = false),
            AppModel("Drive", android.R.drawable.ic_menu_save, isActive = false),
            AppModel("Maps", android.R.drawable.ic_menu_mapmode, isActive = false),
            AppModel("YouTube", android.R.drawable.ic_media_play, isActive = false),
            AppModel("Photos", android.R.drawable.ic_menu_gallery, isActive = false)
        )

        val entertainmentApps = listOf(
            AppModel("Netflix", android.R.drawable.ic_media_play, isActive = false),
            AppModel("Spotify", android.R.drawable.ic_media_play, isActive = false),
            AppModel("Music", android.R.drawable.ic_media_play, isActive = false),
            AppModel("Gallery", android.R.drawable.ic_menu_gallery, isActive = false)
        )

        return listOf(
            AppModel("Соцсети", android.R.drawable.ic_menu_edit, true, socialApps, isActive = false),
            AppModel("Google", android.R.drawable.ic_menu_search, true, googleApps, isActive = false),
            AppModel("YouTube", android.R.drawable.ic_media_play, isActive = false),
            AppModel("Развлечения", android.R.drawable.ic_media_play, true, entertainmentApps, isActive = false),
            AppModel("WhatsApp", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("Telegram", R.drawable.ic_telegram_color, isActive = true),
            AppModel("Instagram", R.drawable.ic_instagram, isActive = false),
            AppModel("Facebook", android.R.drawable.ic_menu_manage, isActive = false),
            AppModel("TikTok", android.R.drawable.ic_menu_week, isActive = false),
            AppModel("Maps", android.R.drawable.ic_menu_mapmode, isActive = false),
            AppModel("Gmail", android.R.drawable.ic_menu_send, isActive = false),
            AppModel("Drive", android.R.drawable.ic_menu_save, isActive = false),
            AppModel("Photos", android.R.drawable.ic_menu_gallery, isActive = false),
            AppModel("Calendar", android.R.drawable.ic_menu_today, isActive = false),
            AppModel("Clock", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("Calculator", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("Weather", android.R.drawable.ic_menu_info_details, isActive = false),
            AppModel("Settings", android.R.drawable.ic_menu_preferences, isActive = false),
            AppModel("Play Store", android.R.drawable.ic_menu_edit, isActive = false),
            AppModel("Files", android.R.drawable.ic_menu_save, isActive = false),
            AppModel("Camera", android.R.drawable.ic_menu_camera, isActive = false),
            AppModel("Contacts", android.R.drawable.ic_menu_call, isActive = false),
            AppModel("Messages", android.R.drawable.ic_menu_send, isActive = false),
            AppModel("Browser", android.R.drawable.ic_menu_search, isActive = false)
        )
    }
}
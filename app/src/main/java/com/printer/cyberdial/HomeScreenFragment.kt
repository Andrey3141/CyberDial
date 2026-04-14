package com.printer.cyberdial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
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

        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null

        if (currentPage == 0) {
            widgetsContainer.visibility = View.VISIBLE
            setupWidgetClickListeners(view)
        } else {
            widgetsContainer.visibility = View.GONE
        }

        setupAppsForPage()

        // Инициализируем бейдж для Telegram при создании фрагмента
        if (currentPage == 0) {
            val messageRepository = MessageRepository(requireContext())
            val totalUnread = messageRepository.getTotalUnreadCount()
            updateTelegramBadge(totalUnread)
        }
    }

    fun updateTelegramBadge(unreadCount: Int) {
        adapter?.updateUnreadCount(unreadCount)
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
        // Обновляем бейдж при возврате на главный экран
        if (currentPage == 0) {
            val messageRepository = MessageRepository(requireContext())
            val totalUnread = messageRepository.getTotalUnreadCount()
            updateTelegramBadge(totalUnread)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
    }

    private fun setupWidgetClickListeners(view: View) {
        val weatherWidget = view.findViewById<CardView>(R.id.weatherWidget)
        val musicWidget = view.findViewById<CardView>(R.id.musicWidget)
        val calendarWidget = view.findViewById<CardView>(R.id.calendarWidget)

        weatherWidget.setOnClickListener {
            Toast.makeText(requireContext(), "Открытие погоды", Toast.LENGTH_SHORT).show()
        }

        musicWidget.setOnClickListener {
            Toast.makeText(requireContext(), "Открытие музыки", Toast.LENGTH_SHORT).show()
        }

        calendarWidget.setOnClickListener {
            Toast.makeText(requireContext(), "Открытие календаря", Toast.LENGTH_SHORT).show()
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
            layoutManager = GridLayoutManager(requireContext(), 4).apply {
                isAutoMeasureEnabled = false
            }
            recyclerView.layoutManager = layoutManager
        }

        if (adapter == null) {
            adapter = AppIconAdapter(pageApps, 4)
            recyclerView.adapter = adapter
        } else {
            adapter?.updateApps(pageApps)
        }

        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        recyclerView.addItemDecoration(GridSpacingItemDecoration(4, 12, true))
    }

    private fun getAllApps(): List<AppModel> {
        val socialApps = listOf(
            AppModel("WhatsApp", android.R.drawable.ic_menu_edit),
            AppModel("Telegram", R.drawable.ic_telegram_color),
            AppModel("Instagram", android.R.drawable.ic_menu_camera),
            AppModel("Facebook", android.R.drawable.ic_menu_manage),
            AppModel("Twitter", android.R.drawable.ic_menu_edit),
            AppModel("TikTok", android.R.drawable.ic_menu_week)
        )

        val googleApps = listOf(
            AppModel("Google", android.R.drawable.ic_menu_search),
            AppModel("Gmail", android.R.drawable.ic_menu_send),
            AppModel("Drive", android.R.drawable.ic_menu_save),
            AppModel("Maps", android.R.drawable.ic_menu_mapmode),
            AppModel("YouTube", android.R.drawable.ic_media_play),
            AppModel("Photos", android.R.drawable.ic_menu_gallery)
        )

        val entertainmentApps = listOf(
            AppModel("Netflix", android.R.drawable.ic_media_play),
            AppModel("Spotify", android.R.drawable.ic_media_play),
            AppModel("Music", android.R.drawable.ic_media_play),
            AppModel("Gallery", android.R.drawable.ic_menu_gallery)
        )

        return listOf(
            AppModel("Соцсети", android.R.drawable.ic_menu_edit, true, socialApps),
            AppModel("Google", android.R.drawable.ic_menu_search, true, googleApps),
            AppModel("YouTube", android.R.drawable.ic_media_play),
            AppModel("Развлечения", android.R.drawable.ic_media_play, true, entertainmentApps),
            AppModel("WhatsApp", android.R.drawable.ic_menu_edit),
            AppModel("Telegram", R.drawable.ic_telegram_color),
            AppModel("Instagram", android.R.drawable.ic_menu_camera),
            AppModel("Facebook", android.R.drawable.ic_menu_manage),
            AppModel("TikTok", android.R.drawable.ic_menu_week),
            AppModel("Maps", android.R.drawable.ic_menu_mapmode),
            AppModel("Gmail", android.R.drawable.ic_menu_send),
            AppModel("Drive", android.R.drawable.ic_menu_save),
            AppModel("Photos", android.R.drawable.ic_menu_gallery),
            AppModel("Calendar", android.R.drawable.ic_menu_today),
            AppModel("Clock", android.R.drawable.ic_menu_edit),
            AppModel("Calculator", android.R.drawable.ic_menu_edit),
            AppModel("Weather", android.R.drawable.ic_menu_info_details),
            AppModel("Settings", android.R.drawable.ic_menu_preferences),
            AppModel("Play Store", android.R.drawable.ic_menu_edit),
            AppModel("Files", android.R.drawable.ic_menu_save),
            AppModel("Camera", android.R.drawable.ic_menu_camera),
            AppModel("Contacts", android.R.drawable.ic_menu_call),
            AppModel("Messages", android.R.drawable.ic_menu_send),
            AppModel("Browser", android.R.drawable.ic_menu_search)
        )
    }
}
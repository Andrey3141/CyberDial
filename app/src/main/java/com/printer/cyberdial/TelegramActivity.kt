package com.printer.cyberdial

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.abs

class TelegramActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnMenu: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var clockText: TextView
    private lateinit var batteryIcon: ImageView
    private lateinit var batteryPercent: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    private lateinit var updateRunnable: Runnable
    private lateinit var batteryRunnable: Runnable
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatAdapter: ChatAdapter
    private var chatsWithLastMessage = mutableListOf<ChatWithLastMessage>()
    private lateinit var mamulyaTimerManager: MamulyaTimerManager

    private val lastReadMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram)
        messageRepository = MessageRepository(this)

        // Используем глобальный менеджер таймера
        mamulyaTimerManager = MamulyaTimerManager.getInstance(this)

        loadLastRead()

        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        setupViews()
        setupClock()
        setupChatList()
        startChatUpdater()
        startRoleOnTimerFinish()
        startBatteryUpdater()
    }

    private fun startBatteryUpdater() {
        batteryRunnable = object : Runnable {
            override fun run() {
                updateBattery()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(batteryRunnable)
    }

    private fun updateBattery() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
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

    private fun startRoleOnTimerFinish() {
        // Подписываемся на завершение глобального таймера
        mamulyaTimerManager.setOnTimerFinishListener {
            runOnUiThread {
                if (!messageRepository.isMamulyaFirstMessageSent()) {
                    val random = Random()
                    val isMom = random.nextBoolean()
                    val role = if (isMom) "mom" else "scammer"

                    messageRepository.saveMamulyaRole(role)

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val currentTime = timeFormat.format(Date())

                    val firstMessageText = if (role == "mom") {
                        "Привет, родная. Как спалось?"
                    } else {
                        "Привет, родная. Как дела?"
                    }

                    val existingMessages = messageRepository.loadMessages("Мамуля 💖").toMutableList()
                    val firstBotMessage = ChatActivity.MessageModel(firstMessageText, false, currentTime, false, false)
                    existingMessages.add(firstBotMessage)
                    messageRepository.saveMessages("Мамуля 💖", existingMessages)
                    messageRepository.saveLastMessage("Мамуля 💖", firstMessageText, currentTime, "none")
                    messageRepository.saveMamulyaFirstMessageSent(true)

                    updateChats()
                }
            }
        }

        // Если таймер уже завершен, сразу отправляем сообщение
        if (mamulyaTimerManager.isTimerFinished() && !messageRepository.isMamulyaFirstMessageSent()) {
            val random = Random()
            val isMom = random.nextBoolean()
            val role = if (isMom) "mom" else "scammer"

            messageRepository.saveMamulyaRole(role)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val firstMessageText = if (role == "mom") {
                "Привет, родная. Как спалось?"
            } else {
                "Привет, родная. Как дела?"
            }

            val existingMessages = messageRepository.loadMessages("Мамуля 💖").toMutableList()
            val firstBotMessage = ChatActivity.MessageModel(firstMessageText, false, currentTime, false, false)
            existingMessages.add(firstBotMessage)
            messageRepository.saveMessages("Мамуля 💖", existingMessages)
            messageRepository.saveLastMessage("Мамуля 💖", firstMessageText, currentTime, "none")
            messageRepository.saveMamulyaFirstMessageSent(true)

            updateChats()
        }
    }

    private fun loadLastRead() {
        val prefs = getSharedPreferences("last_read", MODE_PRIVATE)
        val baseChats = listOf("Анна", "Михаил", "Мамуля 💖", "Папа", "Друг", "Работа", "Канал", "Группа")
        for (chat in baseChats) {
            lastReadMap[chat] = prefs.getInt("read_$chat", 0)
        }
    }

    private fun saveLastRead(chatName: String, count: Int) {
        val prefs = getSharedPreferences("last_read", MODE_PRIVATE)
        prefs.edit().putInt("read_$chatName", count).apply()
        lastReadMap[chatName] = count
        updateTotalUnreadCount()
    }

    private fun getUnreadCount(chatName: String): Int {
        val messages = messageRepository.loadMessages(chatName)
        val lastRead = lastReadMap[chatName] ?: 0
        var unread = 0
        for (i in lastRead until messages.size) {
            if (!messages[i].isOutgoing && !messages[i].isRead) {
                unread++
            }
        }
        return unread
    }

    private fun updateTotalUnreadCount() {
        val baseChats = listOf("Анна", "Михаил", "Мамуля 💖", "Папа", "Друг", "Работа", "Канал", "Группа")
        var total = 0
        for (chat in baseChats) {
            total += getUnreadCount(chat)
        }
        messageRepository.saveTotalUnreadCount(total)
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.chatRecyclerView)
        btnMenu = findViewById(R.id.btnMenu)
        btnSearch = findViewById(R.id.btnSearch)
        clockText = findViewById(R.id.clockText)
        batteryIcon = findViewById(R.id.batteryIcon)
        batteryPercent = findViewById(R.id.batteryPercent)

        btnMenu.setOnClickListener {
            Toast.makeText(this, "Меню", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            Toast.makeText(this, "Поиск", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClock() {
        timeRunnable = object : Runnable {
            override fun run() {
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                clockText.text = dateFormat.format(Date())
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(timeRunnable)
    }

    private fun startChatUpdater() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateChats()
                handler.postDelayed(this, 2000)
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateChats() {
        val updatedChats = mutableListOf<ChatWithLastMessage>()
        val baseChats = listOf(
            ChatModel("Анна", "#FF8A8A", true),
            ChatModel("Михаил", "#5EC8C8", true),
            ChatModel("Мамуля 💖", "#FFD966", true),
            ChatModel("Папа", "#9ECE6E", true),
            ChatModel("Друг", "#FFAD6A", true),
            ChatModel("Работа", "#B89AFF", false),
            ChatModel("Канал", "#F8A5C2", false),
            ChatModel("Группа", "#7EC8E3", false)
        )

        for (chat in baseChats) {
            val lastMsg = messageRepository.getLastMessage(chat.name)
            val lastMessageText = lastMsg?.first ?: "Напишите первое сообщение"
            val lastMessageTime = lastMsg?.second ?: ""
            val lastMessageStatus = lastMsg?.third ?: "none"

            val savedMessages = messageRepository.loadMessages(chat.name)
            val finalMessageText = if (savedMessages.isNotEmpty()) {
                savedMessages.last().text
            } else {
                lastMessageText
            }

            val finalMessageTime = if (savedMessages.isNotEmpty()) {
                savedMessages.last().time
            } else {
                lastMessageTime
            }

            val unreadCount = getUnreadCount(chat.name)

            updatedChats.add(ChatWithLastMessage(
                chat.name, chat.color, chat.isPrivate,
                finalMessageText, finalMessageTime, lastMessageStatus, unreadCount
            ))
        }

        updatedChats.sortByDescending { it.lastTime }

        chatsWithLastMessage.clear()
        chatsWithLastMessage.addAll(updatedChats)
        chatAdapter.notifyDataSetChanged()
        updateTotalUnreadCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(batteryRunnable)
        // Не отписываемся от таймера, т.к. он глобальный
    }

    private fun setupChatList() {
        val baseChats = listOf(
            ChatModel("Анна", "#FF8A8A", true),
            ChatModel("Михаил", "#5EC8C8", true),
            ChatModel("Мамуля 💖", "#FFD966", true),
            ChatModel("Папа", "#9ECE6E", true),
            ChatModel("Друг", "#FFAD6A", true),
            ChatModel("Работа", "#B89AFF", false),
            ChatModel("Канал", "#F8A5C2", false),
            ChatModel("Группа", "#7EC8E3", false)
        )

        chatsWithLastMessage.clear()
        for (chat in baseChats) {
            val lastMsg = messageRepository.getLastMessage(chat.name)
            val lastMessageText = lastMsg?.first ?: "Напишите первое сообщение"
            val lastMessageTime = lastMsg?.second ?: ""
            val lastMessageStatus = lastMsg?.third ?: "none"

            val savedMessages = messageRepository.loadMessages(chat.name)
            val finalMessageText = if (savedMessages.isNotEmpty()) {
                savedMessages.last().text
            } else {
                lastMessageText
            }

            val finalMessageTime = if (savedMessages.isNotEmpty()) {
                savedMessages.last().time
            } else {
                lastMessageTime
            }

            val unreadCount = getUnreadCount(chat.name)

            chatsWithLastMessage.add(ChatWithLastMessage(
                chat.name, chat.color, chat.isPrivate,
                finalMessageText, finalMessageTime, lastMessageStatus, unreadCount
            ))
        }

        chatsWithLastMessage.sortByDescending { it.lastTime }

        chatAdapter = ChatAdapter(chatsWithLastMessage) { chat ->
            val messages = messageRepository.loadMessages(chat.name)
            saveLastRead(chat.name, messages.size)

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chat_name", chat.name)
            intent.putExtra("chat_color", chat.color)
            intent.putExtra("is_private", chat.isPrivate)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
    }

    data class ChatModel(
        val name: String,
        val color: String,
        val isPrivate: Boolean
    )

    data class ChatWithLastMessage(
        val name: String,
        val color: String,
        val isPrivate: Boolean,
        val lastMessage: String,
        val lastTime: String,
        val lastStatus: String,
        val unreadCount: Int = 0
    )

    inner class ChatAdapter(
        private val chats: List<ChatWithLastMessage>,
        private val onChatClick: (ChatWithLastMessage) -> Unit
    ) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val avatarText: TextView = view.findViewById(R.id.avatarText)
            val name: TextView = view.findViewById(R.id.chatName)
            val message: TextView = view.findViewById(R.id.chatMessage)
            val time: TextView = view.findViewById(R.id.chatTime)
            val storyRing: CardView = view.findViewById(R.id.storyRing)
            val checkmarksContainer: LinearLayout = view.findViewById(R.id.checkmarksContainer)
            val checkmark1: ImageView = view.findViewById(R.id.checkmark1)
            val unreadBadge: TextView = view.findViewById(R.id.unreadBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            holder.name.text = chat.name
            holder.message.text = chat.lastMessage
            holder.time.text = chat.lastTime

            val firstLetter = chat.name.first().toString()
            holder.avatarText.text = firstLetter
            holder.avatarText.setBackgroundColor(android.graphics.Color.parseColor(chat.color))

            val storyValue = abs(chat.name.hashCode()) % 3
            val hasStory = chat.isPrivate && (storyValue == 0 || storyValue == 1)
            if (hasStory) {
                holder.storyRing.visibility = View.VISIBLE
            } else {
                holder.storyRing.visibility = View.GONE
            }

            if (chat.unreadCount > 0 && chat.isPrivate) {
                holder.unreadBadge.visibility = View.VISIBLE
                holder.unreadBadge.text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString()
            } else {
                holder.unreadBadge.visibility = View.GONE
            }

            if (!chat.isPrivate) {
                holder.checkmarksContainer.visibility = View.GONE
            } else {
                holder.checkmarksContainer.visibility = View.VISIBLE
                when (chat.lastStatus) {
                    "sent" -> {
                        holder.checkmark1.setImageResource(R.drawable.ic_check_single)
                        holder.checkmark1.setColorFilter(0xFF8D8D93.toInt())
                    }
                    "delivered" -> {
                        holder.checkmark1.setImageResource(R.drawable.ic_check_double)
                        holder.checkmark1.setColorFilter(0xFF8D8D93.toInt())
                    }
                    "read" -> {
                        holder.checkmark1.setImageResource(R.drawable.ic_check_double)
                        holder.checkmark1.setColorFilter(0xFF34B7F1.toInt())
                    }
                    else -> {
                        holder.checkmarksContainer.visibility = View.GONE
                    }
                }
            }

            holder.itemView.setOnClickListener {
                onChatClick(chat)
            }
        }

        override fun getItemCount(): Int = chats.size
    }
}
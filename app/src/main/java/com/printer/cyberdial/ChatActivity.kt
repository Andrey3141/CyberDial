package com.printer.cyberdial

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var chatAvatar: TextView
    private lateinit var chatName: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var clockText: TextView
    private lateinit var batteryIcon: ImageView
    private lateinit var batteryPercent: TextView

    private val messages = mutableListOf<MessageModel>()
    private lateinit var messageAdapter: MessageAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    private lateinit var batteryRunnable: Runnable

    private var chatTitle: String = ""
    private var chatColor: String = "#3D5A80"
    private var isPrivate: Boolean = true
    private lateinit var messageRepository: MessageRepository
    private var isReplyScheduled = false

    private var currentScenarioNode = "start"
    private val random = Random()

    private var mamulyaDialogStep = 0
    private var mamulyaRole: String? = null
    private var mamulyaDialogCompleted = false
    private var lastMessageCount = 0

    private val momBotMessages = listOf(
        "Привет, родная. Как спалось?",
        "😂 Кстати о выкупе. Ты не забыла, что у тёти Веры юбилей в субботу? Я записала нас с тобой на 15:00.",
        "Во-первых, печень полезно. Во-вторых, йога никуда не денется. В-третьих, я купила тебе платье.",
        "Нет. Вишнёвое. Очень идёт к твоим тёмным кругам под глазами, между прочим.",
        "Я любя. Приедешь ко мне завтра вечером? Платье померить. И кот по тебе скучает. Он твою комнату обнюхивает и грустит.",
        "Это тоже любовь. Приезжай в 18:00. Я суп с фрикадельками сварю.",
        "Они всегда для тебя маленькие. Даже когда тебе будет 50.\nотправила стикер: котёнок обнимает сердечко",
        "Это что за зверь? Почему не кот?",
        "Ага. Колючую, но голодную. Фрикадельки в 18:00. Не опаздывай.",
        "И я тебя. Целую в макушку. Хотя ты всё равно выше меня стала."
    )

    private val momUserResponses = listOf(
        "Привет. Нормально. Твой таракан мне снился. Он требовал выкуп хлебными крошками.",
        "Мам, я в субботу на йогу записалась. И вообще, тётя Вера каждый год говорит, что я «исхудала до ужаса» и кормит меня котлетами из печени.",
        "Опять бежевое?",
        "Спасибо, мам. За комплимент отдельное спасибо.",
        "Кот грустит по моей кровати, потому что ты не пускаешь его на свою.",
        "Договорились. Только фрикадельки пусть будут маленькие, как в детстве.",
        "отправила стикер: ёж в тапках",
        "Это ёж. Он символизирует мою колючую натуру.",
        "Не буду. Люблю тебя."
    )

    private val scammerBotMessages = listOf(
        "Привет, родная. Как дела?",
        "Ох уж этот таракан 😊 Ты не представляешь, я сегодня вспоминала, как ты маленькая боялась пауков. Забавно.",
        "Взрослая совсем. Слушай, дочень, я тут по делам бегала весь день. Устала как собака. И сейчас в магазине дурацкая ситуация — карта не проходит. Технический сбой в банке, говорят. А мне нужно купить тёте Вере подарок на юбилей, я уже выбрала.",
        "Да немного, около 15 тысяч. Я завтра же верну, как только банк починят. Ты же знаешь, я не люблю просить.",
        "Да, ту самую, на которую ты мне стикеры с котёнком кидала. Только, дочень, можно с пометкой «на подарок»? Просто для отчётности, сама знаешь, как банки любят блокировать.",
        "Ты моя умничка. Кстати, как тот бежевый свитер? Надела хоть раз?",
        "Ну и ладно. Твоё право. Главное, что ты есть у меня. Перевела?",
        "Секунду. Проверяю. Да, всё пришло, спасибо, солнышко. Ты меня спасла. Тётя Вера будет в восторге.",
        "Сюрприз. 😊 Ладно, беги, работай. Я завтра тебе фрикадельки сварю, приезжай вечером.",
        "И я тебя. Целую в макушку."
    )

    private val scammerUserResponses = listOf(
        "Привет. Нормально. Твой таракан мне снился.",
        "Ага. А теперь я сама пауков на балконе выношу.",
        "Ой, мам, давай я тебе переведу? Сколько надо?",
        "Конечно, мам. На карту какую? Ту, что у меня есть?",
        "Хорошо. Сейчас переведу.",
        "Мам, я его ни разу не надела. И не надену.",
        "Да, только что. Пришло?",
        "А что за подарок?",
        "Договорились. Люблю тебя."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        chatTitle = intent.getStringExtra("chat_name") ?: "Чат"
        chatColor = intent.getStringExtra("chat_color") ?: "#3D5A80"
        isPrivate = intent.getBooleanExtra("is_private", true)
        messageRepository = MessageRepository(this)

        if (chatTitle == "Мамуля 💖") {
            mamulyaRole = messageRepository.getMamulyaRole()
            mamulyaDialogCompleted = messageRepository.isMamulyaDialogCompleted()
        }

        setupViews()
        setupClock()
        setupBattery()
        loadMessages()
        setupOptionsContainer()

        startMessageChecker()
    }

    override fun onResume() {
        super.onResume()
        checkForNewMessages()
    }

    private fun setupBattery() {
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

    private fun startMessageChecker() {
        val checkerRunnable = object : Runnable {
            override fun run() {
                checkForNewMessages()
                handler.postDelayed(this, 2000)
            }
        }
        handler.post(checkerRunnable)
    }

    private fun checkForNewMessages() {
        if (chatTitle != "Мамуля 💖") return
        if (mamulyaDialogCompleted) return

        val currentMessages = messageRepository.loadMessages(chatTitle)
        if (currentMessages.size > lastMessageCount) {
            val newMessages = currentMessages.drop(lastMessageCount)
            messages.addAll(newMessages)
            messageAdapter.notifyItemRangeInserted(lastMessageCount, newMessages.size)
            recyclerView.scrollToPosition(messages.size - 1)
            lastMessageCount = messages.size

            val firstBotMessageText = if (mamulyaRole == "mom") momBotMessages[0] else scammerBotMessages[0]
            val hasFirstBotMessage = messages.any { it.text == firstBotMessageText && !it.isOutgoing }

            if (hasFirstBotMessage) {
                val userResponses = if (mamulyaRole == "mom") momUserResponses else scammerUserResponses
                val userResponseCount = messages.count { it.isOutgoing && it.text in userResponses }
                mamulyaDialogStep = userResponseCount

                if (mamulyaDialogStep >= userResponses.size) {
                    mamulyaDialogCompleted = true
                    messageRepository.saveMamulyaDialogCompleted(true)
                    optionsContainer.visibility = View.GONE
                } else {
                    showMamulyaOptions()
                }
            }
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.messagesRecyclerView)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.btnMenu)
        btnSearch = findViewById(R.id.btnSearch)
        chatAvatar = findViewById(R.id.chatAvatar)
        chatName = findViewById(R.id.chatName)
        clockText = findViewById(R.id.clockText)
        optionsContainer = findViewById(R.id.optionsContainer)
        batteryIcon = findViewById(R.id.batteryIcon)
        batteryPercent = findViewById(R.id.batteryPercent)

        val firstLetter = chatTitle.first().toString()
        chatAvatar.text = firstLetter
        chatAvatar.setBackgroundColor(android.graphics.Color.parseColor(chatColor))
        chatName.text = chatTitle

        btnBack.setOnClickListener {
            saveMessages()
            updateTotalUnreadCount()
            finish()
        }

        btnMenu.setOnClickListener {
            android.widget.Toast.makeText(this, "Меню", android.widget.Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            android.widget.Toast.makeText(this, "Поиск", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalUnreadCount() {
        val baseChats = listOf("Анна", "Михаил", "Мамуля 💖", "Папа", "Друг", "Работа", "Канал", "Группа")
        var total = 0
        for (chat in baseChats) {
            val chatMessages = messageRepository.loadMessages(chat)
            val prefs = getSharedPreferences("last_read", MODE_PRIVATE)
            val lastRead = prefs.getInt("read_$chat", 0)
            for (i in lastRead until chatMessages.size) {
                if (!chatMessages[i].isOutgoing && !chatMessages[i].isRead) {
                    total++
                }
            }
        }
        messageRepository.saveTotalUnreadCount(total)
    }

    private fun setupClock() {
        timeRunnable = object : Runnable {
            override fun run() {
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentTime = dateFormat.format(Date())
                clockText.text = currentTime
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(timeRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
        handler.removeCallbacks(batteryRunnable)
        saveMessages()
        updateTotalUnreadCount()
    }

    private fun saveMessages() {
        messageRepository.saveMessages(chatTitle, messages)
        if (messages.isNotEmpty()) {
            val lastMsg = messages.last()
            messageRepository.saveLastMessage(chatTitle, lastMsg.text, lastMsg.time,
                if (lastMsg.isOutgoing) {
                    when {
                        lastMsg.isRead -> "read"
                        lastMsg.isDelivered -> "delivered"
                        else -> "sent"
                    }
                } else "none"
            )
        }
    }

    private fun loadMessages() {
        val savedMessages = messageRepository.loadMessages(chatTitle)
        messages.clear()
        messages.addAll(savedMessages)
        lastMessageCount = messages.size

        messageAdapter = MessageAdapter(messages, chatColor)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter
        recyclerView.scrollToPosition(messages.size - 1)

        if (chatTitle == "Мамуля 💖" && mamulyaRole != null && !mamulyaDialogCompleted) {
            val firstBotMessageText = if (mamulyaRole == "mom") momBotMessages[0] else scammerBotMessages[0]
            val hasFirstBotMessage = messages.any { it.text == firstBotMessageText && !it.isOutgoing }

            if (hasFirstBotMessage) {
                val userResponses = if (mamulyaRole == "mom") momUserResponses else scammerUserResponses
                val userResponseCount = messages.count { it.isOutgoing && it.text in userResponses }
                mamulyaDialogStep = userResponseCount

                if (mamulyaDialogStep >= userResponses.size) {
                    mamulyaDialogCompleted = true
                    messageRepository.saveMamulyaDialogCompleted(true)
                    optionsContainer.visibility = View.GONE
                } else {
                    showMamulyaOptions()
                }
            }
            return
        }

        if (isPrivate && messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (!lastMessage.isOutgoing) {
                showOptionsForCurrentNode()
            }
        } else if (isPrivate && messages.isEmpty()) {
            startInitialGreeting()
        }
    }

    private fun showMamulyaOptions() {
        if (mamulyaDialogCompleted) {
            optionsContainer.visibility = View.GONE
            return
        }

        optionsContainer.removeAllViews()

        val currentStep = mamulyaDialogStep
        val isMom = mamulyaRole == "mom"

        val optionsText = if (isMom) {
            if (currentStep < momUserResponses.size) {
                listOf(momUserResponses[currentStep])
            } else {
                emptyList()
            }
        } else {
            if (currentStep < scammerUserResponses.size) {
                listOf(scammerUserResponses[currentStep])
            } else {
                emptyList()
            }
        }

        if (optionsText.isEmpty()) {
            optionsContainer.visibility = View.GONE
            mamulyaDialogCompleted = true
            messageRepository.saveMamulyaDialogCompleted(true)
            return
        }

        optionsContainer.visibility = View.VISIBLE

        for (optionText in optionsText) {
            val button = MaterialButton(this)
            button.text = optionText
            button.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            button.setOnClickListener {
                sendMamulyaResponse(optionText)
            }
            optionsContainer.addView(button)
        }
    }

    private fun sendMamulyaResponse(userText: String) {
        optionsContainer.visibility = View.GONE
        optionsContainer.removeAllViews()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val userMessage = MessageModel(userText, true, currentTime, false, false)
        messages.add(userMessage)
        lastMessageCount = messages.size
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        saveMessages()

        simulateUserMessageStatus(messages.size - 1)
    }

    private fun simulateUserMessageStatus(position: Int) {
        handler.postDelayed({
            if (position < messages.size && messages[position].isOutgoing && !messages[position].isDelivered) {
                messages[position] = messages[position].copy(isDelivered = true)
                messageAdapter.notifyItemChanged(position)
                saveMessages()

                handler.postDelayed({
                    if (position < messages.size && messages[position].isOutgoing && !messages[position].isRead) {
                        messages[position] = messages[position].copy(isRead = true)
                        messageAdapter.notifyItemChanged(position)
                        saveMessages()
                        updateTotalUnreadCount()

                        sendBotMamulyaResponse()
                    }
                }, (2000 + random.nextInt(4000)).toLong())
            }
        }, (1000 + random.nextInt(3000)).toLong())
    }

    private fun sendBotMamulyaResponse() {
        val isMom = mamulyaRole == "mom"
        val currentStep = mamulyaDialogStep

        val nextBotIndex = currentStep + 1

        val botText = if (isMom) {
            if (nextBotIndex < momBotMessages.size) {
                momBotMessages[nextBotIndex]
            } else {
                null
            }
        } else {
            if (nextBotIndex < scammerBotMessages.size) {
                scammerBotMessages[nextBotIndex]
            } else {
                null
            }
        }

        if (botText == null) {
            mamulyaDialogCompleted = true
            messageRepository.saveMamulyaDialogCompleted(true)
            optionsContainer.visibility = View.GONE
            return
        }

        handler.postDelayed({
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val botMessage = MessageModel(botText, false, currentTime, false, false)
            messages.add(botMessage)
            lastMessageCount = messages.size
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            saveMessages()
            updateTotalUnreadCount()

            mamulyaDialogStep++

            val nextStep = mamulyaDialogStep
            val hasMoreOptions = if (isMom) {
                nextStep < momUserResponses.size
            } else {
                nextStep < scammerUserResponses.size
            }

            if (hasMoreOptions) {
                showMamulyaOptions()
            } else {
                mamulyaDialogCompleted = true
                messageRepository.saveMamulyaDialogCompleted(true)
                optionsContainer.visibility = View.GONE
            }
        }, (1500 + random.nextInt(4000)).toLong())
    }

    private fun startInitialGreeting() {
        handler.postDelayed({
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val greeting = getBotMessageForNode("start")
            val replyMessage = MessageModel(greeting, false, currentTime, false, false)
            messages.add(replyMessage)
            lastMessageCount = messages.size
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            saveMessages()
            currentScenarioNode = "start"
            showOptionsForCurrentNode()
        }, 500)
    }

    private fun setupOptionsContainer() {}

    private fun showOptionsForCurrentNode() {
        if (chatTitle == "Мамуля 💖") return

        optionsContainer.removeAllViews()

        val options = getOptionsForNode(currentScenarioNode)
        if (options.isEmpty()) {
            optionsContainer.visibility = View.GONE
            return
        }

        optionsContainer.visibility = View.VISIBLE

        for (option in options) {
            val button = MaterialButton(this)
            button.text = option.text
            button.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            button.setOnClickListener {
                sendUserChoice(option)
            }
            optionsContainer.addView(button)
        }
    }

    private fun sendUserChoice(option: ScenarioOption) {
        optionsContainer.visibility = View.GONE
        optionsContainer.removeAllViews()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val userMessage = MessageModel(option.text, true, currentTime, false, false)
        messages.add(userMessage)
        lastMessageCount = messages.size
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        saveMessages()

        currentScenarioNode = option.nextNode

        startMessageStatusSimulation(messages.size - 1, option.nextNode)
    }

    private fun startMessageStatusSimulation(position: Int, nextNode: String) {
        handler.postDelayed({
            if (position < messages.size && messages[position].isOutgoing && !messages[position].isDelivered) {
                messages[position] = messages[position].copy(isDelivered = true)
                messageAdapter.notifyItemChanged(position)
                saveMessages()

                handler.postDelayed({
                    if (position < messages.size && messages[position].isOutgoing && !messages[position].isRead) {
                        messages[position] = messages[position].copy(isRead = true)
                        messageAdapter.notifyItemChanged(position)
                        saveMessages()
                        updateTotalUnreadCount()

                        if (!isReplyScheduled) {
                            startReplySimulationForNode(nextNode, messages[position].text)
                        }
                    }
                }, (4000 + random.nextInt(16000)).toLong())
            }
        }, (1000 + random.nextInt(4000)).toLong())
    }

    private fun startReplySimulationForNode(nextNode: String, userMessageText: String) {
        if (!isPrivate) return
        if (chatTitle == "Мамуля 💖") return

        isReplyScheduled = true

        handler.postDelayed({
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val replyText = getBotMessageForNode(nextNode)
            currentScenarioNode = nextNode

            val replyMessage = MessageModel(replyText, false, currentTime, false, false)
            messages.add(replyMessage)
            lastMessageCount = messages.size
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            saveMessages()
            updateTotalUnreadCount()
            isReplyScheduled = false

            showOptionsForCurrentNode()
        }, (2000 + random.nextInt(8000)).toLong())
    }

    data class ScenarioOption(
        val text: String,
        val nextNode: String
    )

    private fun getOptionsForNode(node: String): List<ScenarioOption> {
        return when (node) {
            "start" -> listOf(
                ScenarioOption("Привет! Как дела?", "greeting_response"),
                ScenarioOption("Что нового?", "news_response"),
                ScenarioOption("Расскажи о себе", "about_response"),
                ScenarioOption("Пока!", "bye_response")
            )
            "greeting_response" -> listOf(
                ScenarioOption("Отлично! А у тебя?", "mutual_greeting"),
                ScenarioOption("Норм, работаю", "work_response"),
                ScenarioOption("Скучаю, давай поболтаем", "chat_response")
            )
            "mutual_greeting" -> listOf(
                ScenarioOption("Тоже отлично! Чем занимаешься?", "activity_question"),
                ScenarioOption("Рад это слышать!", "positive_response")
            )
            "activity_question" -> listOf(
                ScenarioOption("Смотрю фильм", "movie_response"),
                ScenarioOption("Читаю книгу", "book_response"),
                ScenarioOption("Гуляю", "walk_response")
            )
            "movie_response" -> listOf(
                ScenarioOption("Какой фильм?", "movie_name_response"),
                ScenarioOption("Посоветуй что-нибудь", "recommend_response")
            )
            "movie_name_response" -> listOf(
                ScenarioOption("Звучит интересно, посмотрю", "positive_response"),
                ScenarioOption("Уже смотрел, норм", "positive_response")
            )
            "recommend_response" -> listOf(
                ScenarioOption("Спасибо, посмотрю", "positive_response"),
                ScenarioOption("Не мой жанр", "bye_response")
            )
            "book_response" -> listOf(
                ScenarioOption("Какую книгу?", "book_name_response"),
                ScenarioOption("Давно не читал(а)", "positive_response")
            )
            "book_name_response" -> listOf(
                ScenarioOption("Добавлю в список", "positive_response"),
                ScenarioOption("Читал(а), понравилось", "positive_response")
            )
            "walk_response" -> listOf(
                ScenarioOption("Погода хорошая?", "weather_response"),
                ScenarioOption("С кем гуляешь?", "company_response")
            )
            "weather_response" -> listOf(
                ScenarioOption("У нас тоже", "positive_response"),
                ScenarioOption("Повезло", "positive_response")
            )
            "company_response" -> listOf(
                ScenarioOption("Классно проведи время", "positive_response"),
                ScenarioOption("Передавай привет", "positive_response")
            )
            "work_response" -> listOf(
                ScenarioOption("Удачи в работе!", "bye_response"),
                ScenarioOption("Отдыхай потом", "bye_response")
            )
            "chat_response" -> listOf(
                ScenarioOption("Давай!", "topic_choice"),
                ScenarioOption("О чем поговорим?", "topic_choice")
            )
            "topic_choice" -> listOf(
                ScenarioOption("О музыке", "music_response"),
                ScenarioOption("О путешествиях", "travel_response"),
                ScenarioOption("О еде", "food_response"),
                ScenarioOption("Пока!", "bye_response")
            )
            "music_response" -> listOf(
                ScenarioOption("Какой жанр любишь?", "music_genre_response"),
                ScenarioOption("Посоветуй исполнителя", "music_recommend_response")
            )
            "music_genre_response" -> listOf(
                ScenarioOption("Классный выбор!", "positive_response"),
                ScenarioOption("Я тоже такое слушаю", "positive_response")
            )
            "music_recommend_response" -> listOf(
                ScenarioOption("Послушаю, спасибо", "positive_response"),
                ScenarioOption("Знаю этого исполнителя", "positive_response")
            )
            "travel_response" -> listOf(
                ScenarioOption("Куда хочешь поехать?", "travel_destination"),
                ScenarioOption("Где уже был(а)?", "travel_past")
            )
            "travel_destination" -> listOf(
                ScenarioOption("Мечтаю там побывать", "positive_response"),
                ScenarioOption("Был(а), очень круто", "positive_response")
            )
            "travel_past" -> listOf(
                ScenarioOption("Здорово!", "positive_response"),
                ScenarioOption("Хочу туда же", "positive_response")
            )
            "food_response" -> listOf(
                ScenarioOption("Любимое блюдо?", "food_favorite"),
                ScenarioOption("Где вкусно кормят?", "food_place")
            )
            "food_favorite" -> listOf(
                ScenarioOption("Вкуснятина!", "positive_response"),
                ScenarioOption("Тоже люблю", "positive_response")
            )
            "food_place" -> listOf(
                ScenarioOption("Схожу как-нибудь", "positive_response"),
                ScenarioOption("Знаю это место", "positive_response")
            )
            "news_response" -> listOf(
                ScenarioOption("Расскажи!", "news_detail"),
                ScenarioOption("Не интересно", "bye_response")
            )
            "news_detail" -> listOf(
                ScenarioOption("Ничего себе!", "positive_response"),
                ScenarioOption("Уже слышал(а)", "positive_response")
            )
            "about_response" -> listOf(
                ScenarioOption("Интересно", "positive_response"),
                ScenarioOption("Расскажи еще", "about_more")
            )
            "about_more" -> listOf(
                ScenarioOption("Круто!", "positive_response"),
                ScenarioOption("Необычно", "positive_response")
            )
            "positive_response" -> listOf(
                ScenarioOption("Рад(а) поболтать! Напиши еще 😊", "bye_response"),
                ScenarioOption("Пока!", "bye_response")
            )
            "bye_response" -> listOf(
                ScenarioOption("Пока! Было приятно пообщаться 👋", "end"),
                ScenarioOption("До свидания! 👋", "end")
            )
            else -> emptyList()
        }
    }

    private fun getBotMessageForNode(node: String): String {
        return when (node) {
            "start" -> "Привет! Я бот. Давай пообщаемся? Выбери вариант ответа:"
            "greeting_response" -> "У меня всё отлично! А у тебя как?"
            "mutual_greeting" -> "Класс! Чем занимаешься сегодня?"
            "activity_question" -> "Ого, здорово! Что именно?"
            "movie_response" -> "Фильмы - это круто! Какой фильм смотришь?"
            "movie_name_response" -> "Отличный выбор! Надеюсь, понравится :)"
            "recommend_response" -> "Рекомендую 'Побег из Шоушенка' или 'Начало'. Шедевры!"
            "book_response" -> "Чтение - полезное дело! Что читаешь?"
            "book_name_response" -> "Хорошая книга! Я слышал о ней."
            "walk_response" -> "Приятной прогулки! Как погода?"
            "weather_response" -> "Отлично, рад за тебя!"
            "company_response" -> "Классная компания!"
            "work_response" -> "Работа - это важно, но не забывай отдыхать."
            "chat_response" -> "Отлично! О чем хочешь поговорить?"
            "topic_choice" -> "Выбери тему для разговора:"
            "music_response" -> "Музыка - это жизнь! Что слушаешь?"
            "music_genre_response" -> "Отличный вкус!"
            "music_recommend_response" -> "Советую послушать Radiohead или Arctic Monkeys"
            "travel_response" -> "Путешествия - моя страсть! А ты куда мечтаешь поехать?"
            "travel_destination" -> "Отличное место! Обязательно посети."
            "travel_past" -> "Круто! Расскажешь подробнее в следующий раз?"
            "food_response" -> "О еде можно говорить бесконечно! Что любишь?"
            "food_favorite" -> "О да, это вкусно!"
            "food_place" -> "Запомню, спасибо за совет!"
            "news_response" -> "Ого, ты не слышал? Вчера вышел новый альбом!"
            "news_detail" -> "Да, неожиданно, но приятно!"
            "about_response" -> "Я - виртуальный помощник. Могу поддержать разговор!"
            "about_more" -> "Еще я умею распознавать настроение и подбирать темы."
            "positive_response" -> "Отлично! Рад(а) что тебе нравится общаться со мной."
            "bye_response" -> "Было приятно поболтать! Заходи еще 😊"
            "end" -> "Пока-пока! Напиши, если захочешь поговорить снова ✨"
            else -> "Выбери один из вариантов ответа, чтобы продолжить диалог 😊"
        }
    }

    data class MessageModel(
        val text: String,
        val isOutgoing: Boolean,
        val time: String,
        val isDelivered: Boolean = false,
        val isRead: Boolean = false
    )

    inner class MessageAdapter(
        private val messages: List<MessageModel>,
        private val chatColor: String
    ) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val messageText: TextView = view.findViewById(R.id.messageText)
            val messageTime: TextView = view.findViewById(R.id.messageTime)
            val checkmark: ImageView = view.findViewById(R.id.checkmark)
            val messageBubble: View = view.findViewById(R.id.messageBubble)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.messageText.text = message.text
            holder.messageTime.text = message.time

            if (message.isOutgoing) {
                val params = holder.messageBubble.layoutParams as LinearLayout.LayoutParams
                params.gravity = android.view.Gravity.END
                holder.messageBubble.layoutParams = params
                holder.messageBubble.setBackgroundResource(R.drawable.bubble_outgoing)
                holder.messageBubble.setBackgroundColor(android.graphics.Color.parseColor(chatColor))

                holder.checkmark.visibility = View.VISIBLE
                when {
                    message.isRead -> {
                        holder.checkmark.setImageResource(R.drawable.ic_check_double)
                        holder.checkmark.setColorFilter(0xFF34B7F1.toInt())
                    }
                    message.isDelivered -> {
                        holder.checkmark.setImageResource(R.drawable.ic_check_double)
                        holder.checkmark.setColorFilter(0xFF8D8D93.toInt())
                    }
                    else -> {
                        holder.checkmark.setImageResource(R.drawable.ic_check_single)
                        holder.checkmark.setColorFilter(0xFF8D8D93.toInt())
                    }
                }
            } else {
                val params = holder.messageBubble.layoutParams as LinearLayout.LayoutParams
                params.gravity = android.view.Gravity.START
                holder.messageBubble.layoutParams = params
                holder.messageBubble.setBackgroundResource(R.drawable.bubble_incoming)
                holder.messageBubble.setBackgroundColor(android.graphics.Color.parseColor("#2A2F36"))
                holder.checkmark.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = messages.size
    }
}
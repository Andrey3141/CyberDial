package com.printer.cyberdial

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("telegram_messages", Context.MODE_PRIVATE)
    private val rolePrefs: SharedPreferences = context.getSharedPreferences("mamulya_role", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        val baseChats = listOf("Анна", "Михаил", "Мамуля 💖", "Папа", "Друг", "Работа", "Канал", "Группа")
        var total = 0
        for (chat in baseChats) {
            val messages = loadMessages(chat)
            val lastReadPrefs = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)
            val lastRead = lastReadPrefs.getInt("read_$chat", 0)
            for (i in lastRead until messages.size) {
                if (!messages[i].isOutgoing && !messages[i].isRead) {
                    total++
                }
            }
        }
        if (getTotalUnreadCount() == 0 && total > 0) {
            saveTotalUnreadCount(total)
        }
    }

    fun saveMamulyaRole(role: String) {
        rolePrefs.edit().putString("mamulya_role", role).apply()
    }

    fun getMamulyaRole(): String? {
        return rolePrefs.getString("mamulya_role", null)
    }

    fun saveMamulyaDialogCompleted(completed: Boolean) {
        rolePrefs.edit().putBoolean("mamulya_dialog_completed", completed).apply()
    }

    fun isMamulyaDialogCompleted(): Boolean {
        return rolePrefs.getBoolean("mamulya_dialog_completed", false)
    }

    fun saveMamulyaFirstMessageSent(sent: Boolean) {
        rolePrefs.edit().putBoolean("mamulya_first_message_sent", sent).apply()
    }

    fun isMamulyaFirstMessageSent(): Boolean {
        return rolePrefs.getBoolean("mamulya_first_message_sent", false)
    }

    fun saveMessages(chatName: String, messages: List<ChatActivity.MessageModel>) {
        val json = gson.toJson(messages)
        prefs.edit().putString("chat_$chatName", json).apply()
    }

    fun loadMessages(chatName: String): List<ChatActivity.MessageModel> {
        val json = prefs.getString("chat_$chatName", null)
        return if (json != null) {
            val type = object : TypeToken<List<ChatActivity.MessageModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            getDefaultMessages(chatName)
        }
    }

    fun saveLastMessage(chatName: String, message: String, time: String, status: String) {
        prefs.edit().putString("last_msg_$chatName", message).apply()
        prefs.edit().putString("last_time_$chatName", time).apply()
        prefs.edit().putString("last_status_$chatName", status).apply()
    }

    fun getLastMessage(chatName: String): Triple<String, String, String>? {
        val message = prefs.getString("last_msg_$chatName", null) ?: return null
        val time = prefs.getString("last_time_$chatName", "00:00") ?: "00:00"
        val status = prefs.getString("last_status_$chatName", "sent") ?: "sent"
        return Triple(message, time, status)
    }

    fun saveTotalUnreadCount(count: Int) {
        prefs.edit().putInt("total_unread_count", count).apply()
    }

    fun getTotalUnreadCount(): Int {
        return prefs.getInt("total_unread_count", 0)
    }

    private fun getDefaultMessages(chatName: String): List<ChatActivity.MessageModel> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        if (chatName == "Мамуля 💖") {
            return getMamulyaHistory()
        }

        val privateChats = listOf("Анна", "Михаил", "Папа", "Друг")
        return if (privateChats.contains(chatName)) {
            listOf(
                ChatActivity.MessageModel("Привет! Я бот. Выбери вариант ответа:", false, currentTime, false, false)
            )
        } else {
            listOf()
        }
    }

    private fun getMamulyaHistory(): List<ChatActivity.MessageModel> {
        val messages = mutableListOf<ChatActivity.MessageModel>()

        messages.add(ChatActivity.MessageModel("Мам, я доехала. Ключи получила. Коробки в коридоре, но я их завтра разберу.", true, "21:15", true, true))
        messages.add(ChatActivity.MessageModel("Слава богу. А замки поменяла? Ты спроси у соседей, кто прошлый хозяин. А окна пластиковые? Там щель может быть.", false, "21:18", false, true))
        messages.add(ChatActivity.MessageModel("Мам, у меня студия за 20 тысяч, там щели везде. Я жива.", true, "21:20", true, true))
        messages.add(ChatActivity.MessageModel("Поела хоть?", false, "21:22", false, true))
        messages.add(ChatActivity.MessageModel("Бутерброд.", true, "21:23", true, true))
        messages.add(ChatActivity.MessageModel("📱 отправила голосовое (42 секунды, можно не слушать — там про поджелудочную)", false, "21:25", false, true))

        messages.add(ChatActivity.MessageModel("Привет. Давно не писала. У тебя всё нормально? Я по тебе скучаю.", false, "10:05", false, true))
        messages.add(ChatActivity.MessageModel("Привет. Да всё ок. Просто работа.", true, "10:30", true, true))
        messages.add(ChatActivity.MessageModel("Я не про работу. Ты вчера в 2 ночи была в сети. Это ненормально.", false, "10:32", false, true))
        messages.add(ChatActivity.MessageModel("Мам, это нормально. Я взрослая.", true, "10:35", true, true))
        messages.add(ChatActivity.MessageModel("Для меня ты всегда ребёнок. Стирать-то научилась или как?", false, "10:37", false, true))
        messages.add(ChatActivity.MessageModel("(нет ответа 4 часа)", true, "14:35", true, true))
        messages.add(ChatActivity.MessageModel("Ладно, не дуйся. Я тебе суп в контейнере в дверь положила, пока ты на работе была. Забери.", false, "15:20", false, true))

        messages.add(ChatActivity.MessageModel("Мам, я не могу найти свой синий свитер. Ты не брала?", true, "19:45", true, true))
        messages.add(ChatActivity.MessageModel("Это который с дыркой? Я его выкинула.", false, "19:47", false, true))
        messages.add(ChatActivity.MessageModel("ЧТО?!! Это был дизайнерский оверсайз!!!", true, "19:48", true, true))
        messages.add(ChatActivity.MessageModel("Это была тряпка, которую моль ела. Я тебе новый купила. Нормальный, шерстяной.", false, "19:50", false, true))
        messages.add(ChatActivity.MessageModel("Я не буду носить бежевый кардиган с пуговицами от тети Зои.", true, "19:52", true, true))
        messages.add(ChatActivity.MessageModel("📷 отправила фото: на плечиках висит тот самый ужасный бежевый кардиган", false, "19:53", false, true))
        messages.add(ChatActivity.MessageModel("Он классика.", false, "19:54", false, true))
        messages.add(ChatActivity.MessageModel("Я у двери оставлю. Забери.", false, "19:55", false, true))

        messages.add(ChatActivity.MessageModel("Ты почему не отвечаешь? Я звонила три раза.", false, "14:05", false, true))
        messages.add(ChatActivity.MessageModel("Температура 38. Лежу.", true, "14:10", true, true))
        messages.add(ChatActivity.MessageModel("Я сейчас такси возьму, приеду с бульоном и горчичниками.", false, "14:12", false, true))
        messages.add(ChatActivity.MessageModel("НЕ НАДО. Мам, правда. Я заказала лекарства с доставкой. И суп из пакета сварила.", true, "14:14", true, true))
        messages.add(ChatActivity.MessageModel("Из пакета — это отрава.", false, "14:15", false, true))
        messages.add(ChatActivity.MessageModel("Диктуй код от домофона.", false, "14:16", false, true))
        messages.add(ChatActivity.MessageModel("Я серьезно. Если приедешь — убегу к соседке.", true, "14:18", true, true))
        messages.add(ChatActivity.MessageModel("Ладно. Сбрось хотя бы показания градусника.", false, "14:22", false, true))
        messages.add(ChatActivity.MessageModel("38.2", true, "14:23", true, true))
        messages.add(ChatActivity.MessageModel("Ври больше. С таким голосом у тебя 39.5. Я на тебя смотрю через «Госуслуги»? Нет? Жаль.", false, "14:25", false, true))

        messages.add(ChatActivity.MessageModel("Доброе утро, птенчик. Тебе приснилось что-нибудь сегодня?", false, "08:30", false, true))
        messages.add(ChatActivity.MessageModel("Привет. Да. Что я опоздала на поезд.", true, "08:35", true, true))
        messages.add(ChatActivity.MessageModel("А мне приснилось, что тебе 5 лет и ты потерялась в магазине. Проснулась в слезах.", false, "08:37", false, true))
        messages.add(ChatActivity.MessageModel("Мамуль... Я не потерялась. Я просто теперь живу в 20 минутах на автобусе.", true, "08:40", true, true))
        messages.add(ChatActivity.MessageModel("Я знаю. Я просто люблю тебя даже на расстоянии.", false, "08:42", false, true))
        messages.add(ChatActivity.MessageModel("Кстати, ты свой паспорт у меня забыла. И зарядку от старого ноутбука.", false, "08:43", false, true))
        messages.add(ChatActivity.MessageModel("И таракан у нас на кухне объявился. Думаю, он по твоим вещам скучает.", false, "08:44", false, true))
        messages.add(ChatActivity.MessageModel("Таракан скучает по моим хлопьям, мам.", true, "08:46", true, true))
        messages.add(ChatActivity.MessageModel("Неважно. Приезжай в субботу. Я пирог с капустой испекла. А таракана мы завтра выведем.", false, "08:48", false, true))
        messages.add(ChatActivity.MessageModel("Хорошо. Приеду. И не выводите таракана без меня — я хочу посмотреть на его глаза, когда он поймет, что всё кончено.", true, "08:51", true, true))
        messages.add(ChatActivity.MessageModel("😂 поставила реакцию", false, "08:52", false, true))
        messages.add(ChatActivity.MessageModel("Только свитер мой бежевый надень. Он теплый.", false, "08:53", false, true))
        messages.add(ChatActivity.MessageModel("Мам.", true, "08:54", true, true))
        messages.add(ChatActivity.MessageModel("Шучу. Надевай свою дырявую тряпку. Люблю. 💖", false, "08:55", false, true))

        return messages
    }
}
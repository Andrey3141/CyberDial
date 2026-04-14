# ROADMAP.md

## Баги

### Бейдж на иконке Telegram не отображается
- **Статус:** Не исправлено
- **Описание:** На иконке Telegram должен отображаться красный бейдж с суммой непрочитанных сообщений из всех чатов. Сейчас бейдж не виден.
- **Что сделано:**
  - В `MessageRepository.kt` добавлены методы `saveTotalUnreadCount()` и `getTotalUnreadCount()`
  - В `AppIconAdapter.kt` добавлен `badgeContainer`, `badgeText` и метод `updateUnreadCount()`
  - В `MainActivity.kt` добавлен `unreadUpdateRunnable`
  - В `HomeScreenFragment.kt` добавлен метод `updateTelegramBadge()`
  - В `TelegramActivity.kt` и `ChatActivity.kt` добавлен метод `updateTotalUnreadCount()`
  - В `MessageRepository.kt` добавлен `init` блок для начального подсчета
- **Причина:** Требует дальнейшей диагностики

# 📰 News — персональная лента новостей

Android-приложение для чтения новостей по интересующим темам. Написано на **Kotlin** с использованием **Jetpack Compose**, **Clean Architecture** и **Hilt**.

---

## ✨ Возможности

### 📱 Основной экран
- Персональная лента новостей по выбранным темам
- Краткая сводка (количество тем, статей, период)
- Управление подписками (добавить/удалить/выбрать темы)
- Поиск и добавление новых тем
- Поделиться статьёй (share intent)
- Открытие статьи в браузере

### ⚙️ Настройки
- 🌐 **Язык** — English, Русский, Français, Deutsch
- 🕐 **Интервал обновления** — от 15 минут до 24 часов
- 🔔 **Уведомления** — о новых статьях
- 📶 **Только Wi-Fi** — загрузка только через Wi-Fi

### 🔄 Фоновая работа
- **WorkManager** — периодическое обновление статей
- **Push-уведомления** — при появлении новых статей
- **Автоматическая смена языка** — при смене языка очищаются старые статьи и загружаются новые

---

## 🏗️ Архитектура

Проект использует **Clean Architecture** с тремя слоями:

```
app/src/main/java/com/dron/news/
├── data/                    # Data Layer
│   ├── local/              # Room (DAO, Database, DbModel)
│   ├── remote/             # Retrofit (API, DTO)
│   ├── mapper/             # Преобразования DTO ↔ DbModel ↔ Domain
│   ├── repository/         # Реализации репозиториев
│   └── background/         # WorkManager (Worker, Notifications)
├── domain/                  # Domain Layer
│   ├── entity/             # Бизнес-модели (Article, Settings, ...)
│   ├── repository/         # Интерфейсы репозиториев
│   └── usecase/            # Use Cases (AddSubscription, UpdateLanguage, ...)
├── presentation/            # Presentation Layer
│   ├── navigation/         # NavHost, Routes
│   ├── screen/             # Экраны (Compose)
│   │   ├── subscriptions/  # Главный экран
│   │   └── settings/       # Экран настроек
│   ├── startup/            # AppStartupManager
│   └── ui/theme/           # Тема, цвета, шрифты
└── di/                      # Dependency Injection (Hilt)
```

---

## 🛠️ Технологии

| Технология | Для чего |
|------------|----------|
| **Kotlin** | Основной язык |
| **Jetpack Compose** | UI |
| **Material 3** | Дизайн-система |
| **Hilt** | Dependency Injection |
| **Room** | Локальная БД |
| **Retrofit** | Сетевые запросы |
| **Kotlinx Serialization** | JSON |
| **DataStore** | Хранение настроек |
| **WorkManager** | Фоновая работа |
| **Navigation Compose** | Навигация |
| **Coil** | Загрузка изображений |
| **Coroutines & Flow** | Асинхронность |

---

## 🚀 Установка и запуск

### Требования
- **Android Studio** Meerkat (2026.1.2) или новее
- **JDK 17+**
- **Android SDK** 26+ (Android 8.0)
- **NewsAPI ключ** (бесплатный)

### Шаг 1: Клонирование
```bash
git clone https://github.com/SlavaDronov/News.git
cd News
```

### Шаг 2: Получите API-ключ
1. Зарегистрируйтесь на [newsapi.org](https://newsapi.org)
2. Получите бесплатный API-ключ (100 запросов/день)

### Шаг 3: Создайте `api.properties`
В **корне проекта** создайте файл `api.properties`:

```properties
NEWS_API_KEY=ваш_api_ключ_из_newsapi
```

⚠️ **Важно:** файл `api.properties` уже в `.gitignore` — он **не попадёт** в Git.

### Шаг 4: Соберите проект
```bash
./gradlew build
```

### Шаг 5: Запустите
- Подключите устройство или запустите эмулятор (Android 8.0+)
- Нажмите **Run** в Android Studio

---

## 📸 Скриншоты

> Скриншоты будут добавлены позже

| Главный экран | Настройки | Диалог языка |
|---------------|-----------|--------------|
| _скоро_ | _скоро_ | _скоро_ |

---

## 🔑 Безопасность

Проект использует **безопасный подход** к API-ключам:

1. **Ключ хранится** в `api.properties` (в `.gitignore`)
2. **Ключ передаётся** в код через `BuildConfig.NEWS_API_KEY`
3. **В коде НЕТ** хардкода ключа
4. **В Git попадает** только код без ключа

### Как это работает:

**Шаг 1 — читаем ключ из `api.properties` в `build.gradle.kts`:**
```kotlin
// app/build.gradle.kts
private val apiProperties = Properties().apply {
    val file = rootProject.file("api.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

private val apiKey: String = apiProperties.getProperty("NEWS_API_KEY") ?: ""
```

**Шаг 2 — передаём ключ в `BuildConfig`:**
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "NEWS_API_KEY", apiKey)
    }
    buildFeatures {
        buildConfig = true
    }
}
```

**Шаг 3 — используем ключ в `Retrofit` через аннотацию:**
```kotlin
// NewsApiService.kt
@GET("v2/everything?apiKey=${BuildConfig.NEWS_API_KEY}")
suspend fun loadArticles(
    @Query("q") topic: String,
    @Query("language") language: String
): NewsResponseDto
```

**Шаг 4 — вызываем API без передачи ключа:**
```kotlin
// NewsRepositoryImpl.kt
private suspend fun loadArticles(
    topic: String,
    language: Language
): List<ArticleDbModel> {
    return try {
        newsApiService.loadArticles(
            topic = topic,
            language = language.toQueryParam()
        ).toDbModels(topic)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e("NewsRepository", e.stackTraceToString())
        emptyList()
    }
}
```

**Итог:**
- ✅ Ключ **читается** из `api.properties` (файл в `.gitignore`)
- ✅ Ключ **подставляется** в `BuildConfig` при сборке
- ✅ Ключ **используется** в URL через `@GET`-аннотацию
- ✅ В Git попадает **только код** без ключа

---

## 🎯 Ключевые особенности реализации

### 1. **Смена языка — мгновенно**
При смене языка в настройках:
1. Обновляется `DataStore`
2. Очищаются старые статьи из БД
3. Сразу загружаются новые (на выбранном языке)
4. UI обновляется автоматически

### 2. **Фоновое обновление через WorkManager**
```kotlin
@HiltWorker
class RefreshDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase,
    private val notificationsHelper: NotificationsHelper,
    private val getSettingsUseCase: GetSettingsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = getSettingsUseCase().first()
        val updated = updateSubscribedArticlesUseCase()
        if (updated.isNotEmpty() && settings.notificationEnabled) {
            notificationsHelper.showNewArticlesNotification(updated)
        }
        return Result.success()
    }
}
```

### 3. **Reactive UI через Flow**
```kotlin
// Подписка на изменения статей в БД
fun observeSelectedTopics() {
    state.map { it.selectedTopics }
        .distinctUntilChanged()
        .flatMapLatest { getArticlesByTopicsUseCase(it) }
        .onEach { _state.update { s -> s.copy(articles = it) } }
        .launchIn(viewModelScope)
}
```

### 4. **Премиум-UI компоненты**
- Анимированные кнопки с эффектом нажатия
- Спиннер в кнопке обновления
- Snackbar с результатом обновления
- Диалоги выбора с выделением
- Плавные анимации появления/исчезновения

---

## 📊 Структура экранов

```
┌─────────────────────────────────────────┐
│  SubscriptionsScreen                    │
│  ┌───────────────────────────────────┐  │
│  │  МОЯ ЛЕНТА              ⚙   ↻    │  │
│  │                                   │  │
│  │  СВОДКА ДНЯ                       │  │
│  │  ┌──────┬──────┬──────┐           │  │
│  │  │ 5    │ 25   │ 24ч  │           │  │
│  │  │ ТЕМЫ │НОВОСТИ│ПЕРИОД│          │  │
│  │  └──────┴──────┴──────┘           │  │
│  │                                   │  │
│  │  ВАШИ ТЕМЫ                        │  │
│  │  [Kotlin] [Android] [AI] [+]     │  │
│  │                                   │  │
│  │  НОВОСТИ                          │  │
│  │  [Featured Article Card]          │  │
│  │  [Article Card]                   │  │
│  │  [Article Card]                   │  │
│  └───────────────────────────────────┘  │
│                          [+ Добавить]   │
└─────────────────────────────────────────┘
              ↓ нажали ⚙
┌─────────────────────────────────────────┐
│  SettingsScreen                         │
│  ← Настройки                            │
│  ┌───────────────────────────────────┐  │
│  │ 🌐  Язык              → Русский   │  │
│  ├───────────────────────────────────┤  │
│  │ 🕐  Интервал          → 15 минут  │  │
│  ├───────────────────────────────────┤  │
│  │ 🔔  Уведомления            [ON]   │  │
│  ├───────────────────────────────────┤  │
│  │ 📶  Только Wi-Fi           [OFF]  │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## 🤝 Вклад в проект

Хотите улучшить проект? Отлично!

1. Fork проекта
2. Создайте ветку (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте (`git commit -m 'Add: AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

---

## 📋 Планы развития

- [x] Главный экран с подписками
- [x] Экран настроек
- [x] Фоновое обновление (WorkManager)
- [x] Push-уведомления
- [x] Смена языка
- [ ] Экран деталей статьи
- [ ] Тёмная тема
- [ ] Избранное
- [ ] Pull-to-refresh
- [ ] Пагинация
- [ ] Тесты (Unit + UI)

---

## 📄 Лицензия

Этот проект распространяется под лицензией **MIT**.
Подробности в файле [LICENSE](LICENSE).

---

## 👤 Автор

**Slava Dronov**
- GitHub: [@SlavaDronov](https://github.com/SlavaDronov)

---

## 🙏 Благодарности

- Огромное спасибо **Андрею Сумину** — за обучение
- [NewsAPI](https://newsapi.org) — за бесплатный API
- [Android Developers](https://developer.android.com) — за документацию
- Сообщество **Jetpack Compose** — за вдохновение

---

<p align="center">
  <b>⭐ Поставьте звезду, если проект был полезен! ⭐</b>
</p>
package com.dron.news.presentation.screen.subscriptions

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import coil3.compose.SubcomposeAsyncImage
import com.dron.news.domain.entity.Article
import com.dron.news.presentation.ui.theme.CustomIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var isAddingTopic by remember { mutableStateOf(false) }
    var isManagingTopics by remember { mutableStateOf(false) }

    // ✅ Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

// ✅ Отслеживаем переход состояния
    var wasRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            wasRefreshing = true
        } else if (wasRefreshing) {
            wasRefreshing = false
            if (state.articles.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = "Обновлено: ${state.articles.size} новостей",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val selectedTopicsCount = state.selectedTopics.size
    val articlesCount = state.articles.size

    // ✅ Оборачиваем в Scaffold для Snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // ------------------------------------------------------------
                // ШАПКА
                // ------------------------------------------------------------

                item {
                    FeedHeader(
                        onRefresh = {
                            viewModel.processCommand(SubscriptionCommand.RefreshData)
                        },
                        onNavigateToSettings = onNavigateToSettings,
                        isRefreshing = state.isRefreshing  // ← ПЕРЕДАЁМ
                    )
                }

                // ------------------------------------------------------------
                // СТАТИСТИКА ЛЕНТЫ
                // ------------------------------------------------------------

                item {
                    FeedOverviewCard(
                        topicsCount = selectedTopicsCount,
                        articlesCount = articlesCount
                    )
                }

                // ------------------------------------------------------------
                // ТЕМЫ
                // ------------------------------------------------------------

                item {
                    TopicsSection(
                        state = state,
                        isManagingTopics = isManagingTopics,
                        onManageClick = {
                            isManagingTopics = !isManagingTopics
                        },
                        onAddTopicClick = {
                            isAddingTopic = true
                        },
                        onToggleTopic = { topic ->
                            viewModel.processCommand(
                                SubscriptionCommand.ToggleTopicSelection(topic)
                            )
                        },
                        onRemoveTopic = { topic ->
                            viewModel.processCommand(
                                SubscriptionCommand.RemoveSubscription(topic)
                            )
                        }
                    )
                }

                // ------------------------------------------------------------
                // ДОБАВЛЕНИЕ ТЕМЫ
                // ------------------------------------------------------------


                // ------------------------------------------------------------
                // НОВОСТИ
                // ------------------------------------------------------------

                item {
                    SectionHeader(
                        title = "Последние новости",
                        subtitle = if (articlesCount > 0) {
                            "$articlesCount новостей"
                        } else {
                            "Персональная лента для вас"
                        }
                    )
                }

                if (state.articles.isEmpty()) {

                    item {
                        EmptyFeedCard(
                            hasSubscriptions = state.subscriptions.isNotEmpty(),
                            onRefresh = {
                                viewModel.processCommand(
                                    SubscriptionCommand.RefreshData
                                )
                            }
                        )
                    }

                } else {

                    // Первая статья — главная

                    item {
                        FeaturedArticleCard(
                            article = state.articles.first()
                        )
                    }

                    // Остальные новости

                    itemsIndexed(
                        items = state.articles.drop(1),
                        key = { index, article ->
                            "${article.url}-$index"
                        }
                    ) { _, article ->

                        ArticleCard(
                            article = article
                        )
                    }
                }
            }

            // ------------------------------------------------------------
            // ПЛАВАЮЩАЯ КНОПКА
            // ------------------------------------------------------------

            AnimatedVisibility(
                visible = isAddingTopic,

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp +
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                    ),

                enter = fadeIn(
                    animationSpec = tween(220)
                ) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = tween(220)
                ),

                exit = fadeOut(
                    animationSpec = tween(160)
                ) + scaleOut(
                    targetScale = 0.97f,
                    animationSpec = tween(160)
                )
            ) {

                AddTopicPanel(
                    query = state.query,
                    enabled = state.query.isNotBlank(),

                    onQueryChange = { query ->
                        viewModel.processCommand(
                            SubscriptionCommand.InputTopic(query)
                        )
                    },

                    onSubscribe = {
                        if (state.query.isNotBlank()) {

                            viewModel.processCommand(
                                SubscriptionCommand.ClickSubscribe
                            )

                            isAddingTopic = false
                        }
                    },

                    onClose = {
                        viewModel.processCommand(
                            SubscriptionCommand.InputTopic("")
                        )

                        isAddingTopic = false
                    }
                )
            }

            AnimatedVisibility(
                visible = !isAddingTopic,

                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp,
                        bottom = 20.dp +
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                    ),

                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {

                AddTopicFloatingButton(
                    onClick = {
                        isAddingTopic = true
                    }
                )
            }
        }
    }
}


// ============================================================================
// ШАПКА
// ============================================================================

@Composable
private fun FeedHeader(
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isRefreshing: Boolean = false  // ← НОВЫЙ ПАРАМЕТР
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "МОЯ ЛЕНТА",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ваш мир.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2).sp
                )
            )

            Text(
                text = "Подобрано для вас.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-1.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Всё важное — в одном месте.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // КНОПКИ В ПРАВОМ УГЛУ
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Кнопка настроек
            PremiumIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = "Настройки",
                onClick = onNavigateToSettings
            )

            // ✅ Кнопка обновления со спиннером
            RefreshIconButton(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }

    }
}

/**
 * Премиум-кнопка обновления с анимированным спиннером.
 *
 * Когда [isRefreshing] = true:
 * - Показывает крутящийся индикатор
 * - Кнопка некликабельна
 *
 * Когда [isRefreshing] = false:
 * - Показывает иконку обновления
 * - Кликабельна
 */
@Composable
private fun RefreshIconButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "refresh_scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            isRefreshing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(150),
        label = "refresh_color"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(animatedColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isRefreshing,
                onClick = onRefresh
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Обновить",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
@Composable
private fun PremiumIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    // Состояние нажатия для анимации
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Анимация масштаба при нажатии
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    // Анимация цвета фона при нажатии
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            containerColor
        },
        animationSpec = tween(150),
        label = "icon_color"
    )

    Box(
        modifier = modifier
            .size(48.dp)  // Стандартный размер для иконок в Material 3
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(animatedColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,  // Убираем стандартный ripple, у нас своя анимация
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}


// ============================================================================
// СТАТИСТИКА
// ============================================================================

@Composable
private fun FeedOverviewCard(
    topicsCount: Int,
    articlesCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),

        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Декоративные круги

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.08f)
                    .background(
                        Color.White,
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .alpha(0.06f)
                    .background(
                        Color.White,
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "СВОДКА ДНЯ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OverviewMetric(
                        value = topicsCount.toString(),
                        label = "ТЕМЫ",
                        modifier = Modifier.weight(1f)
                    )

                    OverviewMetric(
                        value = articlesCount.toString(),
                        label = "НОВОСТИ",
                        modifier = Modifier.weight(1f)
                    )

                    OverviewMetric(
                        value = "24 ч",
                        label = "ПЕРИОД",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
private fun OverviewMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onPrimary.copy(
                alpha = 0.7f
            )
        )
    }
}


// ============================================================================
// ТЕМЫ
// ============================================================================

@Composable
private fun TopicsSection(
    state: SubscriptionsState,
    isManagingTopics: Boolean,
    onManageClick: () -> Unit,
    onAddTopicClick: () -> Unit,
    onToggleTopic: (String) -> Unit,
    onRemoveTopic: (String) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "ВАШИ ТЕМЫ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (state.subscriptions.isEmpty()) {
                        "Создайте свою ленту"
                    } else {
                        "${state.subscriptions.size} интересов"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.subscriptions.isNotEmpty()) {

                IconButton(
                    onClick = onManageClick
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Настроить темы"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (state.subscriptions.isEmpty()) {

            EmptyTopicsCard(
                onClick = onAddTopicClick
            )

        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                state.subscriptions.forEach { (topic, selected) ->

                    TopicChip(
                        topic = topic,
                        selected = selected,
                        showRemove = isManagingTopics,

                        onClick = {
                            onToggleTopic(topic)
                        },

                        onRemove = {
                            onRemoveTopic(topic)
                        }
                    )
                }

                AddTopicChip(
                    onClick = onAddTopicClick
                )
            }
        }
    }
}


// ============================================================================
// CHIP ТЕМЫ
// ============================================================================

@Composable
private fun TopicChip(
    topic: String,
    selected: Boolean,
    showRemove: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {

    val background = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .animateContentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(50),
        color = background,

        border = if (!selected) {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        } else {
            null
        }
    ) {

        Row(
            modifier = Modifier.padding(
                start = 15.dp,
                end = if (showRemove) 6.dp else 15.dp,
                top = 10.dp,
                bottom = 10.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            if (selected) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = topic,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor
            )

            AnimatedVisibility(
                visible = showRemove,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Удалить тему $topic",
                        modifier = Modifier.size(15.dp),
                        tint = contentColor
                    )
                }
            }
        }
    }
}


// ============================================================================
// ДОБАВИТЬ ТЕМУ
// ============================================================================

@Composable
private fun AddTopicChip(
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Добавить",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}


// ============================================================================
// ПУСТЫЕ ТЕМЫ
// ============================================================================

@Composable
private fun EmptyTopicsCard(
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.12f
                        )
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.RssFeed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Создайте свою ленту",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Выберите интересующие вас темы.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// ============================================================================
// ДОБАВЛЕНИЕ ТЕМЫ
// ============================================================================

@Composable
private fun AddTopicPanel(
    query: String,
    enabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    onClose: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f
                            )
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Добавить тему",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "Что вы хотите отслеживать?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onClose
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Закрыть"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp
                        ),

                    singleLine = true,

                    cursorBrush = SolidColor(
                        MaterialTheme.colorScheme.primary
                    ),

                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),

                    decorationBox = { innerTextField ->

                        if (query.isEmpty()) {

                            Text(
                                text = "Например: искусственный интеллект",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        enabled = enabled,
                        onClick = onSubscribe
                    ),

                shape = RoundedCornerShape(16.dp),

                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "Подписаться",

                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),

                        color = if (enabled) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}


// ============================================================================
// ЗАГОЛОВОК РАЗДЕЛА
// ============================================================================

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,

                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ============================================================================
// ГЛАВНАЯ НОВОСТЬ
// ============================================================================

private fun shareArticle(
    context: android.content.Context,
    article: Article
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_SUBJECT,
            article.title
        )
        putExtra(
            Intent.EXTRA_TEXT,
            "${article.title}\n\n${article.url}"
        )
    }

    val shareIntent = Intent.createChooser(
        sendIntent,
        "Поделиться статьёй"
    )

    context.startActivity(shareIntent)
}

@Composable
private fun FeaturedArticleCard(
    article: Article
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(article.url)
//                )
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    article.url.toUri()
                )
                context.startActivity(intent)
            },

        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {

        Column {

            //FeaturedImagePlaceholder()
            FeaturedArticleImage(article.imageUrl)

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                ArticleMeta(
                    sourceName = article.sourceName,
                    publishedAt = article.publishedAt
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = article.title,

                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 30.sp
                    ),

                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                if (article.description.isNotBlank()) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = article.description,

                        style = MaterialTheme.typography.bodyMedium,

                        color = MaterialTheme.colorScheme.onSurfaceVariant,

                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ArticleReadMore(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(article.url)
                            )
                            context.startActivity(intent)
                        }
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            shareArticle(
                                context = context,
                                article = article
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Поделиться"
                        )
                    }
                }
            }
        }
    }
}


// ============================================================================
// ЗАГЛУШКА ИЗОБРАЖЕНИЯ
// ============================================================================

@Composable
private fun FeaturedArticleImage(
    imageUrl: String?
) {
    if (imageUrl.isNullOrBlank()) {
        FeaturedImagePlaceholder()
        return
    }

    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        contentScale = ContentScale.Crop,
        loading = {
            FeaturedImagePlaceholder()
        },
        error = {
            FeaturedImagePlaceholder()
        }
    )
}

@Composable
private fun FeaturedImagePlaceholder() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.9f
                        ),
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.35f
                        ),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {

        Icon(
            imageVector = Icons.Outlined.Article,
            contentDescription = null,

            modifier = Modifier
                .size(44.dp)
                .align(Alignment.Center),

            tint = MaterialTheme.colorScheme.onPrimary.copy(
                alpha = 0.8f
            )
        )
    }
}


// ============================================================================
// КАРТОЧКА НОВОСТИ
// ============================================================================

@Composable
private fun ArticleCard(
    article: Article
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(article.url)
//                )
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    article.url.toUri()
                )

                context.startActivity(intent)
            },

        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {

        Column {

            // Большое изображение
            FeaturedArticleImage(article.imageUrl)

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // Источник + дата
                ArticleMeta(
                    sourceName = article.sourceName,
                    publishedAt = article.publishedAt
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Заголовок
                Text(
                    text = article.title,

                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 30.sp
                    ),

                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Описание
                if (article.description.isNotBlank()) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = article.description,

                        style = MaterialTheme.typography.bodyMedium,

                        color = MaterialTheme.colorScheme.onSurfaceVariant,

                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Действия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ArticleReadMore(
                        onClick = {
//                            val intent = Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse(article.url)
//                            )
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                article.url.toUri()
                            )
                            context.startActivity(intent)
                        }
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            shareArticle(
                                context = context,
                                article = article
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Поделиться"
                        )
                    }
                }
            }
        }
    }
}


// ============================================================================
// МИНИАТЮРА
// ============================================================================


@Composable
private fun ArticleThumbnail(
    imageUrl: String?
) {
    if (imageUrl.isNullOrBlank()) {
        ArticleThumbnailPlaceholder()
        return
    }

    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .width(92.dp)
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            ArticleThumbnailPlaceholder()
        },
        error = {
            ArticleThumbnailPlaceholder()
        }
    )
}
@Composable
private fun ArticleThumbnailPlaceholder() {

    Box(
        modifier = Modifier
            .width(92.dp)
            .height(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),

        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Outlined.Article,
            contentDescription = null,

            modifier = Modifier.size(28.dp),

            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// ============================================================================
// МЕТА ИНФОРМАЦИЯ
// ============================================================================

@Composable
private fun ArticleMeta(
    sourceName: String,
    publishedAt: Long
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = sourceName.uppercase(),

            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.Bold
            ),

            color = MaterialTheme.colorScheme.primary,

            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(7.dp))

        Box(
            modifier = Modifier
                .size(3.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = formatArticleDate(publishedAt),

            style = MaterialTheme.typography.labelSmall,

            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// ============================================================================
// ЧИТАТЬ
// ============================================================================

@Composable
private fun ArticleReadMore(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Читать",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = CustomIcons.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}


// ============================================================================
// ПУСТАЯ ЛЕНТА
// ============================================================================

@Composable
private fun EmptyFeedCard(
    hasSubscriptions: Boolean,
    onRefresh: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 36.dp
                ),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.background
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = null,

                    modifier = Modifier.size(28.dp),

                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (hasSubscriptions) {
                    "В ленте пока тихо"
                } else {
                    "Ваша лента ждёт вас"
                },

                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasSubscriptions) {
                    "Обновите ленту, чтобы получить свежие новости."
                } else {
                    "Добавьте интересующие темы, чтобы создать персональную ленту."
                },

                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasSubscriptions) {

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onRefresh
                ) {

                    Text(
                        text = "Обновить ленту",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ============================================================================
// ПЛАВАЮЩАЯ КНОПКА
// ============================================================================

@Composable
private fun AddTopicFloatingButton(
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(20.dp),

        color = MaterialTheme.colorScheme.onSurface,

        shadowElevation = 8.dp
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 14.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,

                modifier = Modifier.size(19.dp),

                tint = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "Добавить тему",

                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),

                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}


// ============================================================================
// ДАТА
// ============================================================================

private fun formatArticleDate(timestamp: Long): String {

    return try {

        SimpleDateFormat(
            "dd MMM",
            Locale.getDefault()
        ).format(Date(timestamp))

    } catch (_: Exception) {

        ""
    }
}
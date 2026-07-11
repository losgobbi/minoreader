package org.dev.minoreader

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dev.minoreader.platform.UrlOpener
import org.dev.minoreader.ui.DetailScreen
import org.dev.minoreader.ui.FeedListScreen
import org.dev.minoreader.ui.ListMode
import org.dev.minoreader.ui.MainViewModel
import org.dev.minoreader.ui.SearchScreen
import org.dev.minoreader.ui.SettingsScreen
import org.dev.minoreader.ui.SplashScreen
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

sealed interface Screen {
    data object Feed : Screen
    data object Read : Screen
    data object Favorites : Screen
    data object Settings : Screen
    data object Search : Screen
    data class Detail(val articleId: Long) : Screen
}

private const val SPLASH_MILLIS = 2100L

@Composable
fun App() {
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(SPLASH_MILLIS)
                showSplash = false
            }
            if (showSplash) {
                SplashScreen()
            } else {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent() {
    val vm: MainViewModel = koinInject()
    val opener: UrlOpener = koinInject()

    var screen by remember { mutableStateOf<Screen>(Screen.Feed) }
    var lastTab by remember { mutableStateOf<Screen>(Screen.Feed) }

    when (val current = screen) {
        is Screen.Detail -> DetailScreen(
            articleId = current.articleId,
            vm = vm,
            onOpenOriginal = { url -> opener.open(url) },
            onBack = { screen = lastTab },
        )

        Screen.Search -> SearchScreen(
            vm = vm,
            onOpenArticle = { id -> screen = Screen.Detail(id) },
            onBack = { screen = lastTab },
        )

        else -> MainScaffold(
            current = current,
            vm = vm,
            onSelectTab = { tab -> screen = tab; lastTab = tab },
            onOpenArticle = { id -> screen = Screen.Detail(id) },
            onOpenSearch = { screen = Screen.Search },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    current: Screen,
    vm: MainViewModel,
    onSelectTab: (Screen) -> Unit,
    onOpenArticle: (Long) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val refreshing by vm.refreshing.collectAsState()
    val status by vm.status.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(status) {
        status?.let {
            snackbar.showSnackbar(it)
            vm.clearStatus()
        }
    }

    val title = when (current) {
        Screen.Read -> "Read"
        Screen.Favorites -> "Favorites"
        Screen.Settings -> "Settings"
        else -> "minoreader"
    }
    val showListActions = current == Screen.Feed || current == Screen.Read || current == Screen.Favorites

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (showListActions) {
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        if (refreshing) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp).size(20.dp))
                        } else {
                            IconButton(onClick = { vm.refresh() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current == Screen.Feed,
                    onClick = { onSelectTab(Screen.Feed) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Feed") },
                )
                NavigationBarItem(
                    selected = current == Screen.Read,
                    onClick = { onSelectTab(Screen.Read) },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("Read") },
                )
                NavigationBarItem(
                    selected = current == Screen.Favorites,
                    onClick = { onSelectTab(Screen.Favorites) },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Favorites") },
                )
                NavigationBarItem(
                    selected = current == Screen.Settings,
                    onClick = { onSelectTab(Screen.Settings) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (current) {
                Screen.Feed -> FeedListScreen(vm, ListMode.FEED, onOpenArticle)
                Screen.Read -> FeedListScreen(vm, ListMode.READ, onOpenArticle)
                Screen.Favorites -> FeedListScreen(vm, ListMode.FAVORITES, onOpenArticle)
                Screen.Settings -> SettingsScreen(vm = vm)
                else -> Unit
            }
        }
    }
}

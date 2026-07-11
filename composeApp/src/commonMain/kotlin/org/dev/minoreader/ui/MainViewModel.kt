package org.dev.minoreader.ui

import org.dev.minoreader.data.ArticleDetail
import org.dev.minoreader.data.ArticleListItem
import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.db.Feed
import org.dev.minoreader.reader.ContentBlock
import org.dev.minoreader.reader.ReaderService
import org.dev.minoreader.rss.FeedService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel(
    private val feedRepo: FeedRepository,
    private val articleRepo: ArticleRepository,
    private val feedService: FeedService,
    private val readerService: ReaderService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> =
        feedRepo.categories().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val feeds: StateFlow<List<Feed>> =
        feedRepo.feeds().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val feed: StateFlow<List<ArticleListItem>> =
        _selectedCategory
            .flatMapLatest { category -> articleRepo.unread(category) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val favorites: StateFlow<List<ArticleListItem>> =
        articleRepo.favorites().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val read: StateFlow<List<ArticleListItem>> =
        articleRepo.read().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<ArticleListItem>> =
        _searchQuery
            .debounce(250)
            .flatMapLatest { query ->
                if (query.isBlank()) flowOf(emptyList()) else articleRepo.search(query.trim())
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun markRead(id: Long, read: Boolean = true) {
        scope.launch { articleRepo.setRead(id, read) }
    }

    fun toggleFavorite(item: ArticleListItem) {
        scope.launch { articleRepo.setFavorite(item.id, !item.isFavorite) }
    }

    fun setFavorite(id: Long, favorite: Boolean) {
        scope.launch { articleRepo.setFavorite(id, favorite) }
    }

    fun addFeed(url: String, category: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return
        scope.launch {
            _status.value = "Adding feed…"
            val result = feedService.addFeedFromUrl(cleanUrl, category.trim().ifBlank { "General" })
            _status.value = result.fold(
                onSuccess = { "Feed added: $it" },
                onFailure = { "Failed to add: ${it.message ?: "unknown"}" },
            )
        }
    }

    fun updateFeed(id: Long, url: String, title: String, category: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return
        scope.launch {
            val result = runCatching {
                feedRepo.updateFeed(
                    id = id,
                    url = cleanUrl,
                    title = title.trim().ifBlank { cleanUrl },
                    category = category.trim().ifBlank { "General" },
                )
            }
            _status.value = result.fold(
                onSuccess = { "Feed updated" },
                onFailure = { "Failed to update: ${it.message ?: "unknown"}" },
            )
        }
    }

    fun deleteFeed(id: Long) {
        scope.launch { feedRepo.deleteFeed(id) }
    }

    fun refresh() {
        scope.launch {
            _refreshing.value = true
            val n = runCatching { feedService.refreshAll() }.getOrElse { 0 }
            _refreshing.value = false
            _status.value = "Refreshed $n feed(s)"
        }
    }

    fun clearStatus() {
        _status.value = null
    }

    suspend fun loadDetail(id: Long): ArticleDetail? = articleRepo.detail(id)

    /** Loads the article content as blocks (cache → fetch+extract → excerpt). */
    suspend fun loadArticleBlocks(detail: ArticleDetail): List<ContentBlock> = readerService.load(detail)
}

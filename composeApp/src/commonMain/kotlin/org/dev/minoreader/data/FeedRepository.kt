package org.dev.minoreader.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import org.dev.minoreader.db.Feed
import org.dev.minoreader.db.MinoDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FeedRepository(db: MinoDb) {
    private val q = db.feedQueries

    fun feeds(): Flow<List<Feed>> = q.selectAllFeeds().asFlow().mapToList(Dispatchers.Default)

    fun categories(): Flow<List<String>> = q.selectCategories().asFlow().mapToList(Dispatchers.Default)

    suspend fun addFeed(url: String, title: String, category: String) = withContext(Dispatchers.Default) {
        q.insertFeed(url = url, title = title, category = category, faviconUrl = null, lastFetchedAt = null)
    }

    suspend fun deleteFeed(id: Long) = withContext(Dispatchers.Default) { q.deleteFeed(id) }

    suspend fun setCategory(id: Long, category: String) = withContext(Dispatchers.Default) {
        q.updateFeedCategory(category, id)
    }

    /** Edits a feed's url/title/category. Throws if the new url collides with another feed. */
    suspend fun updateFeed(id: Long, url: String, title: String, category: String) =
        withContext(Dispatchers.Default) {
            q.updateFeed(url = url, title = title, category = category, id = id)
        }

    suspend fun markFetched(id: Long, title: String, ts: Long) = withContext(Dispatchers.Default) {
        q.updateFeedFetched(ts, title, id)
    }

    suspend fun allFeeds(): List<Feed> = withContext(Dispatchers.Default) { q.selectAllFeeds().executeAsList() }
}

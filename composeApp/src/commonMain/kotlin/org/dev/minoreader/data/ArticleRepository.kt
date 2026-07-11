package org.dev.minoreader.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import org.dev.minoreader.db.MinoDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ArticleRepository(db: MinoDb) {
    private val q = db.articleQueries

    /** Main feed: unread, optionally filtered by category. */
    fun unread(category: String?): Flow<List<ArticleListItem>> {
        val query = if (category == null) {
            q.selectUnread(::mapListItem)
        } else {
            q.selectUnreadByCategory(category, ::mapListItem)
        }
        return query.asFlow().mapToList(Dispatchers.Default)
    }

    fun favorites(): Flow<List<ArticleListItem>> =
        q.selectFavorites(::mapListItem).asFlow().mapToList(Dispatchers.Default)

    /** Read history (most recent first). */
    fun read(): Flow<List<ArticleListItem>> =
        q.selectRead(::mapListItem).asFlow().mapToList(Dispatchers.Default)

    /** Search across title and excerpt (all articles). */
    fun search(query: String): Flow<List<ArticleListItem>> =
        q.searchArticles(query, ::mapListItem).asFlow().mapToList(Dispatchers.Default)

    suspend fun detail(id: Long): ArticleDetail? = withContext(Dispatchers.Default) {
        q.selectArticleById(id, ::mapDetail).executeAsOneOrNull()
    }

    suspend fun setRead(id: Long, read: Boolean) = withContext(Dispatchers.Default) {
        q.setRead(if (read) 1L else 0L, id)
    }

    suspend fun setFavorite(id: Long, fav: Boolean) = withContext(Dispatchers.Default) {
        q.setFavorite(if (fav) 1L else 0L, id)
    }

    suspend fun setContentHtml(id: Long, html: String) = withContext(Dispatchers.Default) {
        q.setContentHtml(html, id)
    }

    suspend fun insert(
        feedId: Long,
        guid: String,
        title: String,
        link: String?,
        author: String?,
        publishedAt: Long?,
        excerpt: String?,
        contentHtml: String?,
        fetchedAt: Long,
    ) = withContext(Dispatchers.Default) {
        q.insertArticle(feedId, guid, title, link, author, publishedAt, excerpt, contentHtml, fetchedAt)
    }
}

private fun mapListItem(
    id: Long,
    feedId: Long,
    guid: String,
    title: String,
    link: String?,
    author: String?,
    publishedAt: Long?,
    excerpt: String?,
    contentHtml: String?,
    isRead: Long,
    isFavorite: Long,
    fetchedAt: Long,
    feedTitle: String,
    category: String,
): ArticleListItem = ArticleListItem(
    id = id,
    feedTitle = feedTitle,
    category = category,
    title = title,
    link = link,
    publishedAt = publishedAt,
    isRead = isRead == 1L,
    isFavorite = isFavorite == 1L,
)

private fun mapDetail(
    id: Long,
    feedId: Long,
    guid: String,
    title: String,
    link: String?,
    author: String?,
    publishedAt: Long?,
    excerpt: String?,
    contentHtml: String?,
    isRead: Long,
    isFavorite: Long,
    fetchedAt: Long,
    feedTitle: String,
    category: String,
): ArticleDetail = ArticleDetail(
    id = id,
    feedTitle = feedTitle,
    category = category,
    title = title,
    link = link,
    author = author,
    publishedAt = publishedAt,
    excerpt = excerpt,
    contentHtml = contentHtml,
    isRead = isRead == 1L,
    isFavorite = isFavorite == 1L,
)

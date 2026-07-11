package org.dev.minoreader.data

/** Lightweight item for the feed list (one-line title). */
data class ArticleListItem(
    val id: Long,
    val feedTitle: String,
    val category: String,
    val title: String,
    val link: String?,
    val publishedAt: Long?,
    val isRead: Boolean,
    val isFavorite: Boolean,
)

/** Full data for the detail screen. */
data class ArticleDetail(
    val id: Long,
    val feedTitle: String,
    val category: String,
    val title: String,
    val link: String?,
    val author: String?,
    val publishedAt: Long?,
    val excerpt: String?,
    val contentHtml: String?,
    val isRead: Boolean,
    val isFavorite: Boolean,
)

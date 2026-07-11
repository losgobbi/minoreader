package org.dev.minoreader

import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.db.MinoDb
import org.dev.minoreader.di.platformModule
import org.dev.minoreader.di.sharedModule
import org.dev.minoreader.platform.UrlOpener
import org.dev.minoreader.rss.FeedService
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

/** Verifies the DI graph (desktop platform module + shared) resolves and creates the real DB. */
class DiGraphTest {

    @Test
    fun resolvesDesktopGraph() {
        val app = koinApplication { modules(platformModule(), sharedModule) }
        val koin = app.koin
        try {
            assertNotNull(koin.get<MinoDb>())
            assertNotNull(koin.get<FeedRepository>())
            assertNotNull(koin.get<ArticleRepository>())
            assertNotNull(koin.get<FeedService>())
            assertNotNull(koin.get<UrlOpener>())
        } finally {
            app.close()
        }
    }
}

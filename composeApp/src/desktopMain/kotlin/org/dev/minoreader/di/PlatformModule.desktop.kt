package org.dev.minoreader.di

import org.dev.minoreader.data.DesktopSqlDriverFactory
import org.dev.minoreader.data.SqlDriverFactory
import org.dev.minoreader.platform.DesktopUrlOpener
import org.dev.minoreader.platform.UrlOpener
import org.dev.minoreader.reader.ArticleExtractor
import org.dev.minoreader.reader.JsoupArticleExtractor
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<SqlDriverFactory> { DesktopSqlDriverFactory() }
    single<UrlOpener> { DesktopUrlOpener() }
    single<ArticleExtractor> { JsoupArticleExtractor() }
}

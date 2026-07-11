package org.dev.minoreader.di

import org.dev.minoreader.data.AndroidSqlDriverFactory
import org.dev.minoreader.data.SqlDriverFactory
import org.dev.minoreader.platform.AndroidUrlOpener
import org.dev.minoreader.platform.UrlOpener
import org.dev.minoreader.reader.ArticleExtractor
import org.dev.minoreader.reader.JsoupArticleExtractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<SqlDriverFactory> { AndroidSqlDriverFactory(androidContext()) }
    single<UrlOpener> { AndroidUrlOpener(androidContext()) }
    single<ArticleExtractor> { JsoupArticleExtractor() }
}

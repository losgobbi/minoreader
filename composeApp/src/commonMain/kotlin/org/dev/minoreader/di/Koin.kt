package org.dev.minoreader.di

import org.dev.minoreader.data.ArticleRepository
import org.dev.minoreader.data.FeedRepository
import org.dev.minoreader.data.SqlDriverFactory
import org.dev.minoreader.db.MinoDb
import org.dev.minoreader.reader.ReaderService
import org.dev.minoreader.rss.FeedService
import org.dev.minoreader.ui.MainViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/** Provided by each platform (SQLite driver, URL opener, extractor). */
expect fun platformModule(): Module

val sharedModule = module {
    single { MinoDb(get<SqlDriverFactory>().create()) }
    single { FeedRepository(get()) }
    single { ArticleRepository(get()) }
    single { FeedService(get(), get()) }
    single { ReaderService(get(), get()) }
    single { MainViewModel(get(), get(), get(), get()) }
}

fun appModules(): List<Module> = listOf(platformModule(), sharedModule)

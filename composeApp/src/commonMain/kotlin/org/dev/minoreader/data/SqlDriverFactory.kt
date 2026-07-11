package org.dev.minoreader.data

import app.cash.sqldelight.db.SqlDriver

/** Platform-specific factory: Android uses AndroidSqliteDriver, desktop uses JdbcSqliteDriver. */
interface SqlDriverFactory {
    fun create(): SqlDriver
}

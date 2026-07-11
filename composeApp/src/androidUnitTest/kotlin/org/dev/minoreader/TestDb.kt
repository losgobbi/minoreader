package org.dev.minoreader

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.dev.minoreader.db.MinoDb
import kotlinx.coroutines.runBlocking

// Android unit tests run on the local JVM, so the JDBC SQLite driver works here too —
// giving the same shared coverage (commonTest) as the Linux desktop test run.
actual fun newInMemoryDb(): MinoDb {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    MinoDb.Schema.create(driver)
    driver.execute(null, "PRAGMA foreign_keys=ON;", 0)
    return MinoDb(driver)
}

actual fun runTestBlocking(block: suspend () -> Unit) = runBlocking { block() }

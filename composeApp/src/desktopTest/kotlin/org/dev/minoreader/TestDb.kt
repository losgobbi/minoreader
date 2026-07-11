package org.dev.minoreader

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.dev.minoreader.db.MinoDb

fun newInMemoryDb(): MinoDb {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    MinoDb.Schema.create(driver)
    driver.execute(null, "PRAGMA foreign_keys=ON;", 0)
    return MinoDb(driver)
}

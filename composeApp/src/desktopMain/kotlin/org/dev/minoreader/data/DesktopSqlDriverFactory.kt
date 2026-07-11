package org.dev.minoreader.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.dev.minoreader.db.MinoDb
import java.io.File

class DesktopSqlDriverFactory : SqlDriverFactory {
    override fun create(): SqlDriver {
        val dbFile = File(appDataDir(), "minoreader.db")
        val isNew = !dbFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        if (isNew) MinoDb.Schema.create(driver)
        driver.execute(null, "PRAGMA foreign_keys=ON;", 0)
        return driver
    }
}

/** Per-OS data directory (Linux: ~/.local/share/minoreader). */
fun appDataDir(): File {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    val dir = when {
        os.contains("win") -> File(System.getenv("APPDATA") ?: home, "minoreader")
        os.contains("mac") -> File(home, "Library/Application Support/minoreader")
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "minoreader")
    }
    if (!dir.exists()) dir.mkdirs()
    return dir
}

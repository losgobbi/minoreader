package org.dev.minoreader.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.dev.minoreader.db.MinoDb

class AndroidSqlDriverFactory(private val context: Context) : SqlDriverFactory {
    override fun create(): SqlDriver = AndroidSqliteDriver(
        schema = MinoDb.Schema,
        context = context,
        name = "minoreader.db",
        callback = object : AndroidSqliteDriver.Callback(MinoDb.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}

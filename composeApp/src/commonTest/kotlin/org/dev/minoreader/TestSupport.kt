package org.dev.minoreader

import org.dev.minoreader.db.MinoDb

/** A fresh in-memory database for tests. Actual per platform (JDBC SQLite on the JVM). */
expect fun newInMemoryDb(): MinoDb

/** Runs a suspending test body to completion. Actual per platform (runBlocking on the JVM). */
expect fun runTestBlocking(block: suspend () -> Unit)

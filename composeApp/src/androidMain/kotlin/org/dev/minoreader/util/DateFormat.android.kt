package org.dev.minoreader.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())

actual fun formatDate(millis: Long?): String? =
    millis?.let { fmt.format(Instant.ofEpochMilli(it)) }

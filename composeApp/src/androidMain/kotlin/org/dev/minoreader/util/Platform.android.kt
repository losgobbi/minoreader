package org.dev.minoreader.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun parseFeedDate(raw: String?): Long? {
    val s = raw?.trim()
    if (s.isNullOrBlank()) return null
    runCatching { return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }
    runCatching { return OffsetDateTime.parse(s).toInstant().toEpochMilli() }
    runCatching { return Instant.parse(s).toEpochMilli() }
    return null
}

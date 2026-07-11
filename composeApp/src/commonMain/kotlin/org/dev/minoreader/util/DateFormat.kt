package org.dev.minoreader.util

/** Formats epoch millis to a readable local date/time; null-safe. */
expect fun formatDate(millis: Long?): String?

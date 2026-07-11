package org.dev.minoreader.util

/** Current epoch millis. */
expect fun currentTimeMillis(): Long

/** Parses a feed date (RSS RFC-822 or Atom ISO-8601) to epoch millis; null if it doesn't parse. */
expect fun parseFeedDate(raw: String?): Long?

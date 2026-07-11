package org.dev.minoreader

import org.dev.minoreader.util.parseFeedDate
import org.dev.minoreader.util.stripHtml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Date parsing + HTML stripping — shared logic, runs on both desktop and Android. */
class HelpersTest {

    @Test
    fun dateAndHtmlHelpers() {
        assertTrue((parseFeedDate("Wed, 02 Oct 2024 13:00:00 GMT") ?: 0) > 0)
        assertTrue((parseFeedDate("2024-10-02T13:00:00Z") ?: 0) > 0)
        assertNull(parseFeedDate("garbage"))
        assertNull(parseFeedDate(null))
        assertEquals("a & b", stripHtml("<b>a</b> &amp; b"))
        assertNull(stripHtml("   "))
    }
}

package com.example.finance_tracker.core.data

import com.example.finance_tracker.core.data.model.DefaultCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultCategoriesTest {

    @Test
    fun newUser_getsTheDefaults() {
        assertEquals(DefaultCategories.ALL, DefaultCategories.merge(emptyList()))
    }

    @Test
    fun userCategories_areAddedWithoutCaseDuplicates_andSorted() {
        val merged = DefaultCategories.merge(listOf("food", "Pets", " Coffee ", ""))

        assertEquals(1, merged.count { it.equals("food", ignoreCase = true) })
        assertTrue("Food" in merged) // the default spelling wins
        assertTrue("Pets" in merged)
        assertTrue("Coffee" in merged) // trimmed
        assertTrue(merged.none { it.isBlank() })
        assertEquals(merged.sortedBy { it.lowercase() }, merged)
    }
}

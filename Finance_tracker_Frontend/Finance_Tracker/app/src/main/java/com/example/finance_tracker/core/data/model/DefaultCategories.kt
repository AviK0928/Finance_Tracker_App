package com.example.finance_tracker.core.data.model

/** Suggested categories. Categories are free text on the backend, so the user can type any other one. */
object DefaultCategories {

    val ALL = listOf(
        "Education", "Entertainment", "Food", "Groceries", "Health", "Other",
        "Rent", "Salary", "Shopping", "Transport", "Travel", "Utilities"
    )

    /** Defaults plus the user's own categories, without case-insensitive duplicates, sorted. */
    fun merge(userCategories: List<String>): List<String> =
        (ALL + userCategories)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
}

package com.example.project2.data

import androidx.compose.runtime.mutableStateListOf
import com.example.project2.model.FormEntry

object FormRepository {
    private val entries = mutableStateListOf<FormEntry>()

    fun getEntries(): List<FormEntry> = entries
    fun addEntry(entry: FormEntry) {
        entries.add(entry)
    }
    fun updateEntry(index: Int, entry: FormEntry) {
        if (index in entries.indices) {
            entries[index] = entry
        }
    }

    fun getEntry(index: Int): FormEntry? {
        return entries.getOrNull(index)
    }
}

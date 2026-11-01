package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class PasswordEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val category: String = "general", // "login", "card", "iban", "general"
    val timestamp: Long = System.currentTimeMillis()
)

class KeyboardPassManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keyboard_pass_prefs", Context.MODE_PRIVATE)
    private val KEY_PASSWORDS = "passwords_list_json"
    private val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"

    fun getAllEntries(): List<PasswordEntry> {
        val jsonStr = prefs.getString(KEY_PASSWORDS, null) ?: return getDefaultSampleEntries()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<PasswordEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PasswordEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        url = obj.optString("url", ""),
                        notes = obj.optString("notes", ""),
                        category = obj.optString("category", "general"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            getDefaultSampleEntries()
        }
    }

    fun saveEntry(entry: PasswordEntry) {
        val current = getAllEntries().toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            current[index] = entry
        } else {
            current.add(0, entry)
        }
        persist(current)
    }

    fun deleteEntry(id: String) {
        val current = getAllEntries().filter { it.id != id }
        persist(current)
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()

    /**
     * Import entries from CSV (supports generic CSV and Samsung Pass exported CSV/format)
     * Header format: Title, URL, Username, Password, Note
     */
    fun importFromCsv(csvContent: String): Int {
        if (csvContent.isBlank()) return 0
        var count = 0
        val current = getAllEntries().toMutableList()
        val lines = csvContent.lines().filter { it.isNotBlank() }

        for (line in lines) {
            // Skip header lines
            val lower = line.lowercase()
            if (lower.contains("username") && lower.contains("password")) continue
            if (lower.startsWith("title,") || lower.startsWith("name,")) continue

            // Split by comma taking into account basic quotes
            val parts = parseCsvLine(line)
            if (parts.size >= 2) {
                val title = parts.getOrNull(0)?.trim() ?: "حساب مستورد"
                val username = parts.getOrNull(1)?.trim() ?: ""
                val password = parts.getOrNull(2)?.trim() ?: ""
                val url = parts.getOrNull(3)?.trim() ?: ""
                val notes = parts.getOrNull(4)?.trim() ?: ""

                // Detect category (IBAN or Card or Login)
                val cat = when {
                    title.contains("iban", ignoreCase = true) || title.contains("آيبان") || username.startsWith("SA") -> "iban"
                    title.contains("card", ignoreCase = true) || title.contains("بطاقة") || password.length == 16 -> "card"
                    else -> "login"
                }

                current.add(
                    0,
                    PasswordEntry(
                        title = title,
                        username = username,
                        password = password,
                        url = url,
                        notes = notes,
                        category = cat
                    )
                )
                count++
            }
        }

        if (count > 0) {
            persist(current)
        }
        return count
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(cur.toString().trim())
                    cur = StringBuilder()
                }
                else -> cur.append(ch)
            }
        }
        result.add(cur.toString().trim())
        return result
    }

    private fun persist(list: List<PasswordEntry>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("username", item.username)
            obj.put("password", item.password)
            obj.put("url", item.url)
            obj.put("notes", item.notes)
            obj.put("category", item.category)
            obj.put("timestamp", item.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_PASSWORDS, jsonArray.toString()).apply()
    }

    private fun getDefaultSampleEntries(): List<PasswordEntry> {
        return listOf(
            PasswordEntry(
                title = "مصرف الراجحي - Al Rajhi",
                username = "SA0380000000608010167519",
                password = "••• آيبان معتمد",
                notes = "الآيبان البنكي للتحويل المباشر",
                category = "iban"
            ),
            PasswordEntry(
                title = "بطاقة مدى الرقمية Mada",
                username = "4588 •••• •••• 1290",
                password = "CVV: 489 / Exp: 09/28",
                notes = "بطاقة الدفع السريع",
                category = "card"
            ),
            PasswordEntry(
                title = "بريد جوجل Google Account",
                username = "user@gmail.com",
                password = "••••••••",
                url = "google.com",
                category = "login"
            )
        )
    }
}

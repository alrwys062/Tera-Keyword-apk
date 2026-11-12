import json, re

with open("en_translations.json", "r", encoding="utf-8") as f:
    en_map = json.load(f)

with open("keys_meta.json", "r", encoding="utf-8") as f:
    meta_map = json.load(f)

with open("app/src/main/java/com/example/util/AppStrings.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Update getResId to check both context.packageName and "com.example"
old_get_res_id = """    fun getResId(context: Context, key: String): Int {
        return resIdCache.computeIfAbsent(key) {
            context.resources.getIdentifier(key, "string", context.packageName)
        }
    }"""

new_get_res_id = """    fun getResId(context: Context, key: String): Int {
        return resIdCache.computeIfAbsent(key) {
            var id = context.resources.getIdentifier(key, "string", context.packageName)
            if (id == 0) {
                id = context.resources.getIdentifier(key, "string", "com.example")
            }
            id
        }
    }"""

assert old_get_res_id in content, "old_get_res_id not found in AppStrings.kt"
content = content.replace(old_get_res_id, new_get_res_id, 1)

# Generate codeFallbackMap
code_fallback_lines = ["    private val codeFallbackMap: Map<String, String> = mapOf("]
for k, meta in sorted(meta_map.items()):
    val = meta["text"].replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "")
    code_fallback_lines.append(f'        "{k}" to "{val}",')
code_fallback_lines.append("    )\n")
code_fallback_block = "\n".join(code_fallback_lines)

# Generate additional English entries for translations["en"]
en_extra_lines = []
for k, en_val in sorted(en_map.items()):
    val = en_val.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "")
    en_extra_lines.append(f'            "{k}" to "{val}",')
en_extra_block = "\n".join(en_extra_lines)

# Insert codeFallbackMap right before translations
target_before_translations = "    private val translations: Map<String, Map<String, String>> = mapOf("
assert target_before_translations in content, "target_before_translations not found"
content = content.replace(target_before_translations, code_fallback_block + "\n" + target_before_translations, 1)

# In translations["en"] to mapOf(, add the en_extra_block
target_en_map = '"en" to mapOf('
idx_en = content.find(target_en_map)
assert idx_en != -1, "target_en_map not found"
insert_pos = idx_en + len(target_en_map) + 1
content = content[:insert_pos] + "\n" + en_extra_block + content[insert_pos:]

# Update resolve and tr, trf
old_resolve_block = """    private fun resolve(context: Context, lang: String, key: String): String {
        // (1) Check Android localized resource from values-<lang>/strings.xml
        val localizedRes = getLocalizedResource(context, lang, key)
        if (!localizedRes.isNullOrBlank()) {
            return localizedRes
        }

        // (2) Check built-in translations map
        val builtIn = translations[lang]?.get(key)
        if (builtIn != null) {
            return builtIn
        }

        // (3) Check auto translations
        val autoTranslations = getAutoTranslations(context, lang)
        val autoTr = autoTranslations[key]
        if (!autoTr.isNullOrBlank()) {
            return autoTr
        }

        // (4) Fallback to raw resource or English/Arabic
        val rawRes = getRawResourceString(context, key)
        return rawRes ?: translations["en"]?.get(key) ?: translations["ar"]?.get(key) ?: key
    }"""

new_resolve_block = """    private fun formatFallbackText(key: String): String {
        if (!key.startsWith("ui_")) return key
        return ""
    }

    private fun resolve(context: Context, lang: String, key: String): String {
        // (1) Check Android localized resource from values-<lang>/strings.xml if not English
        if (lang != "en") {
            val localizedRes = getLocalizedResource(context, lang, key)
            if (!localizedRes.isNullOrBlank()) {
                return localizedRes
            }
        }

        // (2) Check built-in translations map for current language
        val builtIn = translations[lang]?.get(key)
        if (!builtIn.isNullOrBlank()) {
            return builtIn
        }

        // (3) Check auto translations for current language
        val autoTranslations = getAutoTranslations(context, lang)
        val autoTr = autoTranslations[key]
        if (!autoTr.isNullOrBlank()) {
            return autoTr
        }

        // (4) Fallback to English text from values/strings.xml
        val rawRes = getRawResourceString(context, key)
        if (!rawRes.isNullOrBlank()) {
            return rawRes
        }

        val enBuiltIn = translations["en"]?.get(key)
        if (!enBuiltIn.isNullOrBlank()) {
            return enBuiltIn
        }

        // (5) Fallback to code fallback text
        val codeFallback = codeFallbackMap[key]
            ?: masterSourceCache?.get(key)?.optString("text")
            ?: translations["ar"]?.get(key)
        if (!codeFallback.isNullOrBlank()) {
            return codeFallback
        }

        // (6) Strictly forbidden for the app to display the key name itself
        return formatFallbackText(key)
    }"""

assert old_resolve_block in content, "old_resolve_block not found"
content = content.replace(old_resolve_block, new_resolve_block, 1)

# Update tr(key: String) to never return raw key
old_tr_no_ctx = """    fun tr(key: String): String {
        val ctx = appContext
        return if (ctx != null) tr(ctx, key) else (translations["en"]?.get(key) ?: translations["ar"]?.get(key) ?: key)
    }"""

new_tr_no_ctx = """    fun tr(key: String): String {
        val ctx = appContext
        if (ctx != null) {
            return tr(ctx, key)
        }
        val enText = translations["en"]?.get(key)
        if (!enText.isNullOrBlank()) return enText
        val codeText = codeFallbackMap[key] ?: translations["ar"]?.get(key)
        if (!codeText.isNullOrBlank()) return codeText
        return formatFallbackText(key)
    }"""

assert old_tr_no_ctx in content, "old_tr_no_ctx not found"
content = content.replace(old_tr_no_ctx, new_tr_no_ctx, 1)

with open("app/src/main/java/com/example/util/AppStrings.kt", "w", encoding="utf-8") as f:
    f.write(content)

print("Successfully updated AppStrings.kt")

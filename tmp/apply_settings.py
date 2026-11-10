import json

with open('public/assets/aistudio/i18n_source_03414659.json') as f:
    data = json.load(f)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt') as f:
    content = f.read()
    lines = content.splitlines(keepends=True)

occ_list = []
for k, v in data.items():
    for occ in v.get('occurrences', []):
        if 'SettingsScreen' in occ.get('file', ''):
            occ_list.append((k, occ, v.get('text', '')))

# Special format replacements map
format_map = {
    'ui_settings_screen_002': ('"${srcLangObj.flag} $srcName"', 'AppStrings.trf(context, "ui_settings_screen_002", srcLangObj.flag, srcName)'),
    'ui_settings_screen_003': ('"${tgtLangObj.flag} $tgtName"', 'AppStrings.trf(context, "ui_settings_screen_003", tgtLangObj.flag, tgtName)'),
    'ui_settings_screen_021': ('"تم إصلاح ومزامنة نصوص الحافظة بنجاح ($count نصوص) ✓"', 'AppStrings.trf(context, "ui_settings_screen_021", count)'),
    'ui_settings_screen_027': ('"قائمة الاختصارات المحفوظة (${shortcutsList.size}):"', 'AppStrings.trf(context, "ui_settings_screen_027", shortcutsList.size)'),
    'ui_settings_screen_035': ('"${(settings.emojiBarHeightFactor * 100).toInt()}%"', 'AppStrings.trf(context, "ui_settings_screen_035", (settings.emojiBarHeightFactor * 100).toInt())'),
    'ui_settings_screen_064': ('"المدخل: ${stats.inputWordCount} ⟵ المخرج: ${stats.outputWordCount} كلمة"', 'AppStrings.trf(context, "ui_settings_screen_064", stats.inputWordCount, stats.outputWordCount)'),
    'ui_settings_screen_065': ('"تطابق الحجم 100% ✓"', 'AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_065")'),
    'ui_settings_screen_076': ('"${(settings.soundVolume * 100).toInt()}%"', 'AppStrings.trf(context, "ui_settings_screen_076", (settings.soundVolume * 100).toInt())'),
    'ui_settings_screen_081': ('"${settings.vibrationDurationMs} ms"', 'AppStrings.trf(context, "ui_settings_screen_081", settings.vibrationDurationMs)'),
    'ui_settings_screen_095': ('"${settings.keyRepeatSpeedMs} ms"', 'AppStrings.trf(context, "ui_settings_screen_095", settings.keyRepeatSpeedMs)'),
    'ui_settings_screen_097': ('"${settings.longPressDelayMs} ms"', 'AppStrings.trf(context, "ui_settings_screen_097", settings.longPressDelayMs)'),
    'ui_settings_screen_104': ('"${settings.toolbarHeightDp} dp"', 'AppStrings.trf(context, "ui_settings_screen_104", settings.toolbarHeightDp)'),
    'ui_settings_screen_105': ('"${settings.keyHeightDp} dp (${(settings.keyHeightFactor * 100).toInt()}%)"', 'AppStrings.trf(context, "ui_settings_screen_105", settings.keyHeightDp, (settings.keyHeightFactor * 100).toInt())'),
    'ui_settings_screen_106': ('"${settings.keyFontSizeSp} sp"', 'AppStrings.trf(context, "ui_settings_screen_106", settings.keyFontSizeSp)'),
}

# Group occurrences by line
by_line = {}
for k, occ, txt in occ_list:
    l_idx = occ['line'] - 1
    if l_idx not in by_line:
        by_line[l_idx] = []
    by_line[l_idx].append((k, occ['original'], txt))

replaced_count = 0
new_lines = list(lines)

for l_idx, items in sorted(by_line.items(), key=lambda x: x[0]):
    line_str = new_lines[l_idx]
    for k, orig, txt in items:
        if k in format_map:
            target_str, repl_str = format_map[k]
            if target_str in line_str:
                line_str = line_str.replace(target_str, repl_str, 1)
                replaced_count += 1
            elif orig in line_str:
                line_str = line_str.replace(f'"{orig}"', repl_str, 1)
                replaced_count += 1
        else:
            quoted_orig = f'"{orig}"'
            repl_str = f'AppStrings.trCode(context, appLanguageCode, "{k}")'
            if quoted_orig in line_str:
                line_str = line_str.replace(quoted_orig, repl_str, 1)
                replaced_count += 1
            elif orig in line_str:
                line_str = line_str.replace(orig, repl_str, 1)
                replaced_count += 1
            else:
                print(f'Warning: line {l_idx+1} could not replace {repr(orig)}')
    new_lines[l_idx] = line_str

print(f'Successfully replaced {replaced_count} occurrences out of {len(occ_list)}')

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.writelines(new_lines)

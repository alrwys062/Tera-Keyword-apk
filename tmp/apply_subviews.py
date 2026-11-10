import json

with open('public/assets/aistudio/i18n_source_03414659.json') as f:
    data = json.load(f)

with open('app/src/main/java/com/example/keyboard/KeyboardSubViews.kt') as f:
    content = f.read()
    lines = content.splitlines(keepends=True)

occ_list = []
for k, v in data.items():
    for occ in v.get('occurrences', []):
        if 'KeyboardSubViews' in occ.get('file', ''):
            occ_list.append((k, occ, v.get('text', '')))

format_map = {
    'ui_keyboard_sub_views_006': ('"${allLangs.size} لغة عالمية"', 'AppStrings.trf("ui_keyboard_sub_views_006", allLangs.size)'),
    'ui_keyboard_sub_views_013': ('"تم إصلاح ومزامنة نصوص الحافظة ($count نصوص) ✓"', 'AppStrings.trf("ui_keyboard_sub_views_013", count)'),
    'ui_keyboard_sub_views_021': ('"تم الفحص والمزامنة ($count نصوص) ✓"', 'AppStrings.trf("ui_keyboard_sub_views_021", count)'),
    'ui_keyboard_sub_views_026': ('"معاينة النص الكامل (${clip.text.length} حرف)"', 'AppStrings.trf("ui_keyboard_sub_views_026", clip.text.length)'),
    'ui_keyboard_sub_views_039': ('"الكلمات: ${stats.inputWordCount} مدخلة ⟵ ${stats.outputWordCount} مخرجة"', 'AppStrings.trf("ui_keyboard_sub_views_039", stats.inputWordCount, stats.outputWordCount)'),
    'ui_keyboard_sub_views_040': ('"إلى $langName"', 'AppStrings.trf("ui_keyboard_sub_views_040", langName)'),
    'ui_keyboard_sub_views_042': ('"$original ← $corrected"', 'AppStrings.trf("ui_keyboard_sub_views_042", original, corrected)'),
    'ui_keyboard_sub_views_052': ('"أصوات المفاتيح المخصصة (${(settings.soundVolume * 100).toInt()}%)"', 'AppStrings.trf("ui_keyboard_sub_views_052", (settings.soundVolume * 100).toInt())'),
    'ui_keyboard_sub_views_054': ('"سرعة وثقل استجابة المفاتيح (${settings.keyRepeatSpeedMs}ms)"', 'AppStrings.trf("ui_keyboard_sub_views_054", settings.keyRepeatSpeedMs)'),
    'ui_keyboard_sub_views_055': ('"الاهتزاز اللمسي (${settings.vibrationDurationMs}ms)"', 'AppStrings.trf("ui_keyboard_sub_views_055", settings.vibrationDurationMs)'),
    'ui_keyboard_sub_views_057': ('"ارتفاع الكيبورد وحجمه (${(settings.keyHeightFactor * 100).toInt()}%)"', 'AppStrings.trf("ui_keyboard_sub_views_057", (settings.keyHeightFactor * 100).toInt())'),
    'ui_keyboard_sub_views_059': ('"حجم خط الحروف والرموز (${(settings.keyFontSizeFactor * 100).toInt()}%)"', 'AppStrings.trf("ui_keyboard_sub_views_059", (settings.keyFontSizeFactor * 100).toInt())'),
    'ui_keyboard_sub_views_061': ('"سرعة التنقل وسحب الحروف العائمة (${settings.floatingNavSpeed}/10)"', 'AppStrings.trf("ui_keyboard_sub_views_061", settings.floatingNavSpeed)'),
    'ui_keyboard_sub_views_070': ('"هوامش الكيبورد المريحة (جانبي: ${settings.sideMarginDp}dp، سفلي: ${settings.bottomMarginDp}dp)"', 'AppStrings.trf("ui_keyboard_sub_views_070", settings.sideMarginDp, settings.bottomMarginDp)'),
    'ui_keyboard_sub_views_072': ('"الهامش الجانبي (${settings.sideMarginDp}dp):"', 'AppStrings.trf("ui_keyboard_sub_views_072", settings.sideMarginDp)'),
    'ui_keyboard_sub_views_073': ('"الهامش السفلي (${settings.bottomMarginDp}dp):"', 'AppStrings.trf("ui_keyboard_sub_views_073", settings.bottomMarginDp)'),
    'ui_keyboard_sub_views_091': ('"${cat.icon} ${cat.nameAr}"', 'AppStrings.trf("ui_keyboard_sub_views_091", cat.icon, cat.nameAr)'),
    'ui_keyboard_sub_views_092': ('"= $result"', 'AppStrings.trf("ui_keyboard_sub_views_092", result)'),
    'ui_keyboard_sub_views_093': ('"$expression = $result"', 'AppStrings.trf("ui_keyboard_sub_views_093", expression, result)'),
    'ui_keyboard_sub_views_099': ('"ثيمات الكيبورد (${filteredThemes.size})"', 'AppStrings.trf("ui_keyboard_sub_views_099", filteredThemes.size)'),
    'ui_keyboard_sub_views_108': ('"ملصقاتي وعباراتي المصممة (${myStickers.size})"', 'AppStrings.trf("ui_keyboard_sub_views_108", myStickers.size)')
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
            repl_str = f'AppStrings.tr("{k}")'
            if quoted_orig in line_str:
                line_str = line_str.replace(quoted_orig, repl_str, 1)
                replaced_count += 1
            elif orig in line_str:
                line_str = line_str.replace(orig, repl_str, 1)
                replaced_count += 1
            else:
                print(f'Warning: line {l_idx+1} could not replace {repr(orig)}')
    new_lines[l_idx] = line_str

# Also ensure AppStrings import is present
full_text = "".join(new_lines)
if "import com.example.util.AppStrings" not in full_text:
    idx = full_text.find("package com.example.keyboard\n")
    if idx != -1:
        insert_pos = idx + len("package com.example.keyboard\n\n")
        full_text = full_text[:insert_pos] + "import com.example.util.AppStrings\n" + full_text[insert_pos:]

print(f'Successfully replaced {replaced_count} occurrences out of {len(occ_list)}')

with open('app/src/main/java/com/example/keyboard/KeyboardSubViews.kt', 'w') as f:
    f.write(full_text)

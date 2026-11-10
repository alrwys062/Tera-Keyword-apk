import json

with open('public/assets/aistudio/i18n_source_03414659.json') as f:
    data = json.load(f)

with open('app/src/main/java/com/example/keyboard/TurboKeyboardView.kt') as f:
    content = f.read()
    lines = content.splitlines(keepends=True)

occ_list = []
for k, v in data.items():
    for occ in v.get('occurrences', []):
        if 'TurboKeyboardView' in occ.get('file', ''):
            occ_list.append((k, occ, v.get('text', '')))

format_map = {
    'ui_turbo_keyboard_view_006': ('"$word "', 'AppStrings.trf("ui_turbo_keyboard_view_006", word)'),
    'ui_turbo_keyboard_view_007': ('"تمت إضافة $trimmed لشريط الابتسامات العلوي ✨"', 'AppStrings.trf(context, "ui_turbo_keyboard_view_007", trimmed)'),
    'ui_turbo_keyboard_view_008': ('" $sticker "', 'AppStrings.trf("ui_turbo_keyboard_view_008", sticker)'),
    'ui_turbo_keyboard_view_009': ('" $it "', 'AppStrings.trf("ui_turbo_keyboard_view_009", it)'),
    'ui_turbo_keyboard_view_010': ('" $mediaText "', 'AppStrings.trf("ui_turbo_keyboard_view_010", mediaText)'),
    'ui_turbo_keyboard_view_012': ('"خيارات أداة: $actionTitle"', 'AppStrings.trf("ui_turbo_keyboard_view_012", actionTitle)'),
    'ui_turbo_keyboard_view_014': ('"حذف زر $actionTitle من الشريط"', 'AppStrings.trf("ui_turbo_keyboard_view_014", actionTitle)'),
    'ui_turbo_keyboard_view_019': ('"استعادة: $lastDeletedText"', 'AppStrings.trf("ui_turbo_keyboard_view_019", lastDeletedText)'),
    'ui_turbo_keyboard_view_024': ('"◀  $spacebarLabel  ▶"', 'AppStrings.trf("ui_turbo_keyboard_view_024", spacebarLabel)'),
    'ui_turbo_keyboard_view_026': ('"الحروف العائمة (${longPressChar}) ="', 'AppStrings.trf("ui_turbo_keyboard_view_026", longPressChar)'),
    'ui_turbo_keyboard_view_031': ('"تم لصق وإضافة: $clip"', 'AppStrings.trf(context, "ui_turbo_keyboard_view_031", clip)'),
    'ui_turbo_keyboard_view_033': ('"ارتفاع الشريط: ${(heightFactor * 100).toInt()}%"', 'AppStrings.trf("ui_turbo_keyboard_view_033", (heightFactor * 100).toInt())'),
    'ui_turbo_keyboard_view_034': ('"حجم الأيقونات: ${(buttonSizeFactor * 100).toInt()}%"', 'AppStrings.trf("ui_turbo_keyboard_view_034", (buttonSizeFactor * 100).toInt())'),
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

full_text = "".join(new_lines)
if "import com.example.util.AppStrings" not in full_text:
    idx = full_text.find("package com.example.keyboard\n")
    if idx != -1:
        insert_pos = idx + len("package com.example.keyboard\n\n")
        full_text = full_text[:insert_pos] + "import com.example.util.AppStrings\n" + full_text[insert_pos:]

print(f'Successfully replaced {replaced_count} occurrences out of {len(occ_list)}')

with open('app/src/main/java/com/example/keyboard/TurboKeyboardView.kt', 'w') as f:
    f.write(full_text)

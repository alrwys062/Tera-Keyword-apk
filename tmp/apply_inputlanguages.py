import json

with open('public/assets/aistudio/i18n_source_03414659.json') as f:
    data = json.load(f)

with open('app/src/main/java/com/example/ui/screens/InputLanguagesScreen.kt') as f:
    content = f.read()
    lines = content.splitlines(keepends=True)

occ_list = []
for k, v in data.items():
    for occ in v.get('occurrences', []):
        if 'InputLanguagesScreen' in occ.get('file', ''):
            occ_list.append((k, occ, v.get('text', '')))

format_map = {
    'ui_input_languages_screen_001': ('"تم تنزيل وتثبيت لغة ${lang.nameAr} بنجاح!"', 'AppStrings.trf(context, "ui_input_languages_screen_001", lang.nameAr)'),
    'ui_input_languages_screen_010': ('"${lang.nameAr} (${lang.nameEn})"', 'AppStrings.trf(context, "ui_input_languages_screen_010", lang.nameAr, lang.nameEn)'),
    'ui_input_languages_screen_011': ('"تم تطبيق لغة ${lang.nameNative} على الكيبورد!"', 'AppStrings.trf(context, "ui_input_languages_screen_011", lang.nameNative)'),
    'ui_input_languages_screen_013': ('"تم حذف لغة ${lang.nameAr}"', 'AppStrings.trf(context, "ui_input_languages_screen_013", lang.nameAr)'),
    'ui_input_languages_screen_016': ('"${lang.nameAr} • الحجم: ${lang.size}"', 'AppStrings.trf(context, "ui_input_languages_screen_016", lang.nameAr, lang.size)'),
    'ui_input_languages_screen_018': ('"جاري التنزيل ${(progress * 100).toInt()}%"', 'AppStrings.trf(context, "ui_input_languages_screen_018", (progress * 100).toInt())'),
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
            repl_str = f'AppStrings.tr(context, "{k}")'
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
    idx = full_text.find("package com.example.ui.screens\n")
    if idx != -1:
        insert_pos = idx + len("package com.example.ui.screens\n\n")
        full_text = full_text[:insert_pos] + "import com.example.util.AppStrings\n" + full_text[insert_pos:]

print(f'Successfully replaced {replaced_count} occurrences out of {len(occ_list)}')

with open('app/src/main/java/com/example/ui/screens/InputLanguagesScreen.kt', 'w') as f:
    f.write(full_text)

import json

with open('public/assets/aistudio/i18n_source_03414659.json') as f:
    data = json.load(f)

with open('app/src/main/java/com/example/ui/screens/ClipboardScreen.kt') as f:
    content = f.read()
    lines = content.splitlines(keepends=True)

occ_list = []
for k, v in data.items():
    for occ in v.get('occurrences', []):
        if 'ClipboardScreen' in occ.get('file', ''):
            occ_list.append((k, occ, v.get('text', '')))

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

with open('app/src/main/java/com/example/ui/screens/ClipboardScreen.kt', 'w') as f:
    f.write(full_text)

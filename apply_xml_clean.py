import json, re

with open("en_translations.json", "r", encoding="utf-8") as f:
    en_map = json.load(f)

with open("keys_meta.json", "r", encoding="utf-8") as f:
    meta_map = json.load(f)

def to_android_xml(val):
    val = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    val = re.sub(r"(?<!\\)'", r"\'", val)
    val = re.sub(r'(?<!\\)"', r'\"', val)
    val = val.replace("\r\n", "\\n").replace("\n", "\\n")
    # escape literal percent
    val = re.sub(r'(%[0-9]+\$[a-zA-Z])%', r'\1%%', val)
    val = re.sub(r'([0-9]+)%', r'\1%%', val)
    return val

# 1. Update app/src/main/res/values/strings.xml
with open("public/assets/aistudio/strings_85982a6a.xml", "r", encoding="utf-8") as f:
    lines = f.readlines()

output_lines = []
existing_keys = set()
for line in lines:
    m = re.search(r'<string name="([^"]+)"', line)
    if m:
        key = m.group(1)
        if key in en_map:
            val = to_android_xml(en_map[key])
            output_lines.append(f'    <string name="{key}">{val}</string>\n')
            existing_keys.add(key)
        else:
            output_lines.append(line)
    elif '</resources>' in line:
        # Add any missing keys from en_map that were not in strings.xml
        for k in sorted(en_map.keys()):
            if k not in existing_keys:
                val = to_android_xml(en_map[k])
                output_lines.append(f'    <string name="{k}">{val}</string>\n')
                existing_keys.add(k)
        output_lines.append(line)
    else:
        output_lines.append(line)

with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.writelines(output_lines)

print(f"values/strings.xml written. Total en keys matched: {len(existing_keys)}")

# 2. Update app/src/main/res/values-ar/strings.xml
# Take the first 69 lines (the base translations) and add all 372 keys
with open("app/src/main/res/values-ar/strings.xml", "r", encoding="utf-8") as f:
    ar_lines = f.readlines()

ar_output = []
for line in ar_lines:
    if '</resources>' in line:
        break
    m = re.search(r'<string name="([^"]+)"', line)
    if m and m.group(1).startswith("ui_"):
        continue # skip existing ui_ in ar to replace with clean escaped versions
    ar_output.append(line)

for k in sorted(meta_map.keys()):
    ar_text = meta_map[k]["text"]
    val = to_android_xml(ar_text)
    ar_output.append(f'    <string name="{k}">{val}</string>\n')

ar_output.append('</resources>\n')

with open("app/src/main/res/values-ar/strings.xml", "w", encoding="utf-8") as f:
    f.writelines(ar_output)

print(f"values-ar/strings.xml written with {len(meta_map)} keys.")

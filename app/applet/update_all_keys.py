import os
import re
import xml.etree.ElementTree as ET

# 1. Find all .kt files and extract all ui_ keys referenced
kt_files = []
for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt'):
            kt_files.append(os.path.join(root, f))

pattern = re.compile(r'"(ui_[a-zA-Z0-9_]+)"')
found_keys = set()
key_to_files = {}

for path in kt_files:
    with open(path, 'r', encoding='utf-8') as file:
        content = file.read()
        matches = pattern.findall(content)
        for m in matches:
            found_keys.add(m)
            key_to_files.setdefault(m, set()).add(os.path.basename(path))

print(f"Total unique ui_ keys found in .kt files: {len(found_keys)}")

# Read AppStrings.kt to get codeFallbackMap
app_strings_path = 'app/src/main/java/com/example/util/AppStrings.kt'
with open(app_strings_path, 'r', encoding='utf-8') as f:
    app_strings_content = f.read()

fallback_map = {}
fb_pattern = re.compile(r'"(ui_[a-zA-Z0-9_]+)"\s*->\s*"([^"]+)"')
for match in fb_pattern.findall(app_strings_content):
    fallback_map[match[0]] = match[1]

# Read strings.xml
strings_path = 'app/src/main/res/values/strings.xml'
tree = ET.parse(strings_path)
root = tree.getroot()

existing_xml_keys = {}
for child in root:
    if child.tag == 'string':
        name = child.attrib.get('name')
        if name:
            existing_xml_keys[name] = child.text or ""

added_count = 0
added_list = []
for k in sorted(found_keys):
    if k not in existing_xml_keys:
        text = fallback_map.get(k, k.replace('_', ' ').title())
        new_elem = ET.Element('string', {'name': k})
        new_elem.text = text
        root.append(new_elem)
        added_count += 1
        added_list.append(k)

tree.write(strings_path, encoding='utf-8', xml_declaration=True)
print(f"Added {added_count} missing keys to strings.xml")
print("Added keys:", added_list)

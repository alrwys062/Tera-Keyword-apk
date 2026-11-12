import os
import re
import xml.etree.ElementTree as ET

kt_files = []
for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt'):
            kt_files.append(os.path.join(root, f))

pattern = re.compile(r'"(ui_[a-zA-Z0-9_]+)"')
found_keys = set()
key_locations = {}

for path in kt_files:
    try:
        with open(path, 'r', encoding='utf-8') as file:
            content = file.read()
            matches = pattern.findall(content)
            for m in matches:
                found_keys.add(m)
                key_locations.setdefault(m, set()).add(path)
    except Exception as e:
        print(f"Error reading {path}: {e}")

strings_path = 'app/src/main/res/values/strings.xml'
xml_keys = set()
if os.path.exists(strings_path):
    tree = ET.parse(strings_path)
    root = tree.getroot()
    for child in root:
        if child.tag == 'string':
            name = child.attrib.get('name')
            if name:
                xml_keys.add(name)

missing_in_strings = found_keys - xml_keys

print(f"Total unique ui_ keys found in .kt files: {len(found_keys)}")
print(f"Keys already in strings.xml: {len(found_keys & xml_keys)}")
print(f"Keys missing in strings.xml: {len(missing_in_strings)}")

print("\nMissing keys:")
for k in sorted(missing_in_strings):
    locs = ', '.join(key_locations[k])
    print(f"  {k} (used in {locs})")

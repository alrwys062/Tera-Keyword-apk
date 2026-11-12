import os
import re

kt_files = []
for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt'):
            kt_files.append(os.path.join(root, f))

pattern = re.compile(r'"(ui_[a-zA-Z0-9_]+)"')
all_keys = set()
key_to_files = {}

for path in kt_files:
    with open(path, 'r', encoding='utf-8') as file:
        content = file.read()
        matches = pattern.findall(content)
        for m in matches:
            all_keys.add(m)
            key_to_files.setdefault(m, set()).add(os.path.basename(path))

print(f"Total unique ui_ keys found in .kt files: {len(all_keys)}")
for k in sorted(all_keys):
    print(f"{k}: found in {', '.join(key_to_files[k])}")

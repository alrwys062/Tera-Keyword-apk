import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt') as f:
    lines = f.readlines()

for idx, line in enumerate(lines):
    # check for Arabic text
    m = re.findall(r'"([^"]*[\u0600-\u06FF][^"]*)"', line)
    if m:
        # ignore custom Arabic rows defaults like 'ض ص ق...'
        if any(char in 'ضصقف' for char in m[0]):
            continue
        print(f"Line {idx+1}: {repr(m)}")

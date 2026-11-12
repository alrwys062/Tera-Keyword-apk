import re

path = 'app/src/main/java/com/example/util/AppStrings.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace $s inside string literals with \$s or replace %1$s with %1\$s
# Actually, any occurrence of $ followed by a word character (like $s, $dp, $ms, $%) in strings needs escaping or handling.
# Wait, if we use triple quotes or escape $, e.g. %1$s -> %1\$s
fixed = re.sub(r'%([0-9]*)\$s', r'%\1\\$s', content)
fixed = re.sub(r'%s', r'\\$s', fixed)

with open(path, 'w', encoding='utf-8') as f:
    f.write(fixed)

print("Fixed AppStrings.kt string interpolations")

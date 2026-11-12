import json, re, xml.etree.ElementTree as ET

with open("en_translations.json", "r", encoding="utf-8") as f:
    en_map = json.load(f)

with open("keys_meta.json", "r", encoding="utf-8") as f:
    meta_map = json.load(f)

def escape_xml(s):
    # s is plain text
    # escape & < >
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    # escape single quotes with backslash for Android XML
    s = s.replace("'", "\\'")
    # escape double quotes
    s = s.replace('"', '\\"')
    # replace newlines with \n
    s = s.replace("\n", "\\n")
    return s

print(f"Total keys to process: {len(en_map)}")

# 1. Update app/src/main/res/values/strings.xml
with open("app/src/main/res/values/strings.xml", "r", encoding="utf-8") as f:
    en_xml_content = f.read()

# We need to replace or add all 372 ui_ keys with their English translation
# Parse the XML to keep existing non-ui keys and update/insert ui_ keys
tree = ET.parse("app/src/main/res/values/strings.xml")
root = tree.getroot()

existing_nodes = {}
for child in root:
    if child.tag == "string":
        existing_nodes[child.attrib.get("name")] = child

# For each of the 372 keys, update its text or create node
added_count = 0
updated_count = 0
for k, en_val in en_map.items():
    if k in existing_nodes:
        existing_nodes[k].text = en_val
        updated_count += 1
    else:
        new_elem = ET.SubElement(root, "string", {"name": k})
        new_elem.text = en_val
        added_count += 1

print(f"values/strings.xml: updated {updated_count}, added {added_count}")

# Write back values/strings.xml nicely formatted
# Let us write it with clean Android strings xml syntax
lines = ['<resources>']
for child in root:
    if child.tag == "string":
        name = child.attrib["name"]
        text = child.text or ""
        escaped = escape_xml(text)
        # check if contains formatting like %1$s or plain %
        # If string has % that is not format, formatted="false" might be needed, but in our case:
        lines.append(f'    <string name="{name}">{escaped}</string>')
    elif child.tag == "color":
        lines.append(f'    <color name="{child.attrib["name"]}">{child.text}</color>')
lines.append('</resources>\n')

with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write("\n".join(lines))
print("Successfully written app/src/main/res/values/strings.xml")

# 2. Update app/src/main/res/values-ar/strings.xml
ar_tree = ET.parse("app/src/main/res/values-ar/strings.xml")
ar_root = ar_tree.getroot()
ar_existing_nodes = {}
for child in ar_root:
    if child.tag == "string":
        ar_existing_nodes[child.attrib.get("name")] = child

ar_added = 0
ar_updated = 0
for k, meta in meta_map.items():
    ar_val = meta["text"]
    if k in ar_existing_nodes:
        ar_existing_nodes[k].text = ar_val
        ar_updated += 1
    else:
        new_elem = ET.SubElement(ar_root, "string", {"name": k})
        new_elem.text = ar_val
        ar_added += 1

print(f"values-ar/strings.xml: updated {ar_updated}, added {ar_added}")

ar_lines = ['<resources>']
for child in ar_root:
    if child.tag == "string":
        name = child.attrib["name"]
        text = child.text or ""
        escaped = escape_xml(text)
        ar_lines.append(f'    <string name="{name}">{escaped}</string>')
ar_lines.append('</resources>\n')

with open("app/src/main/res/values-ar/strings.xml", "w", encoding="utf-8") as f:
    f.write("\n".join(ar_lines))
print("Successfully written app/src/main/res/values-ar/strings.xml")

import xml.etree.ElementTree as ET
import urllib.request
import urllib.parse
import json
import re
import time
import sys
import os
import concurrent.futures

def protect(text):
    tokens = {}
    counter = 0

    # 1. Protect Turbo Keyboard
    def rep_tk(m):
        nonlocal counter
        tok = f"__TKPROT{counter}__"
        tokens[tok] = m.group(0)
        counter += 1
        return tok
    text = re.sub(r'Turbo\s+Keyboard', rep_tk, text, flags=re.IGNORECASE)

    # 2. Protect placeholders like %1$s, %2$s, %d, %s, %1$d, etc.
    def rep_ph(m):
        nonlocal counter
        tok = f"__PHPROT{counter}__"
        tokens[tok] = m.group(0)
        counter += 1
        return tok
    text = re.sub(r'%(?:\d+\$)?[#0\- +,\(]?(?:\d+)?(?:\.\d+)?[a-zA-Z%]', rep_ph, text)

    # 3. Protect URLs and channels
    def rep_url(m):
        nonlocal counter
        tok = f"__URLPROT{counter}__"
        tokens[tok] = m.group(0)
        counter += 1
        return tok
    text = re.sub(r'https?://\S+|t\.me/\S+', rep_url, text)

    return text, tokens

def unprotect(text, tokens):
    for tok, val in tokens.items():
        # case-insensitive replace
        pattern = re.compile(re.escape(tok), re.IGNORECASE)
        text = pattern.sub(val, text)
    return text

def xml_escape(val):
    val = val.replace('&', '&amp;')
    val = val.replace('<', '&lt;')
    val = val.replace('>', '&gt;')
    val = val.replace("'", "\\'")
    val = val.replace('"', '\\"')
    return val

def translate_single(item, target_code):
    name = item["name"]
    raw_text = item["text"]
    formatted = item["formatted"]
    
    if name == "app_name":
        return {"name": name, "text": "Turbo Keyboard", "formatted": formatted}
    if not raw_text or not raw_text.strip():
        return {"name": name, "text": raw_text, "formatted": formatted}
        
    protected, tokens = protect(raw_text)
    
    # If nothing to translate or purely tokens
    if not re.search(r'[a-zA-Z\u0600-\u06FF]', protected):
        unprot = unprotect(protected, tokens)
        return {"name": name, "text": unprot, "formatted": formatted}
        
    encoded = urllib.parse.quote(protected)
    url = f"https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl={target_code}&q={encoded}"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    
    tr_text = protected
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=6) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                res = []
                for elem in data:
                    if isinstance(elem, list) and len(elem) > 0 and isinstance(elem[0], str):
                        res.append(elem[0])
                    elif isinstance(elem, str):
                        res.append(elem)
                tr_text = "".join(res)
                break
        except Exception:
            time.sleep(0.2 * (attempt + 1))
            
    unprot = unprotect(tr_text, tokens)
    return {"name": name, "text": unprot, "formatted": formatted}

def generate_language_strings(target_code, folder_code):
    tree = ET.parse("app/src/main/res/values/strings.xml")
    root = tree.getroot()
    
    entries = []
    for c in root:
        if c.tag == "string":
            entries.append({
                "name": c.attrib["name"],
                "text": c.text or "",
                "formatted": c.attrib.get("formatted")
            })
            
    print(f"Translating {len(entries)} keys for {target_code} (values-{folder_code})...")
    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
        futures = [executor.submit(translate_single, entry, target_code) for entry in entries]
        translated = [f.result() for f in futures]
        
    out_dir = f"app/src/main/res/values-{folder_code}"
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, "strings.xml")
    
    with open(out_file, "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n')
        f.write('<resources>\n')
        for item in translated:
            name = item["name"]
            txt = item["text"]
            if name == "app_name":
                txt = "Turbo Keyboard"
            formatted_attr = f' formatted="{item["formatted"]}"' if item["formatted"] else ""
            escaped = xml_escape(txt)
            f.write(f'    <string name="{name}"{formatted_attr}>{escaped}</string>\n')
        f.write('</resources>\n')
        
    # Validate with ET
    ET.parse(out_file)
    print(f"Successfully generated and validated {out_file} with {len(translated)} keys in {time.time() - start_time:.2f}s")

if __name__ == '__main__':
    target = sys.argv[1]
    folder = sys.argv[2]
    generate_language_strings(target, folder)

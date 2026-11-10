import urllib.request
import urllib.parse
import json
import time
import concurrent.futures

def translate_one(text):
    if not text or not text.strip():
        return text
    url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=zh-CN&dt=t&q=" + urllib.parse.quote(text)
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return "".join([part[0] for part in data[0] if part[0]])
    except Exception as e:
        return text

samples = [f"Item {i}: Keyboard settings and configuration options" for i in range(20)]
start = time.time()
with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
    results = list(executor.map(translate_one, samples))

print(f"Translated {len(results)} items in {time.time() - start:.2f}s")
print("First result:", results[0])

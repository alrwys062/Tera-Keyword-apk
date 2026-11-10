import multiprocessing
import translate_lang
import time
import os

langs = [
    ('nl', 'nl'),
    ('pl', 'pl'),
    ('vi', 'vi'),
    ('th', 'th'),
    ('el', 'el'),
    ('ku', 'ku')
]

def worker(item):
    target, folder = item
    dest = f"app/src/main/res/values-{folder}/strings.xml"
    if os.path.exists(dest) and os.path.getsize(dest) > 10000:
        print(f"Skipping {folder}, already exists.")
        return
    t0 = time.time()
    print(f"Starting {target} -> values-{folder}...")
    translate_lang.generate_language_strings(target, folder)
    print(f"Finished {folder} in {time.time()-t0:.1f}s")

if __name__ == '__main__':
    with multiprocessing.Pool(processes=3) as pool:
        pool.map(worker, langs)

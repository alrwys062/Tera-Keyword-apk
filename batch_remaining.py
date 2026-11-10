import translate_lang
import time
from concurrent.futures import ProcessPoolExecutor

remaining = [
    ('nl', 'nl'),
    ('pl', 'pl'),
    ('vi', 'vi'),
    ('th', 'th'),
    ('el', 'el'),
    ('ku', 'ku')
]

def run(item):
    target, folder = item
    print(f"=== Starting {target} -> values-{folder} ===")
    t0 = time.time()
    translate_lang.generate_language_strings(target, folder)
    print(f"=== Finished {folder} in {time.time()-t0:.1f}s ===")

if __name__ == '__main__':
    with ProcessPoolExecutor(max_workers=2) as executor:
        list(executor.map(run, remaining))

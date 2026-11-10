import translate_lang
import time

languages = [
    ('ja', 'ja'),
    ('ko', 'ko'),
    ('nl', 'nl'),
    ('pl', 'pl'),
    ('vi', 'vi'),
    ('th', 'th'),
    ('el', 'el'),
    ('ku', 'ku')
]

for target, folder in languages:
    print(f"=== Starting {target} (values-{folder}) ===")
    t0 = time.time()
    translate_lang.generate_language_strings(target, folder)
    print(f"=== Finished {target} in {time.time()-t0:.1f}s ===")


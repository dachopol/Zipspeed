import os
import re

directories = ["app/src/main/java/com/example/ui"]
kotlin_files = []
for root, _, files in os.walk(directories[0]):
    for f in files:
        if f.endswith(".kt"):
            kotlin_files.append(os.path.join(root, f))

# regex to find if (language == Language.TH) "th" else "en"
# Note: some are multi-line. It's safer to use a script or do it semi-manually.

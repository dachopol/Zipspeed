import os
import re

directories = ["app/src/main/java/com/example/ui", "app/src/main/java/com/example"]

# Dictionaries to store strings
en_strings = {}
th_strings = {}
counter = 1

def slugify(text):
    text = text.lower()
    text = re.sub(r'[^a-z0-9]+', '_', text)
    return text.strip('_')[:30]

for root, _, files in os.walk(directories[0]):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            # Simple regex to match single line simple strings:
            # if (language == Language.TH) "..." else "..."
            pattern = r'if\s*\(\s*language\s*==\s*Language\.TH\s*\)\s*"([^"]+)"\s*else\s*"([^"]+)"'
            
            def replacer(match):
                global counter
                th_text = match.group(1)
                en_text = match.group(2)
                key = f"str_{slugify(en_text)}_{counter}"
                counter += 1
                en_strings[key] = en_text
                th_strings[key] = th_text
                return f'stringResource(R.string.{key})'
            
            new_content, count = re.subn(pattern, replacer, content)
            if count > 0:
                # Add import if needed
                if "import androidx.compose.ui.res.stringResource" not in new_content:
                    new_content = new_content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.compose.ui.res.stringResource\nimport com.example.R")
                with open(filepath, 'w') as f:
                    f.write(new_content)

print("Extracted strings.")

os.makedirs("app/src/main/res/values", exist_ok=True)
os.makedirs("app/src/main/res/values-th", exist_ok=True)

with open("app/src/main/res/values/strings_gen.xml", "w") as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
    for k, v in en_strings.items():
        v = v.replace("'", "\\'").replace("&", "&amp;")
        f.write(f'    <string name="{k}">{v}</string>\n')
    f.write('</resources>\n')

with open("app/src/main/res/values-th/strings_gen.xml", "w") as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
    for k, v in th_strings.items():
        v = v.replace("'", "\\'").replace("&", "&amp;")
        f.write(f'    <string name="{k}">{v}</string>\n')
    f.write('</resources>\n')

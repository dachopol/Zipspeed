import { readFile } from 'node:fs/promises';

const files = ['public/index.html', 'app/src/main/assets/index.html'];
const failures = [];

for (const file of files) {
  let source;
  try {
    source = await readFile(file, 'utf8');
  } catch (error) {
    failures.push(`${file}: cannot read (${error.code ?? error.message})`);
    continue;
  }

  if (source.includes('\uFFFD')) failures.push(`${file}: contains replacement character (possible encoding corruption)`);
  if (!/<meta[^>]+charset=["']utf-8["']/i.test(source)) failures.push(`${file}: missing UTF-8 charset`);
  if (!/<meta[^>]+name=["']viewport["']/i.test(source)) failures.push(`${file}: missing viewport meta`);
  if (/target=["']_blank["'](?![^>]*rel=["'][^"']*noopener)/i.test(source)) failures.push(`${file}: target=_blank should include rel=noopener`);

  const unlabeledButtons = [...source.matchAll(/<button\b([^>]*)>/gi)]
    .filter(([, attrs]) => !/\baria-label\s*=|\bdata-i\s*=|>\s*[^<\s]/i.test(attrs));
  if (unlabeledButtons.length) failures.push(`${file}: ${unlabeledButtons.length} button(s) may lack an accessible name`);
}

if (failures.length) {
  console.error('UI audit failed:');
  for (const failure of failures) console.error(`- ${failure}`);
  process.exitCode = 1;
} else {
  console.log(`UI audit passed for ${files.length} canonical entry point(s).`);
}

import { access, mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";

const EXPECTED = {
  applicationId: "com.aistudio.zipspeed.zskt",
  versionCode: 71,
  versionName: "71.0.0"
};

const REQUIRED_FILES = [
  "PROJECT_WORKFLOW.md",
  "ใช้แชทสร้างแอพอัปโหลดอัตโนมัติ.txt",
  "public/index.html",
  "app/src/main/AndroidManifest.xml",
  "app/src/main/assets/index.html",
  "README.md",
  "README_TH.md"
];

const LEGACY_PATHS = [
  ".gradle",
  ".idea",
  "Zipspeed_Space_v3",
  "index.html",
  "zipspeed_ai_studio.zip",
  "redesign.patch",
  "CHANGED_FILES.txt",
  "START_HERE_TH.md",
  "untitled.tsx",
  "update.sh",
  "update_webtest.sh",
  "app/applet",
  "src/app.mjs",
  "audit.mjs",
  "runtime-check.mjs"
];

const TEXT_EXTENSIONS = new Set([
  ".kt", ".kts", ".html", ".js", ".mjs", ".json", ".xml", ".md", ".txt",
  ".yml", ".yaml", ".properties", ".gradle", ".toml"
]);

const SKIP_DIRS = new Set([
  ".git", ".gradle", ".idea", "node_modules", "dist", "build", ".kotlin", "artifacts"
]);

const read = (p) => readFile(p, "utf8");
const exists = async (p) => {
  try {
    await access(p);
    return true;
  } catch {
    return false;
  }
};

async function collectTextFiles(dir) {
  const out = [];
  if (!(await exists(dir))) return out;
  for (const entry of await readdir(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (!SKIP_DIRS.has(entry.name)) out.push(...await collectTextFiles(full));
      continue;
    }
    if (TEXT_EXTENSIONS.has(path.extname(entry.name).toLowerCase())) out.push(full);
  }
  return out;
}

const checks = [];
const add = (name, ok, detail) => checks.push({
  name,
  status: ok ? "PASS" : "FAIL",
  detail
});

for (const required of REQUIRED_FILES) {
  add(`required source: ${required}`, await exists(required), "must exist");
}

for (const legacy of LEGACY_PATHS) {
  add(`legacy path absent: ${legacy}`, !(await exists(legacy)), "must be absent");
}

const gradle = await read("app/build.gradle.kts");
const pkg = JSON.parse(await read("package.json"));
const workflow = await read(".github/workflows/android-ci.yml");

add(
  "applicationId locked",
  gradle.includes(`applicationId = "${EXPECTED.applicationId}"`),
  EXPECTED.applicationId
);
add(
  "versionCode locked",
  new RegExp(`versionCode\\s*=\\s*${EXPECTED.versionCode}\\b`).test(gradle),
  String(EXPECTED.versionCode)
);
add(
  "versionName locked",
  gradle.includes(`versionName = "${EXPECTED.versionName}"`),
  EXPECTED.versionName
);
add(
  "web version aligned",
  pkg.version === EXPECTED.versionName,
  pkg.version
);

const runtimeFiles = [
  ...await collectTextFiles("app/src/main"),
  ...await collectTextFiles("public")
];

const violations = [];
const patterns = [
  {
    label: "Math.random in runtime source",
    regex: /\bMath\.random\s*\(/
  },
  {
    label: "kotlin.random.Random in runtime source",
    regex: /\bkotlin\.random\.Random\b/
  },
  {
    label: "java.util.Random in runtime source",
    regex: /\bjava\.util\.Random\b/
  },
  {
    label: "numeric fake fallback for measured testState metric",
    regex: /\btestState\.(downloadMbps|uploadMbps|pingMs|jitterMs|packetLossPercent)\s*\?\:\s*-?\d+(?:\.\d+)?/
  },
  {
    label: "direct hardcoded measured metric",
    regex: /\b(downloadMbps|uploadMbps|pingMs|jitterMs|packetLossPercent)\s*=\s*-?\d+(?:\.\d+)?\b/
  },
  {
    label: "hardcoded ISP presented as runtime value",
    regex: /\bispName\s*=\s*"(?!(?:Unknown|ไม่ทราบ|--|Error))[^"]+"/
  },
  {
    label: "hardcoded server location presented as runtime value",
    regex: /\bserverLocation\s*=\s*"(?!(?:Unknown|ไม่ทราบ|--|Error))[^"]+"/
  },
  {
    label: "synthetic heatmap deadzone fallback",
    regex: /deadzonePoint\?\.dbm\s*\?\:\s*["']?-?\d/
  },
  {
    label: "synthetic point speed fallback",
    regex: /pt\.speed\s*\|\|\s*\d+/
  },
  {
    label: "synthetic router coordinate default",
    regex: /router[XY]\s*:\s*Double\s*=\s*-?\d/
  },
  {
    label: "synthetic room blueprint",
    regex: /Living Room \(Router\)|Kitchen Balcony|Working Study Room|Master Bedroom/
  },
  {
    label: "local fake premium entitlement",
    regex: /_isProPlan\.value\s*=\s*true/
  }
];

for (const file of runtimeFiles) {
  let source;
  try {
    source = await read(file);
  } catch {
    continue;
  }

  // D3 is a vendored visualization library; its internal RNG is not a Zipspeed
  // measurement source. Keep all non-random policy checks active for the file.
  const isVendoredD3 = file.replaceAll("\\", "/") === "app/src/main/assets/d3.v7.min.js";

  // The web space background randomizes decorative star coordinates only.
  // Remove only that exact initializer from the RNG scan; measurement code remains scanned.
  let randomScanSource = source;
  if (file.replaceAll("\\", "/") === "public/index.html") {
    randomScanSource = randomScanSource.replace(
      /const stars=Array\.from\(\{length:160\},\(\)=>\(\{x:\(Math\.random\(\)-\.5\)\*1800,y:\(Math\.random\(\)-\.5\)\*1800,z:Math\.random\(\)\*1200\+1\}\)\);/,
      ""
    );
  }

  for (const pattern of patterns) {
    const isRandomPattern =
      pattern.label === "Math.random in runtime source" ||
      pattern.label === "kotlin.random.Random in runtime source" ||
      pattern.label === "java.util.Random in runtime source";

    const target = isRandomPattern ? randomScanSource : source;
    if (isRandomPattern && isVendoredD3) continue;

    if (pattern.regex.test(target)) {
      violations.push(`${file}: ${pattern.label}`);
    }
  }
}

add(
  "Anti-Random / Real Data runtime scan",
  violations.length === 0,
  violations.length ? violations.join("; ") : "no prohibited fake/random/hardcoded runtime metric pattern found"
);

add(
  "project policy runs in CI",
  workflow.includes("npm run project:policy"),
  "npm run project:policy"
);
add(
  "canonical UI audit runs in CI",
  workflow.includes("npm run ui:audit"),
  "npm run ui:audit"
);
add(
  "web build runs in CI",
  workflow.includes("npm run build"),
  "npm run build"
);
add(
  "Android unit test runs in CI",
  workflow.includes(":app:testDebugUnitTest"),
  ":app:testDebugUnitTest"
);
add(
  "Android lint runs in CI",
  workflow.includes(":app:lintDebug"),
  ":app:lintDebug"
);
add(
  "Android debug build runs in CI",
  workflow.includes(":app:assembleDebug"),
  ":app:assembleDebug"
);
add(
  "clean source ZIP artifact configured",
  workflow.includes("Zipspeed-v71-source"),
  "Zipspeed-v71-source"
);

const failed = checks.filter((c) => c.status === "FAIL");
const generatedAt = new Date().toISOString();
const summary = {
  generatedAt,
  sourceOfTruth: "dachopol/Zipspeed main",
  expected: EXPECTED,
  checks,
  result: failed.length ? "FAIL" : "PASS",
  note: "This gate validates source policy only. Runtime/device/Play Console evidence remains separate."
};

await mkdir("artifacts/zipspeed-project-policy", { recursive: true });
await writeFile(
  "artifacts/zipspeed-project-policy/summary.json",
  JSON.stringify(summary, null, 2) + "\n"
);

const md = [
  "# Zipspeed Project Policy Gate",
  "",
  `Generated: ${generatedAt}`,
  "",
  `Result: **${summary.result}**`,
  "",
  "| Check | Status | Detail |",
  "|---|---|---|",
  ...checks.map((c) => `| ${c.name} | ${c.status} | ${String(c.detail).replaceAll("|", "\\|")} |`),
  "",
  "> Build success does not prove real-device behavior or Play Console acceptance."
].join("\n");

await writeFile("artifacts/zipspeed-project-policy/summary.md", md + "\n");

for (const c of checks) {
  console.log(`${c.status.padEnd(4)}  ${c.name}: ${c.detail}`);
}

if (failed.length) {
  console.error(`Project policy gate failed: ${failed.length} check(s) failed.`);
  process.exit(1);
}

console.log("Zipspeed project policy gate PASS.");

import { access, mkdir, readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";

const EXPECTED = {
  applicationId: "com.aistudio.zipspeed.zskt",
  versionCode: 71,
  versionName: "71.0.0",
  targetSdk: 36,
  compileSdk: 36
};

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
  "update_webtest.sh"
];

const TEXT_EXTENSIONS = new Set([
  ".md", ".txt", ".json", ".kts", ".kt", ".xml", ".mjs", ".js",
  ".html", ".yml", ".yaml", ".properties", ".gradle", ".toml", ".sh", ".py"
]);
const SKIP_DIRS = new Set([
  ".git", ".gradle", ".idea", "node_modules", "dist", "build", ".kotlin", "artifacts"
]);

const STALE_VERSION_PATTERNS = [
  ["legacy Android versionCode 42", /versionCode\s*=\s*42\b/],
  ["legacy Android versionName 42.x", /versionName\s*=\s*["']42\.0(?:\.0)?["']/],
  ["legacy web package version 3.0.1", /"version"\s*:\s*"3\.0\.1"/]
];

const read = (p) => readFile(p, "utf8");
const exists = async (p) => {
  try {
    await access(p);
    return true;
  } catch {
    return false;
  }
};

async function collectTextFiles(dir = ".") {
  const out = [];
  for (const entry of await readdir(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (!SKIP_DIRS.has(entry.name)) out.push(...await collectTextFiles(full));
      continue;
    }
    if (TEXT_EXTENSIONS.has(path.extname(entry.name).toLowerCase()) || entry.name === ".gitignore") {
      out.push(full);
    }
  }
  return out;
}

const gradle = await read("app/build.gradle.kts");
const manifest = await read("app/src/main/AndroidManifest.xml");
const pkg = JSON.parse(await read("package.json"));
const workflow = await read(".github/workflows/android-ci.yml");

const checks = [];
const add = (name, ok, detail) => checks.push({ name, status: ok ? "PASS" : "FAIL", detail });
const toVerify = [
  "Play App Signing + upload key ownership and validity",
  "Signed release AAB upload accepted by the live Play Console",
  "Current Privacy Policy URL/content matches the exact release artifact",
  "Play Console Data Safety answers match runtime data flows and enabled SDKs",
  "Ads declaration, App access, Target audience and IARC Content Rating",
  "Closed-test eligibility/status in the live Play Console (when the developer account is subject to that rule)",
  "Package-name registration / developer-verification status in the live Play Console"
];

add("applicationId",
  gradle.includes(`applicationId = "${EXPECTED.applicationId}"`),
  EXPECTED.applicationId);
add("compileSdk",
  new RegExp(`compileSdk\\s*\\{[^}]*release\\(${EXPECTED.compileSdk}\\)`, "s").test(gradle),
  String(EXPECTED.compileSdk));
add("targetSdk",
  new RegExp(`targetSdk\\s*=\\s*${EXPECTED.targetSdk}\\b`).test(gradle),
  String(EXPECTED.targetSdk));
add("versionCode",
  new RegExp(`versionCode\\s*=\\s*${EXPECTED.versionCode}\\b`).test(gradle),
  String(EXPECTED.versionCode));
add("versionName",
  gradle.includes(`versionName = "${EXPECTED.versionName}"`),
  EXPECTED.versionName);
add("web project version",
  pkg.version === EXPECTED.versionName,
  pkg.version);
add("UI audit script",
  pkg.scripts?.["ui:audit"] === "node scripts/audit-ui.mjs",
  pkg.scripts?.["ui:audit"] ?? "missing");
add("INTERNET permission",
  manifest.includes("android.permission.INTERNET"),
  "required for network tests");
add("ACCESS_NETWORK_STATE permission",
  manifest.includes("android.permission.ACCESS_NETWORK_STATE"),
  "required for network status");
add("CI UI audit",
  workflow.includes("npm run ui:audit"),
  "npm run ui:audit");
add("CI unit tests",
  workflow.includes(":app:testDebugUnitTest"),
  ":app:testDebugUnitTest");
add("CI lint",
  workflow.includes(":app:lintDebug"),
  ":app:lintDebug");
add("CI debug build",
  workflow.includes(":app:assembleDebug"),
  ":app:assembleDebug");
add("CI Play gate",
  workflow.includes("npm run play:check") || workflow.includes("node play-console-check.mjs"),
  "play-console-test-check");
add("CI evidence artifact",
  workflow.includes("zipspeed-play-console-evidence"),
  "zipspeed-play-console-evidence");
add("Signed release is explicitly gated",
  workflow.includes("ENABLE_SIGNED_RELEASE") && workflow.includes(":app:bundleRelease"),
  "bundleRelease only when release signing is enabled");

for (const legacyPath of LEGACY_PATHS) {
  add(`legacy path removed: ${legacyPath}`, !(await exists(legacyPath)), "must be absent");
}

const staleVersionHits = [];
for (const file of await collectTextFiles()) {
  let source;
  try {
    source = await read(file);
  } catch {
    continue;
  }
  for (const [label, pattern] of STALE_VERSION_PATTERNS) {
    if (pattern.test(source)) staleVersionHits.push(`${file}: ${label}`);
  }
}
add(
  "no stale project version markers",
  staleVersionHits.length === 0,
  staleVersionHits.length ? staleVersionHits.join("; ") : "v71 sources/docs only"
);

const failed = checks.filter((c) => c.status === "FAIL");
const now = new Date().toISOString();

await mkdir("artifacts/zipspeed-play-console-evidence", { recursive: true });

const summary = {
  generatedAt: now,
  buildType: "TEST BUILD",
  expected: EXPECTED,
  checks,
  toVerify,
  result: failed.length === 0 ? "PASS" : "FAIL",
  note: "PASS means repository/CI source gates passed. It does not prove Play Console approval or signed-AAB upload acceptance."
};

await writeFile(
  "artifacts/zipspeed-play-console-evidence/summary.json",
  JSON.stringify(summary, null, 2) + "\n"
);

const md = [
  "# Zipspeed Play Console Test Gate Evidence",
  "",
  `Generated: ${now}`,
  "",
  `Result: **${summary.result}**`,
  "",
  "| Check | Status | Detail |",
  "|---|---|---|",
  ...checks.map((c) => `| ${c.name} | ${c.status} | ${String(c.detail).replaceAll("|", "\\|")} |`),
  "",
  "## TO VERIFY / UNVERIFIED",
  "",
  ...toVerify.map((x) => `- ${x}`),
  "",
  "> A repository gate cannot prove live Play Console state, account eligibility, signing-key ownership, policy-form accuracy or AAB upload acceptance."
].join("\n");

await writeFile("artifacts/zipspeed-play-console-evidence/summary.md", md + "\n");

for (const c of checks) {
  console.log(`${c.status.padEnd(4)}  ${c.name}: ${c.detail}`);
}
for (const item of toVerify) console.log(`TO VERIFY  ${item}`);

if (failed.length) {
  console.error(`Play Console Test Gate failed: ${failed.length} check(s) failed.`);
  process.exit(1);
}

console.log("Play Console Test Gate PASS (repository/CI source + hygiene scope only).");

import { mkdir, readFile, writeFile } from "node:fs/promises";

const EXPECTED = {
  applicationId: "com.aistudio.zipspeed.zskt",
  versionCode: 71,
  versionName: "71.0.0",
  targetSdk: 36,
  compileSdk: 36
};

const read = (p) => readFile(p, "utf8");
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
add("INTERNET permission",
  manifest.includes("android.permission.INTERNET"),
  "required for network tests");
add("ACCESS_NETWORK_STATE permission",
  manifest.includes("android.permission.ACCESS_NETWORK_STATE"),
  "required for network status");
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

console.log("Play Console Test Gate PASS (repository/CI source scope only).");

import { spawnSync } from "node:child_process";
import { mkdirSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";

const cwd = fileURLToPath(new URL("../projects/runbook-assistant/", import.meta.url));
mkdirSync(join(cwd, "build"), { recursive: true });
function run(tool, args) {
  const executable = process.env.JAVA_HOME
    ? join(process.env.JAVA_HOME, "bin", tool + (process.platform === "win32" ? ".exe" : ""))
    : tool;
  const result = spawnSync(executable, args, { cwd, stdio: "inherit" });
  if (result.error) console.error(result.error.message);
  if (result.status !== 0) process.exit(result.status ?? 1);
}
run("javac", [
  "--release",
  "21",
  "-d",
  "build",
  "src/RunbookAssistant.java",
  "src/RunbookAssistantTest.java",
]);
run("java", ["-cp", "build", "RunbookAssistantTest"]);
// Use the default threshold so local environment overrides cannot change CI fixtures.
delete process.env.MIN_SCORE;
run("java", [
  "-cp",
  "build",
  "RunbookAssistant",
  "--evaluate",
  "data/runbooks.tsv",
  "data/queries.tsv",
]);

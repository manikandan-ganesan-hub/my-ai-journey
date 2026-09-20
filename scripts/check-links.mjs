import { existsSync, readFileSync, readdirSync, statSync } from "node:fs";
import { dirname, extname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import MarkdownIt from "markdown-it";
import GithubSlugger from "github-slugger";

const root = fileURLToPath(new URL("../", import.meta.url));
const parser = new MarkdownIt();
const external = process.argv.includes("--external");
const skip = new Set([".git", "node_modules", "build"]);
function files(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    if (skip.has(entry.name)) return [];
    const path = join(directory, entry.name);
    return entry.isDirectory() ? files(path) : extname(path) === ".md" ? [path] : [];
  });
}
const docs = files(root);
const parsed = new Map(docs.map((path) => [path, parser.parse(readFileSync(path, "utf8"), {})]));
function anchors(path) {
  const slugger = new GithubSlugger();
  const tokens = parsed.get(path) ?? [];
  return new Set(
    tokens.flatMap((token, index) => {
      if (token.type !== "heading_open") return [];
      const text = (tokens[index + 1].children ?? [])
        .filter((child) => ["text", "code_inline"].includes(child.type))
        .map((child) => child.content)
        .join("");
      return [slugger.slug(text)];
    }),
  );
}
const urls = new Set();
const errors = [];
let localCount = 0;
function inspect(tokens, path) {
  for (const token of tokens) {
    const href = token.type === "image" ? token.attrGet("src") : token.attrGet("href");
    if (href) {
      if (/^https?:\/\//.test(href)) {
        const url = new URL(href);
        url.hash = "";
        urls.add(url.href);
      } else if (!/^[a-z][a-z\d+.-]*:/i.test(href)) {
        localCount++;
        const [target, fragment] = href.split("#");
        const destination = target ? resolve(dirname(path), decodeURIComponent(target)) : path;
        if (!existsSync(destination)) errors.push(`${path}: missing ${href}`);
        else if (
          fragment &&
          statSync(destination).isFile() &&
          extname(destination) === ".md" &&
          !anchors(destination).has(decodeURIComponent(fragment))
        ) {
          errors.push(`${path}: missing heading ${href}`);
        }
      }
    }
    if (token.children) inspect(token.children, path);
  }
}
for (const [path, tokens] of parsed) inspect(tokens, path);
if (external) {
  const queue = [...urls];
  async function worker() {
    while (queue.length) {
      const url = queue.shift();
      let failure;
      for (let attempt = 0; attempt < 2; attempt++) {
        try {
          const response = await fetch(url, {
            signal: AbortSignal.timeout(20000),
            headers: { "User-Agent": "my-ai-journey-link-check/0.1" },
          });
          await response.body?.cancel();
          if (!response.ok) throw new Error(`HTTP ${response.status}`);
          console.log(`OK ${url}`);
          failure = undefined;
          break;
        } catch (error) {
          failure = `${url}: ${error.message}`;
        }
      }
      if (failure) errors.push(failure);
    }
  }
  await Promise.all(Array.from({ length: Math.min(4, queue.length) }, worker));
}
console.log(
  `Checked ${docs.length} Markdown files, ${localCount} local links${external ? `, ${urls.size} external URLs` : ""}.`,
);
if (errors.length) {
  errors.forEach((error) => console.error(error));
  process.exitCode = 1;
}

# Contributing

[Home](README.md)

Small corrections and experiments are more useful than a large list of headings.
Open a content-correction issue for a factual problem or a project-idea issue for
a new experiment. Keep each change focused enough for one maintainer to review.

## Local checks

Prerequisites: JDK 21+, Node.js 22+, and npm. Run from the repository root:

```sh
npm ci
npm run format
npm test
npm run links:external
```

`npm test` checks Prettier formatting, Markdown lint, local file/heading links,
Java 21-targeted compilation, the regression suite, and the retrieval
fixture. Java uses no downloaded libraries. npm dependencies are maintenance
tools pinned by package.json and package-lock.json. `JAVA_HOME`, when set,
selects the JDK used by the test script; otherwise it uses PATH.

External links are checked separately because network failures should not make
every code review nondeterministic. A scheduled GitHub Actions job checks them
weekly and fails on unreachable URLs. Investigate failures; do not blanket-ignore
403, 429, or timeouts. An HTTP success is not proof that a page is still relevant.
The checker covers Markdown links/images and reference links, not bare URLs inside
code blocks. Review any newly added literal URLs manually.

Render changed Markdown in GitHub's preview or an editor with Mermaid support.
Check diagrams for syntax, clipping, and accurate arrows. Preview wide tables on
a narrow viewport. These visual and semantic checks remain a reviewer task.

## Topic and project expectations

Start with intuition and one concrete problem. Define new terms, explain the
mechanism, then discuss limitations and production decisions. Add an exercise and
primary references. Use previous/home/next links when a page belongs to the learning
path. Do not force identical headings onto every subject.

Mark material as conceptual, working, planned, or an external resource. A working
project needs prerequisites, dependency versions, `.env.example`, run commands,
expected output, error handling, tests, and API cost notes. Prefer a local path.
Do not imply a mock implements the model behavior it replaces.

Use synthetic data only. Never commit API keys, private documents, employer code,
customer records, or trading data. Keep `.env` local. Cite sources near claims and
record access dates for changing material. See the [attribution policy](RESOURCES.md).
Original contributions are provided under this repository's [MIT license](LICENSE).

## Maintenance cadence

Review link failures and dependency pull requests when they arrive. For a new model
or library version, rerun relevant examples and evaluations before updating status.
If an example stops working, mark it accordingly and explain the failure. Do not
keep a tested-version badge that no longer has supporting evidence.

# First release verification

[Home](../README.md) · [Initial audit](initial-audit.md)

Verified locally on 2026-09-21. Base commit: `394524d`.
Feature branch: `feat/practical-ai-engineering`.

## Evidence and limits

| Check                             | Result                                                                                                                                    |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| `npm test`                        | Passed with Node.js 22.15.1, npm 10.9.2, and Oracle JDK 24.0.1 compiling with `--release 21`                                              |
| Java 21 compilation and execution | Passed on Temurin 21.0.12.1 using its native default target; 45 checks passed                                                             |
| Example query                     | Returned `duplicate-events`, score `0.482`, and the exact documented excerpt                                                              |
| Retrieval fixture                 | 3/4 answerable hits@1; 1/1 unanswerable abstentions; the paraphrase miss remains visible                                                  |
| Markdown formatting and lint      | Prettier and markdownlint passed                                                                                                          |
| Local links                       | File links and heading links passed; a temporary missing-file/missing-heading probe correctly failed, while a valid reference link passed |
| External references               | All 14 distinct Markdown URLs returned successful HTTP responses; pages were also opened for relevance                                    |
| Rendered content                  | Markdown rendered locally; all four Mermaid diagrams rendered and were visually reviewed                                                  |
| Narrow layout                     | Entry README, modules, project, and architecture previewed at 390px; tables/code scroll within their containers                           |
| Dependency installation           | Locked npm maintenance dependencies installed; initial audit reported no known vulnerabilities                                            |
| GitHub Actions                    | Configuration added; no remote run claimed because the branch has not been pushed                                                         |

The Java 21 compiler's `--release 21` invocation hit a local Windows sandbox
`AccessDeniedException` while closing its `ct.sym` archive. Native Java 21
compilation without cross-release mode passed, as did the Java 21 runtime tests.
The standard repository check command passed with JDK 24 targeting Java 21.
The CI job is configured to exercise that standard command on Linux/JDK 21;
its first remote execution remains unverified.

Rendering used Markdown-it and Mermaid CLI 11.17.0's renderer in headless Chrome.
The preview approximated GitHub styling; it is not a claim of testing GitHub's
own renderer. No screenshot or generated preview is required to run the repository.

## Scope review

- The root README distinguishes conceptual pages, a working example, planned work,
  and external resources. No framework badge claims an untested integration.
- Text and diagrams were written for this repository. Presentation references are
  acknowledged in the audit and resources; no external lesson was copied.
- Data is fictional. The CLI performs no model calls, network requests, or actions.
- Spring AI generation, learned embeddings, a Python comparison, and deployed
  service controls remain planned. The architecture page is explicitly a design.
- The evaluation fixture is small and hand-written. It is not a general accuracy
  benchmark or proof that a retrieved passage answers an arbitrary question.
- No remote branch, pull request, merge, or published release was created.

## Final source tree

Generated `node_modules/`, compiled `build/` directories, and Git internals are
excluded below and ignored by Git.

```text
my-ai-journey/
├── .editorconfig
├── .gitattributes
├── .gitignore
├── .markdownlint-cli2.jsonc
├── .prettierignore
├── .prettierrc.json
├── .github/
│   ├── dependabot.yml
│   ├── pull_request_template.md
│   ├── ISSUE_TEMPLATE/
│   │   ├── content-correction.md
│   │   └── project-idea.md
│   └── workflows/
│       ├── links.yml
│       └── verify.yml
├── README.md
├── ROADMAP.md
├── RESOURCES.md
├── CONTRIBUTING.md
├── LICENSE
├── package.json
├── package-lock.json
├── architecture/
│   └── document-assistant.md
├── docs/
│   ├── initial-audit.md
│   └── verification.md
├── learning/
│   ├── glossary.md
│   ├── llm-components.md
│   └── retrieval.md
├── projects/
│   └── runbook-assistant/
│       ├── .env.example
│       ├── README.md
│       ├── dependencies.properties
│       ├── data/
│       │   ├── queries.tsv
│       │   └── runbooks.tsv
│       └── src/
│           ├── RunbookAssistant.java
│           └── RunbookAssistantTest.java
└── scripts/
    ├── check-links.mjs
    └── test-java.mjs
```

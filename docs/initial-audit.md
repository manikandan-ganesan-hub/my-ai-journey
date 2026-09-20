# Initial repository audit

Inspected on 2026-09-21 before content changes.

- Base commit: `394524d15765638d68883d574aa57202c6e3b7fe` (`Create README.md`).
- History: one commit; the only tracked file was `README.md`.
- The full README and directory inventory were reviewed. There were no topic
  templates, source files, tests, configuration files, or license.
- The fresh clone had a clean working tree. No pre-existing local user changes
  were present in this checkout; other checkouts were not modified.
- Useful material: concepts-first learning, practical projects, evaluation,
  data preparation, and contribution intent.
- Gaps: headings and bullet lists described future coverage without lesson
  content. Framework/version badges had no supporting dependency files or tests.
  There was no duplicate implementation to reconcile.

## Implementation plan

1. Preserve the concepts-to-projects direction and move broad ambitions into a roadmap.
2. Write two focused modules and an accessible entry README.
3. Implement a local Java retrieval baseline over synthetic runbooks, with tests.
4. Explain a proposed Spring AI architecture without claiming it is implemented.
5. Add maintenance checks, contribution templates, verified sources, and MIT licensing.
6. Verify locally and commit by concern on `feat/practical-ai-engineering`.

Presentation references were inspected for usability: the short resource groupings
in [learn-ai-engineering](https://github.com/ashishps1/learn-ai-engineering) and
the roadmap/notebook entry points in
[llm-course](https://github.com/mlabonne/llm-course). Their wording, diagrams,
folder structure, and topic sequence were not reused. This release uses a narrow
read-run-design path suited to backend developers. Accessed 2026-09-21.

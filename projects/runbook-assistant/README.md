# Java runbook assistant

Status: Available · Working local retrieval example · Synthetic data only

[Previous: Retrieval](../../learning/retrieval.md) · [Home](../../README.md) ·
[Next: Service architecture](../../architecture/document-assistant.md)

Find a relevant runbook passage and return its text with a source ID. This example
uses word-count vectors and cosine similarity. It has no language model, learned
embeddings, remote service, or tool execution. The response is an excerpt, not a
generated answer. Do not use these fictional runbooks to operate a real system.

## Prerequisites and setup

Install JDK 21 or newer and put both `java` and `javac` on your PATH. From this
directory, run the commands below in PowerShell, Bash, or a similar terminal.
There are no third-party Java dependencies; [dependencies.properties](dependencies.properties)
records the Java baseline. Compilation targets Java 21 APIs and bytecode.

```sh
javac --release 21 -d build src/RunbookAssistant.java src/RunbookAssistantTest.java
java -cp build RunbookAssistantTest
java -cp build RunbookAssistant data/runbooks.tsv "How do I handle duplicate events?"
```

Expected test output: `PASS: 45 checks`. Expected query output:

```text
status=EXCERPT
source=duplicate-events similarity=0.482
For duplicate events, compare the event ID with the processed ID store. Do not apply the same event twice.
```

The score is lexical similarity, not confidence that the excerpt answers the
question. Read the source before relying on it.

```sh
java -cp build RunbookAssistant data/runbooks.tsv "banana bread recipe"
```

```text
status=NO_EVIDENCE
No matching passage. Try different wording.
```

No evidence is a normal result (exit 0). Invalid arguments, configuration,
unreadable files, and malformed data print `Error: ...` to stderr and exit 2.
If Java cannot find a class, compile first and run from this project directory.
If an older JDK rejects `--release 21`, install JDK 21 or newer.

## Configuration and cost

[.env.example](.env.example) documents `MIN_SCORE`, default `0.15`. The CLI does
not load dotenv files; set the variable in your shell only if you want to override
the default. Allowed values are finite numbers in `[0, 1]`. Zero-overlap passages
are always excluded. To restore the default, unset the variable.

PowerShell:

```powershell
$env:MIN_SCORE = '0.30'
java -cp build RunbookAssistant data/runbooks.tsv "duplicate events"
Remove-Item Env:MIN_SCORE
```

Bash:

```sh
MIN_SCORE=0.30 java -cp build RunbookAssistant data/runbooks.tsv "duplicate events"
```

No API key, network call, GPU, model download, or paid API is involved. Only local
CPU and memory are used. A future generation adapter could introduce provider
charges or local-model hardware costs; that adapter is planned, not included.

## Data flow and code map

1. `load` reads UTF-8 TSV rows as `id`, `title`, and `passage`. Each row is a chunk.
2. `vector` lowercases with `Locale.ROOT`, keeps Unicode letters/numbers, removes
   a small explicit English stopword set, and counts remaining words.
3. `retrieve` scores the question against each title-plus-passage vector, filters
   by threshold, sorts by descending score, and breaks ties by source ID.
4. `answer` returns the top passage unchanged or `NO_EVIDENCE`.

The [source](src/RunbookAssistant.java) uses records for the data contract.
`retrieve` supports `k` from 1 to 10; the CLI deliberately returns only one excerpt.
The entire corpus is scanned for each query and vectors are recalculated. That is
easy to inspect for three rows, but unsuitable for a large corpus.

Corpus files are bounded to 1 MiB. Blank/comment lines are skipped. Duplicate IDs,
blank fields, extra columns, malformed UTF-8, and terminal control characters are
rejected. Queries are limited to 500 Java characters and cannot contain controls.
There is no stemming, synonym expansion, document permission system, or persistence.

## Evaluation

```sh
java -cp build RunbookAssistant --evaluate data/runbooks.tsv data/queries.tsv
```

The final line with the default threshold is:

```text
Retrieval hits@1: 3/4; unanswerable abstentions: 1/1
```

The `replayed messages` query is labeled `duplicate-events` but returns `NONE`.
That known failure is kept in the fixture. The evaluation command reports all
matches and misses; a miss does not change its exit code. The test suite locks the
current baseline metrics so changes require deliberate review.

This is five hand-written cases over three hand-written passages, with no held-out
set. It is a regression fixture, not evidence of general retrieval accuracy.
The [tests](src/RunbookAssistantTest.java) also cover citation integrity, cosine
arithmetic, ties, locale handling, invalid input, file limits, and CLI errors.
They use explicit checks, so `-ea` is not required.

## Experiments worth trying

- Add a TSV row and a query with its relevant ID. Preserve the tab separators.
- Ask `How do I delete events?`. Lexical overlap can retrieve the duplicate-events
  passage even though it does not answer a deletion question.
- Raise `MIN_SCORE` and inspect both lost matches and abstentions. A threshold
  cannot reliably distinguish every answerable question from an unsupported one.
- Propose a semantic retriever behind the same `Hit` contract. Keep source IDs
  stable and compare both systems on questions not used for tuning.

The [next architecture page](../../architecture/document-assistant.md) explains
what changes before adding generation or exposing this as a service.

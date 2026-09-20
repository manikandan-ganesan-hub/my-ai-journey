import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Dependency-free regression suite. Explicit checks run without Java's -ea flag. */
public final class RunbookAssistantTest {
    private static int checks;
    @FunctionalInterface interface Checked { void run() throws Exception; }

    static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }

    static void rejects(Checked action, Class<? extends Exception> type, String name) throws Exception {
        try {
            action.run();
        } catch (Exception e) {
            check(type.isInstance(e), name + ": wrong exception " + e);
            return;
        }
        throw new AssertionError(name + ": expected rejection");
    }

    public static void main(String[] args) throws Exception {
        var corpus = RunbookAssistant.load(Path.of("data/runbooks.tsv"));
        var answer = RunbookAssistant.answer(corpus, "How do I handle duplicate events?", 0.15);
        check(answer.status() == RunbookAssistant.Status.EXCERPT, "excerpt status");
        check(answer.sourceId().equals("duplicate-events"), "correct citation");
        check(answer.text().equals(corpus.getFirst().text()), "verbatim source, not invented text");
        check(RunbookAssistant.answer(corpus, "banana bread recipe", 0).status()
                == RunbookAssistant.Status.NO_EVIDENCE, "zero overlap excluded even at threshold zero");
        check(RunbookAssistant.answer(corpus, "the and of", 0).status()
                == RunbookAssistant.Status.NO_EVIDENCE, "stopword-only query");
        check(RunbookAssistant.answer(corpus, "!!!", 0).status()
                == RunbookAssistant.Status.NO_EVIDENCE, "punctuation-only query");
        check(RunbookAssistant.answer(corpus, "replayed messages", 0.15).status()
                == RunbookAssistant.Status.NO_EVIDENCE, "known lexical paraphrase miss");
        check(RunbookAssistant.vector("DUPLICATE duplicate").get("duplicate") == 2, "term frequency");
        check(RunbookAssistant.vector("café CAFÉ").get("café") == 2, "Unicode letters");
        var a = RunbookAssistant.vector("duplicate event");
        var b = RunbookAssistant.vector("event latency");
        check(Math.abs(RunbookAssistant.cosine(a, b) - 0.5) < 1e-12, "documented cosine example");
        check(RunbookAssistant.cosine(a, a) > 0.99999, "identical vector");
        check(RunbookAssistant.cosine(a, RunbookAssistant.vector("")) == 0, "zero vector");
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            check(RunbookAssistant.vector("ID").containsKey("id"), "locale-independent normalization");
        } finally {
            Locale.setDefault(previous);
        }
        var tied = List.of(new RunbookAssistant.Passage("b", "same", "words"),
                new RunbookAssistant.Passage("a", "same", "words"));
        check(RunbookAssistant.retrieve(tied, "same", 1, 0).getFirst().passage().id().equals("a"),
                "stable source-ID tie break");
        check(RunbookAssistant.retrieve(tied, "same", 2, 0).size() == 2, "top-k limit");
        check(RunbookAssistant.retrieve(tied, "same", 1, 1).isEmpty(), "threshold filtering");
        rejects(() -> RunbookAssistant.answer(corpus, " ", 0), IllegalArgumentException.class, "blank query");
        rejects(() -> RunbookAssistant.answer(corpus, "x".repeat(501), 0),
                IllegalArgumentException.class, "query bound");
        rejects(() -> RunbookAssistant.answer(corpus, "hello\u001b[31m", 0),
                IllegalArgumentException.class, "terminal control in query");
        rejects(() -> RunbookAssistant.retrieve(corpus, "events", 0, 0),
                IllegalArgumentException.class, "invalid k");
        for (String value : List.of("NaN", "Infinity", "-0.1", "1.1", "bad", "")) {
            rejects(() -> RunbookAssistant.threshold(value), IllegalArgumentException.class,
                    "invalid configuration: " + value);
        }
        check(RunbookAssistant.threshold(null) == 0.15, "default threshold");

        Path temporary = Files.createTempFile("runbook-test-", ".tsv");
        try {
            for (String content : List.of("", "# comment", "a\ttitle", "a\t\ttext",
                    "a\ttitle\ttext\textra", "a\ttitle\ttext\na\tother\ttext",
                    "a\ttitle\tbad\u001btext")) {
                Files.writeString(temporary, content);
                rejects(() -> RunbookAssistant.load(temporary), IllegalArgumentException.class, "invalid corpus");
            }
            Files.write(temporary, new byte[] {(byte) 0xc3, (byte) 0x28});
            rejects(() -> RunbookAssistant.load(temporary), java.io.IOException.class, "invalid UTF-8");
            Files.writeString(temporary, "x".repeat(RunbookAssistant.MAX_FILE_BYTES + 1));
            rejects(() -> RunbookAssistant.load(temporary), IllegalArgumentException.class, "file bound");
            Files.writeString(temporary, "question\tunknown-id");
            rejects(() -> RunbookAssistant.evaluate(corpus, temporary, 0.15, System.out),
                    IllegalArgumentException.class, "unknown evaluation label");
        } finally {
            Files.deleteIfExists(temporary);
        }
        rejects(() -> RunbookAssistant.load(temporary), java.io.IOException.class, "missing file");

        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        try (var out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
                var err = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            var evaluation = RunbookAssistant.evaluate(corpus, Path.of("data/queries.tsv"), 0.15, out);
            check(evaluation.equals(new RunbookAssistant.Evaluation(3, 4, 1, 1)), "fixture baseline metrics");
            check(RunbookAssistant.run(new String[] {}, null, out, err) == 2, "usage exit code");
            check(stderr.toString(StandardCharsets.UTF_8).contains("Usage:"), "useful usage error");
            check(RunbookAssistant.run(new String[] {"data/runbooks.tsv", "duplicate events"},
                    null, out, err) == 0, "successful CLI");
            check(stdout.toString(StandardCharsets.UTF_8).contains("source=duplicate-events"), "CLI citation");
            check(RunbookAssistant.run(new String[] {"data/runbooks.tsv", "banana bread recipe"},
                    null, out, err) == 0, "no evidence is a normal result");
            check(RunbookAssistant.run(new String[] {"data/runbooks.tsv", "events"},
                    "NaN", out, err) == 2, "bad configuration exit code");
        }
        System.out.println("PASS: " + checks + " checks");
    }
}

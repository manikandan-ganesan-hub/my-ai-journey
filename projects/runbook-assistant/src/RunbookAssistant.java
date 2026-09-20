import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Local lexical retrieval baseline. It never invokes a model or executes source text. */
public final class RunbookAssistant {
    static final int MAX_QUERY_LENGTH = 500;
    static final int MAX_FILE_BYTES = 1_048_576;
    static final double DEFAULT_MIN_SCORE = 0.15;
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "do", "for", "how", "i", "in", "is", "it",
            "of", "on", "the", "to", "what", "with");

    record Passage(String id, String title, String text) {}
    record Hit(Passage passage, double score) {}
    enum Status { EXCERPT, NO_EVIDENCE }
    record Answer(Status status, String text, String sourceId, double score) {}
    record Evaluation(int hits, int answerable, int abstentions, int unanswerable) {}

    private RunbookAssistant() {}

    static Map<String, Integer> vector(String text) {
        var result = new HashMap<String, Integer>();
        var matcher = WORD.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String word = matcher.group();
            if (!STOP_WORDS.contains(word)) {
                result.merge(word, 1, Integer::sum);
            }
        }
        return result;
    }

    static double cosine(Map<String, Integer> left, Map<String, Integer> right) {
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (var entry : left.entrySet()) {
            double count = entry.getValue();
            dot += count * right.getOrDefault(entry.getKey(), 0);
            leftNorm += count * count;
        }
        for (int count : right.values()) {
            rightNorm += (double) count * count;
        }
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return Math.min(1, dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm)));
    }

    // Read a bounded snapshot, rather than trusting a size check before an unbounded read.
    static List<String> readLines(Path path) throws IOException {
        byte[] bytes;
        try (var input = Files.newInputStream(path)) {
            bytes = input.readNBytes(MAX_FILE_BYTES + 1);
        }
        if (bytes.length > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File exceeds 1 MiB: " + path);
        }
        String content = StandardCharsets.UTF_8.newDecoder()
                .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        return content.lines().toList();
    }

    static List<Passage> load(Path path) throws IOException {
        var passages = new ArrayList<Passage>();
        var ids = new HashSet<String>();
        int lineNumber = 0;
        for (String line : readLines(path)) {
            lineNumber++;
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 3 || !fields[0].matches("[a-z0-9-]+")
                    || fields[1].isBlank() || fields[2].isBlank()
                    || line.codePoints().anyMatch(c -> Character.isISOControl(c) && c != '\t')) {
                throw new IllegalArgumentException("Invalid corpus row at line " + lineNumber);
            }
            if (!ids.add(fields[0])) {
                throw new IllegalArgumentException("Duplicate source ID: " + fields[0]);
            }
            passages.add(new Passage(fields[0], fields[1], fields[2]));
        }
        if (passages.isEmpty()) {
            throw new IllegalArgumentException("Corpus contains no passages");
        }
        return List.copyOf(passages);
    }

    static List<Hit> retrieve(List<Passage> corpus, String question, int k, double minScore) {
        if (question == null || question.isBlank() || question.length() > MAX_QUERY_LENGTH
                || question.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Question must be 1–500 characters without control characters");
        }
        if (k < 1 || k > 10) {
            throw new IllegalArgumentException("k must be between 1 and 10");
        }
        validateThreshold(minScore);
        Map<String, Integer> query = vector(question);
        return corpus.stream()
                .map(p -> new Hit(p, cosine(query, vector(p.title() + " " + p.text()))))
                .filter(hit -> hit.score() > 0 && hit.score() >= minScore)
                .sorted(Comparator.comparingDouble(Hit::score).reversed()
                        .thenComparing(hit -> hit.passage().id()))
                .limit(k)
                .toList();
    }

    static Answer answer(List<Passage> corpus, String question, double minScore) {
        List<Hit> hits = retrieve(corpus, question, 1, minScore);
        if (hits.isEmpty()) {
            return new Answer(Status.NO_EVIDENCE, "No matching passage. Try different wording.", "", 0);
        }
        Hit best = hits.getFirst();
        return new Answer(Status.EXCERPT, best.passage().text(), best.passage().id(), best.score());
    }

    static void validateThreshold(double score) {
        if (!Double.isFinite(score) || score < 0 || score > 1) {
            throw new IllegalArgumentException("MIN_SCORE must be a finite number between 0 and 1");
        }
    }

    static double threshold(String value) {
        try {
            double score = value == null ? DEFAULT_MIN_SCORE : Double.parseDouble(value);
            validateThreshold(score);
            return score;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("MIN_SCORE must be a finite number between 0 and 1");
        }
    }

    static Evaluation evaluate(List<Passage> corpus, Path fixture, double minScore, PrintStream out)
            throws IOException {
        Set<String> ids = new HashSet<>();
        corpus.forEach(p -> ids.add(p.id()));
        int hits = 0;
        int answerable = 0;
        int abstentions = 0;
        int unanswerable = 0;
        for (String line : readLines(fixture)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 2 || !(fields[1].equals("NONE") || ids.contains(fields[1]))) {
                throw new IllegalArgumentException("Invalid evaluation row or unknown source ID");
            }
            Answer answer = answer(corpus, fields[0], minScore);
            String actual = answer.sourceId().isEmpty() ? "NONE" : answer.sourceId();
            boolean match = actual.equals(fields[1]);
            if (fields[1].equals("NONE")) {
                unanswerable++;
                if (match) abstentions++;
            } else {
                answerable++;
                if (match) hits++;
            }
            out.printf("%s expected=%s actual=%s query=%s%n",
                    match ? "MATCH" : "MISS", fields[1], actual, fields[0]);
        }
        if (answerable + unanswerable == 0) {
            throw new IllegalArgumentException("Evaluation fixture contains no cases");
        }
        out.printf("Retrieval hits@1: %d/%d; unanswerable abstentions: %d/%d%n",
                hits, answerable, abstentions, unanswerable);
        return new Evaluation(hits, answerable, abstentions, unanswerable);
    }

    static int run(String[] args, String minScore, PrintStream out, PrintStream err) {
        try {
            double threshold = threshold(minScore);
            if (args.length == 3 && args[0].equals("--evaluate")) {
                evaluate(load(Path.of(args[1])), Path.of(args[2]), threshold, out);
            } else if (args.length == 2 && !args[0].startsWith("--")) {
                Answer answer = answer(load(Path.of(args[0])), args[1], threshold);
                out.println("status=" + answer.status());
                if (answer.status() == Status.EXCERPT) {
                    out.printf(Locale.ROOT, "source=%s similarity=%.3f%n", answer.sourceId(), answer.score());
                }
                out.println(answer.text());
            } else {
                throw new IllegalArgumentException(
                        "Usage: RunbookAssistant <corpus.tsv> \"question\" OR --evaluate <corpus.tsv> <queries.tsv>");
            }
            return 0;
        } catch (IOException | IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    public static void main(String[] args) {
        System.exit(run(args, System.getenv("MIN_SCORE"), System.out, System.err));
    }
}

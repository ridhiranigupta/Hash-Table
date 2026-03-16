package src;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PlagiarismDetector {

    private final Map<String, Set<String>> ngramIndex = new HashMap<>();
    private final int N = 5; // 5-grams

    // Add document to the system
    public void addDocument(String documentId, String content) {
        List<String> ngrams = extractNGrams(content);
        for (String ngram : ngrams) {
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(documentId);
        }
    }

    // Extract n-grams from text
    private List<String> extractNGrams(String text) {
        String[] words = text.split("\\s+"); // simple word tokenizer
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    // Analyze a document for plagiarism
    public void analyzeDocument(String documentId, String content) {
        List<String> ngrams = extractNGrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        // Count matching n-grams
        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.get(ngram);
            if (docs != null) {
                for (String docId : docs) {
                    if (!docId.equals(documentId)) {
                        matchCount.put(docId, matchCount.getOrDefault(docId, 0) + 1);
                    }
                }
            }
        }

        System.out.println("Analyzing document: " + documentId);
        System.out.println("Extracted " + ngrams.size() + " n-grams");

        // Compute similarity
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String docId = entry.getKey();
            int matches = entry.getValue();
            double similarity = matches * 100.0 / ngrams.size();
            String status = similarity >= 50.0 ? "PLAGIARISM DETECTED" : similarity >= 10.0 ? "suspicious" : "low";
            System.out.printf("→ Found %d matching n-grams with \"%s\"\n", matches, docId);
            System.out.printf("→ Similarity: %.1f%% (%s)\n", similarity, status);
        }

        // Add this document to index
        addDocument(documentId, content);
    }

    // Read file content
    public String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Main method for demo
    public static void main(String[] args) throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Sample documents
        String doc1 = "The quick brown fox jumps over the lazy dog";
        String doc2 = "The quick brown fox jumps over the very lazy dog";
        String doc3 = "Lorem ipsum dolor sit amet consectetur adipiscing elit";

        detector.addDocument("essay_001.txt", doc1);
        detector.addDocument("essay_002.txt", doc2);
        detector.addDocument("essay_003.txt", doc3);

        // Analyze a new document
        String newDoc = "The quick brown fox jumps over the lazy dog and runs away";
        detector.analyzeDocument("essay_123.txt", newDoc);
    }
}

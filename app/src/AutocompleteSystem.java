package src;

import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
        String word = null; // full word at leaf
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> frequencyMap = new HashMap<>();

    // Insert query into Trie
    public void insert(String query, int frequency) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isWord = true;
        node.word = query;

        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + frequency);
    }

    // Update frequency for an existing or new query
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insert(query, 0); // ensure it exists in Trie
    }

    // Get top K suggestions for prefix
    public List<String> getSuggestions(String prefix, int topK) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        PriorityQueue<String> heap = new PriorityQueue<>((a, b) -> {
            int freqCompare = Integer.compare(frequencyMap.get(a), frequencyMap.get(b));
            if (freqCompare == 0) return b.compareTo(a); // tie-break lexicographically
            return freqCompare;
        });

        dfs(node, heap, topK);

        List<String> result = new ArrayList<>();
        while (!heap.isEmpty()) result.add(heap.poll());
        Collections.reverse(result); // highest frequency first
        return result;
    }

    // DFS traversal to collect words under node
    private void dfs(TrieNode node, PriorityQueue<String> heap, int topK) {
        if (node.isWord) {
            heap.offer(node.word);
            if (heap.size() > topK) heap.poll();
        }
        for (TrieNode child : node.children.values()) {
            dfs(child, heap, topK);
        }
    }

    // Demo main method
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        // Insert sample queries with frequencies
        system.insert("java tutorial", 1_234_567);
        system.insert("javascript", 987_654);
        system.insert("java download", 456_789);
        system.insert("java 21 features", 123_456);
        system.insert("javelin sport", 50_000);

        // Get suggestions for prefix "jav"
        List<String> suggestions = system.getSuggestions("jav", 10);
        System.out.println("Suggestions for prefix 'jav':");
        for (String s : suggestions) {
            System.out.printf("%s (%d searches)\n", s, system.frequencyMap.get(s));
        }

        // Update frequency dynamically
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        System.out.println("\nUpdated frequency for 'java 21 features': " +
                system.frequencyMap.get("java 21 features"));

        // Get suggestions again
        suggestions = system.getSuggestions("jav", 10);
        System.out.println("\nSuggestions after update:");
        for (String s : suggestions) {
            System.out.printf("%s (%d searches)\n", s, system.frequencyMap.get(s));
        }
    }
}

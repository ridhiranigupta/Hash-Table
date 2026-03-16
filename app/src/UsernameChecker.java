import java.util.*;

public class UsernameChecker {

    // Store registered usernames: username -> userId
    private final Map<String, Integer> registeredUsers = new HashMap<>();

    // Track username attempt frequency: username -> count
    private final Map<String, Integer> attemptFrequency = new HashMap<>();

    private int userIdCounter = 1; // simple incremental userId generator

    // Check availability
    public boolean checkAvailability(String username) {
        incrementAttempt(username);
        return !registeredUsers.containsKey(username);
    }

    // Register a username
    public boolean registerUsername(String username) {
        if (checkAvailability(username)) {
            registeredUsers.put(username, userIdCounter++);
            return true;
        }
        return false;
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        // Try appending numbers
        while (suggestions.size() < 3) {
            String alt = username + suffix;
            if (!registeredUsers.containsKey(alt)) suggestions.add(alt);
            suffix++;
        }

        // Replace underscore with dot if available
        if (username.contains("_")) {
            String alt = username.replace("_", ".");
            if (!registeredUsers.containsKey(alt)) suggestions.add(alt);
        }

        return suggestions;
    }

    // Increment attempt count
    private void incrementAttempt(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
    }

    // Get the most attempted username
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Main method for testing
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Pre-register some usernames
        checker.registerUsername("john_doe");
        checker.registerUsername("admin");

        // Sample checks
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe")); // false
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith")); // true

        // Suggestions
        System.out.println("Alternatives for john_doe: " + checker.suggestAlternatives("john_doe"));

        // Simulate multiple attempts
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("john_doe");

        System.out.println("Most attempted username: " + checker.getMostAttempted()); // admin
    }
}
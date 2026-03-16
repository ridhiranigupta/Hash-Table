package src;

import java.util.*;
import java.time.*;

public class TransactionAnalyzer {

    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime timestamp;

        public Transaction(int id, double amount, String merchant, String account, LocalDateTime timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("id:%d, amount:%.2f, merchant:%s, account:%s, time:%s",
                    id, amount, merchant, account, timestamp);
        }
    }

    private final List<Transaction> transactions = new ArrayList<>();

    // Add transaction
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum: return pairs summing to target
    public List<List<Transaction>> findTwoSum(double target) {
        List<List<Transaction>> result = new ArrayList<>();
        Map<Double, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement), t));
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with time window (e.g., 1 hour)
    public List<List<Transaction>> findTwoSumWithWindow(double target, Duration window) {
        List<List<Transaction>> result = new ArrayList<>();
        transactions.sort(Comparator.comparing(t -> t.timestamp));
        Map<Double, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction candidate : map.get(complement)) {
                    if (!candidate.account.equals(t.account) &&
                            Duration.between(candidate.timestamp, t.timestamp).abs().compareTo(window) <= 0) {
                        result.add(Arrays.asList(candidate, t));
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum: recursively find K transactions summing to target
    public List<List<Transaction>> findKSum(int k, double target) {
        List<List<Transaction>> result = new ArrayList<>();
        transactions.sort(Comparator.comparingDouble(t -> t.amount));
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k, double target, List<Transaction> path, List<List<Transaction>> result) {
        if (k == 0 && Math.abs(target) < 1e-6) {
            result.add(new ArrayList<>(path));
            return;
        }
        if (k == 0 || start >= transactions.size()) return;

        for (int i = start; i < transactions.size(); i++) {
            path.add(transactions.get(i));
            kSumHelper(i + 1, k - 1, target - transactions.get(i).amount, path, result);
            path.remove(path.size() - 1);
        }
    }

    // Detect duplicates: same amount, same merchant, different accounts
    public List<Map<String, Object>> detectDuplicates() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "-" + t.merchant;
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : map.entrySet()) {
            List<Transaction> list = entry.getValue();
            Set<String> accounts = new HashSet<>();
            for (Transaction t : list) accounts.add(t.account);
            if (accounts.size() > 1) {
                Map<String, Object> duplicateInfo = new HashMap<>();
                String[] parts = entry.getKey().split("-");
                duplicateInfo.put("amount", Double.parseDouble(parts[0]));
                duplicateInfo.put("merchant", parts[1]);
                duplicateInfo.put("accounts", accounts);
                result.add(duplicateInfo);
            }
        }
        return result;
    }

    // Demo main method
    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        analyzer.addTransaction(new Transaction(1, 500, "Store A", "acc1", LocalDateTime.of(2026,3,16,10,0)));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "acc2", LocalDateTime.of(2026,3,16,10,15)));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "acc3", LocalDateTime.of(2026,3,16,10,30)));
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "acc2", LocalDateTime.of(2026,3,16,10,45)));

        System.out.println("Classic Two-Sum target=500:");
        List<List<Transaction>> twoSumPairs = analyzer.findTwoSum(500);
        for (List<Transaction> pair : twoSumPairs) System.out.println(pair);

        System.out.println("\nTwo-Sum within 1 hour, target=500:");
        List<List<Transaction>> twoSumWindow = analyzer.findTwoSumWithWindow(500, Duration.ofHours(1));
        for (List<Transaction> pair : twoSumWindow) System.out.println(pair);

        System.out.println("\nK-Sum k=3, target=1000:");
        List<List<Transaction>> kSumTriplets = analyzer.findKSum(3, 1000);
        for (List<Transaction> triplet : kSumTriplets) System.out.println(triplet);

        System.out.println("\nDuplicate detection:");
        List<Map<String,Object>> duplicates = analyzer.detectDuplicates();
        for (Map<String,Object> d : duplicates) System.out.println(d);
    }
}

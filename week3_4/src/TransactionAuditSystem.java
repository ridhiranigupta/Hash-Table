
import java.util.*;

class Transaction {
    String id;
    double fee;
    String timestamp; // HH:MM format

    public Transaction(String id, double fee, String timestamp) {
        this.id = id;
        this.fee = fee;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return id + ":" + fee + "@" + timestamp;
    }
}

public class TransactionAuditSystem {

    // 🔵 Bubble Sort (by fee)
    public static void bubbleSortByFee(List<Transaction> list) {
        int n = list.size();
        int passes = 0, swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            passes++;

            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).fee > list.get(j + 1).fee) {
                    Transaction temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swaps++;
                    swapped = true;
                }
            }

            if (!swapped) break; // early termination
        }

        System.out.println("Bubble Sort -> Passes: " + passes + ", Swaps: " + swaps);
    }

    // 🟢 Insertion Sort (by fee + timestamp)
    public static void insertionSortByFeeAndTime(List<Transaction> list) {
        for (int i = 1; i < list.size(); i++) {
            Transaction key = list.get(i);
            int j = i - 1;

            while (j >= 0 && compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    // Comparator logic
    private static int compare(Transaction a, Transaction b) {
        if (a.fee != b.fee) {
            return Double.compare(a.fee, b.fee);
        }
        return a.timestamp.compareTo(b.timestamp);
    }

    // 🚨 Outlier detection (> $50)
    public static List<Transaction> findHighFeeOutliers(List<Transaction> list) {
        List<Transaction> outliers = new ArrayList<>();

        for (Transaction t : list) {
            if (t.fee > 50.0) {
                outliers.add(t);
            }
        }
        return outliers;
    }

    // 🎯 Main processing logic
    public static void processTransactions(List<Transaction> transactions) {
        int size = transactions.size();

        if (size <= 100) {
            bubbleSortByFee(transactions);
        } else if (size <= 1000) {
            insertionSortByFeeAndTime(transactions);
        } else {
            System.out.println("Large dataset detected. Consider advanced sorting.");
        }

        // Print sorted list
        System.out.println("\nSorted Transactions:");
        for (Transaction t : transactions) {
            System.out.println(t);
        }

        // Outliers
        List<Transaction> outliers = findHighFeeOutliers(transactions);

        System.out.println("\nHigh-fee outliers:");
        if (outliers.isEmpty()) {
            System.out.println("None");
        } else {
            for (Transaction t : outliers) {
                System.out.println(t);
            }
        }
    }

    // 🚀 Main method (Sample Run)
    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction("id1", 10.5, "10:00"));
        transactions.add(new Transaction("id2", 25.0, "09:30"));
        transactions.add(new Transaction("id3", 5.0, "10:15"));

        processTransactions(transactions);
    }
}

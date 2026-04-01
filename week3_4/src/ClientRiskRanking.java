import java.util.*;

class Client {
    String name;
    int riskScore;
    double accountBalance;

    public Client(String name, int riskScore, double accountBalance) {
        this.name = name;
        this.riskScore = riskScore;
        this.accountBalance = accountBalance;
    }

    @Override
    public String toString() {
        return name + ":" + riskScore + " (Bal:" + accountBalance + ")";
    }
}

public class ClientRiskRanking {

    // 🔵 Bubble Sort (Ascending by riskScore)
    public static void bubbleSortAscending(Client[] arr) {
        int n = arr.length;
        int swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].riskScore > arr[j + 1].riskScore) {

                    // Swap
                    Client temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    swaps++;
                    swapped = true;

                    // 🔍 Visualization of swap
                    System.out.println("Swapped: " + arr[j].name + " <-> " + arr[j + 1].name);
                }
            }

            if (!swapped) break; // Early termination
        }

        System.out.println("Total swaps: " + swaps);
    }

    // 🟢 Insertion Sort (Descending by riskScore + accountBalance)
    public static void insertionSortDescending(Client[] arr) {
        for (int i = 1; i < arr.length; i++) {
            Client key = arr[i];
            int j = i - 1;

            while (j >= 0 && compare(arr[j], key) < 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // Comparator:
    // 1. Higher riskScore first
    // 2. If equal → higher accountBalance first
    private static int compare(Client a, Client b) {
        if (a.riskScore != b.riskScore) {
            return Integer.compare(a.riskScore, b.riskScore);
        }
        return Double.compare(a.accountBalance, b.accountBalance);
    }

    // 🔝 Top 10 highest risk clients
    public static void printTopRisks(Client[] arr, int topN) {
        System.out.println("\nTop " + topN + " High-Risk Clients:");

        for (int i = 0; i < Math.min(topN, arr.length); i++) {
            System.out.println(arr[i].name + " (" + arr[i].riskScore + ")");
        }
    }

    // Utility to print array
    public static void printArray(Client[] arr, String message) {
        System.out.println("\n" + message);
        for (Client c : arr) {
            System.out.println(c);
        }
    }

    // 🚀 Main
    public static void main(String[] args) {

        Client[] clients = {
                new Client("clientC", 80, 5000),
                new Client("clientA", 20, 2000),
                new Client("clientB", 50, 3000)
        };

        // 🔵 Bubble Sort Demo
        bubbleSortAscending(clients);
        printArray(clients, "After Bubble Sort (Ascending):");

        // 🟢 Insertion Sort Demo
        insertionSortDescending(clients);
        printArray(clients, "After Insertion Sort (Descending):");

        // 🔝 Top risks
        printTopRisks(clients, 10);
    }
}

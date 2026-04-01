import java.util.*;

class Asset {
    String name;
    double returnRate;   // %
    double volatility;   // risk measure

    public Asset(String name, double returnRate, double volatility) {
        this.name = name;
        this.returnRate = returnRate;
        this.volatility = volatility;
    }

    @Override
    public String toString() {
        return name + ":" + returnRate + "% (Vol:" + volatility + ")";
    }
}

public class PortfolioSorting {

    // 🔵 MERGE SORT (Stable, Ascending by returnRate)
    public static void mergeSort(Asset[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;

            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);

            merge(arr, left, mid, right);
        }
    }

    private static void merge(Asset[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Asset[] L = new Asset[n1];
        Asset[] R = new Asset[n2];

        for (int i = 0; i < n1; i++)
            L[i] = arr[left + i];
        for (int j = 0; j < n2; j++)
            R[j] = arr[mid + 1 + j];

        int i = 0, j = 0, k = left;

        // Stable merge (<= keeps original order)
        while (i < n1 && j < n2) {
            if (L[i].returnRate <= R[j].returnRate) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // 🟢 QUICK SORT (Hybrid + DESC returnRate + ASC volatility)
    private static final int INSERTION_THRESHOLD = 10;

    public static void quickSort(Asset[] arr, int low, int high) {
        if (high - low <= INSERTION_THRESHOLD) {
            insertionSort(arr, low, high);
            return;
        }

        if (low < high) {
            int pivotIndex = medianOfThree(arr, low, high);
            swap(arr, pivotIndex, high);

            int pi = partition(arr, low, high);

            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    // Comparator:
    // 1. returnRate DESC
    // 2. volatility ASC
    private static int compare(Asset a, Asset b) {
        if (a.returnRate != b.returnRate) {
            return Double.compare(b.returnRate, a.returnRate); // DESC
        }
        return Double.compare(a.volatility, b.volatility); // ASC
    }

    private static int partition(Asset[] arr, int low, int high) {
        Asset pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (compare(arr[j], pivot) < 0) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);
        return i + 1;
    }

    // 🔀 Median-of-3 pivot selection
    private static int medianOfThree(Asset[] arr, int low, int high) {
        int mid = (low + high) / 2;

        if (arr[low].returnRate > arr[mid].returnRate)
            swap(arr, low, mid);
        if (arr[low].returnRate > arr[high].returnRate)
            swap(arr, low, high);
        if (arr[mid].returnRate > arr[high].returnRate)
            swap(arr, mid, high);

        return mid;
    }

    // (Optional) Random pivot
    private static int randomPivot(int low, int high) {
        return low + new Random().nextInt(high - low + 1);
    }

    // 🔧 Insertion Sort (used for small partitions)
    private static void insertionSort(Asset[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            Asset key = arr[i];
            int j = i - 1;

            while (j >= low && compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // Utility swap
    private static void swap(Asset[] arr, int i, int j) {
        Asset temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // Print helper
    public static void printArray(Asset[] arr, String msg) {
        System.out.println("\n" + msg);
        for (Asset a : arr) {
            System.out.println(a);
        }
    }

    // 🚀 Main
    public static void main(String[] args) {

        Asset[] assets = {
                new Asset("AAPL", 12.0, 0.30),
                new Asset("TSLA", 8.0, 0.60),
                new Asset("GOOG", 15.0, 0.25)
        };

        // 🔵 Merge Sort (Ascending, Stable)
        mergeSort(assets, 0, assets.length - 1);
        printArray(assets, "Merge Sort (Ascending by Return):");

        // 🟢 Quick Sort (Descending + volatility ASC)
        quickSort(assets, 0, assets.length - 1);
        printArray(assets, "Quick Sort (DESC Return + ASC Volatility):");
    }
}

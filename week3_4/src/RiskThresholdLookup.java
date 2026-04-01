import java.util.*;

public class RiskThresholdLookup {

    // 🔵 LINEAR SEARCH (unsorted)
    public static int linearSearch(int[] arr, int target) {
        int comparisons = 0;

        for (int i = 0; i < arr.length; i++) {
            comparisons++;
            if (arr[i] == target) {
                System.out.println("Linear -> Comparisons: " + comparisons);
                return i;
            }
        }

        System.out.println("Linear -> Comparisons: " + comparisons);
        return -1;
    }

    // 🟢 BINARY SEARCH (find insertion point / lower_bound)
    public static int lowerBound(int[] arr, int target) {
        int low = 0, high = arr.length;
        int comparisons = 0;

        while (low < high) {
            int mid = (low + high) / 2;
            comparisons++;

            if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        System.out.println("Binary (lower_bound) -> Comparisons: " + comparisons);
        return low; // insertion point
    }

    // 🔍 Floor (largest ≤ target)
    public static Integer floor(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        Integer result = null;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (arr[mid] == target) {
                return arr[mid];
            } else if (arr[mid] < target) {
                result = arr[mid]; // possible floor
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    // 🔍 Ceiling (smallest ≥ target)
    public static Integer ceiling(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        Integer result = null;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (arr[mid] == target) {
                return arr[mid];
            } else if (arr[mid] > target) {
                result = arr[mid]; // possible ceiling
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    // 🚀 Main
    public static void main(String[] args) {

        int[] unsorted = {50, 10, 100, 25};
        int target = 30;

        // 🔵 Linear Search (unsorted)
        int index = linearSearch(unsorted, target);
        System.out.println("Linear result for " + target + ": " +
                (index == -1 ? "Not Found" : "Found at index " + index));

        // 🟢 Sort for Binary Search
        int[] sorted = {10, 25, 50, 100};
        System.out.println("\nSorted Risks: " + Arrays.toString(sorted));

        // 🟢 Binary insertion point
        int insertPos = lowerBound(sorted, target);
        System.out.println("Insertion point for " + target + ": index " + insertPos);

        // 🔍 Floor & Ceiling
        Integer floorVal = floor(sorted, target);
        Integer ceilVal = ceiling(sorted, target);

        System.out.println("Floor(" + target + "): " + floorVal);
        System.out.println("Ceiling(" + target + "): " + ceilVal);
    }
}
package src;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryManager {

    // Stock for each product: productId -> Atomic stock count
    private final ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // Waiting list for each product (FIFO): productId -> Queue of userIds
    private final ConcurrentHashMap<String, Queue<Integer>> waitingList = new ConcurrentHashMap<>();

    // Initialize stock
    public void addProduct(String productId, int stockCount) {
        stockMap.put(productId, new AtomicInteger(stockCount));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock availability
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return stock != null ? stock.get() : 0;
    }

    // Purchase item
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        // Atomic decrement operation
        while (true) {
            int currentStock = stock.get();
            if (currentStock > 0) {
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                // Add to waiting list
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }
        }
    }

    // Get waiting list for a product
    public List<Integer> getWaitingList(String productId) {
        Queue<Integer> queue = waitingList.get(productId);
        if (queue == null) return Collections.emptyList();
        return new ArrayList<>(queue);
    }

    // Main method for testing
    public static void main(String[] args) {

        InventoryManager manager = new InventoryManager();

        // Add product with 5 units for testing
        manager.addProduct("IPHONE15_256GB", 5);

        System.out.println("Stock check: " + manager.checkStock("IPHONE15_256GB") + " units available");

        // Simulate multiple users purchasing concurrently
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 7; i++) {
            final int userId = 1000 + i;
            executor.submit(() -> {
                String result = manager.purchaseItem("IPHONE15_256GB", userId);
                System.out.println("User " + userId + ": " + result);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting list: " + manager.getWaitingList("IPHONE15_256GB"));
        System.out.println("Remaining stock: " + manager.checkStock("IPHONE15_256GB") + " units");
    }
}

package src;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RealTimeAnalyticsDashboard {

    // Page view counts: pageUrl -> count
    private final ConcurrentHashMap<String, AtomicInteger> pageViews = new ConcurrentHashMap<>();

    // Unique visitors: pageUrl -> set of userIds
    private final ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source counts: source -> count
    private final ConcurrentHashMap<String, AtomicInteger> trafficSources = new ConcurrentHashMap<>();

    // Scheduled executor to update dashboard
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RealTimeAnalyticsDashboard() {
        // Update dashboard every 5 seconds
        scheduler.scheduleAtFixedRate(this::displayDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // Page view event
    public void processEvent(String url, String userId, String source) {
        pageViews.computeIfAbsent(url, k -> new AtomicInteger(0)).incrementAndGet();

        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);

        trafficSources.computeIfAbsent(source, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Display top pages and traffic sources
    private void displayDashboard() {
        System.out.println("\n===== Real-Time Analytics Dashboard =====");

        // Top 10 pages
        PriorityQueue<Map.Entry<String, AtomicInteger>> topPagesQueue =
                new PriorityQueue<>((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()));

        topPagesQueue.addAll(pageViews.entrySet());

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, AtomicInteger> entry : topPagesQueue) {
            if (rank > 10) break;
            String url = entry.getKey();
            int views = entry.getValue().get();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", rank, url, views, unique);
            rank++;
        }

        // Traffic sources
        int totalVisits = trafficSources.values().stream().mapToInt(AtomicInteger::get).sum();
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, AtomicInteger> entry : trafficSources.entrySet()) {
            String source = entry.getKey();
            int count = entry.getValue().get();
            double percent = totalVisits == 0 ? 0 : (count * 100.0 / totalVisits);
            System.out.printf("%s: %.1f%%\n", source, percent);
        }

        System.out.println("========================================\n");
    }

    // Stop scheduler
    public void shutdown() {
        scheduler.shutdown();
    }

    // Demo main method
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();

        // Simulate streaming events
        Random rand = new Random();
        String[] pages = {"/article/breaking-news", "/sports/championship", "/tech/ai-update",
                "/politics/election", "/lifestyle/health-tips"};
        String[] sources = {"Google", "Facebook", "Direct", "Twitter", "Other"};

        for (int i = 0; i < 100; i++) {
            String page = pages[rand.nextInt(pages.length)];
            String source = sources[rand.nextInt(sources.length)];
            String userId = "user_" + rand.nextInt(50); // 50 unique users
            dashboard.processEvent(page, userId, source);
            Thread.sleep(50); // simulate incoming events
        }

        // Let dashboard update a few times
        Thread.sleep(15000);

        dashboard.shutdown();
    }
}
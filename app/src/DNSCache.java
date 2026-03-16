package src;

import java.util.*;
import java.util.concurrent.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSCache {

    // DNS entry class
    private static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime; // in milliseconds

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int maxCacheSize;
    private final long defaultTTL;
    private final Map<String, DNSEntry> cache;
    private final Deque<String> lruQueue;

    private long hitCount = 0;
    private long missCount = 0;
    private long totalLookupTime = 0;

    // Cleaner thread
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public DNSCache(int maxCacheSize, long defaultTTLSeconds) {
        this.maxCacheSize = maxCacheSize;
        this.defaultTTL = defaultTTLSeconds;
        this.cache = new ConcurrentHashMap<>();
        this.lruQueue = new LinkedList<>();

        // Periodically clean expired entries every second
        cleaner.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.SECONDS);
    }

    // Resolve domain name
    public String resolve(String domain) {
        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            // Cache hit
            hitCount++;
            touchLRU(domain);
            recordLookupTime(start);
            System.out.println("Cache HIT: " + domain + " -> " + entry.ipAddress);
            return entry.ipAddress;
        }

        // Cache miss or expired
        missCount++;
        System.out.println("Cache MISS: " + domain + " → Query upstream DNS...");

        // Query upstream DNS
        String ip = queryUpstream(domain);

        // Insert/update cache
        put(domain, ip, defaultTTL);

        recordLookupTime(start);
        return ip;
    }

    // Insert/update cache
    private synchronized void put(String domain, String ip, long ttl) {
        if (cache.size() >= maxCacheSize) {
            // Evict LRU
            String oldest = lruQueue.pollFirst();
            if (oldest != null) {
                cache.remove(oldest);
            }
        }

        DNSEntry entry = new DNSEntry(domain, ip, ttl);
        cache.put(domain, entry);
        touchLRU(domain);
    }

    // Update LRU order
    private synchronized void touchLRU(String domain) {
        lruQueue.remove(domain);
        lruQueue.addLast(domain);
    }

    // Clean expired entries
    private synchronized void cleanExpired() {
        Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DNSEntry> mapEntry = iterator.next();
            if (mapEntry.getValue().isExpired()) {
                iterator.remove();
                lruQueue.remove(mapEntry.getKey());
            }
        }
    }

    // Query upstream DNS
    private String queryUpstream(String domain) {
        try {
            InetAddress address = InetAddress.getByName(domain);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return "0.0.0.0";
        }
    }

    // Record lookup time
    private synchronized void recordLookupTime(long start) {
        long elapsedNanos = System.nanoTime() - start;
        totalLookupTime += elapsedNanos;
    }

    // Get cache statistics
    public void getCacheStats() {
        long total = hitCount + missCount;
        double hitRate = total == 0 ? 0 : (hitCount * 100.0 / total);
        double avgLookupMs = total == 0 ? 0 : (totalLookupTime / 1_000_000.0 / total);

        System.out.printf("Cache Stats → Hits: %d, Misses: %d, Hit Rate: %.2f%%, Avg Lookup: %.2f ms%n",
                hitCount, missCount, hitRate, avgLookupMs);
    }

    // Shutdown cleaner thread
    public void shutdown() {
        cleaner.shutdown();
    }

    // Main method for demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5, 5); // max 5 entries, TTL 5 seconds

        System.out.println("Resolving google.com:");
        dnsCache.resolve("google.com"); // MISS
        Thread.sleep(100);
        dnsCache.resolve("google.com"); // HIT

        System.out.println("Resolving yahoo.com:");
        dnsCache.resolve("yahoo.com"); // MISS

        System.out.println("Resolving bing.com:");
        dnsCache.resolve("bing.com"); // MISS

        Thread.sleep(6000); // Wait for TTL expiration

        System.out.println("Resolving google.com after TTL expiry:");
        dnsCache.resolve("google.com"); // MISS (expired)

        dnsCache.getCacheStats();

        dnsCache.shutdown();
    }
}

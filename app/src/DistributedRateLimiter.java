package src;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedRateLimiter {

    // Token bucket for each client
    private static class TokenBucket {
        private final int maxTokens;
        private final int refillRatePerHour; // tokens added per hour
        private int tokens;
        private long lastRefillTimestamp; // in ms

        public TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.refillRatePerHour = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        // synchronized to ensure thread safety per bucket
        public synchronized boolean tryConsume() {
            refillTokens();
            if (tokens > 0) {
                tokens--;
                return true;
            } else {
                return false;
            }
        }

        private void refillTokens() {
            long now = System.currentTimeMillis();
            long elapsedMs = now - lastRefillTimestamp;
            // refill every hour proportionally
            long refillTokens = (elapsedMs * refillRatePerHour) / (60 * 60 * 1000);
            if (refillTokens > 0) {
                tokens = Math.min(maxTokens, tokens + (int) refillTokens);
                lastRefillTimestamp = now;
            }
        }

        public synchronized int getRemainingTokens() {
            refillTokens();
            return tokens;
        }

        public synchronized long getResetTimeMs() {
            refillTokens();
            // time until tokens fully refill
            long remainingTokens = maxTokens - tokens;
            return System.currentTimeMillis() + (remainingTokens * 60L * 60_000L) / refillRatePerHour;
        }
    }

    // Map clientId -> TokenBucket
    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    private final int maxRequestsPerHour;

    public DistributedRateLimiter(int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    // Check rate limit for a client
    public boolean checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId, id -> new TokenBucket(maxRequestsPerHour));
        return bucket.tryConsume();
    }

    // Get client status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) {
            return String.format("{used: 0, limit: %d, reset: %d}", maxRequestsPerHour, System.currentTimeMillis() / 1000 + 3600);
        }
        int used = maxRequestsPerHour - bucket.getRemainingTokens();
        long resetSec = bucket.getResetTimeMs() / 1000;
        return String.format("{used: %d, limit: %d, reset: %d}", used, maxRequestsPerHour, resetSec);
    }

    // Demo main method
    public static void main(String[] args) throws InterruptedException {
        DistributedRateLimiter limiter = new DistributedRateLimiter(5); // 5 requests/hour for demo

        String clientId = "abc123";

        for (int i = 1; i <= 7; i++) {
            boolean allowed = limiter.checkRateLimit(clientId);
            if (allowed) {
                System.out.printf("Request %d: Allowed (%d requests remaining)\n", i, limiter.clientBuckets.get(clientId).getRemainingTokens());
            } else {
                System.out.printf("Request %d: Denied (0 requests remaining, retry after %d s)\n", i,
                        (limiter.clientBuckets.get(clientId).getResetTimeMs() - System.currentTimeMillis()) / 1000);
            }
        }

        System.out.println("Client Status: " + limiter.getRateLimitStatus(clientId));
    }
}

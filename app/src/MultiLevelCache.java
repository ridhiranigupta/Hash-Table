package src;

import java.util.*;

public class MultiLevelCache {

    static class VideoData {
        String videoId;
        String content; // For simplicity, just a string
        public VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    private final int L1_CAPACITY = 10_000;
    private final int L2_CAPACITY = 100_000;

    // L1 Cache: in-memory, LRU
    private final LinkedHashMap<String, VideoData> l1Cache;
    // L2 Cache: SSD simulation, LRU
    private final LinkedHashMap<String, VideoData> l2Cache;
    // L3: database simulation
    private final Map<String, VideoData> l3Database;

    // Access counters
    private final Map<String, Integer> accessCount;

    // Hit statistics
    private int l1Hits = 0, l1Misses = 0;
    private int l2Hits = 0, l2Misses = 0;
    private int l3Hits = 0, l3Misses = 0;

    public MultiLevelCache() {
        this.l1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        this.l2Cache = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L2_CAPACITY;
            }
        };
        this.l3Database = new HashMap<>();
        this.accessCount = new HashMap<>();
    }

    // Add video to database (L3)
    public void addVideoToDB(VideoData video) {
        l3Database.put(video.videoId, video);
    }

    // Fetch video
    public VideoData getVideo(String videoId) {
        // Check L1
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            simulateDelay(0.5);
            return l1Cache.get(videoId);
        } else {
            l1Misses++;
        }

        // Check L2
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            simulateDelay(5);
            VideoData video = l2Cache.get(videoId);
            promoteToL1(video);
            return video;
        } else {
            l2Misses++;
        }

        // Check L3
        if (l3Database.containsKey(videoId)) {
            l3Hits++;
            simulateDelay(150);
            VideoData video = l3Database.get(videoId);
            addToL2(video);
            return video;
        } else {
            l3Misses++;
            return null;
        }
    }

    // Promote video from L2 to L1
    private void promoteToL1(VideoData video) {
        l1Cache.put(video.videoId, video);
        accessCount.put(video.videoId, accessCount.getOrDefault(video.videoId, 0) + 1);
    }

    // Add video to L2, count accesses
    private void addToL2(VideoData video) {
        l2Cache.put(video.videoId, video);
        accessCount.put(video.videoId, 1);
    }

    // Simulate delay in ms
    private void simulateDelay(double ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Cache statistics
    public void printStatistics() {
        int l1Total = l1Hits + l1Misses;
        int l2Total = l2Hits + l2Misses;
        int l3Total = l3Hits + l3Misses;
        int overallHits = l1Hits + l2Hits + l3Hits;
        int overallTotal = l1Total + l2Total + l3Total;

        System.out.printf("L1 Cache: Hit Rate %.1f%%, Avg Time: 0.5ms%n", l1Total == 0 ? 0 : l1Hits * 100.0 / l1Total);
        System.out.printf("L2 Cache: Hit Rate %.1f%%, Avg Time: 5ms%n", l2Total == 0 ? 0 : l2Hits * 100.0 / l2Total);
        System.out.printf("L3 DB: Hit Rate %.1f%%, Avg Time: 150ms%n", l3Total == 0 ? 0 : l3Hits * 100.0 / l3Total);
        System.out.printf("Overall: Hit Rate %.1f%%, Avg Time: %.2fms%n", overallTotal == 0 ? 0 : overallHits * 100.0 / overallTotal,
                (l1Hits*0.5 + l2Hits*5 + l3Hits*150)/(overallHits == 0 ? 1 : overallHits));
    }

    // Demo main
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        // Populate L3 DB with some videos
        for (int i = 1; i <= 5; i++) {
            cache.addVideoToDB(new VideoData("video_" + i, "Content of video " + i));
        }

        // Access videos
        System.out.println("Fetching video_1");
        cache.getVideo("video_1"); // L1 miss, L2 miss, L3 hit → add to L2

        System.out.println("Fetching video_1 again");
        cache.getVideo("video_1"); // L1 miss, L2 hit → promote to L1

        System.out.println("Fetching video_2");
        cache.getVideo("video_2"); // L1 miss, L2 miss, L3 hit → add to L2

        cache.printStatistics();
    }
}

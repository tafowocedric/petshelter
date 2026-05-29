package com.petshelter.util;

/**
 * Demonstrates Java's automatic garbage collection.
 * [GARBAGE COLLECTION] [ENCAPSULATION]
 */
public final class GarbageCollectionDemo {
    private GarbageCollectionDemo() {
        throw new AssertionError("Utility class — do not instantiate.");
    }

    public static void run() {
        Runtime rt = Runtime.getRuntime();

        long beforeAlloc = usedMb(rt);
        System.out.println("[GC] Used heap (before allocation): " + beforeAlloc + " MB");

        int[] bigData = new int[12_500_000];     // 12.5M * 4 bytes ≈ 50 MB
        for (int i = 0; i < bigData.length; i++) {
            bigData[i] = i;
        }

        long afterAlloc = usedMb(rt);
        System.out.println("[GC] Used heap (after allocation): " + afterAlloc + " MB");

        bigData = null;
        System.gc();

        // Give the collector a moment
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long afterGc = usedMb(rt);
        System.out.println("[GC] Used heap (after System.gc): " + afterGc + " MB");

        long reclaimed = afterAlloc - afterGc;
        System.out.println("[GC] Memory reclaimed: " + reclaimed + " MB");
        System.out.println("[GC] (JVM is not required to honor System.gc(); results vary.)");
    }

    private static long usedMb(Runtime rt) {
        long bytes = rt.totalMemory() - rt.freeMemory();
        return bytes / (1024 * 1024);
    }
}

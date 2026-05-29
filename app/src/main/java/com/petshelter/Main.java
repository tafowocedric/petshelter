package com.petshelter;

import com.petshelter.db.Database;
import com.petshelter.util.GarbageCollectionDemo;
import com.petshelter.web.WebServer;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            Database.start();

            System.out.println("\n--- Garbage Collection Demo (startup) ---");
            GarbageCollectionDemo.run();
            System.out.println();

            WebServer server = new WebServer(PORT);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
                Database.shutdown();
            }));

            System.out.println("Press Ctrl+C to stop.");
        } catch (Exception e) {
            System.err.println("Startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

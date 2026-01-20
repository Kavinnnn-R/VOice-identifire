package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeakerMatcher {

    // Store MULTIPLE samples per speaker
    private static final Map<String, List<float[]>> voiceDatabase = new HashMap<>();

    private static boolean registering = false;
    private static String currentUser = null;

    private static String lastDetectedSpeaker = "Unknown";
    private static double lastConfidence = 0.0;

    // Tune this
    private static final double MATCH_THRESHOLD = 80.0;
    private static final int REQUIRED_SAMPLES = 15; // frames to register

    private static int registerCount = 0;

    /* ================= REGISTER ================= */

    public synchronized static void enableRegister(String name) {
        registering = true;
        currentUser = name;
        registerCount = 0;
        voiceDatabase.putIfAbsent(name, new ArrayList<>());
        System.out.println("🔴 Registering voice for: " + name);
    }

    /* ================= MATCH ================= */

    public synchronized static void match(float[] mfcc) {

        if (mfcc == null || mfcc.length == 0) return;

        /* -------- REGISTER MODE -------- */
        if (registering) {
            voiceDatabase.get(currentUser).add(mfcc.clone());
            registerCount++;

            if (registerCount >= REQUIRED_SAMPLES) {
                registering = false;
                lastDetectedSpeaker = currentUser;
                System.out.println("✅ Voice registered for: " + currentUser);
            }
            return;
        }

        if (voiceDatabase.isEmpty()) {
            lastDetectedSpeaker = "No voices";
            lastConfidence = 0;
            return;
        }

        String bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        for (var entry : voiceDatabase.entrySet()) {
            double avgDistance = averageDistance(entry.getValue(), mfcc);

            if (avgDistance < minDistance) {
                minDistance = avgDistance;
                bestMatch = entry.getKey();
            }
        }

        /* -------- DECISION -------- */
        if (bestMatch != null && minDistance < MATCH_THRESHOLD) {
            lastDetectedSpeaker = bestMatch;
            lastConfidence = confidence(minDistance);
        } else {
            lastDetectedSpeaker = "Unknown";
            lastConfidence = 0;
        }
    }

    /* ================= HELPERS ================= */

    private static double averageDistance(List<float[]> samples, float[] input) {
        double sum = 0;
        for (float[] s : samples) {
            sum += distance(s, input);
        }
        return sum / samples.size();
    }

    private static double distance(float[] a, float[] b) {
        double sum = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private static double confidence(double distance) {
        return Math.max(0, 100 - (distance / MATCH_THRESHOLD) * 100);
    }

    /* ================= UI ACCESS ================= */

    public synchronized static String getLastDetectedSpeaker() {
        return lastDetectedSpeaker;
    }

    public synchronized static String getConfidenceText() {
        return String.format("%.1f%%", lastConfidence);
    }
}

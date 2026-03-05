package search;

import java.util.Collection;

/**
 * Port of abstr2.logs.
 */
public final class Logs {

    private Logs() {
    }

    public static void logProcessing(int visited, Collection<String> urlsToProcess, int cached, int processed) {
        if (visited % 10 == 1) {
            int urlsL = urlsToProcess.size();
            System.out.printf("count: todo %d vs visited %d, cache %d processed %d%n", urlsL, visited, cached, processed);
        }
    }
}

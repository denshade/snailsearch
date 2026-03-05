package search;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Port of abstr2.robots.
 */
public final class Robots {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private Robots() {
    }

    public static void doRobotDelay(ContextMap contextmap) {
        BaseRobotRules rp = contextmap.robotRules;
        long delayMs = 1000; // default 1 second
        if (rp != null && rp.getCrawlDelay() > 0) {
            delayMs = rp.getCrawlDelay(); // crawler-commons returns milliseconds
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static ContextMap loadRobotsParser(ContextMap contextmap) {
        String hostname = contextmap.currentHost;
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        BaseRobotRules rp;
        try {
            String robotsUrl = "https://" + hostname + "/robots.txt";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(robotsUrl))
                    .header("User-Agent", "Java")
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            rp = parser.parseContent(robotsUrl, response.body().getBytes(), "text/plain", "Java");
        } catch (Exception e) {
            System.out.println("error load robots.txt");
            rp = null;
        }
        contextmap.robotRules = rp;
        return contextmap;
    }
}

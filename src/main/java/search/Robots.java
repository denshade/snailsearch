package search;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Port of abstr2.robots. Uses crawler-commons only here for parsing; rest of app uses {@link RobotRules}.
 */
public final class Robots {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private Robots() {
    }

    public static void doRobotDelay(ContextMap contextmap) {
        RobotRules rp = contextmap.robotRules;
        long delayMs = 1000; // default 1 second
        if (rp != null && rp.getCrawlDelay() > 0) {
            delayMs = rp.getCrawlDelay();
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
        contextmap.robotRules = parseRobotsTxt(hostname);
        return contextmap;
    }

    private static RobotRules parseRobotsTxt(String hostname) {
        try {
            String robotsUrl = "https://" + hostname + "/robots.txt";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(robotsUrl))
                    .header("User-Agent", UserAgent.getUserAgent())
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return CrawlerCommonsAdapter.parse(robotsUrl, response.body(), "text/plain", UserAgent.getUserAgent());
        } catch (Exception e) {
            System.out.println("error load robots.txt");
            return null;
        }
    }

    /** Adapter so only this package needs crawler-commons. */
    static class CrawlerCommonsAdapter implements RobotRules {
        private final crawlercommons.robots.BaseRobotRules delegate;

        CrawlerCommonsAdapter(crawlercommons.robots.BaseRobotRules delegate) {
            this.delegate = delegate;
        }

        static RobotRules parse(String robotsUrl, String content, String contentType, String userAgent) {
            crawlercommons.robots.SimpleRobotRulesParser parser = new crawlercommons.robots.SimpleRobotRulesParser();
            crawlercommons.robots.BaseRobotRules r = parser.parseContent(
                    robotsUrl, content.getBytes(java.nio.charset.StandardCharsets.UTF_8), contentType, userAgent);
            return new CrawlerCommonsAdapter(r);
        }

        @Override
        public boolean isAllowed(String url) {
            return delegate.isAllowed(url);
        }

        @Override
        public long getCrawlDelay() {
            return delegate.getCrawlDelay();
        }
    }
}

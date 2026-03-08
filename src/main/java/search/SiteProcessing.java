package search;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Port of abstr2.site_processing.
 */
public final class SiteProcessing {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private SiteProcessing() {
    }

    public static String etagHead(String url) {
        String tag = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", UserAgent.getUserAgent())
                    .build();
            HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            tag = response.headers().firstValue("ETag").orElse(null);
        } catch (Exception e) {
            System.out.println("failed to head " + url);
        }
        if (tag == null) {
            return null;
        }
        return tag.replace("\"", "");
    }
}

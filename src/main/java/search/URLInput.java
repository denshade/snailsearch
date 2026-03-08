package search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Port of snail_pipes.URLInput.
 */
public class URLInput {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^a-zA-Z]");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static ProcessResult process(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", UserAgent.getUserAgent())
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String sourceCode = response.body();
            String etag = response.headers().firstValue("ETag").orElse(null);
            if (etag != null) {
                etag = etag.replace("\"", "");
            }

            Document soup = Jsoup.parse(sourceCode);
            String text = soup.text();
            List<String> wordlist = Arrays.stream(WORD_SPLIT.split(text.toLowerCase()))
                    .filter(w -> !w.isEmpty())
                    .collect(Collectors.toList());

            List<String> anchorlist = new ArrayList<>();
            Elements links = soup.select("a[href]");
            for (Element a : links) {
                String href = a.attr("href");
                if (href != null && !href.isEmpty()) {
                    anchorlist.add(href);
                }
            }
            return new ProcessResult(wordlist, anchorlist, etag != null ? etag : "");
        } catch (IOException | InterruptedException e) {
            return new ProcessResult(List.of(), List.of(), "");
        }
    }
}

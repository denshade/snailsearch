package search;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Port of snail_pipes.URLInput.ProcessResult.
 */
public class ProcessResult {

    private final List<String> wordlist;
    private final List<String> anchorlist;
    private final String etag;

    public ProcessResult(List<String> wordlist, List<String> anchorlist, String etag) {
        this.wordlist = wordlist != null ? wordlist : List.of();
        this.anchorlist = anchorlist != null ? anchorlist : List.of();
        this.etag = etag != null ? etag : "";
    }

    public List<String> getAnchorUrls(String baseUrl) {
        List<String> urlsToProcess = new ArrayList<>();
        try {
            URI base = URI.create(baseUrl);
            for (String anchor : anchorlist) {
                URI resolved = base.resolve(anchor);
                urlsToProcess.add(resolved.toString());
            }
        } catch (Exception e) {
            // skip invalid anchors
        }
        return urlsToProcess;
    }

    public Set<String> getWordlistSet() {
        return wordlist.stream().filter(w -> !w.isEmpty()).collect(Collectors.toSet());
    }

    public String getEtag() {
        return etag;
    }
}

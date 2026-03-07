package search;

import java.util.List;

/**
 * Port of abstr2.index. Delegates to IndexStore.
 * Can be instantiated for testing.
 */
public final class Index {

    public Index() {
    }

    public void addToIndex(ContextMap contextmap, ProcessResult processResult) {
        String text = String.join(",", processResult.getWordlistSet());
        String etag = processResult.getEtag();
        contextmap.indexDAO.addPage(contextmap.currentUrl, etag, text);
    }

    public boolean isEtagInIndex(String url, String etag, ContextMap contextmap) {
        return contextmap.indexDAO.isEtagInIndex(url, etag);
    }

    /** All URLs in the current host index (for update_site iteration). */
    public List<String> getUrls(ContextMap contextmap) {
        return contextmap.indexDAO.getUrls();
    }
}

package search;

import search.store.IndexStore;

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
        contextmap.indexStore.addPage(contextmap.currentUrl, etag, text);
    }

    public boolean isEtagInIndex(String url, String etag, ContextMap contextmap) {
        return contextmap.indexStore.isEtagInIndex(url, etag);
    }
}

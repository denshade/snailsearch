package search;

import search.store.IndexStore;

/**
 * Port of abstr2.index. Delegates to IndexStore.
 */
public final class Index {

    private Index() {
    }

    public static void addToIndex(ContextMap contextmap, ProcessResult processResult) {
        String text = String.join(",", processResult.getWordlistSet());
        String etag = processResult.getEtag();
        contextmap.indexStore.addPage(contextmap.currentUrl, etag, text);
    }

    public static boolean isEtagInIndex(String url, String etag, ContextMap contextmap) {
        return contextmap.indexStore.isEtagInIndex(url, etag);
    }
}

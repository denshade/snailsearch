package search;

import java.util.List;

/**
 * Port of snail_pipes.url_filters.URLFilter.
 */
public class URLFilter {

    private final String containText;
    private final List<String> mustNotContain;

    public URLFilter(String containText, List<String> mustNotContain) {
        this.containText = containText;
        this.mustNotContain = mustNotContain != null ? mustNotContain : List.of();
    }

    public boolean matches(String url) {
        for (String item : mustNotContain) {
            if (url.contains(item)) {
                return false;
            }
        }
        return url.contains(containText);
    }
}

package search;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Port of abstr1.process_new_site.
 * SQLite -> URLInput -(Anchors)> AnchorFilter -(Anchors)> Feedback to URLInput
 *          -(Content)> ContentFilter -(URL + context)> Print the context
 */
public final class ProcessNewSite {

    private static final Hosts HOSTS = new Hosts();
    private static final Index INDEX = new Index();
    private static final Urls URLS = new Urls();

    private ProcessNewSite() {
    }

    public static int crawl(String startUrl, ContextMap contextmap) {
        int cached = 0;
        int processed = 0;
        int visited = 0;
        List<String> urlsToProcess = new ArrayList<>();
        urlsToProcess.add(startUrl);
        Set<String> seen = new LinkedHashSet<>();
        seen.add(startUrl);

        while (!urlsToProcess.isEmpty()) {
            String url = urlsToProcess.remove(0);
            contextmap.currentUrl = url;
            visited++;

            if (!URLS.needsBeIndexed(contextmap)) {
                HOSTS.addToUnknownHosts(contextmap);
                continue;
            }

            String tag = SiteProcessing.etagHead(url);
            if (INDEX.isEtagInIndex(url, tag, contextmap)) {
                cached++;
                continue;
            }

            ProcessResult processResult = URLInput.process(url);
            INDEX.addToIndex(contextmap, processResult);

            for (String anchor : processResult.getAnchorUrls(url)) {
                if (!seen.contains(anchor)) {
                    seen.add(anchor);
                    urlsToProcess.add(anchor);
                }
            }
            processed++;
            Robots.doRobotDelay(contextmap);
            Logs.logProcessing(visited, urlsToProcess, cached, processed);
        }
        return visited;
    }

    public static void createIndexForHostname(String hostname, ContextMap contextmap, String starturl) {
        contextmap.currentHost = hostname;
        contextmap = HOSTS.createHostSpecificIndex(contextmap);
        contextmap = HOSTS.loadHosts(contextmap);
        contextmap = Robots.loadRobotsParser(contextmap);
        if (starturl == null) {
            starturl = "https://" + hostname;
        }
        contextmap.urlFilter = new URLFilter("https://" + hostname, List.of());
        System.out.println(crawl(starturl, contextmap));
    }

    public static void main(String[] args) {
        ContextMap contextmap = new ContextMap();
        contextmap.useCsv = true;  // use CSV; set to false for SQLite
        //createIndexForHostname("nl.wikipedia.org", contextmap, null);
        //createIndexForHostname("lite.cnn.com", contextmap, null);
        createIndexForHostname("nos.nl", contextmap, null);
        // createIndexForHostname("rtl.nl", contextmap, null);
    }
}

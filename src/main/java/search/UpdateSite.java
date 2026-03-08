package search;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of abstr1.update_site.
 * Updates existing site index: iterates over URLs already in the index, re-checks etag,
 * re-processes changed pages and discovers new anchors. Contrast with ProcessNewSite
 * which does BFS from a single start URL.
 */
public final class UpdateSite {

    private static final Hosts HOSTS = new Hosts();
    private static final Index INDEX = new Index();
    private static final Urls URLS = new Urls();

    private UpdateSite() {
    }

    public static int crawl(String startUrl, ContextMap contextmap) {
        int cached = 0;
        int processed = 0;
        int visited = 0;
        List<String> urlsToProcess = new ArrayList<>();
        urlsToProcess.add(startUrl);

        contextmap.hostDAO.startScanIndex(startUrl);
        List<String> allUrls = INDEX.getUrls(contextmap);

        System.out.println("processing " + startUrl);

        for (String url : allUrls) {
            contextmap.currentUrl = url;
            if (!allUrls.isEmpty()) {
                contextmap.hostDAO.updateScanIndexProgress(startUrl, visited * 100.0 / allUrls.size());
            }
            visited++;

            if (!URLS.needsBeIndexed(contextmap)) {
                HOSTS.addToUnknownHosts(contextmap);
                urlsToProcess.remove(url);
                continue;
            }

            String tag = SiteProcessing.etagHead(url);
            if (INDEX.isEtagInIndex(url, tag, contextmap)) {
                urlsToProcess.remove(url);
                cached++;
                continue;
            }

            ProcessResult processResult = URLInput.process(url);
            INDEX.addToIndex(contextmap, processResult);
            for (String anchor : processResult.getAnchorUrls(url)) {
                if (!urlsToProcess.contains(anchor)) {
                    urlsToProcess.add(anchor);
                }
            }
            processed++;
            Robots.doRobotDelay(contextmap);
            Logs.logProcessing(visited, urlsToProcess, cached, processed);
        }

        contextmap.hostDAO.stopScanIndex(startUrl);
        return visited;
    }

    public static void updateIndexForHostname(String hostname, ContextMap contextmap, String starturl) {
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
        String dataDir = "data";
        boolean useCsv = true;
        Hosts hosts = new Hosts(dataDir);
        List<String> hostnames = hosts.getHosts(useCsv);

        for (String host : hostnames) {
            ContextMap contextmap = new ContextMap();
            contextmap.useCsv = useCsv;
            updateIndexForHostname(host, contextmap, null);
        }
    }
}

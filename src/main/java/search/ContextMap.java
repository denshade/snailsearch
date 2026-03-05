package search;

import crawlercommons.robots.BaseRobotRules;

import search.store.HostStore;
import search.store.IndexStore;

/**
 * Port of abstr2.context.ContextMap.
 */
public class ContextMap {

    public String currentUrl;
    public URLFilter urlFilter;
    public BaseRobotRules robotRules;
    public String currentHost;

    /** Which store to use for hosts and index. Set before init. */
    public boolean useCsv = true;

    public HostStore hostStore;
    public IndexStore indexStore;

    public ContextMap() {
    }
}

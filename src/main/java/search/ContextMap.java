package search;

import search.store.HostDAO;
import search.store.IndexDAO;

/**
 * Port of abstr2.context.ContextMap.
 */
public class ContextMap {

    public String currentUrl;
    public URLFilter urlFilter;
    public RobotRules robotRules;
    public String currentHost;

    /** Which store to use for hosts and index. Set before init. */
    public boolean useCsv = true;

    public HostDAO hostDAO;
    public IndexDAO indexDAO;

    public ContextMap() {
    }
}

package search;

/**
 * One row from the indices table: index (host) name, update start/end, and completion percentage.
 */
public final class IndexRecord {

    private final String indexName;
    private final String updateStart;
    private final String updateEnd;
    private final double pctComplete;

    public IndexRecord(String indexName, String updateStart, String updateEnd, double pctComplete) {
        this.indexName = indexName != null ? indexName : "";
        this.updateStart = updateStart != null ? updateStart : "";
        this.updateEnd = updateEnd != null ? updateEnd : "";
        this.pctComplete = pctComplete;
    }

    public String getIndexName() { return indexName; }
    public String getUpdateStart() { return updateStart; }
    public String getUpdateEnd() { return updateEnd; }
    public double getPctComplete() { return pctComplete; }
}

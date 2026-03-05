package search;

import crawlercommons.robots.BaseRobotRules;

import java.sql.Connection;

/**
 * Port of abstr2.context.ContextMap.
 */
public class ContextMap {

    public String currentUrl;
    public URLFilter urlFilter;
    public BaseRobotRules robotRules;
    public Connection hostConnection;
    public java.sql.Statement hostStatement;
    public String currentHost;
    public Connection indexConnection;
    public java.sql.Statement indexStatement;

    public ContextMap() {
    }
}

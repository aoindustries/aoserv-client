package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @see  HttpdWorker
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdWorkerTable extends CachedTableIntegerKey<HttpdWorker> {

    HttpdWorkerTable(AOServConnector connector) {
	super(connector, HttpdWorker.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(HttpdWorker.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_BUSINESS_SERVER_name+'.'+BusinessServer.COLUMN_SERVER_name+'.'+Server.COLUMN_ACCOUNTING_name, ASCENDING),
        new OrderBy(HttpdWorker.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_BUSINESS_SERVER_name+'.'+BusinessServer.COLUMN_SERVER_name+'.'+Server.COLUMN_NAME_name, ASCENDING),
        new OrderBy(HttpdWorker.COLUMN_CODE_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public HttpdWorker get(int pkey) throws IOException, SQLException {
    	return getUniqueRow(HttpdWorker.COLUMN_PKEY, pkey);
    }

    List<HttpdWorker> getHttpdWorkers(HttpdServer server) throws IOException, SQLException {
        int serverPKey=server.pkey;
        List<HttpdWorker> cached=getRows();
        int size=cached.size();
        List<HttpdWorker> matches=new ArrayList<HttpdWorker>(size);
    Loop:
    	for(int c=0;c<size;c++) {
            HttpdWorker worker=cached.get(c);
            HttpdTomcatSite hts=worker.getHttpdTomcatSite();
            if(hts!=null) {
                List<HttpdSiteBind> binds=hts.getHttpdSite().getHttpdSiteBinds();
                // If one of the binds is this server, then count as a match
                for(int d=0;d<binds.size();d++) {
                    if(binds.get(d).getHttpdBind().httpd_server==serverPKey) {
                        matches.add(worker);
                        continue Loop;
                    }
                }
            } else {
                HttpdSharedTomcat hst=worker.getHttpdSharedTomcat();
                if(hst!=null) {
                    // If one of the binds is this server, then count as a match
                    for(HttpdTomcatSharedSite htss : hst.getHttpdTomcatSharedSites()) {
                        List<HttpdSiteBind> binds=htss.getHttpdTomcatSite().getHttpdSite().getHttpdSiteBinds();
                        for(int e=0;e<binds.size();e++) {
                            if(binds.get(e).getHttpdBind().httpd_server==serverPKey) {
                                matches.add(worker);
                                continue Loop;
                            }
                        }
                    }
                } else {
                    connector.logger.log(Level.WARNING, "pkey="+worker.pkey, new SQLException("HttpdWorker doesn't have either HttpdTomcatSite or HttpdSharedTomcat"));
                }
            }
        }
        return matches;
    }

    List<HttpdWorker> getHttpdWorkers(HttpdTomcatSite tomcatSite) throws IOException, SQLException {
        return getIndexedRows(HttpdWorker.COLUMN_TOMCAT_SITE, tomcatSite.pkey);
    }

    HttpdWorker getHttpdWorker(NetBind nb) throws IOException, SQLException {
        return getUniqueRow(HttpdWorker.COLUMN_NET_BIND, nb.pkey);
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.HTTPD_WORKERS;
    }
}
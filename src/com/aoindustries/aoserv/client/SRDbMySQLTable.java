package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */

/**
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class SRDbMySQLTable extends ServerReportSectionTable<SRDbMySQL> {

    SRDbMySQLTable(AOServConnector connector) {
	super(connector, SRDbMySQL.class);
    }

    public SRDbMySQL get(Object serverReport) {
        return get(((Integer)serverReport).intValue());
    }

    public SRDbMySQL get(int serverReport) {
	return getUniqueRow(SRDbMySQL.COLUMN_SERVER_REPORT, serverReport);
    }

    int getTableID() {
	return SchemaTable.SR_DB_MYSQL;
    }
}
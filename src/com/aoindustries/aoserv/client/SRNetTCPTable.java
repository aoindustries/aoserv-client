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
final public class SRNetTCPTable extends ServerReportSectionTable<SRNetTCP> {

    SRNetTCPTable(AOServConnector connector) {
	super(connector, SRNetTCP.class);
    }

    public SRNetTCP get(Object serverReport) {
        return get(((Integer)serverReport).intValue());
    }

    public SRNetTCP get(int serverReport) {
	return getUniqueRow(SRNetTCP.COLUMN_SERVER_REPORT, serverReport);
    }

    int getTableID() {
	return SchemaTable.SR_NET_TCP;
    }
}
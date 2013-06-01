package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;

/**
 * @see  HttpdJKProtocol
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdJKProtocolTable extends GlobalTableStringKey<HttpdJKProtocol> {

    HttpdJKProtocolTable(AOServConnector connector) {
	super(connector, HttpdJKProtocol.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(HttpdJKProtocol.COLUMN_PROTOCOL_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public HttpdJKProtocol get(String protocol) throws IOException, SQLException {
        return getUniqueRow(HttpdJKProtocol.COLUMN_PROTOCOL, protocol);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.HTTPD_JK_PROTOCOLS;
    }
}
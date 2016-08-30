/*
 * Copyright 2001-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @see  HttpdServer
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdServerTable extends CachedTableIntegerKey<HttpdServer> {

	HttpdServerTable(AOServConnector connector) {
		super(connector, HttpdServer.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(HttpdServer.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(HttpdServer.COLUMN_NUMBER_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public HttpdServer get(int pkey) throws IOException, SQLException {
		return getUniqueRow(HttpdServer.COLUMN_PKEY, pkey);
	}

	List<HttpdServer> getHttpdServers(AOServer ao) throws IOException, SQLException {
		return getIndexedRows(HttpdServer.COLUMN_AO_SERVER, ao.pkey);
	}

	List<HttpdServer> getHttpdServers(Package pk) throws IOException, SQLException {
		return getIndexedRows(HttpdServer.COLUMN_PACKAGE, pk.pkey);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.HTTPD_SERVERS;
	}
}
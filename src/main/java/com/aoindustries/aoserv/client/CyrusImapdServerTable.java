/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-client.
 *
 * aoserv-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @see  CyrusImapdServer
 *
 * @author  AO Industries, Inc.
 */
final public class CyrusImapdServerTable extends CachedTableIntegerKey<CyrusImapdServer> {

	CyrusImapdServerTable(AOServConnector connector) {
		super(connector, CyrusImapdServer.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(CyrusImapdServer.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public CyrusImapdServer get(int ao_server) throws IOException, SQLException {
		return getUniqueRow(CyrusImapdServer.COLUMN_AO_SERVER, ao_server);
	}

	CyrusImapdServer getCyrusImapdServerBySieveNetBind(NetBind nb) throws IOException, SQLException {
		return getUniqueRow(CyrusImapdServer.COLUMN_SIEVE_NET_BIND, nb.pkey);
	}

	List<CyrusImapdServer> getCyrusImapdServers(SslCertificate sslCert) throws IOException, SQLException {
		return getIndexedRows(CyrusImapdServer.COLUMN_CERTIFICATE, sslCert.pkey);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.CYRUS_IMAPD_SERVERS;
	}
}

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

import com.aoindustries.io.TerminalWriter;
import com.aoindustries.sql.SQLUtility;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

/**
 * @author  AO Industries, Inc.
 */
final public class SslCertificateTable extends CachedTableIntegerKey<SslCertificate> {

	SslCertificateTable(AOServConnector connector) {
		super(connector, SslCertificate.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(SslCertificate.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(SslCertificate.COLUMN_CERT_FILE_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public SslCertificate get(int pkey) throws IOException, SQLException {
		return getUniqueRow(SslCertificate.COLUMN_PKEY, pkey);
	}

	List<SslCertificate> getSslCertificates(AOServer aoServer) throws IOException, SQLException {
		return getIndexedRows(SslCertificate.COLUMN_AO_SERVER, aoServer.pkey);
	}

	List<SslCertificate> getSslCertificates(Package pk) throws IOException, SQLException {
		return getIndexedRows(SslCertificate.COLUMN_PACKAGE, pk.pkey);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.SSL_CERTIFICATES;
	}

	@Override
	boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command = args[0];
		if(command.equalsIgnoreCase(AOSHCommand.CHECK_SSL_CERTIFICATE)) {
			if(AOSH.checkParamCount(AOSHCommand.CHECK_SSL_CERTIFICATE, args, 2, err)) {
				List<SslCertificate.Check> results = connector.getSimpleAOClient().checkSslCertificate(
					args[1],
					args[2]
				);
				int size = results.size();
				final int COLUMNS = 3;
				Object[] values = new Object[size * COLUMNS];
				for(int i = 0; i < size; i++) {
					SslCertificate.Check status = results.get(i);
					values[i * COLUMNS] = status.getCheck();
					values[i * COLUMNS + 1] = status.getResult();
					values[i * COLUMNS + 2] = status.getAlertLevel();
				}
				// Display as a table
				SQLUtility.printTable(
					new String[] {"check", "result", "alert_level"},
					values,
					out,
					isInteractive,
					new boolean[] {false, false, false}
				);
				out.flush();
			}
			return true;
		}
		return false;
	}
}

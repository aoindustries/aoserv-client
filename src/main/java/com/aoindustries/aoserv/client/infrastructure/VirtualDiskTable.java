/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2008-2009, 2014, 2016, 2017, 2018  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.infrastructure;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedTableIntegerKey;
import com.aoindustries.aoserv.client.aosh.AOSH;
import com.aoindustries.aoserv.client.aosh.Command;
import com.aoindustries.aoserv.client.billing.Package;
import com.aoindustries.aoserv.client.net.Host;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.io.TerminalWriter;
import com.aoindustries.sql.SQLUtility;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

/**
 * @see  VirtualDisk
 *
 * @author  AO Industries, Inc.
 */
final public class VirtualDiskTable extends CachedTableIntegerKey<VirtualDisk> {

	public VirtualDiskTable(AOServConnector connector) {
		super(connector, VirtualDisk.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(VirtualDisk.COLUMN_VIRTUAL_SERVER_name+'.'+VirtualServer.COLUMN_SERVER_name+'.'+Host.COLUMN_PACKAGE_name+'.'+Package.COLUMN_NAME_name, ASCENDING),
		new OrderBy(VirtualDisk.COLUMN_VIRTUAL_SERVER_name+'.'+VirtualServer.COLUMN_SERVER_name+'.'+Host.COLUMN_NAME_name, ASCENDING),
		new OrderBy(VirtualDisk.COLUMN_DEVICE_name, ASCENDING)
	};
	@Override
	protected OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public VirtualDisk get(int pkey) throws IOException, SQLException {
		return getUniqueRow(VirtualDisk.COLUMN_PKEY, pkey);
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.VIRTUAL_DISKS;
	}

	List<VirtualDisk> getVirtualDisks(VirtualServer vs) throws IOException, SQLException {
		return getIndexedRows(VirtualDisk.COLUMN_VIRTUAL_SERVER, vs.getPkey());
	}

	@Override
	public boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command=args[0];
		if(command.equalsIgnoreCase(Command.VERIFY_VIRTUAL_DISK)) {
			if(AOSH.checkParamCount(Command.VERIFY_VIRTUAL_DISK, args, 2, err)) {
				long lastVerified = connector.getSimpleAOClient().verifyVirtualDisk(args[1], args[2]);
				if(isInteractive) {
					out.println(SQLUtility.getDateTime(lastVerified));
				} else {
					out.println(lastVerified);
				}
				out.flush();
			}
			return true;
		}
		return false;
	}
}
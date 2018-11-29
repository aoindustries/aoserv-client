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
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * @see  IpAddressMonitoring
 *
 * @author  AO Industries, Inc.
 */
final public class IpAddressMonitoringTable extends CachedTableIntegerKey<IpAddressMonitoring> {

	IpAddressMonitoringTable(AOServConnector connector) {
		super(connector, IpAddressMonitoring.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(IpAddressMonitoring.COLUMN_ID_name + '.' + IPAddress.COLUMN_IP_ADDRESS_name, ASCENDING),
		new OrderBy(IpAddressMonitoring.COLUMN_ID_name + '.' + IPAddress.COLUMN_DEVICE_name+'.'+NetDevice.COLUMN_SERVER_name+'.'+Server.COLUMN_PACKAGE_name+'.'+Package.COLUMN_NAME_name, ASCENDING),
		new OrderBy(IpAddressMonitoring.COLUMN_ID_name + '.' + IPAddress.COLUMN_DEVICE_name+'.'+NetDevice.COLUMN_SERVER_name+'.'+Server.COLUMN_NAME_name, ASCENDING),
		new OrderBy(IpAddressMonitoring.COLUMN_ID_name + '.' + IPAddress.COLUMN_DEVICE_name+'.'+NetDevice.COLUMN_DEVICE_ID_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public IpAddressMonitoring get(int id) throws IOException, SQLException {
		return getUniqueRow(IpAddressMonitoring.COLUMN_ID, id);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.IpAddressMonitoring;
	}

	@Override
	boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command=args[0];
		if(command.equalsIgnoreCase(AOSHCommand.SET_IP_ADDRESS_MONITORING_ENABLED)) {
			if(AOSH.checkParamCount(AOSHCommand.SET_IP_ADDRESS_MONITORING_ENABLED, args, 4, err)) {
				connector.getSimpleAOClient().setIPAddressMonitoringEnabled(
					AOSH.parseInetAddress(args[1], "ip_address"),
					args[2],
					args[3],
					AOSH.parseBoolean(args[4], "enabled")
				);
			}
			return true;
		}
		return false;
	}
}

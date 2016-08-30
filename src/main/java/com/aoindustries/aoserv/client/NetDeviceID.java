/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2001-2013, 2016  AO Industries, Inc.
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

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An <code>NetDeviceID</code> is a simple wrapper for the
 * different names of network devices used in Linux servers.
 *
 * @see  NetDevice
 *
 * @author  AO Industries, Inc.
 */
final public class NetDeviceID extends GlobalObjectStringKey<NetDeviceID> implements Comparable<NetDeviceID> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_NAME_name = "name";

	public static final String
		BMC="bmc",
		BOND0="bond0",
		BOND1="bond1",
		BOND2="bond2",
		LO="lo",
		ETH0="eth0",
		ETH1="eth1",
		ETH2="eth2",
		ETH3="eth3",
		ETH4="eth4",
		ETH5="eth5",
		ETH6="eth6"
	;

	private boolean is_loopback;

	@Override
	Object getColumnImpl(int i) {
		if(i==COLUMN_NAME) return pkey;
		if(i==1) return is_loopback;
		throw new IllegalArgumentException("Invalid index: "+i);
	}

	public String getName() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.NET_DEVICE_IDS;
	}

	@Override
	public void init(ResultSet results) throws SQLException {
		pkey=results.getString(1);
		is_loopback=results.getBoolean(2);
	}

	public boolean isLoopback() {
		return is_loopback;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readUTF().intern();
		is_loopback=in.readBoolean();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		out.writeBoolean(is_loopback);
	}

	@Override
	public int compareTo(NetDeviceID other) {
		return pkey.compareTo(other.pkey);
	}
}

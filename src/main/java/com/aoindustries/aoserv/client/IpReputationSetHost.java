/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2012-2013, 2016, 2017  AO Industries, Inc.
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
import com.aoindustries.math.LongLong;
import com.aoindustries.math.SafeMath;
import com.aoindustries.net.InetAddress;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * One host tracked by an <code>IpReputationSet</code>.
 *
 * @author  AO Industries, Inc.
 */
final public class IpReputationSetHost extends CachedObjectLongKey<IpReputationSetHost> {

	static final int
		COLUMN_PKEY = 0,
		COLUMN_SET = 1
	;
	static final String
		COLUMN_SET_name  = "set",
		COLUMN_HOST_name = "host"
	;

	int set;
	private int host;
	private short goodReputation;
	private short badReputation;

	@Override
	public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.IP_REPUTATION_SET_HOSTS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		int pos = 1;
		pkey           = result.getLong(pos++);
		set            = result.getInt(pos++);
		host           = result.getInt(pos++);
		goodReputation = result.getShort(pos++);
		badReputation  = result.getShort(pos++);
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeLong(pkey);
		out.writeCompressedInt(set);
		out.writeInt(host);
		out.writeShort(goodReputation);
		out.writeShort(badReputation);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey           = in.readLong();
		set            = in.readCompressedInt();
		host           = in.readInt();
		goodReputation = in.readShort();
		badReputation  = in.readShort();
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY     : return pkey;
			case COLUMN_SET      : return set;
			case 2               : return getHostAddress();
			case 3               : return goodReputation;
			case 4               : return badReputation;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public IpReputationSet getSet() throws SQLException, IOException {
		IpReputationSet obj = table.connector.getIpReputationSets().get(set);
		if(obj==null) throw new SQLException("Unable to find IpReputationSet: " + set);
		return obj;
	}

	/**
	 * Gets the 32-bit host address.
	 */
	public int getHost() {
		return host;
	}

	/**
	 * Gets the IPv4 host address.
	 */
	public InetAddress getHostAddress() {
		return InetAddress.valueOf(
			LongLong.valueOf(
				0,
				((long)host) & 0x00000000ffffffffL
			)
		);
	}

	/**
	 * Gets the current good reputation for this host.
	 */
	public short getGoodReputation() {
		return goodReputation;
	}

	/**
	 * Gets the current bad reputation for this host.
	 */
	public short getBadReputation() {
		return badReputation;
	}

	/**
	 * The effective reputation is the good minus the bad.
	 */
	public short getReputation() {
		return SafeMath.castShort(goodReputation - badReputation);
	}
}

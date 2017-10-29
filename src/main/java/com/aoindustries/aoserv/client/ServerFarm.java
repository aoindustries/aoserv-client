/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2000-2009, 2016, 2017  AO Industries, Inc.
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
 * AO Industries provides greater reliability through the use of multiple network locations.
 * Each location is represented by a <code>ServerFarm</code> object.
 *
 * @author  AO Industries, Inc.
 */
final public class ServerFarm extends CachedObjectStringKey<ServerFarm> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_NAME_name = "name";

	private String description;
	private int owner;
	private boolean use_restricted_smtp_port;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_NAME: return pkey;
			case 1: return description;
			case 2: return owner;
			case 3: return use_restricted_smtp_port;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public Package getOwner() throws IOException, SQLException {
		// May be filtered
		return table.connector.getPackages().get(owner);
	}

	/**
	 * TODO: Remove this flag once all servers are CentOS 7+ with firewalld-based egress filters.
	 */
	public boolean useRestrictedSmtpPort() {
		return use_restricted_smtp_port;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.SERVER_FARMS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
		description = result.getString(2);
		owner = result.getInt(3);
		use_restricted_smtp_port = result.getBoolean(4);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readUTF().intern();
		description=in.readUTF();
		owner=in.readCompressedInt();
		use_restricted_smtp_port = in.readBoolean();
	}

	@Override
	String toStringImpl() {
		return description;
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeUTF(pkey);
		out.writeUTF(description);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) {
			out.writeUTF("192.168.0.0/16");
			out.writeBoolean(false);
			out.writeUTF("mob");
		}
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_102)>=0) out.writeCompressedInt(owner);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_26)>=0) out.writeBoolean(use_restricted_smtp_port);
	}
}

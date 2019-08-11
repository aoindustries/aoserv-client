/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2000-2013, 2016, 2017, 2018, 2019  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.email;

import com.aoindustries.aoserv.client.GlobalObjectStringKey;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.io.stream.StreamableInput;
import com.aoindustries.io.stream.StreamableOutput;
import com.aoindustries.sql.UnmodifiableTimestamp;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Multiple versions of Majordomo are supported by the system.
 * Each <code>MajordomoServer</code> is of a specific version,
 * and all its <code>MajordomoList</code>s inherit that
 * <code>MajordomoVersion</code>.
 *
 * @see  MajordomoServer
 *
 * @author  AO Industries, Inc.
 */
final public class MajordomoVersion extends GlobalObjectStringKey<MajordomoVersion> {

	static final int COLUMN_VERSION=0;
	static final String COLUMN_VERSION_name = "version";

	/**
	 * The default Majordomo version.
	 */
	public static final String DEFAULT_VERSION="1.94.5";

	private UnmodifiableTimestamp created;

	@Override
	protected Object getColumnImpl(int i) {
		if(i==COLUMN_VERSION) return pkey;
		if(i==1) return created;
		throw new IllegalArgumentException("Invalid index: " + i);
	}

	public UnmodifiableTimestamp getCreated() {
		return created;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.MAJORDOMO_VERSIONS;
	}

	public String getVersion() {
		return pkey;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getString(1);
		created = UnmodifiableTimestamp.valueOf(result.getTimestamp(2));
	}

	@Override
	public void read(StreamableInput in, AoservProtocol.Version protocolVersion) throws IOException {
		pkey=in.readUTF().intern();
		created = in.readUnmodifiableTimestamp();
	}

	@Override
	public void write(StreamableOutput out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeUTF(pkey);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_83_0) < 0) {
			out.writeLong(created.getTime());
		} else {
			out.writeTimestamp(created);
		}
	}
}

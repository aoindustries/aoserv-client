/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2000-2009, 2016  AO Industries, Inc.
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

import static com.aoindustries.aoserv.client.ApplicationResources.accessor;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author  AO Industries, Inc.
 */
final public class Language extends GlobalObjectStringKey<Language> {

	static final int COLUMN_CODE = 0;
	static final String COLUMN_CODE_name = "code";

	public static final String
		EN="en",
		JA="ja"
	;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_CODE: return pkey;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	@Override
	String toStringImpl() {
		return accessor.getMessage("Language."+pkey+".toString");
	}

	public String getCode() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.LANGUAGES;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey = in.readUTF().intern();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
	}
}

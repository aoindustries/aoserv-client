/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2009, 2016, 2017, 2018  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.linux;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.GlobalTableUnixPathKey;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.validator.UnixPath;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @see  Shell
 *
 * @author  AO Industries, Inc.
 */
final public class ShellTable extends GlobalTableUnixPathKey<Shell> {

	public ShellTable(AOServConnector connector) {
		super(connector, Shell.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(Shell.COLUMN_PATH_name, ASCENDING)
	};
	@Override
	protected OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.SHELLS;
	}

	@Override
	public Shell get(UnixPath path) throws IOException, SQLException {
		return getUniqueRow(Shell.COLUMN_PATH, path);
	}
}
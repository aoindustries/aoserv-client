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
package com.aoindustries.aoserv.client.email;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedTableIntegerKey;
import com.aoindustries.aoserv.client.linux.Server;
import com.aoindustries.aoserv.client.schema.Table;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * @see  SystemAlias
 *
 * @author  AO Industries, Inc.
 */
final public class SystemAliasTable extends CachedTableIntegerKey<SystemAlias> {

	SystemAliasTable(AOServConnector connector) {
		super(connector, SystemAlias.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(SystemAlias.COLUMN_AO_SERVER_name+'.'+Server.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(SystemAlias.COLUMN_ADDRESS_name, ASCENDING)
	};
	@Override
	protected OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	public List<SystemAlias> getSystemEmailAliases(Server ao) throws IOException, SQLException {
		return getIndexedRows(SystemAlias.COLUMN_AO_SERVER, ao.getPkey());
	}

	@Override
	public SystemAlias get(int pkey) throws IOException, SQLException {
		return getUniqueRow(SystemAlias.COLUMN_PKEY, pkey);
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.SYSTEM_EMAIL_ALIASES;
	}
}

/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2013, 2016  AO Industries, Inc.
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

import java.io.IOException;
import java.sql.SQLException;

/**
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class DistroReportTypeTable extends GlobalTableStringKey<DistroReportType> {

	DistroReportTypeTable(AOServConnector connector) {
		super(connector, DistroReportType.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(DistroReportType.COLUMN_NAME_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public DistroReportType get(String name) throws IOException, SQLException {
		return getUniqueRow(DistroReportType.COLUMN_NAME, name);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.DISTRO_REPORT_TYPES;
	}
}

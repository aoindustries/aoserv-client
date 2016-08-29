/*
 * Copyright 2006-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The table containing all of the possible time zones.
 *
 * @see TimeZone
 *
 * @author  AO Industries, Inc.
 */
final public class TimeZoneTable extends GlobalTableStringKey<TimeZone> {

	TimeZoneTable(AOServConnector connector) {
		super(connector, TimeZone.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(TimeZone.COLUMN_NAME_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.TIME_ZONES;
	}

	@Override
	public TimeZone get(String name) throws IOException, SQLException {
		return getUniqueRow(TimeZone.COLUMN_NAME, name);
	}
}

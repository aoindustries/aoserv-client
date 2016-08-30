/*
 * Copyright 2008-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @see  Rack
 *
 * @author  AO Industries, Inc.
 */
final public class RackTable extends CachedTableIntegerKey<Rack> {

	RackTable(AOServConnector connector) {
	super(connector, Rack.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(Rack.COLUMN_FARM_name, ASCENDING),
		new OrderBy(Rack.COLUMN_NAME_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public Rack get(int pkey) throws IOException, SQLException {
		return getUniqueRow(Rack.COLUMN_PKEY, pkey);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.RACKS;
	}
}
/*
 * Copyright 2001-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;

/**
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class BankTable extends CachedTableStringKey<Bank> {

	BankTable(AOServConnector connector) {
		super(connector, Bank.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(Bank.COLUMN_NAME_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public Bank get(String name) throws IOException, SQLException {
		return getUniqueRow(Bank.COLUMN_NAME, name);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.BANKS;
	}
}
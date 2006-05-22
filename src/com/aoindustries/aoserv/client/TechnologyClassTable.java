package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */

/**
 * @see  TechnologyClass
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class TechnologyClassTable extends GlobalTableStringKey<TechnologyClass> {

    TechnologyClassTable(AOServConnector connector) {
	super(connector, TechnologyClass.class);
    }

    int getTableID() {
	return SchemaTable.TECHNOLOGY_CLASSES;
    }

    public TechnologyClass get(Object pkey) {
	return getUniqueRow(TechnologyClass.COLUMN_NAME, pkey);
    }
}
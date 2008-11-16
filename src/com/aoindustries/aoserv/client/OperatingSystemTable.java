package com.aoindustries.aoserv.client;

/*
 * Copyright 2003-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * All of the operating systems referenced from other tables.
 *
 * @see OperatingSystem
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class OperatingSystemTable extends GlobalTableStringKey<OperatingSystem> {

    OperatingSystemTable(AOServConnector connector) {
	super(connector, OperatingSystem.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(OperatingSystem.COLUMN_NAME_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public OperatingSystem get(Object pkey) {
	return getUniqueRow(OperatingSystem.COLUMN_NAME, pkey);
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.OPERATING_SYSTEMS;
    }
}
package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  LinuxGroupType
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class LinuxGroupTypeTable extends GlobalTableStringKey<LinuxGroupType> {

    LinuxGroupTypeTable(AOServConnector connector) {
	super(connector, LinuxGroupType.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(LinuxGroupType.COLUMN_DESCRIPTION_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public LinuxGroupType get(Object pkey) {
	return getUniqueRow(LinuxGroupType.COLUMN_NAME, pkey);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.LINUX_GROUP_TYPES;
    }
}
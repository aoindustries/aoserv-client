package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
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

    public LinuxGroupType get(Object pkey) {
	return getUniqueRow(LinuxGroupType.COLUMN_NAME, pkey);
    }

    int getTableID() {
	return SchemaTable.LINUX_GROUP_TYPES;
    }
}
package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  DNSType
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class DNSTypeTable extends GlobalTableStringKey<DNSType> {

    DNSTypeTable(AOServConnector connector) {
	super(connector, DNSType.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(DNSType.COLUMN_DESCRIPTION_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public DNSType get(Object pkey) {
	return getUniqueRow(DNSType.COLUMN_TYPE, pkey);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.DNS_TYPES;
    }
}
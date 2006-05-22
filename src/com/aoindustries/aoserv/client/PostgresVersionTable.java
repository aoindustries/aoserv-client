package com.aoindustries.aoserv.client;

/*
 * Copyright 2002-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */

/**
 * @see  PostgresVersion
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class PostgresVersionTable extends GlobalTableIntegerKey<PostgresVersion> {

    PostgresVersionTable(AOServConnector connector) {
	super(connector, PostgresVersion.class);
    }

    public PostgresVersion get(Object pkey) {
	return getUniqueRow(PostgresVersion.COLUMN_VERSION, pkey);
    }

    public PostgresVersion get(int pkey) {
	return getUniqueRow(PostgresVersion.COLUMN_VERSION, pkey);
    }

    public PostgresVersion getPostgresVersion(String version, OperatingSystemVersion osv) {
	return get(
            connector
            .technologyNames
            .get(PostgresVersion.TECHNOLOGY_NAME)
            .getTechnologyVersion(connector, version, osv)
            .getPKey()
	);
    }

    int getTableID() {
	return SchemaTable.POSTGRES_VERSIONS;
    }
}
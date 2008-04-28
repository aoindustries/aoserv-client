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
 * @see  HttpdBind
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdBindTable extends CachedTableIntegerKey<HttpdBind> {

    HttpdBindTable(AOServConnector connector) {
	super(connector, HttpdBind.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING),
        new OrderBy(HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_IP_ADDRESS_name+'.'+IPAddress.COLUMN_IP_ADDRESS_name, ASCENDING),
        new OrderBy(HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_IP_ADDRESS_name+'.'+IPAddress.COLUMN_NET_DEVICE_name+'.'+NetDevice.COLUMN_DEVICE_ID_name, ASCENDING),
        new OrderBy(HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_PORT_name, ASCENDING),
        new OrderBy(HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_NET_PROTOCOL_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    List<HttpdBind> getHttpdBinds(HttpdServer server) {
        return getIndexedRows(HttpdBind.COLUMN_HTTPD_SERVER, server.pkey);
    }

    public HttpdBind get(Object pkey) {
	return getUniqueRow(HttpdBind.COLUMN_NET_BIND, pkey);
    }

    public HttpdBind get(int pkey) {
	return getUniqueRow(HttpdBind.COLUMN_NET_BIND, pkey);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.HTTPD_BINDS;
    }
}
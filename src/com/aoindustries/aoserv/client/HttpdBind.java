package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.sql.*;
import com.aoindustries.util.*;
import java.io.*;
import java.sql.*;

/**
 * Each <code>HttpdServer</code> may listen for network connections on
 * multiple <code>NetBind</code>s.  An <code>HttpdBind</code> ties
 * <code>HttpdServer</code>s to <code>NetBinds</code>.
 *
 * @see  HttpdServer
 * @see  NetBind
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdBind extends CachedObjectIntegerKey<HttpdBind> {

    static final int
        COLUMN_NET_BIND=0,
        COLUMN_HTTPD_SERVER=1
    ;
    static final String COLUMN_NET_BIND_name = "net_bind";
    
    int httpd_server;

    public Object getColumn(int i) {
        switch(i) {
            case COLUMN_NET_BIND: return Integer.valueOf(pkey);
            case COLUMN_HTTPD_SERVER: return Integer.valueOf(httpd_server);
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public HttpdServer getHttpdServer() {
	HttpdServer obj=table.connector.httpdServers.get(httpd_server);
	if(obj==null) throw new WrappedException(new SQLException("Unable to find HttpdServer: "+httpd_server));
	return obj;
    }

    public NetBind getNetBind() {
	NetBind obj=table.connector.netBinds.get(pkey);
	if(obj==null) throw new WrappedException(new SQLException("Unable to find NetBind: "+pkey));
	return obj;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.HTTPD_BINDS;
    }

    void initImpl(ResultSet result) throws SQLException {
	pkey=result.getInt(1);
	httpd_server=result.getInt(2);
    }

    public void read(CompressedDataInputStream in) throws IOException {
	pkey=in.readCompressedInt();
	httpd_server=in.readCompressedInt();
    }

    String toStringImpl() {
        HttpdServer server=getHttpdServer();
        NetBind bind=getNetBind();
        return server.toString()+'|'+bind.toString();
    }

    public void write(CompressedDataOutputStream out, String version) throws IOException {
	out.writeCompressedInt(pkey);
	out.writeCompressedInt(httpd_server);
    }
}
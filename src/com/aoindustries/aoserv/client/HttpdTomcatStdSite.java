package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.sql.*;
import java.io.*;
import java.sql.*;
import java.util.Locale;

/**
 * An <code>HttpdStdTomcatSite</code> inicates that a
 * <code>HttpdTomcatSite</code> is configured in the standard layout
 * of one Tomcat instance per Java virtual machine.
 *
 * @see  HttpdTomcatSite
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdTomcatStdSite extends CachedObjectIntegerKey<HttpdTomcatStdSite> {

    static final int COLUMN_TOMCAT_SITE=0;
    static final String COLUMN_TOMCAT_SITE_name = "tomcat_site";

    public static final String DEFAULT_TOMCAT_VERSION_PREFIX=HttpdTomcatVersion.VERSION_6_0_PREFIX;

    int tomcat4_shutdown_port;
    private String tomcat4_shutdown_key;

    Object getColumnImpl(int i) {
        switch(i) {
            case COLUMN_TOMCAT_SITE: return Integer.valueOf(pkey);
            case 1: return tomcat4_shutdown_port==-1?null:Integer.valueOf(tomcat4_shutdown_port);
            case 2: return tomcat4_shutdown_key;
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public HttpdTomcatSite getHttpdTomcatSite() throws SQLException, IOException {
	HttpdTomcatSite obj=table.connector.getHttpdTomcatSites().get(pkey);
	if(obj==null) throw new SQLException("Unable to find HttpdTomcatSite: "+pkey);
	return obj;
    }

    public String getTomcat4ShutdownKey() {
        return tomcat4_shutdown_key;
    }

    public NetBind getTomcat4ShutdownPort() throws IOException, SQLException {
        if(tomcat4_shutdown_port==-1) return null;
        NetBind nb=table.connector.getNetBinds().get(tomcat4_shutdown_port);
        if(nb==null) throw new SQLException("Unable to find NetBind: "+tomcat4_shutdown_port);
        return nb;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.HTTPD_TOMCAT_STD_SITES;
    }

    public void init(ResultSet result) throws SQLException {
	pkey=result.getInt(1);
        tomcat4_shutdown_port=result.getInt(2);
        if(result.wasNull()) tomcat4_shutdown_port=-1;
        tomcat4_shutdown_key=result.getString(3);
    }

    public void read(CompressedDataInputStream in) throws IOException {
	pkey=in.readCompressedInt();
        tomcat4_shutdown_port=in.readCompressedInt();
        tomcat4_shutdown_key=in.readNullUTF();
    }

    @Override
    String toStringImpl(Locale userLocale) throws SQLException, IOException {
        return getHttpdTomcatSite().toStringImpl(userLocale);
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
	out.writeCompressedInt(pkey);
        out.writeCompressedInt(tomcat4_shutdown_port);
        out.writeNullUTF(tomcat4_shutdown_key);
    }
}
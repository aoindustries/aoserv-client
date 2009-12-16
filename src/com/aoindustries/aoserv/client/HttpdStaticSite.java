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
import java.util.List;
import java.util.Locale;

/**
 * An <code>HttpdStaticSite</code> inicates that an <code>HttpdSite</code>
 * serves static content only.
 *
 * @see  HttpdSite
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdStaticSite extends CachedObjectIntegerKey<HttpdStaticSite> {

    static final int COLUMN_HTTPD_SITE=0;
    static final String COLUMN_HTTPD_SITE_name = "httpd_site";

    Object getColumnImpl(int i) {
	if(i==COLUMN_HTTPD_SITE) return Integer.valueOf(pkey);
	throw new IllegalArgumentException("Invalid index: "+i);
    }

    public HttpdSite getHttpdSite() throws SQLException, IOException {
	HttpdSite obj=table.connector.getHttpdSites().get(pkey);
	if(obj==null) throw new SQLException("Unable to find HttpdSite: "+pkey);
	return obj;
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.HTTPD_STATIC_SITES;
    }

    public void init(ResultSet result) throws SQLException {
        pkey=result.getInt(1);
    }

    public void read(CompressedDataInputStream in) throws IOException {
    	pkey=in.readCompressedInt();
    }

    public List<AOServObject> getDependencies() throws IOException, SQLException {
        return createDependencyList(
            getHttpdSite()
        );
    }

    public List<AOServObject> getDependentObjects() throws IOException, SQLException {
        return createDependencyList(
        );
    }

    @Override
    String toStringImpl(Locale userLocale) throws SQLException, IOException {
        return getHttpdSite().toStringImpl(userLocale);
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
	out.writeCompressedInt(pkey);
    }
}
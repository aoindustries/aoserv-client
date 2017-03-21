/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2001-2012, 2016, 2017  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-client.
 *
 * aoserv-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.TerminalWriter;
import com.aoindustries.net.DomainName;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @see  HttpdSiteURL
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdSiteURLTable extends CachedTableIntegerKey<HttpdSiteURL> {

	HttpdSiteURLTable(AOServConnector connector) {
		super(connector, HttpdSiteURL.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(HttpdSiteURL.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND_name+'.'+HttpdSiteBind.COLUMN_HTTPD_SITE_name+'.'+HttpdSite.COLUMN_SITE_NAME_name, ASCENDING),
		new OrderBy(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND_name+'.'+HttpdSiteBind.COLUMN_HTTPD_SITE_name+'.'+HttpdSite.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND_name+'.'+HttpdSiteBind.COLUMN_HTTPD_BIND_name+'.'+HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_IP_ADDRESS_name+'.'+IPAddress.COLUMN_IP_ADDRESS_name, ASCENDING),
		new OrderBy(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND_name+'.'+HttpdSiteBind.COLUMN_HTTPD_BIND_name+'.'+HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_IP_ADDRESS_name+'.'+IPAddress.COLUMN_NET_DEVICE_name+'.'+NetDevice.COLUMN_DEVICE_ID_name, ASCENDING),
		new OrderBy(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND_name+'.'+HttpdSiteBind.COLUMN_HTTPD_BIND_name+'.'+HttpdBind.COLUMN_NET_BIND_name+'.'+NetBind.COLUMN_PORT_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	int addHttpdSiteURL(HttpdSiteBind hsb, DomainName hostname) throws IOException, SQLException {
		return connector.requestIntQueryIL(
			true,
			AOServProtocol.CommandID.ADD,
			SchemaTable.TableID.HTTPD_SITE_URLS,
			hsb.pkey,
			hostname
		);
	}

	@Override
	public HttpdSiteURL get(int pkey) throws IOException, SQLException {
		return getUniqueRow(HttpdSiteURL.COLUMN_PKEY, pkey);
	}

	List<HttpdSiteURL> getHttpdSiteURLs(HttpdSiteBind bind) throws IOException, SQLException {
		return getIndexedRows(HttpdSiteURL.COLUMN_HTTPD_SITE_BIND, bind.pkey);
	}

	HttpdSiteURL getPrimaryHttpdSiteURL(HttpdSiteBind bind) throws SQLException, IOException {
		// Use the index first
		List<HttpdSiteURL> cached=getHttpdSiteURLs(bind);
		int size=cached.size();
		for(int c=0;c<size;c++) {
			HttpdSiteURL hsu=cached.get(c);
			if(hsu.isPrimary) return hsu;
		}
		throw new SQLException("Unable to find primary HttpdSiteURL for HttpdSiteBind with pkey="+bind.pkey);
	}

	List<HttpdSiteURL> getAltHttpdSiteURLs(HttpdSiteBind bind) throws IOException, SQLException {
		// Use the index first
		List<HttpdSiteURL> cached=getHttpdSiteURLs(bind);
		int size=cached.size();
		List<HttpdSiteURL> matches=new ArrayList<>(size-1);
		for(int c=0;c<size;c++) {
			HttpdSiteURL hsu=cached.get(c);
			if(!hsu.isPrimary) matches.add(hsu);
		}
		return matches;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.HTTPD_SITE_URLS;
	}

	@Override
	boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command=args[0];
		if(command.equalsIgnoreCase(AOSHCommand.ADD_HTTPD_SITE_URL)) {
			if(AOSH.checkParamCount(AOSHCommand.ADD_HTTPD_SITE_URL, args, 2, err)) {
				out.println(
					connector.getSimpleAOClient().addHttpdSiteURL(
						AOSH.parseInt(args[1], "httpd_site_bind_pkey"),
						AOSH.parseDomainName(args[2], "hostname")
					)
				);
				out.flush();
			}
			return true;
		} else if(command.equalsIgnoreCase(AOSHCommand.REMOVE_HTTPD_SITE_URL)) {
			if(AOSH.checkParamCount(AOSHCommand.REMOVE_HTTPD_SITE_URL, args, 1, err)) {
				connector.getSimpleAOClient().removeHttpdSiteURL(AOSH.parseInt(args[1], "pkey"));
			}
			return true;
		} else if(command.equalsIgnoreCase(AOSHCommand.SET_PRIMARY_HTTPD_SITE_URL)) {
			if(AOSH.checkParamCount(AOSHCommand.SET_PRIMARY_HTTPD_SITE_URL, args, 1, err)) {
				connector.getSimpleAOClient().setPrimaryHttpdSiteURL(AOSH.parseInt(args[1], "pkey"));
			}
			return true;
		} else return false;
	}
}

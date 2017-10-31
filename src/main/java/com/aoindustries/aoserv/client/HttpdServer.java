/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2013, 2016, 2017  AO Industries, Inc.
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

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * An <code>HttpdServer</code> represents one running instance of the
 * Apache web server.  Each physical server may run any number of
 * Apache web servers, and each of those may respond to multiple
 * IP addresses and ports, and serve content for many sites.
 *
 * @see  HttpdBind
 * @see  HttpdSite
 * @see  HttpdSiteBind
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdServer extends CachedObjectIntegerKey<HttpdServer> {

	static final int
		COLUMN_PKEY=0,
		COLUMN_AO_SERVER=1,
		COLUMN_PACKAGE=10
	;
	static final String COLUMN_AO_SERVER_name = "ao_server";
	static final String COLUMN_NUMBER_name = "number";

	/**
	 * The highest recommended number of sites to bind in one server.
	 */
	public static final int RECOMMENDED_MAXIMUM_BINDS=128;

	int ao_server;
	private int number;
	private boolean can_add_sites;
	// TODO: Remove this field
	private boolean is_mod_jk;
	private int max_binds;
	int linux_server_account;
	int linux_server_group;
	private int mod_php_version;
	private boolean use_suexec;
	private int packageNum;
	private boolean is_shared;
	private boolean use_mod_perl;
	private int timeout;
	private int max_concurrency;
	private Boolean mod_access_compat;
	private Boolean mod_actions;
	private Boolean mod_alias;
	private Boolean mod_auth_basic;
	private Boolean mod_authn_core;
	private Boolean mod_authn_file;
	private Boolean mod_authz_core;
	private Boolean mod_authz_groupfile;
	private Boolean mod_authz_host;
	private Boolean mod_authz_user;
	private Boolean mod_autoindex;
	private Boolean mod_deflate;
	private Boolean mod_dir;
	private Boolean mod_filter;
	private Boolean mod_headers;
	private Boolean mod_include;
	private Boolean mod_jk;
	private Boolean mod_log_config;
	private Boolean mod_mime;
	private Boolean mod_mime_magic;
	private Boolean mod_negotiation;
	private Boolean mod_proxy;
	private Boolean mod_proxy_http;
	private Boolean mod_reqtimeout;
	private Boolean mod_rewrite;
	private Boolean mod_setenvif;
	private Boolean mod_socache_shmcb;
	private Boolean mod_ssl;
	private Boolean mod_status;

	public boolean canAddSites() {
		return can_add_sites;
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_AO_SERVER: return ao_server;
			case 2: return number;
			case 3: return can_add_sites;
			case 4: return is_mod_jk;
			case 5: return max_binds;
			case 6: return linux_server_account;
			case 7: return linux_server_group;
			case 8: return mod_php_version== -1 ? null : mod_php_version;
			case 9: return use_suexec;
			case COLUMN_PACKAGE: return packageNum;
			case 11: return is_shared;
			case 12: return use_mod_perl;
			case 13: return timeout;
			case 14: return max_concurrency;
			case 15: return mod_access_compat;
			case 16: return mod_actions;
			case 17: return mod_alias;
			case 18: return mod_auth_basic;
			case 19: return mod_authn_core;
			case 20: return mod_authn_file;
			case 21: return mod_authz_core;
			case 22: return mod_authz_groupfile;
			case 23: return mod_authz_host;
			case 24: return mod_authz_user;
			case 25: return mod_autoindex;
			case 26: return mod_deflate;
			case 27: return mod_dir;
			case 28: return mod_filter;
			case 29: return mod_headers;
			case 30: return mod_include;
			case 31: return mod_jk;
			case 32: return mod_log_config;
			case 33: return mod_mime;
			case 34: return mod_mime_magic;
			case 35: return mod_negotiation;
			case 36: return mod_proxy;
			case 37: return mod_proxy_http;
			case 38: return mod_reqtimeout;
			case 39: return mod_rewrite;
			case 40: return mod_setenvif;
			case 41: return mod_socache_shmcb;
			case 42: return mod_ssl;
			case 43: return mod_status;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public List<HttpdBind> getHttpdBinds() throws IOException, SQLException {
		return table.connector.getHttpdBinds().getHttpdBinds(this);
	}

	public List<HttpdSite> getHttpdSites() throws IOException, SQLException {
		return table.connector.getHttpdSites().getHttpdSites(this);
	}

	public List<HttpdWorker> getHttpdWorkers() throws IOException, SQLException {
		return table.connector.getHttpdWorkers().getHttpdWorkers(this);
	}

	public int getMaxBinds() {
		return max_binds;
	}

	public LinuxServerAccount getLinuxServerAccount() throws SQLException, IOException {
		LinuxServerAccount lsa=table.connector.getLinuxServerAccounts().get(linux_server_account);
		if(lsa==null) throw new SQLException("Unable to find LinuxServerAccount: "+linux_server_account);
		return lsa;
	}

	public LinuxServerGroup getLinuxServerGroup() throws SQLException, IOException {
		LinuxServerGroup lsg=table.connector.getLinuxServerGroups().get(linux_server_group);
		if(lsg==null) throw new SQLException("Unable to find LinuxServerGroup: "+linux_server_group);
		return lsg;
	}

	public TechnologyVersion getModPhpVersion() throws SQLException, IOException {
		if(mod_php_version==-1) return null;
		TechnologyVersion tv=table.connector.getTechnologyVersions().get(mod_php_version);
		if(tv==null) throw new SQLException("Unable to find TechnologyVersion: "+mod_php_version);
		if(
			tv.getOperatingSystemVersion(table.connector).getPkey()
			!= getAOServer().getServer().operating_system_version
		) {
			throw new SQLException("mod_php/operating system version mismatch on HttpdServer: #"+pkey);
		}
		return tv;
	}

	public boolean useSuexec() {
		return use_suexec;
	}

	public Package getPackage() throws IOException, SQLException {
		// Package may be filtered
		return table.connector.getPackages().get(packageNum);
	}

	public boolean isShared() {
		return is_shared;
	}

	public boolean useModPERL() {
		return use_mod_perl;
	}

	/**
	 * Gets the timeout value in seconds.
	 */
	public int getTimeOut() {
		return timeout;
	}

	/**
	 * Gets the maximum concurrency of this server (number of children processes/threads).
	 */
	public int getMaxConcurrency() {
		return max_concurrency;
	}

	public int getNumber() {
		return number;
	}

	public AOServer getAOServer() throws SQLException, IOException {
		AOServer obj=table.connector.getAoServers().get(ao_server);
		if(obj==null) throw new SQLException("Unable to find AOServer: "+ao_server);
		return obj;
	}

	public Boolean getModAccessCompat() {
		return mod_access_compat;
	}

	public Boolean getModActions() {
		return mod_actions;
	}

	public Boolean getModAlias() {
		return mod_alias;
	}

	public Boolean getModAuthBasic() {
		return mod_auth_basic;
	}

	public Boolean getModAuthnCore() {
		return mod_authn_core;
	}

	public Boolean getModAuthnFile() {
		return mod_authn_file;
	}

	public Boolean getModAuthzCore() {
		return mod_authz_core;
	}

	public Boolean getModAuthzGroupfile() {
		return mod_authz_groupfile;
	}

	public Boolean getModAuthzHost() {
		return mod_authz_host;
	}

	public Boolean getModAuthzUser() {
		return mod_authz_user;
	}

	public Boolean getModAutoindex() {
		return mod_autoindex;
	}

	public Boolean getModDeflate() {
		return mod_deflate;
	}

	public Boolean getModDir() {
		return mod_dir;
	}

	public Boolean getModFilter() {
		return mod_filter;
	}

	public Boolean getModHeaders() {
		return mod_headers;
	}

	public Boolean getModInclude() {
		return mod_include;
	}

	public Boolean getModJk() {
		return mod_jk;
	}

	public Boolean getModLogConfig() {
		return mod_log_config;
	}

	public Boolean getModMime() {
		return mod_mime;
	}

	public Boolean getModMimeMagic() {
		return mod_mime_magic;
	}

	public Boolean getModNegotiation() {
		return mod_negotiation;
	}

	public Boolean getModProxy() {
		return mod_proxy;
	}

	public Boolean getModProxyHttp() {
		return mod_proxy_http;
	}

	public Boolean getModReqtimeout() {
		return mod_reqtimeout;
	}

	public Boolean getModRewrite() {
		return mod_rewrite;
	}

	public Boolean getModSetenvif() {
		return mod_setenvif;
	}

	public Boolean getModSocacheShmcb() {
		return mod_socache_shmcb;
	}

	public Boolean getModSsl() {
		return mod_ssl;
	}

	public Boolean getModStatus() {
		return mod_status;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.HTTPD_SERVERS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		int pos=1;
		pkey=result.getInt(pos++);
		ao_server=result.getInt(pos++);
		number=result.getInt(pos++);
		can_add_sites=result.getBoolean(pos++);
		is_mod_jk=result.getBoolean(pos++);
		max_binds=result.getInt(pos++);
		linux_server_account=result.getInt(pos++);
		linux_server_group=result.getInt(pos++);
		mod_php_version=result.getInt(pos++);
		if(result.wasNull()) mod_php_version=-1;
		use_suexec=result.getBoolean(pos++);
		packageNum=result.getInt(pos++);
		is_shared=result.getBoolean(pos++);
		use_mod_perl=result.getBoolean(pos++);
		timeout=result.getInt(pos++);
		max_concurrency=result.getInt(pos++);
		mod_access_compat=result.getBoolean(pos++);
		if(result.wasNull()) mod_access_compat = null;
		mod_actions=result.getBoolean(pos++);
		if(result.wasNull()) mod_actions = null;
		mod_alias=result.getBoolean(pos++);
		if(result.wasNull()) mod_alias = null;
		mod_auth_basic=result.getBoolean(pos++);
		if(result.wasNull()) mod_auth_basic = null;
		mod_authn_core=result.getBoolean(pos++);
		if(result.wasNull()) mod_authn_core = null;
		mod_authn_file=result.getBoolean(pos++);
		if(result.wasNull()) mod_authn_file = null;
		mod_authz_core=result.getBoolean(pos++);
		if(result.wasNull()) mod_authz_core = null;
		mod_authz_groupfile=result.getBoolean(pos++);
		if(result.wasNull()) mod_authz_groupfile = null;
		mod_authz_host=result.getBoolean(pos++);
		if(result.wasNull()) mod_authz_host = null;
		mod_authz_user=result.getBoolean(pos++);
		if(result.wasNull()) mod_authz_user = null;
		mod_autoindex=result.getBoolean(pos++);
		if(result.wasNull()) mod_autoindex = null;
		mod_deflate=result.getBoolean(pos++);
		if(result.wasNull()) mod_deflate = null;
		mod_dir=result.getBoolean(pos++);
		if(result.wasNull()) mod_dir = null;
		mod_filter=result.getBoolean(pos++);
		if(result.wasNull()) mod_filter = null;
		mod_headers=result.getBoolean(pos++);
		if(result.wasNull()) mod_headers = null;
		mod_include=result.getBoolean(pos++);
		if(result.wasNull()) mod_include = null;
		mod_jk=result.getBoolean(pos++);
		if(result.wasNull()) mod_jk = null;
		mod_log_config=result.getBoolean(pos++);
		if(result.wasNull()) mod_log_config = null;
		mod_mime=result.getBoolean(pos++);
		if(result.wasNull()) mod_mime = null;
		mod_mime_magic=result.getBoolean(pos++);
		if(result.wasNull()) mod_mime_magic = null;
		mod_negotiation=result.getBoolean(pos++);
		if(result.wasNull()) mod_negotiation = null;
		mod_proxy=result.getBoolean(pos++);
		if(result.wasNull()) mod_proxy = null;
		mod_proxy_http=result.getBoolean(pos++);
		if(result.wasNull()) mod_proxy_http = null;
		mod_reqtimeout=result.getBoolean(pos++);
		if(result.wasNull()) mod_reqtimeout = null;
		mod_rewrite=result.getBoolean(pos++);
		if(result.wasNull()) mod_rewrite = null;
		mod_setenvif=result.getBoolean(pos++);
		if(result.wasNull()) mod_setenvif = null;
		mod_socache_shmcb=result.getBoolean(pos++);
		if(result.wasNull()) mod_socache_shmcb = null;
		mod_ssl=result.getBoolean(pos++);
		if(result.wasNull()) mod_ssl = null;
		mod_status=result.getBoolean(pos++);
		if(result.wasNull()) mod_status = null;
	}

	/**
	 * @deprecated  All servers now use mod_jk, mod_jserv is no longer supported.
	 */
	@Deprecated
	public boolean isModJK() {
		return is_mod_jk;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readCompressedInt();
		ao_server=in.readCompressedInt();
		number=in.readCompressedInt();
		can_add_sites=in.readBoolean();
		is_mod_jk=in.readBoolean();
		max_binds=in.readCompressedInt();
		linux_server_account=in.readCompressedInt();
		linux_server_group=in.readCompressedInt();
		mod_php_version=in.readCompressedInt();
		use_suexec=in.readBoolean();
		packageNum=in.readCompressedInt();
		is_shared=in.readBoolean();
		use_mod_perl=in.readBoolean();
		timeout=in.readCompressedInt();
		max_concurrency=in.readCompressedInt();
		mod_access_compat = in.readNullBoolean();
		mod_actions = in.readNullBoolean();
		mod_alias = in.readNullBoolean();
		mod_auth_basic = in.readNullBoolean();
		mod_authn_core = in.readNullBoolean();
		mod_authn_file = in.readNullBoolean();
		mod_authz_core = in.readNullBoolean();
		mod_authz_groupfile = in.readNullBoolean();
		mod_authz_host = in.readNullBoolean();
		mod_authz_user = in.readNullBoolean();
		mod_autoindex = in.readNullBoolean();
		mod_deflate = in.readNullBoolean();
		mod_dir = in.readNullBoolean();
		mod_filter = in.readNullBoolean();
		mod_headers = in.readNullBoolean();
		mod_include = in.readNullBoolean();
		mod_jk = in.readNullBoolean();
		mod_log_config = in.readNullBoolean();
		mod_mime = in.readNullBoolean();
		mod_mime_magic = in.readNullBoolean();
		mod_negotiation = in.readNullBoolean();
		mod_proxy = in.readNullBoolean();
		mod_proxy_http = in.readNullBoolean();
		mod_reqtimeout = in.readNullBoolean();
		mod_rewrite = in.readNullBoolean();
		mod_setenvif = in.readNullBoolean();
		mod_socache_shmcb = in.readNullBoolean();
		mod_ssl = in.readNullBoolean();
		mod_status = in.readNullBoolean();
	}

	@Override
	String toStringImpl() {
		return "httpd"+number;
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(ao_server);
		out.writeCompressedInt(number);
		out.writeBoolean(can_add_sites);
		out.writeBoolean(is_mod_jk);
		out.writeCompressedInt(max_binds);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_102)>=0) {
			out.writeCompressedInt(linux_server_account);
			out.writeCompressedInt(linux_server_group);
			out.writeCompressedInt(mod_php_version);
			out.writeBoolean(use_suexec);
			out.writeCompressedInt(packageNum);
			if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_122)<=0) out.writeCompressedInt(-1);
			out.writeBoolean(is_shared);
		}
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_103)>=0) {
			out.writeBoolean(use_mod_perl);
		}
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_130)>=0) {
			out.writeCompressedInt(timeout);
		}
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_68)>=0) {
			out.writeCompressedInt(max_concurrency);
		}
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_81_7) >= 0) {
			out.writeNullBoolean(mod_access_compat);
			out.writeNullBoolean(mod_actions);
			out.writeNullBoolean(mod_alias);
			out.writeNullBoolean(mod_auth_basic);
			out.writeNullBoolean(mod_authn_core);
			out.writeNullBoolean(mod_authn_file);
			out.writeNullBoolean(mod_authz_core);
			out.writeNullBoolean(mod_authz_groupfile);
			out.writeNullBoolean(mod_authz_host);
			out.writeNullBoolean(mod_authz_user);
			out.writeNullBoolean(mod_autoindex);
			out.writeNullBoolean(mod_deflate);
			out.writeNullBoolean(mod_dir);
			out.writeNullBoolean(mod_filter);
			out.writeNullBoolean(mod_headers);
			out.writeNullBoolean(mod_include);
			out.writeNullBoolean(mod_jk);
			out.writeNullBoolean(mod_log_config);
			out.writeNullBoolean(mod_mime);
			out.writeNullBoolean(mod_mime_magic);
			out.writeNullBoolean(mod_negotiation);
			out.writeNullBoolean(mod_proxy);
			out.writeNullBoolean(mod_proxy_http);
			out.writeNullBoolean(mod_reqtimeout);
			out.writeNullBoolean(mod_rewrite);
			out.writeNullBoolean(mod_setenvif);
			out.writeNullBoolean(mod_socache_shmcb);
			out.writeNullBoolean(mod_ssl);
			out.writeNullBoolean(mod_status);
		}
	}
}

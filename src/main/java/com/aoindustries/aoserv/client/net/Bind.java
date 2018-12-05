/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2013, 2016, 2017, 2018  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.net;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedObjectIntegerKey;
import com.aoindustries.aoserv.client.CannotRemoveReason;
import com.aoindustries.aoserv.client.Removable;
import com.aoindustries.aoserv.client.billing.Package;
import com.aoindustries.aoserv.client.distribution.OperatingSystemVersion;
import com.aoindustries.aoserv.client.email.CyrusImapdBind;
import com.aoindustries.aoserv.client.email.CyrusImapdServer;
import com.aoindustries.aoserv.client.email.SendmailBind;
import com.aoindustries.aoserv.client.email.SendmailServer;
import com.aoindustries.aoserv.client.ftp.PrivateServer;
import com.aoindustries.aoserv.client.linux.Server;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.validator.AccountingCode;
import com.aoindustries.aoserv.client.validator.FirewalldZoneName;
import com.aoindustries.aoserv.client.web.HttpdBind;
import com.aoindustries.aoserv.client.web.HttpdServer;
import com.aoindustries.aoserv.client.web.Site;
import com.aoindustries.aoserv.client.web.tomcat.PrivateTomcatSite;
import com.aoindustries.aoserv.client.web.tomcat.SharedTomcat;
import com.aoindustries.aoserv.client.web.tomcat.SharedTomcatSite;
import com.aoindustries.aoserv.client.web.tomcat.Worker;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.net.DomainName;
import com.aoindustries.net.EmptyParameters;
import com.aoindustries.net.HttpParameters;
import com.aoindustries.net.HttpParametersMap;
import com.aoindustries.net.Port;
import com.aoindustries.net.UnmodifiableHttpParameters;
import com.aoindustries.util.IntList;
import com.aoindustries.util.WrappedException;
import com.aoindustries.validation.ValidationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * All listening network ports must be registered as a <code>NetBind</code>.  The
 * <code>NetBind</code> information is also used for internel server and external
 * network monitoring.  If either a network port is not listening that should,
 * or a network port is listening that should not, monitoring personnel are notified
 * to remove the discrepancy.
 *
 * @author  AO Industries, Inc.
 */
final public class Bind extends CachedObjectIntegerKey<Bind> implements Removable {

	static final int
		COLUMN_ID = 0,
		COLUMN_PACKAGE = 1,
		COLUMN_SERVER = 2,
		COLUMN_IP_ADDRESS = 3
	;
	public static final String COLUMN_SERVER_name = "server";
	public static final String COLUMN_IP_ADDRESS_name = "ipAddress";
	public static final String COLUMN_PORT_name = "port";

	private AccountingCode packageName;
	private int server;
	private int ipAddress;
	private Port port;
	private String app_protocol;
	private boolean monitoring_enabled;
	private String monitoring_parameters;

	// Protocol conversion
	private boolean open_firewall;

	@Override
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_ID: return pkey;
			case COLUMN_PACKAGE: return packageName;
			case COLUMN_SERVER: return server;
			case COLUMN_IP_ADDRESS: return ipAddress;
			case 4: return port;
			case 5: return app_protocol;
			case 6: return monitoring_enabled;
			case 7: return monitoring_parameters;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public int getId() {
		return pkey;
	}

	public AccountingCode getPackage_name() {
		return packageName;
	}

	public Package getPackage() throws IOException, SQLException {
		// May be filtered
		return table.getConnector().getPackages().get(packageName);
	}

	public int getServer_pkey() {
		return server;
	}

	public Host getServer() throws SQLException, IOException {
		Host obj=table.getConnector().getServers().get(server);
		if(obj==null) throw new SQLException("Unable to find Server: "+server);
		return obj;
	}

	public int getIpAddress_id() {
		return ipAddress;
	}

	public IpAddress getIpAddress() throws SQLException, IOException {
		IpAddress obj=table.getConnector().getIpAddresses().get(ipAddress);
		if(obj==null) throw new SQLException("Unable to find IPAddress: "+ipAddress);
		return obj;
	}

	public Port getPort() {
		return port;
	}

	public String getAppProtocol_protocol() {
		return app_protocol;
	}

	public AppProtocol getAppProtocol() throws SQLException, IOException {
		AppProtocol obj=table.getConnector().getProtocols().get(app_protocol);
		if(obj==null) throw new SQLException("Unable to find Protocol: "+app_protocol);
		return obj;
	}

	public boolean isMonitoringEnabled() {
		return monitoring_enabled;
	}

	/**
	 * Gets the unmodifiable map of parameters for this bind.
	 */
	public HttpParameters getMonitoringParameters() {
		String myParamString = monitoring_parameters;
		if(myParamString==null) {
			return EmptyParameters.getInstance();
		} else {
			HttpParameters params = getMonitoringParametersCache.get(myParamString);
			if(params==null) {
				params = UnmodifiableHttpParameters.wrap(decodeParameters(myParamString));
				HttpParameters previous = getMonitoringParametersCache.putIfAbsent(myParamString, params);
				if(previous!=null) params = previous;
			}
			return params;
		}
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			pkey = result.getInt(1);
			packageName = AccountingCode.valueOf(result.getString(2));
			server = result.getInt(3);
			ipAddress = result.getInt(4);
			port = Port.valueOf(
				result.getInt(5),
				com.aoindustries.net.Protocol.valueOf(result.getString(6).toUpperCase(Locale.ROOT)) // TODO: toUpperCase unnecessary in 1.81.18+ which uses matching PostgreSQL enum
			);
			app_protocol = result.getString(7);
			monitoring_enabled = result.getBoolean(8);
			monitoring_parameters = result.getString(9);
			open_firewall = result.getBoolean(10);
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		try {
			pkey = in.readCompressedInt();
			packageName = AccountingCode.valueOf(in.readUTF()).intern();
			server = in.readCompressedInt();
			ipAddress = in.readCompressedInt();
			port = Port.valueOf(
				in.readCompressedInt(),
				in.readEnum(com.aoindustries.net.Protocol.class)
			);
			app_protocol = in.readUTF().intern();
			monitoring_enabled = in.readBoolean();
			monitoring_parameters = in.readNullUTF();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(CompressedDataOutputStream out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeUTF(packageName.toString());
		out.writeCompressedInt(server);
		out.writeCompressedInt(ipAddress);
		out.writeCompressedInt(port.getPort());
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_80_0) < 0) {
			out.writeUTF(port.getProtocol().name().toLowerCase(Locale.ROOT));
		} else {
			out.writeEnum(port.getProtocol());
		}
		out.writeUTF(app_protocol);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_80_2) <= 0) {
			out.writeBoolean(open_firewall);
		}
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_0_A_104)>=0) {
			out.writeBoolean(monitoring_enabled);
		} else {
			out.writeCompressedInt(monitoring_enabled?300000:-1);
			out.writeNullUTF(null);
			out.writeNullUTF(null);
			out.writeNullUTF(null);
		}
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_58)>=0) out.writeNullUTF(monitoring_parameters);
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.NET_BINDS;
	}

	@Override
	public String toStringImpl() throws IOException, SQLException {
		return getServer().toStringImpl() + "|" + getIpAddress().toStringImpl() + "|" + getPort();
	}

	public String getDetails() throws SQLException, IOException {
		Server aoServer=getAOServerByDaemonNetBind();
		if(aoServer!=null) return "AOServDaemon";

		Server jilterServer=getAOServerByJilterNetBind();
		if(jilterServer!=null) return "AOServDaemon.JilterManager";

		com.aoindustries.aoserv.client.postgresql.Server ps=getPostgresServer();
		if(ps!=null) return "PostgreSQL version "+ps.getVersion().getTechnologyVersion(table.getConnector()).getVersion()+" in "+ps.getDataDirectory();

		CyrusImapdBind cib = getCyrusImapdBind();
		if(cib != null) {
			CyrusImapdServer ciServer = cib.getCyrusImapdServer();
			DomainName servername = cib.getServername();
			if(servername == null) servername = ciServer.getServername();
			if(servername == null || servername.equals(ciServer.getAOServer().getHostname())) {
				return "Cyrus IMAPD";
			} else {
				return "Cyrus IMAPD @ " + servername;
			}
		}

		CyrusImapdServer cis = getCyrusImapdServerBySieveNetBind();
		if(cis != null) {
			DomainName servername = cis.getServername();
			if(servername == null || servername.equals(cis.getAOServer().getHostname())) {
				return "Cyrus IMAPD";
			} else {
				return "Cyrus IMAPD @ " + servername;
			}
		}

		Worker hw=getHttpdWorker();
		if(hw!=null) {
			SharedTomcat hst=hw.getHttpdSharedTomcat();
			if(hst!=null) {
				return
					hw.getHttpdJKProtocol(table.getConnector()).getProtocol(table.getConnector()).getProtocol()
					+ " connector for Multi-Site Tomcat JVM version "
					+ hst.getHttpdTomcatVersion().getTechnologyVersion(table.getConnector()).getVersion()
					+ " in "
					+ hst.getInstallDirectory()
				;
			}
			com.aoindustries.aoserv.client.web.tomcat.Site hts = hw.getTomcatSite();
			if(hts!=null) {
				return
					hw.getHttpdJKProtocol(table.getConnector()).getProtocol(table.getConnector()).getProtocol()
					+ " connector for Single-Site Tomcat JVM version "
					+ hts.getHttpdTomcatVersion().getTechnologyVersion(table.getConnector()).getVersion()
					+ " in "
					+ hts.getHttpdSite().getInstallDirectory()
				;
			}
		}

		SharedTomcat hst=getHttpdSharedTomcatByShutdownPort();
		if(hst!=null) {
			return
				"Shutdown port for Multi-Site Tomcat JVM version "
				+ hst.getHttpdTomcatVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hst.getInstallDirectory()
			;
		}

		PrivateTomcatSite htss=getHttpdTomcatStdSiteByShutdownPort();
		if(htss!=null) {
			return
				"Shutdown port for Single-Site Tomcat JVM version "
				+ htss.getHttpdTomcatSite().getHttpdTomcatVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ htss.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		HttpdBind hb=getHttpdBind();
		if(hb!=null) {
			HttpdServer hs=hb.getHttpdServer();
			String name = hs.getName();
			OperatingSystemVersion osv = hs.getAOServer().getServer().getOperatingSystemVersion();
			int osvId = osv.getPkey();
			if(osvId == OperatingSystemVersion.CENTOS_5_I686_AND_X86_64) {
				int number = (name == null) ? 1 : Integer.parseInt(name);
				return
					"Apache HTTP Server #"
					+ number
					+ " configured in /etc/httpd/conf/httpd"
					+ number
					+ ".conf"
				;
			} else if(osvId == OperatingSystemVersion.CENTOS_7_X86_64) {
				if(name == null) {
					return "Apache HTTP Server configured in /etc/httpd/conf/httpd.conf";
				} else {
					return
						"Apache HTTP Server ("
						+ name
						+ ") configured in /etc/httpd/conf/httpd@"
						+ hs.getSystemdEscapedName()
						+ ".conf"
					;
				}
			} else {
				throw new AssertionError("Unexpected OperatingSystemVersion: " + osv);
			}
		}

		com.aoindustries.aoserv.client.web.jboss.Site hjs=getHttpdJBossSiteByJNPPort();
		if(hjs!=null) {
			return
				"JNP port for JBoss version "
				+ hjs.getHttpdJBossVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hjs.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		com.aoindustries.aoserv.client.web.jboss.Site hjbs=getHttpdJBossSiteByWebserverPort();
		if(hjbs!=null) {
			return
				"Webserver port for JBoss version "
				+ hjbs.getHttpdJBossVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hjbs.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		hjbs=getHttpdJBossSiteByRMIPort();
		if(hjbs!=null) {
			return
				"RMI port for JBoss version "
				+ hjbs.getHttpdJBossVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hjbs.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		hjbs=getHttpdJBossSiteByHypersonicPort();
		if(hjbs!=null) {
			return
				"Hypersonic port for JBoss version "
				+ hjbs.getHttpdJBossVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hjbs.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		hjbs=getHttpdJBossSiteByJMXPort();
		if(hjbs!=null) {
			return
				"JMX port for JBoss version "
				+ hjbs.getHttpdJBossVersion().getTechnologyVersion(table.getConnector()).getVersion()
				+ " in "
				+ hjbs.getHttpdTomcatSite().getHttpdSite().getInstallDirectory()
			;
		}

		SendmailBind sb = getSendmailBind();
		if(sb != null) {
			SendmailServer ss = sb.getSendmailServer();
			DomainName hostname = ss.getHostname();
			if(hostname == null || hostname.equals(ss.getAoServer().getHostname())) {
				String name = ss.getName();
				if(name == null) {
					return "Sendmail";
				} else {
					return "Sendmail (" + name + ')';
				}
			} else {
				return "Sendmail @ " + hostname;
			}
		}

		TcpRedirect ntr=getNetTcpRedirect();
		if(ntr!=null) return "Port redirected to "+ntr.getDestinationHost().toBracketedString()+':'+ntr.getDestinationPort().getPort();

		PrivateServer pfs=getPrivateFTPServer();
		if(pfs!=null) return "Private FTP server in "+pfs.getLinuxServerAccount().getHome();

		return null;
	}

	/**
	 * A net_bind is disabled when all Disablable uses of it are disabled.
	 * If there are no Disablable uses, it is considered enabled.
	 * 
	 * @see  Disablable
	 */
	public boolean isDisabled() throws SQLException, IOException {
		boolean foundDisablable = false;
		Worker hw = getHttpdWorker();
		if(hw != null) {
			SharedTomcat hst = hw.getHttpdSharedTomcat();
			if(hst != null) {
				// Must also have at least one enabled site
				boolean hasEnabledSite = false;
				for(SharedTomcatSite htss : hst.getHttpdTomcatSharedSites()) {
					if(!htss.getHttpdTomcatSite().getHttpdSite().isDisabled()) {
						hasEnabledSite = true;
						break;
					}
				}
				if(!hst.isDisabled() && hasEnabledSite) return false;
				foundDisablable = true;
			}
			com.aoindustries.aoserv.client.web.tomcat.Site hts = hw.getTomcatSite();
			if(hts != null) {
				if(!hts.getHttpdSite().isDisabled()) return false;
				foundDisablable = true;
			}
		}

		SharedTomcat hst = getHttpdSharedTomcatByShutdownPort();
		if(hst != null) {
			// Must also have at least one enabled site
			boolean hasEnabledSite = false;
			for(SharedTomcatSite htss : hst.getHttpdTomcatSharedSites()) {
				if(!htss.getHttpdTomcatSite().getHttpdSite().isDisabled()) {
					hasEnabledSite = true;
					break;
				}
			}
			if(!hst.isDisabled() && hasEnabledSite) return false;
			foundDisablable = true;
		}

		PrivateTomcatSite htss=getHttpdTomcatStdSiteByShutdownPort();
		if(htss!=null) {
			if(!htss.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		com.aoindustries.aoserv.client.web.jboss.Site hjbs=getHttpdJBossSiteByJNPPort();
		if(hjbs!=null) {
			if(!hjbs.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		hjbs=getHttpdJBossSiteByWebserverPort();
		if(hjbs!=null) {
			if(!hjbs.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		hjbs=getHttpdJBossSiteByRMIPort();
		if(hjbs!=null) {
			if(!hjbs.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		hjbs=getHttpdJBossSiteByHypersonicPort();
		if(hjbs!=null) {
			if(!hjbs.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		hjbs=getHttpdJBossSiteByJMXPort();
		if(hjbs!=null) {
			if(!hjbs.getHttpdTomcatSite().getHttpdSite().isDisabled()) return false;
			foundDisablable = true;
		}

		return foundDisablable;
	}

	public Server getAOServerByDaemonNetBind() throws IOException, SQLException {
		return table.getConnector().getAoServers().getAOServerByDaemonNetBind(this);
	}

	public Server getAOServerByJilterNetBind() throws IOException, SQLException {
		return table.getConnector().getAoServers().getAOServerByJilterNetBind(this);
	}

	public CyrusImapdBind getCyrusImapdBind() throws IOException, SQLException {
		return table.getConnector().getCyrusImapdBinds().get(pkey);
	}

	public CyrusImapdServer getCyrusImapdServerBySieveNetBind() throws IOException, SQLException {
		return table.getConnector().getCyrusImapdServers().getCyrusImapdServerBySieveNetBind(this);
	}

	public HttpdBind getHttpdBind() throws IOException, SQLException {
		return table.getConnector().getHttpdBinds().get(pkey);
	}

	public com.aoindustries.aoserv.client.web.jboss.Site getHttpdJBossSiteByJNPPort() throws IOException, SQLException {
		return table.getConnector().getHttpdJBossSites().getHttpdJBossSiteByJNPPort(this);
	}

	public com.aoindustries.aoserv.client.web.jboss.Site getHttpdJBossSiteByWebserverPort() throws IOException, SQLException {
		return table.getConnector().getHttpdJBossSites().getHttpdJBossSiteByWebserverPort(this);
	}

	public com.aoindustries.aoserv.client.web.jboss.Site getHttpdJBossSiteByRMIPort() throws IOException, SQLException {
		return table.getConnector().getHttpdJBossSites().getHttpdJBossSiteByRMIPort(this);
	}

	public com.aoindustries.aoserv.client.web.jboss.Site getHttpdJBossSiteByHypersonicPort() throws IOException, SQLException {
		return table.getConnector().getHttpdJBossSites().getHttpdJBossSiteByHypersonicPort(this);
	}

	public com.aoindustries.aoserv.client.web.jboss.Site getHttpdJBossSiteByJMXPort() throws IOException, SQLException {
		return table.getConnector().getHttpdJBossSites().getHttpdJBossSiteByJMXPort(this);
	}

	public Worker getHttpdWorker() throws IOException, SQLException {
		return table.getConnector().getHttpdWorkers().getHttpdWorker(this);
	}

	public SharedTomcat getHttpdSharedTomcatByShutdownPort() throws SQLException, IOException {
		return table.getConnector().getHttpdSharedTomcats().getHttpdSharedTomcatByShutdownPort(this);
	}

	public PrivateTomcatSite getHttpdTomcatStdSiteByShutdownPort() throws IOException, SQLException {
		return table.getConnector().getHttpdTomcatStdSites().getHttpdTomcatStdSiteByShutdownPort(this);
	}

	public SendmailBind getSendmailBind() throws IOException, SQLException {
		return table.getConnector().getSendmailBinds().get(pkey);
	}

	public List<BindFirewallZone> getNetBindFirewalldZones() throws IOException, SQLException {
		return table.getConnector().getNetBindFirewalldZones().getNetBindFirewalldZones(this);
	}

	public List<FirewallZone> getFirewalldZones() throws IOException, SQLException {
		List<BindFirewallZone> nbfzs = getNetBindFirewalldZones();
		List<FirewallZone> fzs = new ArrayList<>(nbfzs.size());
		for(BindFirewallZone nbfz : nbfzs) {
			fzs.add(nbfz.getFirewalldZone());
		}
		return fzs;
	}

	public Set<FirewalldZoneName> getFirewalldZoneNames() throws IOException, SQLException {
		List<BindFirewallZone> nbfzs = getNetBindFirewalldZones();
		Set<FirewalldZoneName> fzns = new LinkedHashSet<>(nbfzs.size()*4/3+1);
		for(BindFirewallZone nbfz : nbfzs) {
			fzns.add(nbfz.getFirewalldZone().getName());
		}
		return fzns;
	}

	public TcpRedirect getNetTcpRedirect() throws IOException, SQLException {
		return table.getConnector().getNetTcpRedirects().get(pkey);
	}

	public com.aoindustries.aoserv.client.mysql.Server getMySQLServer() throws IOException, SQLException {
		return table.getConnector().getMysqlServers().getMySQLServer(this);
	}

	public com.aoindustries.aoserv.client.postgresql.Server getPostgresServer() throws IOException, SQLException {
		return table.getConnector().getPostgresServers().getPostgresServer(this);
	}

	public PrivateServer getPrivateFTPServer() throws IOException, SQLException {
		return table.getConnector().getPrivateFTPServers().get(pkey);
	}

	/**
	 * Encodes the parameters in UTF-8.  Will not return null.
	 */
	public static String encodeParameters(HttpParameters monitoringParameters) {
		try {
			StringBuilder SB = new StringBuilder();
			for(Map.Entry<String,List<String>> entry : monitoringParameters.getParameterMap().entrySet()) {
				String name = entry.getKey();
				for(String value : entry.getValue()) {
					if(SB.length()>0) SB.append('&');
					SB.append(URLEncoder.encode(name, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
				}
			}
			return SB.toString();
		} catch(UnsupportedEncodingException err) {
			throw new WrappedException(err);
		}
	}

	/**
	 * Decodes the parameters in UTF-8.
	 */
	public static HttpParameters decodeParameters(String monitoringParameters) {
		if(monitoringParameters==null) {
			return EmptyParameters.getInstance();
		} else {
			try {
				return new HttpParametersMap(monitoringParameters, "UTF-8");
			} catch(UnsupportedEncodingException e) {
				throw new AssertionError("UTF-8 should existing on all platforms", e);
			}
			/*
			try {
				List<String> nameValues = StringUtility.splitString(monitoringParameters, '&');
				Map<String,String> newMap = new HashMap<String,String>(nameValues.length*4/3+1);
				for(String nameValue : nameValues) {
					String name;
					String value;
					int pos = nameValue.indexOf('=');
					if(pos==-1) {
						name = URLDecoder.decode(nameValue, "UTF-8");
						value = "";
					} else {
						name = URLDecoder.decode(nameValue.substring(0, pos), "UTF-8");
						value = URLDecoder.decode(nameValue.substring(pos+1), "UTF-8");
					}
					if(name.length()>0 || value.length()>0) newMap.put(name, value);
				}
				return newMap;
			} catch(UnsupportedEncodingException err) {
				throw new WrappedException(err);
			}*/
		}
	}

	private static final ConcurrentMap<String,HttpParameters> getMonitoringParametersCache = new ConcurrentHashMap<>();

	@Override
	public List<CannotRemoveReason<?>> getCannotRemoveReasons() throws IOException, SQLException {
		List<CannotRemoveReason<?>> reasons=new ArrayList<>();

		AOServConnector conn=table.getConnector();

		// Must be able to access package
		if(getPackage()==null) reasons.add(new CannotRemoveReason<Package>("Unable to access package: "+packageName));

		// ao_servers
		for(Server ao : conn.getAoServers().getRows()) {
			if(
				pkey == ao.getDaemonBind_id()
				|| pkey == ao.getDaemonConnectBind_id()
			) reasons.add(new CannotRemoveReason<>("Used as aoserv-daemon port for server: "+ao.getHostname(), ao));
			if(pkey == ao.getJilterBind_id()) reasons.add(new CannotRemoveReason<>("Used as aoserv-daemon jilter port for server: "+ao.getHostname(), ao));
		}

		// httpd_binds
		for(HttpdBind hb : conn.getHttpdBinds().getRows()) {
			if(equals(hb.getNetBind())) {
				HttpdServer hs=hb.getHttpdServer();
				String name = hs.getName();
				reasons.add(
					new CannotRemoveReason<>(
						name==null
							? "Used by Apache HTTP Server on " + hs.getAOServer().getHostname()
							: "Used by Apache HTTP Server (" + name + ") on " + hs.getAOServer().getHostname(),
						hb
					)
				);
			}
		}

		// httpd_jboss_sites
		for(com.aoindustries.aoserv.client.web.jboss.Site hjb : conn.getHttpdJBossSites().getRows()) {
			Site hs=hjb.getHttpdTomcatSite().getHttpdSite();
			if(equals(hjb.getJnpBind())) reasons.add(new CannotRemoveReason<>("Used as JNP port for JBoss site "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hjb));
			if(equals(hjb.getWebserverBind())) reasons.add(new CannotRemoveReason<>("Used as Webserver port for JBoss site "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hjb));
			if(equals(hjb.getRmiBind())) reasons.add(new CannotRemoveReason<>("Used as RMI port for JBoss site "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hjb));
			if(equals(hjb.getHypersonicBind())) reasons.add(new CannotRemoveReason<>("Used as Hypersonic port for JBoss site "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hjb));
			if(equals(hjb.getJmxBind())) reasons.add(new CannotRemoveReason<>("Used as JMX port for JBoss site "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hjb));
		}

		// httpd_shared_tomcats
		for(SharedTomcat hst : conn.getHttpdSharedTomcats().getRows()) {
			if(equals(hst.getTomcat4ShutdownPort())) reasons.add(new CannotRemoveReason<>("Used as shutdown port for Multi-Site Tomcat JVM "+hst.getInstallDirectory()+" on "+hst.getAOServer().getHostname(), hst));
		}

		// httpd_tomcat_std_sites
		for(PrivateTomcatSite hts : conn.getHttpdTomcatStdSites().getRows()) {
			Site hs=hts.getHttpdTomcatSite().getHttpdSite();
			if(equals(hts.getTomcat4ShutdownPort())) reasons.add(new CannotRemoveReason<>("Used as shutdown port for Single-Site Tomcat JVM "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hts));
		}

		// httpd_workers
		for(Worker hw : conn.getHttpdWorkers().getRows()) {
			if(equals(hw.getBind())) {
				SharedTomcat hst=hw.getHttpdSharedTomcat();
				if(hst!=null) reasons.add(new CannotRemoveReason<>("Used as mod_jk worker for Multi-Site Tomcat JVM "+hst.getInstallDirectory()+" on "+hst.getAOServer().getHostname(), hst));

				com.aoindustries.aoserv.client.web.tomcat.Site hts=hw.getTomcatSite();
				if(hts!=null) {
					Site hs=hts.getHttpdSite();
					reasons.add(new CannotRemoveReason<>("Used as mod_jk worker for Tomcat JVM "+hs.getInstallDirectory()+" on "+hs.getAoServer().getHostname(), hts));
				}
			}
		}

		// mysql_servers
		com.aoindustries.aoserv.client.mysql.Server ms = getMySQLServer();
		if(ms != null) reasons.add(new CannotRemoveReason<>("Used for MySQL server "+ms.getName()+" on "+ms.getAoServer().getHostname(), ms));

		// postgres_servers
		com.aoindustries.aoserv.client.postgresql.Server ps = getPostgresServer();
		if(ps != null) reasons.add(new CannotRemoveReason<>("Used for PostgreSQL server "+ps.getName()+" on "+ps.getAoServer().getHostname(), ps));

		return reasons;
	}

	@Override
	public void remove() throws IOException, SQLException {
		table.getConnector().requestUpdateIL(true,
			AoservProtocol.CommandID.REMOVE,
			Table.TableID.NET_BINDS,
			pkey
		);
	}

	public void setMonitoringEnabled(boolean monitoring_enabled) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(true,
			AoservProtocol.CommandID.SET_NET_BIND_MONITORING,
			pkey,
			monitoring_enabled
		);
	}

	public void setFirewalldZones(final Set<FirewalldZoneName> firewalldZones) throws IOException, SQLException {
		table.getConnector().requestUpdate(true,
			AoservProtocol.CommandID.SET_NET_BIND_FIREWALLD_ZONES,
			new AOServConnector.UpdateRequest() {
				IntList invalidateList;

				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(pkey);
					int size = firewalldZones.size();
					out.writeCompressedInt(size);
					int count = 0;
					for(FirewalldZoneName firewalldZone : firewalldZones) {
						out.writeUTF(firewalldZone.toString());
						count++;
					}
					if(size != count) throw new ConcurrentModificationException();
				}

				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code = in.readByte();
					if(code == AoservProtocol.DONE) {
						invalidateList = AOServConnector.readInvalidateList(in);
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: " + code);
					}
				}

				@Override
				public void afterRelease() {
					table.getConnector().tablesUpdated(invalidateList);
				}
			}
		);
	}
}
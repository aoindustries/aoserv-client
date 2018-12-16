/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2013, 2015, 2016, 2017, 2018  AO Industries, Inc.
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

import com.aoindustries.aoserv.client.account.Administrator;
import com.aoindustries.aoserv.client.aosh.AOSH;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.validator.AccountingCode;
import com.aoindustries.aoserv.client.validator.Gecos;
import com.aoindustries.aoserv.client.validator.GroupId;
import com.aoindustries.aoserv.client.validator.HashedPassword;
import com.aoindustries.aoserv.client.validator.LinuxId;
import com.aoindustries.aoserv.client.validator.MySQLDatabaseName;
import com.aoindustries.aoserv.client.validator.MySQLServerName;
import com.aoindustries.aoserv.client.validator.MySQLTableName;
import com.aoindustries.aoserv.client.validator.MySQLUserId;
import com.aoindustries.aoserv.client.validator.PostgresDatabaseName;
import com.aoindustries.aoserv.client.validator.PostgresServerName;
import com.aoindustries.aoserv.client.validator.PostgresUserId;
import com.aoindustries.aoserv.client.validator.UnixPath;
import com.aoindustries.aoserv.client.validator.UserId;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.io.CompressedWritable;
import com.aoindustries.net.DomainLabel;
import com.aoindustries.net.DomainLabels;
import com.aoindustries.net.DomainName;
import com.aoindustries.net.Email;
import com.aoindustries.net.HostAddress;
import com.aoindustries.net.InetAddress;
import com.aoindustries.net.MacAddress;
import com.aoindustries.net.Port;
import com.aoindustries.table.TableListener;
import com.aoindustries.util.IntArrayList;
import com.aoindustries.util.IntList;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An <code>AOServConnector</code> provides the connection between the object
 * layer and the data.  This connection may be persistent over TCP sockets, or
 * it may be request-based like HTTP.
 *
 * @author  AO Industries, Inc.
 */
abstract public class AOServConnector {

	/**
	 * The maximum size of the master entropy pool in bytes.
	 */
	public static final long MASTER_ENTROPY_POOL_SIZE=(long)64*1024*1024;

	/**
	 * The delay for each retry attempt.
	 */
	private static final long[] retryAttemptDelays = {
		0,
		1,
		2,
		3,
		4,
		6,
		8,
		12,
		16,
		24,
		32,
		48,
		64,
		96,
		128,
		192,
		256,
		384,
		512,
		768,
		1024,
		1536,
		2048,
		3072
	};

	/**
	 * The number of attempts that will be made when request retry is allowed.
	 */
	private static final int RETRY_ATTEMPTS = retryAttemptDelays.length + 1;

	/**
	 * Certain errors will not be retried.
	 */
	static boolean isImmediateFail(Throwable T) {
		String message = T.getMessage();
		return
			(
				(T instanceof IOException)
				&& message!=null
				&& (
					message.equals("Connection attempted with invalid password")
					|| message.equals("Connection attempted with empty password")
					|| message.equals("Connection attempted with empty connect username")
					|| message.startsWith("Unable to find BusinessAdministrator: ")
					|| message.startsWith("Not allowed to switch users from ")
				)
			)
		;
	}

	/**
	 * One thread pool is shared by all instances.
	 */
	final static ExecutorService executorService = Executors.newCachedThreadPool();

	/*private static final String[] profileTitles={
		"Method",
		"Parameter",
		"Use Count",
		"Total Time",
		"Min Time",
		"Avg Time",
		"Max Time"
	};*/

	/**
	 * @see  #getConnectorID()
	 */
	private static class IdLock {}
	final IdLock idLock = new IdLock();
	long id=-1; // Rename back to id

	/**
	 * @see  #getHostname()
	 */
	final HostAddress hostname;

	/**
	 * @see  #getLocalIp()
	 */
	final InetAddress local_ip;

	/**
	 * @see  #getPort()
	 */
	final Port port;

	/**
	 * @see  #getConnectedAs()
	 */
	final UserId connectAs;

	/**
	 * @see  #getAuthenticatedAs()
	 */
	final UserId authenticateAs;

	final DomainName daemonServer;

	final Logger logger;
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Gets the logger for this connector.
	 */
	//public Logger getLogger() {
	//    return logger;
	//}

	protected final String password;

	private static class TestConnectLock {}
	private final TestConnectLock testConnectLock=new TestConnectLock();

	private final com.aoindustries.aoserv.client.account.Schema account;
	public com.aoindustries.aoserv.client.account.Schema getAccount() {return account;}

	private final com.aoindustries.aoserv.client.accounting.Schema accounting;
	public com.aoindustries.aoserv.client.accounting.Schema getAccounting() {return accounting;}

	private final com.aoindustries.aoserv.client.aosh.Schema aosh;
	public com.aoindustries.aoserv.client.aosh.Schema getAosh() {return aosh;}

	private final com.aoindustries.aoserv.client.backup.Schema backup;
	public com.aoindustries.aoserv.client.backup.Schema getBackup() {return backup;}

	private final com.aoindustries.aoserv.client.billing.Schema billing;
	public com.aoindustries.aoserv.client.billing.Schema getBilling() {return billing;}

	private final com.aoindustries.aoserv.client.distribution.Schema distribution;
	public com.aoindustries.aoserv.client.distribution.Schema getDistribution() {return distribution;}

	private final com.aoindustries.aoserv.client.distribution.management.Schema distribution_management;
	public com.aoindustries.aoserv.client.distribution.management.Schema getDistribution_management() {return distribution_management;}

	private final com.aoindustries.aoserv.client.dns.Schema dns;
	public com.aoindustries.aoserv.client.dns.Schema getDns() {return dns;}

	private final com.aoindustries.aoserv.client.email.Schema email;
	public com.aoindustries.aoserv.client.email.Schema getEmail() {return email;}

	private final com.aoindustries.aoserv.client.ftp.Schema ftp;
	public com.aoindustries.aoserv.client.ftp.Schema getFtp() {return ftp;}

	private final com.aoindustries.aoserv.client.infrastructure.Schema infrastructure;
	public com.aoindustries.aoserv.client.infrastructure.Schema getInfrastructure() {return infrastructure;}

	private final com.aoindustries.aoserv.client.linux.Schema linux;
	public com.aoindustries.aoserv.client.linux.Schema getLinux() {return linux;}

	private final com.aoindustries.aoserv.client.master.Schema master;
	public com.aoindustries.aoserv.client.master.Schema getMaster() {return master;}

	private final com.aoindustries.aoserv.client.mysql.Schema mysql;
	public com.aoindustries.aoserv.client.mysql.Schema getMysql() {return mysql;}

	private final com.aoindustries.aoserv.client.net.Schema net;
	public com.aoindustries.aoserv.client.net.Schema getNet() {return net;}

	private final com.aoindustries.aoserv.client.net.monitoring.Schema net_monitoring;
	public com.aoindustries.aoserv.client.net.monitoring.Schema getNet_monitoring() {return net_monitoring;}

	private final com.aoindustries.aoserv.client.net.reputation.Schema net_reputation;
	public com.aoindustries.aoserv.client.net.reputation.Schema getNet_reputation() {return net_reputation;}

	private final com.aoindustries.aoserv.client.payment.Schema payment;
	public com.aoindustries.aoserv.client.payment.Schema getPayment() {return payment;}

	private final com.aoindustries.aoserv.client.pki.Schema pki;
	public com.aoindustries.aoserv.client.pki.Schema getPki() {return pki;}

	private final com.aoindustries.aoserv.client.postgresql.Schema postgresql;
	public com.aoindustries.aoserv.client.postgresql.Schema getPostgresql() {return postgresql;}

	private final com.aoindustries.aoserv.client.reseller.Schema reseller;
	public com.aoindustries.aoserv.client.reseller.Schema getReseller() {return reseller;}

	private final com.aoindustries.aoserv.client.schema.Schema schema;
	public com.aoindustries.aoserv.client.schema.Schema getSchema() {return schema;}

	private final com.aoindustries.aoserv.client.scm.Schema scm;
	public com.aoindustries.aoserv.client.scm.Schema getScm() {return scm;}

	private final com.aoindustries.aoserv.client.signup.Schema signup;
	public com.aoindustries.aoserv.client.signup.Schema getSignup() {return signup;}

	private final com.aoindustries.aoserv.client.ticket.Schema ticket;
	public com.aoindustries.aoserv.client.ticket.Schema getTicket() {return ticket;}

	private final com.aoindustries.aoserv.client.web.Schema web;
	public com.aoindustries.aoserv.client.web.Schema getWeb() {return web;}

	private final com.aoindustries.aoserv.client.web.jboss.Schema web_jboss;
	public com.aoindustries.aoserv.client.web.jboss.Schema getWeb_jboss() {return web_jboss;}

	private final com.aoindustries.aoserv.client.web.tomcat.Schema web_tomcat;
	public com.aoindustries.aoserv.client.web.tomcat.Schema getWeb_tomcat() {return web_tomcat;}

	final List<Schema> schemas;

	/**
	 * The tables are placed in this list in the constructor.
	 * This list is aligned with the table identifiers in
	 * <code>SchemaTable</code>.
	 *
	 * @see  Table
	 */
	final List<AOServTable> tables;

	private final SimpleAOClient simpleAOClient;
	public SimpleAOClient getSimpleAOClient() {return simpleAOClient;}

	protected AOServConnector(
		HostAddress hostname,
		InetAddress local_ip,
		Port port,
		UserId connectAs,
		UserId authenticateAs,
		String password,
		DomainName daemonServer,
		Logger logger
	) throws IOException {
		this.hostname = hostname;
		this.local_ip = local_ip;
		this.port = port;
		this.connectAs = connectAs;
		this.authenticateAs = authenticateAs;
		this.password = password;
		this.daemonServer = daemonServer;
		this.logger = logger;

		// TODO: Load schemas with ServiceLoader
		ArrayList<Schema> newSchemas = new ArrayList<>();
		newSchemas.add(account = new com.aoindustries.aoserv.client.account.Schema(this));
		newSchemas.add(accounting = new com.aoindustries.aoserv.client.accounting.Schema(this));
		newSchemas.add(aosh = new com.aoindustries.aoserv.client.aosh.Schema(this));
		newSchemas.add(backup = new com.aoindustries.aoserv.client.backup.Schema(this));
		newSchemas.add(billing = new com.aoindustries.aoserv.client.billing.Schema(this));
		newSchemas.add(distribution = new com.aoindustries.aoserv.client.distribution.Schema(this));
		newSchemas.add(distribution_management = new com.aoindustries.aoserv.client.distribution.management.Schema(this));
		newSchemas.add(dns = new com.aoindustries.aoserv.client.dns.Schema(this));
		newSchemas.add(email = new com.aoindustries.aoserv.client.email.Schema(this));
		newSchemas.add(ftp = new com.aoindustries.aoserv.client.ftp.Schema(this));
		newSchemas.add(infrastructure = new com.aoindustries.aoserv.client.infrastructure.Schema(this));
		newSchemas.add(linux = new com.aoindustries.aoserv.client.linux.Schema(this));
		newSchemas.add(master = new com.aoindustries.aoserv.client.master.Schema(this));
		newSchemas.add(mysql = new com.aoindustries.aoserv.client.mysql.Schema(this));
		newSchemas.add(net = new com.aoindustries.aoserv.client.net.Schema(this));
		newSchemas.add(net_monitoring = new com.aoindustries.aoserv.client.net.monitoring.Schema(this));
		newSchemas.add(net_reputation = new com.aoindustries.aoserv.client.net.reputation.Schema(this));
		newSchemas.add(payment = new com.aoindustries.aoserv.client.payment.Schema(this));
		newSchemas.add(pki = new com.aoindustries.aoserv.client.pki.Schema(this));
		newSchemas.add(postgresql = new com.aoindustries.aoserv.client.postgresql.Schema(this));
		newSchemas.add(reseller = new com.aoindustries.aoserv.client.reseller.Schema(this));
		newSchemas.add(schema = new com.aoindustries.aoserv.client.schema.Schema(this));
		newSchemas.add(scm = new com.aoindustries.aoserv.client.scm.Schema(this));
		newSchemas.add(signup = new com.aoindustries.aoserv.client.signup.Schema(this));
		newSchemas.add(ticket = new com.aoindustries.aoserv.client.ticket.Schema(this));
		newSchemas.add(web = new com.aoindustries.aoserv.client.web.Schema(this));
		newSchemas.add(web_jboss = new com.aoindustries.aoserv.client.web.jboss.Schema(this));
		newSchemas.add(web_tomcat = new com.aoindustries.aoserv.client.web.tomcat.Schema(this));
		newSchemas.trimToSize();
		schemas = Collections.unmodifiableList(newSchemas);

		// These must match the table IDs in SchemaTable
		ArrayList<AOServTable> newTables = new ArrayList<>();
		newTables.add(linux.getAoServerDaemonHosts());
		newTables.add(linux.getAoServers());
		newTables.add(master.getAoservPermissions());
		newTables.add(schema.getAoservProtocols());
		newTables.add(aosh.getAoshCommands());
		newTables.add(distribution.getArchitectures());
		newTables.add(backup.getBackupPartitions());
		newTables.add(backup.getBackupReports());
		newTables.add(backup.getBackupRetentions());
		newTables.add(accounting.getBankAccounts());
		newTables.add(accounting.getBankTransactionTypes());
		newTables.add(accounting.getBankTransactions());
		newTables.add(accounting.getBanks());
		newTables.add(email.getBlackholeEmailAddresses());
		newTables.add(reseller.getBrands());
		newTables.add(account.getBusinessAdministrators());
		newTables.add(master.getBusinessAdministratorPermissions());
		newTables.add(account.getBusinessProfiles());
		newTables.add(account.getBusinesses());
		newTables.add(account.getBusinessServers());
		newTables.add(payment.getCountryCodes());
		newTables.add(payment.getCreditCardProcessors());
		newTables.add(payment.getCreditCardTransactions());
		newTables.add(payment.getCreditCards());
		newTables.add(scm.getCvsRepositories());
		newTables.add(email.getCyrusImapdBinds());
		newTables.add(email.getCyrusImapdServers());
		newTables.add(account.getDisableLogs());
		newTables.add(distribution_management.getDistroFileTypes());
		newTables.add(distribution_management.getDistroFiles());
		newTables.add(distribution_management.getDistroReportTypes());
		newTables.add(dns.getDnsForbiddenZones());
		newTables.add(dns.getDnsRecords());
		newTables.add(dns.getDnsTLDs());
		newTables.add(dns.getDnsTypes());
		newTables.add(dns.getDnsZones());
		newTables.add(email.getEmailAddresses());
		newTables.add(email.getEmailAttachmentBlocks());
		newTables.add(email.getEmailAttachmentTypes());
		newTables.add(email.getEmailDomains());
		newTables.add(email.getEmailForwardings());
		newTables.add(email.getEmailListAddresses());
		newTables.add(email.getEmailLists());
		newTables.add(email.getEmailPipeAddresses());
		newTables.add(email.getEmailPipes());
		newTables.add(email.getEmailSmtpRelayTypes());
		newTables.add(email.getEmailSmtpRelays());
		newTables.add(email.getEmailSmtpSmartHostDomains());
		newTables.add(email.getEmailSmtpSmartHosts());
		newTables.add(email.getEmailSpamAssassinIntegrationModes());
		newTables.add(pki.getEncryptionKeys());
		newTables.add(accounting.getExpenseCategories());
		newTables.add(backup.getFailoverFileLogs());
		newTables.add(backup.getFailoverFileReplications());
		newTables.add(backup.getFailoverFileSchedules());
		newTables.add(backup.getFailoverMySQLReplications());
		newTables.add(backup.getFileBackupSettings());
		newTables.add(net.getFirewalldZones());
		newTables.add(ftp.getFtpGuestUsers());
		newTables.add(web.getHttpdBinds());
		newTables.add(web_jboss.getHttpdJBossSites());
		newTables.add(web_jboss.getHttpdJBossVersions());
		newTables.add(web_tomcat.getHttpdJKCodes());
		newTables.add(web_tomcat.getHttpdJKProtocols());
		newTables.add(web.getHttpdServers());
		newTables.add(web_tomcat.getHttpdSharedTomcats());
		newTables.add(web.getHttpdSiteAuthenticatedLocationTable());
		newTables.add(web.getHttpdSiteBindHeaders());
		newTables.add(web.getRewriteRuleTable());
		newTables.add(web.getHttpdSiteBinds());
		newTables.add(web.getHttpdSiteURLs());
		newTables.add(web.getHttpdSites());
		newTables.add(web.getHttpdStaticSites());
		newTables.add(web_tomcat.getHttpdTomcatContexts());
		newTables.add(web_tomcat.getHttpdTomcatDataSources());
		newTables.add(web_tomcat.getHttpdTomcatParameters());
		newTables.add(web_tomcat.getHttpdTomcatSiteJkMounts());
		newTables.add(web_tomcat.getHttpdTomcatSites());
		newTables.add(web_tomcat.getHttpdTomcatSharedSites());
		newTables.add(web_tomcat.getHttpdTomcatStdSites());
		newTables.add(web_tomcat.getHttpdTomcatVersions());
		newTables.add(web_tomcat.getHttpdWorkers());
		newTables.add(net.getIpAddresses());
		newTables.add(net_monitoring.getIpAddressMonitoring());
		newTables.add(net_reputation.getIpReputationLimiterLimits());
		newTables.add(net_reputation.getIpReputationLimiterSets());
		newTables.add(net_reputation.getIpReputationLimiters());
		newTables.add(net_reputation.getIpReputationSetHosts());
		newTables.add(net_reputation.getIpReputationSetNetworks());
		newTables.add(net_reputation.getIpReputationSets());
		newTables.add(ticket.getLanguages());
		newTables.add(email.getLinuxAccAddresses());
		newTables.add(linux.getLinuxAccountTypes());
		newTables.add(linux.getLinuxAccounts());
		newTables.add(linux.getLinuxGroupAccounts());
		newTables.add(linux.getLinuxGroupTypes());
		newTables.add(linux.getLinuxGroups());
		newTables.add(linux.getLinuxServerAccounts());
		newTables.add(linux.getLinuxServerGroups());
		newTables.add(email.getMajordomoLists());
		newTables.add(email.getMajordomoServers());
		newTables.add(email.getMajordomoVersions());
		newTables.add(master.getMasterHosts());
		newTables.add(master.getMasterProcesses());
		newTables.add(master.getMasterServerStats());
		newTables.add(master.getMasterServers());
		newTables.add(master.getMasterUsers());
		newTables.add(billing.getMonthlyCharges());
		newTables.add(mysql.getMysqlDatabases());
		newTables.add(mysql.getMysqlDBUsers());
		newTables.add(mysql.getMysqlServerUsers());
		newTables.add(mysql.getMysqlServers());
		newTables.add(mysql.getMysqlUsers());
		newTables.add(net.getNetBindFirewalldZones());
		newTables.add(net.getNetBinds());
		newTables.add(net.getNetDeviceIDs());
		newTables.add(net.getNetDevices());
		newTables.add(net.getNetTcpRedirects());
		newTables.add(billing.getNoticeLogs());
		newTables.add(billing.getNoticeTypes());
		newTables.add(distribution.getOperatingSystemVersions());
		newTables.add(distribution.getOperatingSystems());
		newTables.add(billing.getPackageCategories());
		newTables.add(billing.getPackageDefinitionLimits());
		newTables.add(billing.getPackageDefinitions());
		newTables.add(billing.getPackages());
		newTables.add(payment.getPaymentTypes());
		newTables.add(infrastructure.getPhysicalServers());
		newTables.add(postgresql.getPostgresDatabases());
		newTables.add(postgresql.getPostgresEncodings());
		newTables.add(postgresql.getPostgresServerUsers());
		newTables.add(postgresql.getPostgresServers());
		newTables.add(postgresql.getPostgresUsers());
		newTables.add(postgresql.getPostgresVersions());
		newTables.add(ftp.getPrivateFTPServers());
		newTables.add(infrastructure.getProcessorTypes());
		newTables.add(net.getProtocols());
		newTables.add(infrastructure.getRacks());
		newTables.add(reseller.getResellers());
		newTables.add(billing.getResources());
		newTables.add(schema.getSchemaColumns());
		newTables.add(schema.getSchemaForeignKeys());
		newTables.add(schema.getSchemaTables());
		newTables.add(schema.getSchemaTypes());
		newTables.add(email.getSendmailBinds());
		newTables.add(email.getSendmailServers());
		newTables.add(infrastructure.getServerFarms());
		newTables.add(net.getServers());
		newTables.add(linux.getShells());
		newTables.add(signup.getSignupRequestOptions());
		newTables.add(signup.getSignupRequests());
		newTables.add(email.getSpamEmailMessages());
		newTables.add(pki.getSslCertificateNames());
		newTables.add(pki.getSslCertificateOtherUses());
		newTables.add(pki.getSslCertificates());
		newTables.add(email.getSystemEmailAliases());
		newTables.add(distribution.getTechnologies());
		newTables.add(distribution.getTechnologyClasses());
		newTables.add(distribution.getTechnologyNames());
		newTables.add(distribution.getTechnologyVersions());
		newTables.add(ticket.getTicketActionTypes());
		newTables.add(ticket.getTicketActions());
		newTables.add(ticket.getTicketAssignments());
		newTables.add(reseller.getTicketBrandCategories());
		newTables.add(reseller.getTicketCategories());
		newTables.add(ticket.getTicketPriorities());
		newTables.add(ticket.getTicketStatuses());
		newTables.add(ticket.getTicketTypes());
		newTables.add(ticket.getTickets());
		newTables.add(linux.getTimeZones());
		newTables.add(billing.getTransactionTypes());
		newTables.add(billing.getTransactions());
		newTables.add(account.getUsStates());
		newTables.add(account.getUsernames());
		newTables.add(infrastructure.getVirtualDisks());
		newTables.add(infrastructure.getVirtualServers());
		newTables.add(billing.getWhoisHistory());
		newTables.add(billing.getWhoisHistoryAccount());
		newTables.trimToSize();
		tables = Collections.unmodifiableList(newTables);

		simpleAOClient = new SimpleAOClient(this);
	}

	/**
	 * Uses equivalence equality like {@link Object#equals(java.lang.Object)}.  Two
	 * connectors are considered equal only if they refer to the same
	 * object.
	 */
	@Override
	final public boolean equals(Object O) {
		if(O==null) return false;
		return
			(O instanceof AOServConnector) && // This is just to get rid of the NetBeans 6.5 warning about not checking type of parameter
			this==O
		;
	}

	/**
	 * Uses equivalence hashCode like {@link Object#hashCode()}.
	 */
	@Override
	final public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Clears all caches used by this connector.
	 */
	public void clearCaches() {
		for(AOServTable<?,?> table : tables) table.clearCache();
	}

	/**
	 * Executes an aosh command and captures its output into a <code>String</code>.
	 *
	 * @param  args  the command and arguments to be processed
	 *
	 * @return  the results of the command wrapped into a <code>String</code>
	 *
	 * @exception  IOException  if unable to access the server
	 * @exception  SQLException  if unable to access the database or data integrity
	 *                           checks fail
	 */
	public String executeCommand(String[] args) throws IOException, SQLException {
		return AOSH.executeCommand(this, args);
	}

	/**
	 * Allocates a connection to the server.  These connections must later be
	 * released with the <code>releaseConnection</code> method.  Connection
	 * pooling is obtained this way.  These connections may be over any protocol,
	 * so they may only safely be used for one client/server exchange per
	 * allocation.  Also, if connections are not <i>always</i> released, deadlock
	 * will quickly occur.  Please use a try/finally block immediately after
	 * allocating the connection to make sure it is always released.
	 *
	 * @return  the connection to the server
	 *
	 * @exception  InterruptedIOException  if interrupted while connecting
	 * @exception  IOException  if unable to connect to the server
	 *
	 * @see  #releaseConnection
	 */
	protected abstract AOServConnection getConnection(int maxConnections) throws InterruptedIOException, IOException;

	/**
	 * Gets the default <code>AOServConnector</code> as defined in the
	 * <code>com/aoindustries/aoserv/client/aoserv-client.properties</code>
	 * resource.  Each possible protocol is tried, in order, until a
	 * successful connection is made.
	 *
	 * @return  the first <code>AOServConnector</code> to successfully connect
	 *          to the server
	 *
	 * @exception  IOException  if no connection can be established
	 */
	public static AOServConnector getConnector(Logger logger) throws IOException {
		UserId username = AOServClientConfiguration.getUsername();
		DomainName daemonServer = AOServClientConfiguration.getDaemonServer();
		return getConnector(
			username,
			username,
			AOServClientConfiguration.getPassword(),
			daemonServer,
			logger
		);
	}

	/**
	 * Gets the <code>AOServConnector</code> with the provided authentication
	 * information.  The <code>com/aoindustries/aoserv/client/aoserv-client.properties</code>
	 * resource determines which protocols will be used.  Each possible protocol is
	 * tried, in order, until a successful connection is made.
	 *
	 * @param  username  the username to connect as
	 * @param  password  the password to connect with
	 *
	 * @return  the first <code>AOServConnector</code> to successfully connect
	 *          to the server
	 *
	 * @exception  IOException  if no connection can be established
	 */
	public static AOServConnector getConnector(UserId username, String password, Logger logger) throws IOException {
		return getConnector(username, username, password, null, logger);
	}

	/**
	 * Gets the <code>AOServConnector</code> with the provided authentication
	 * information.  The <code>com/aoindustries/aoserv/client/aoserv-client.properties</code>
	 * resource determines which protocols will be used.  Each possible protocol is
	 * tried, in order, until a successful connection is made.
	 *
	 * @param  connectAs  the username to connect as
	 * @param  authenticateAs  the username used for authentication, if different than
	 *                                        <code>connectAs</code>, this username must have super user
	 *                                        privileges
	 * @param  password  the password to connect with
	 *
	 * @return  the first <code>AOServConnector</code> to successfully connect
	 *          to the server
	 *
	 * @exception  IOException  if no connection can be established
	 */
	public static AOServConnector getConnector(
		UserId connectAs,
		UserId authenticateAs,
		String password,
		DomainName daemonServer,
		Logger logger
	) throws IOException {
		List<String> protocols=AOServClientConfiguration.getProtocols();
		int size=protocols.size();
		for(int c=0;c<size;c++) {
			String protocol=protocols.get(c);
			try {
				AOServConnector connector;
				if(TCPConnector.PROTOCOL.equals(protocol)) {
					connector=TCPConnector.getTCPConnector(
						AOServClientConfiguration.getTcpHostname(),
						AOServClientConfiguration.getTcpLocalIp(),
						AOServClientConfiguration.getTcpPort(),
						connectAs,
						authenticateAs,
						password,
						daemonServer,
						AOServClientConfiguration.getTcpConnectionPoolSize(),
						AOServClientConfiguration.getTcpConnectionMaxAge(),
						logger
					);
				} else if(SSLConnector.PROTOCOL.equals(protocol)) {
					connector=SSLConnector.getSSLConnector(
						AOServClientConfiguration.getSslHostname(),
						AOServClientConfiguration.getSslLocalIp(),
						AOServClientConfiguration.getSslPort(),
						connectAs,
						authenticateAs,
						password,
						daemonServer,
						AOServClientConfiguration.getSslConnectionPoolSize(),
						AOServClientConfiguration.getSslConnectionMaxAge(),
						AOServClientConfiguration.getSslTruststorePath(),
						AOServClientConfiguration.getSslTruststorePassword(),
						logger
					);
				/*
				} else if("http".equals(protocol)) {
					connector=new HTTPConnector();
				} else if("https".equals(protocol)) {
					connector=new HTTPSConnector();
				*/
				} else throw new IOException("Unknown protocol in aoserv.client.protocols: "+protocol);

				return connector;
			} catch(IOException err) {
				logger.log(Level.SEVERE, null, err);
			}
		}
		throw new IOException("Unable to connect using any of the available protocols.");
	}

	/**
	 * Each connector is assigned a unique identifier, which the
	 * server uses to not send events originating from
	 * this connector back to connections of this
	 * connector.
	 *
	 * @return  the globally unique identifier or <code>-1</code> if
	 *          the identifier has not yet been assigned
	 */
	final public long getConnectorID() {
		synchronized(idLock) {
			return id;
		}
	}

	/**
	 * Gets the hostname of the server that is connected to.
	 */
	final public HostAddress getHostname() {
		return hostname;
	}

	/**
	 * Gets the optional local IP address that connections are made from.
	 */
	final public InetAddress getLocalIp() {
		return local_ip;
	}

	/**
	 * Gets the server port that is connected to.
	 */
	final public Port getPort() {
		return port;
	}

	/**
	 * Gets the communication protocol being used.
	 */
	abstract public String getProtocol();

	private static final SecureRandom secureRandom = new SecureRandom();

	public static SecureRandom getRandom() {
		return secureRandom;
	}

	/**
	 * Gets an unmodifiable list of all of the schemas in the system.
	 */
	final public List<Schema> getSchemas() {
		return schemas;
	}

	/**
	 * Each table has a unique ID, as found in <code>SchemaTable</code>.  The actual
	 * <code>AOServTable</code> may be obtained given its identifier.
	 *
	 * @param  tableID  the unique ID of the table
	 *
	 * @return  the appropriate subclass of <code>AOServTable</code>
	 *
	 * @exception  IllegalArgumentException  if unable to find the table
	 *
	 * @see  Table
	 */
	@SuppressWarnings({"unchecked"})
	final public AOServTable<?,? extends AOServObject> getTable(int tableID) throws IllegalArgumentException {
		if(tableID>=0 && tableID<tables.size()) return tables.get(tableID);
		throw new IllegalArgumentException("Table not found for ID="+tableID);
	}

	/**
	 * Gets an unmodifiable list of all of the tables in the system.
	 *
	 * @return  a {@code List<AOServTable>} containing all the tables.  Each
	 *          table is at an index corresponding to its unique ID.
	 *
	 * @see  #getTable(int)
	 * @see  Table
	 */
	final public List<AOServTable> getTables() {
		return tables;
	}

	/**
	 * Gets the <code>BusinessAdministrator</code> who is logged in using
	 * this <code>AOServConnector</code>.  Each username and password pair
	 * resolves to an always-accessible <code>BusinessAdministrator</code>.
	 * Details about permissions and capabilities may be obtained from the
	 * <code>BusinessAdministrator</code>.
	 *
	 * @return  the <code>BusinessAdministrator</code> who is logged in
	 *
	 * @exception  IOException  if unable to communicate with the server
	 * @exception  SQLException  if unable to access the database or the
	 *                           <code>BusinessAdministrator</code> was not
	 *                           found
	 */
	final public Administrator getThisBusinessAdministrator() throws SQLException, IOException {
		Administrator obj = account.getBusinessAdministrators().get(connectAs);
		if(obj==null) throw new SQLException("Unable to find BusinessAdministrator: "+connectAs);
		return obj;
	}

	/**
	 * Manually invalidates the system caches.
	 *
	 * @param tableID the table ID
	 * @param server the pkey of the server or <code>-1</code> for all servers
	 */
	public void invalidateTable(final int tableID, final int server) throws IOException, SQLException {
		requestUpdate(true,
			AoservProtocol.CommandID.INVALIDATE_TABLE,
			new UpdateRequest() {
				IntList tableList;
				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(tableID);
					out.writeCompressedInt(server);
				}
				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AoservProtocol.DONE) tableList=readInvalidateList(in);
					else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unknown response code: "+code);
					}
				}
				@Override
				public void afterRelease() {
					tablesUpdated(tableList);
				}
			}
		);
	}

	public static IntList readInvalidateList(CompressedDataInputStream in) throws IOException {
		IntArrayList tableList=null;
		int tableID;
		while((tableID=in.readCompressedInt())!=-1) {
			if(tableList==null) tableList=new IntArrayList();
			tableList.add(tableID);
		}
		return tableList;
	}

	/**
	 * Determines if the connections made by this protocol
	 * are secure.  A connection is considered secure if
	 * it uses end-point to end-point encryption or goes
	 * over private lines.
	 *
	 * @return  <code>true</code> if the connection is secure
	 *
	 * @exception  IOException  if unable to determine if the connection
	 *                          is secure
	 */
	abstract public boolean isSecure() throws IOException;

	/**
	 * Times how long it takes to make one request with the server.
	 * This will not retry and will return the first error encountered.
	 *
	 * @return  the connection latency in milliseconds
	 */
	final public int ping() throws IOException, SQLException {
		long startTime=System.currentTimeMillis();
		requestUpdate(false, AoservProtocol.CommandID.PING);
		long timeSpan=System.currentTimeMillis()-startTime;
		if(timeSpan>Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int)timeSpan;
	}

	abstract public void printConnectionStatsHTML(Appendable out) throws IOException;

	/**
	 * Releases a connection to the server.  This will either close the
	 * connection or allow another thread to use the connection.
	 * Connections may be of any protocol, so each connection must be
	 * released after every transaction.
	 *
	 * @param  connection  the connection to release
	 *
	 * @exception  if an error occurred while closing or releasing the conection
	 *
	 * @see  #getConnection
	 */
	protected abstract void releaseConnection(AOServConnection connection) throws IOException;

	final public void removeFromAllTables(TableListener listener) {
		for(AOServTable<?,?> table : tables) table.removeTableListener(listener);
	}

	static void writeParams(Object[] params, CompressedDataOutputStream out) throws IOException {
		for(Object param : params) {
			if(param==null) throw new NullPointerException("param is null");
			else if(param instanceof Integer) out.writeCompressedInt(((Integer)param));
			else if(param instanceof Table.TableID) out.writeCompressedInt(((Table.TableID)param).ordinal());
			// Now passed while getting output stream: else if(param instanceof AOServProtocol.CommandID) out.writeCompressedInt(((AOServProtocol.CommandID)param).ordinal());
			else if(param instanceof String) out.writeUTF((String)param);
			else if(param instanceof Float) out.writeFloat((Float)param);
			else if(param instanceof Long) out.writeLong((Long)param);
			else if(param instanceof Boolean) out.writeBoolean((Boolean)param);
			else if(param instanceof Short) out.writeShort((Short)param);
			else if(param instanceof Enum) out.writeEnum((Enum)param);
			else if(param instanceof byte[]) {
				byte[] bytes=(byte[])param;
				out.writeCompressedInt(bytes.length);
				out.write(bytes, 0, bytes.length);
			}
			// Self-validating types
			else if(param instanceof AccountingCode) out.writeUTF(param.toString());
			else if(param instanceof Email) out.writeUTF(param.toString());
			else if(param instanceof HostAddress) out.writeUTF(param.toString());
			else if(param instanceof InetAddress) out.writeUTF(param.toString());
			else if(param instanceof UnixPath) out.writeUTF(param.toString());
			else if(param instanceof UserId) out.writeUTF(param.toString());
			else if(param instanceof DomainLabel) out.writeUTF(param.toString());
			else if(param instanceof DomainLabels) out.writeUTF(param.toString());
			else if(param instanceof DomainName) out.writeUTF(param.toString());
			else if(param instanceof Gecos) out.writeUTF(param.toString());
			else if(param instanceof GroupId) out.writeUTF(param.toString());
			else if(param instanceof HashedPassword) out.writeUTF(param.toString());
			else if(param instanceof LinuxId) out.writeCompressedInt(((LinuxId)param).getId());
			else if(param instanceof MacAddress) out.writeUTF(param.toString());
			else if(param instanceof MySQLDatabaseName) out.writeUTF(param.toString());
			else if(param instanceof MySQLServerName) out.writeUTF(param.toString());
			else if(param instanceof MySQLTableName) out.writeUTF(param.toString());
			else if(param instanceof MySQLUserId) out.writeUTF(param.toString());
			else if(param instanceof Port) {
				Port port = (Port)param;
				out.writeCompressedInt(port.getPort());
				out.writeEnum(port.getProtocol());
			}
			else if(param instanceof PostgresDatabaseName) out.writeUTF(param.toString());
			else if(param instanceof PostgresServerName) out.writeUTF(param.toString());
			else if(param instanceof PostgresUserId) out.writeUTF(param.toString());
			// Any other Writable
			else if(param instanceof AOServWritable) ((AOServWritable)param).write(out, AoservProtocol.Version.CURRENT_VERSION);
			else if(param instanceof CompressedWritable) ((CompressedWritable)param).write(out, AoservProtocol.Version.CURRENT_VERSION.getVersion());
			else throw new IOException("Unknown class for param: "+param.getClass().getName());
		}
	}

	/**
	 * This is the preferred mechanism for providing custom requests that have a return value.
	 *
	 * @see  #requestResult(boolean,ResultRequest)
	 */
	public interface ResultRequest<T> {
		/**
		 * Writes the request to the server.
		 * This does not need to flush the output stream.
		 */
		void writeRequest(CompressedDataOutputStream out) throws IOException;

		/**
		 * Reads the response from the server if the request was successfully sent.
		 */
		void readResponse(CompressedDataInputStream in) throws IOException, SQLException;

		/**
		 * If both the request and response were successful, this is called after the
		 * connection to the server is released.  The result is returned here so
		 * any additional processing in packaging the result may be performed
		 * after the connection is released.
		 */
		T afterRelease();
	}

	final public <T> T requestResult(
		boolean allowRetry,
		AoservProtocol.CommandID commID,
		ResultRequest<T> resultRequest
	) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					resultRequest.writeRequest(out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					resultRequest.readResponse(in);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				return resultRequest.afterRelease();
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public boolean requestBooleanQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readBoolean();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public boolean requestBooleanQueryIL(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				boolean result;
				IntList invalidateList;
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) {
						result = in.readBoolean();
						invalidateList=readInvalidateList(in);
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				tablesUpdated(invalidateList);
				return result;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public int requestIntQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readCompressedInt();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public int requestIntQueryIL(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				int result;
				IntList invalidateList;
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) {
						result=in.readCompressedInt();
						invalidateList=readInvalidateList(in);
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				tablesUpdated(invalidateList);
				return result;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public long requestLongQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readLong();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public short requestShortQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readShort();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public short requestShortQueryIL(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				short result;
				IntList invalidateList;
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) {
						result=in.readShort();
						invalidateList=readInvalidateList(in);
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				tablesUpdated(invalidateList);
				return result;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public String requestStringQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readUTF();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	/**
	 * Performs a query returning a String of any length (not limited to size &lt;= 64k like requestStringQuery).
	 */
	final public String requestLongStringQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readLongUTF();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	/**
	 * Performs a query returning a String of any length (not limited to size &lt;= 64k like requestStringQuery) or <code>null</code>.
	 * Supports nulls.
	 */
	final public String requestNullLongStringQuery(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) return in.readNullLongUTF();
					AoservProtocol.checkResult(code, in);
					throw new IOException("Unexpected response code: "+code);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	/**
	 * This is the preferred mechanism for providing custom requests.
	 *
	 * @see  #requestUpdate(boolean,UpdateRequest)
	 */
	public interface UpdateRequest {
		/**
		 * Writes the request to the server.
		 * This does not need to flush the output stream.
		 */
		void writeRequest(CompressedDataOutputStream out) throws IOException;

		/**
		 * Reads the response from the server if the request was successfully sent.
		 */
		void readResponse(CompressedDataInputStream in) throws IOException, SQLException;

		/**
		 * If both the request and response were successful, this is called after the
		 * connection to the server is released.
		 */
		void afterRelease();
	}

	final public void requestUpdate(
		boolean allowRetry,
		AoservProtocol.CommandID commID,
		UpdateRequest updateRequest
	) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					updateRequest.writeRequest(out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					updateRequest.readResponse(in);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				updateRequest.afterRelease();
				return;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public void requestUpdate(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code!=AoservProtocol.DONE) AoservProtocol.checkResult(code, in);
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				return;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	final public void requestUpdateIL(boolean allowRetry, AoservProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		int attempt = 1;
		int attempts = allowRetry ? RETRY_ATTEMPTS : 1;
		while(!Thread.interrupted()) {
			try {
				IntList invalidateList;
				AOServConnection connection=getConnection(1);
				try {
					CompressedDataOutputStream out = connection.getRequestOut(commID);
					writeParams(params, out);
					out.flush();

					CompressedDataInputStream in=connection.getResponseIn();
					int code=in.readByte();
					if(code==AoservProtocol.DONE) invalidateList=readInvalidateList(in);
					else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				} catch(RuntimeException | IOException err) {
					connection.close();
					throw err;
				} finally {
					releaseConnection(connection);
				}
				tablesUpdated(invalidateList);
				return;
			} catch(InterruptedIOException err) {
				throw err;
			} catch(RuntimeException | IOException | SQLException err) {
				if(Thread.interrupted() || attempt>=attempts || isImmediateFail(err)) throw err;
			}
			try {
				Thread.sleep(retryAttemptDelays[attempt-1]);
			} catch(InterruptedException err) {
				InterruptedIOException ioErr = new InterruptedIOException();
				ioErr.initCause(err);
				throw ioErr;
			}
			attempt++;
		}
		throw new InterruptedIOException();
	}

	public abstract AOServConnector switchUsers(UserId username) throws IOException;

	final public void tablesUpdated(IntList invalidateList) {
		if(invalidateList!=null) {
			int size=invalidateList.size();

			// Clear the caches
			for(int c=0;c<size;c++) {
				int tableID=invalidateList.getInt(c);
				tables.get(tableID).clearCache();
			}

			// Then send the events
			for(int c=0;c<size;c++) {
				int tableID=invalidateList.getInt(c);
				//System.err.println("DEBUG: AOServConnector: tablesUpdated: "+tableID+": "+SchemaTable.TableID.values()[tableID]);
				tables.get(tableID).tableUpdated();
			}
		}
	}

	/**
	 * Tests the connectivity to the server.  This test is only
	 * performed once per server per protocol.  Following that,
	 * the cached results are used.
	 *
	 * @exception  IOException  if unable to contact the server
	 */
	final public void testConnect() throws IOException, SQLException {
		synchronized(testConnectLock) {
			requestUpdate(true,
				AoservProtocol.CommandID.TEST_CONNECTION,
				new UpdateRequest() {
					@Override
					public void writeRequest(CompressedDataOutputStream out) {
					}
					@Override
					public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
						int code=in.readByte();
						if(code!=AoservProtocol.DONE) {
							AoservProtocol.checkResult(code, in);
							throw new IOException("Unexpected response code: "+code);
						}
					}
					@Override
					public void afterRelease() {
					}
				}
			);
		}
	}

	@Override
	final public String toString() {
		return getClass().getName()+"?protocol="+getProtocol()+"&hostname="+hostname+"&local_ip="+local_ip+"&port="+port+"&connectAs="+connectAs+"&authenticateAs="+authenticateAs;
	}

	/**
	 * Is notified when a table listener is being added.
	 */
	void addingTableListener() {
	}

	/**
	 * Gets some entropy from the master server, returns the number of bytes actually obtained.
	 */
	public int getMasterEntropy(final byte[] buff, final int numBytes) throws IOException, SQLException {
		return requestResult(true,
			AoservProtocol.CommandID.GET_MASTER_ENTROPY,
			new ResultRequest<Integer>() {
				int numObtained;

				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(numBytes);
				}

				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AoservProtocol.DONE) {
						numObtained=in.readCompressedInt();
						for(int c=0;c<numObtained;c++) buff[c]=in.readByte();
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				}

				@Override
				public Integer afterRelease() {
					return numObtained;
				}
			}
		);
	}

	/**
	 * Gets the amount of entropy needed by the master server in bytes.
	 */
	public long getMasterEntropyNeeded() throws IOException, SQLException {
		return requestLongQuery(true, AoservProtocol.CommandID.GET_MASTER_ENTROPY_NEEDED);
	}

	/**
	 * Adds some entropy to the master server.
	 */
	public void addMasterEntropy(final byte[] buff, final int numBytes) throws IOException, SQLException {
		requestUpdate(true,
			AoservProtocol.CommandID.ADD_MASTER_ENTROPY,
			new UpdateRequest() {
				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(numBytes);
					for(int c=0;c<numBytes;c++) out.writeByte(buff[c]);
				}
				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code!=AoservProtocol.DONE) {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				}
				@Override
				public void afterRelease() {
				}
			}
		);
	}
}

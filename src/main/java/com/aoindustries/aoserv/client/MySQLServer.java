/*
 * Copyright 2006-2013, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.util.AoCollections;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A <code>MySQLServer</code> corresponds to a unique MySQL install
 * space on one server.  The server name must be unique per server.
 * <code>MySQLDatabase</code>s and <code>MySQLServerUser</code>s are
 * unique per <code>MySQLServer</code>.
 *
 * @see  MySQLDatabase
 * @see  MySQLServerUser
 *
 * @author  AO Industries, Inc.
 */
final public class MySQLServer extends CachedObjectIntegerKey<MySQLServer> {

	// <editor-fold defaultstate="collapsed" desc="Constants">
	/**
	 * The supported versions of MySQL.
	 */
	public static final String
		VERSION_5_6_PREFIX = "5.6.",
		VERSION_5_1_PREFIX = "5.1.",
		VERSION_5_0_PREFIX = "5.0.",
		VERSION_4_1_PREFIX = "4.1.",
		VERSION_4_0_PREFIX = "4.0."
	;

	/**
	 * The directory that contains the MySQLSQL data files.
	 */
	public static final String DATA_BASE_DIR="/var/lib/mysql";

	/**
	 * Gets the versions of MySQL in order of
	 * preference.  Index <code>0</code> is the most
	 * preferred.
	 */
	public static final List<String> PREFERRED_VERSION_PREFIXES = AoCollections.optimalUnmodifiableList(
		Arrays.asList(
			VERSION_5_6_PREFIX,
			VERSION_5_1_PREFIX,
			VERSION_5_0_PREFIX,
			VERSION_4_1_PREFIX,
			VERSION_4_0_PREFIX
		)
	);

	public enum ReservedWord {
		ACTION,
		ADD,
		AFTER,
		AGGREGATE,
		ALL,
		ALTER,
		AND,
		AS,
		ASC,
		AUTO_INCREMENT,
		AVG,
		AVG_ROW_LENGTH,
		BETWEEN,
		BIGINT,
		BINARY,
		BIT,
		BLOB,
		BOOL,
		BOTH,
		BY,
		CASCADE,
		CASE,
		CHANGE,
		CHAR,
		CHARACTER,
		CHECK,
		CHECKSUM,
		COLUMN,
		COLUMNS,
		COMMENT,
		CONSTRAINT,
		CREATE,
		CROSS,
		CURRENT_DATE,
		CURRENT_TIME,
		CURRENT_TIMESTAMP,
		DATA,
		DATABASE,
		DATABASES,
		DATE,
		DATETIME,
		DAY,
		DAY_HOUR,
		DAY_MINUTE,
		DAY_SECOND,
		DAYOFMONTH,
		DAYOFWEEK,
		DAYOFYEAR,
		DEC,
		DECIMAL,
		DEFAULT,
		DELAY_KEY_WRITE,
		DELAYED,
		DELETE,
		DESC,
		DESCRIBE,
		DISTINCT,
		DISTINCTROW,
		DOUBLE,
		DROP,
		ELSE,
		ENCLOSED,
		END,
		ENUM,
		ESCAPE,
		ESCAPED,
		EXISTS,
		EXPLAIN,
		FIELDS,
		FILE,
		FIRST,
		FLOAT,
		FLOAT4,
		FLOAT8,
		FLUSH,
		FOR,
		FOREIGN,
		FROM,
		FULL,
		FUNCTION,
		GLOBAL,
		GRANT,
		GRANTS,
		GROUP,
		HAVING,
		HEAP,
		HIGH_PRIORITY,
		HOSTS,
		HOUR,
		HOUR_MINUTE,
		HOUR_SECOND,
		IDENTIFIED,
		IF,
		IGNORE,
		IN,
		INDEX,
		INFILE,
		INNER,
		INSERT,
		INSERT_ID,
		INT,
		INT1,
		INT2,
		INT3,
		INT4,
		INT8,
		INTEGER,
		INTERVAL,
		INTO,
		IS,
		ISAM,
		JOIN,
		KEY,
		KEYS,
		KILL,
		LAST_INSERT_ID,
		LEADING,
		LEFT,
		LENGTH,
		LIKE,
		LIMIT,
		LINES,
		LOAD,
		LOCAL,
		LOCK,
		LOGS,
		LONG,
		LONGBLOB,
		LONGTEXT,
		LOW_PRIORITY,
		MATCH,
		MAX,
		MAX_ROWS,
		MEDIUMBLOB,
		MEDIUMINT,
		MEDIUMTEXT,
		MIDDLEINT,
		MIN_ROWS,
		MINUTE,
		MINUTE_SECOND,
		MODIFY,
		MONTH,
		MONTHNAME,
		MYISAM,
		NATURAL,
		NO,
		NOT,
		NULL,
		NUMERIC,
		ON,
		OPTIMIZE,
		OPTION,
		OPTIONALLY,
		OR,
		ORDER,
		OUTER,
		OUTFILE,
		PACK_KEYS,
		PARTIAL,
		PASSWORD,
		PRECISION,
		PRIMARY,
		PRIVILEGES,
		PROCEDURE,
		PROCESS,
		PROCESSLIST,
		READ,
		REAL,
		REFERENCES,
		REGEXP,
		RELOAD,
		RENAME,
		REPLACE,
		RESTRICT,
		RETURNS,
		REVOKE,
		RLIKE,
		ROW,
		ROWS,
		SECOND,
		SELECT,
		SET,
		SHOW,
		SHUTDOWN,
		SMALLINT,
		SONAME,
		SQL_BIG_RESULT,
		SQL_BIG_SELECTS,
		SQL_BIG_TABLES,
		SQL_LOG_OFF,
		SQL_LOG_UPDATE,
		SQL_LOW_PRIORITY_UPDATES,
		SQL_SELECT_LIMIT,
		SQL_SMALL_RESULT,
		SQL_WARNINGS,
		STARTING,
		STATUS,
		STRAIGHT_JOIN,
		STRING,
		TABLE,
		TABLES,
		TEMPORARY,
		TERMINATED,
		TEXT,
		THEN,
		TIME,
		TIMESTAMP,
		TINYBLOB,
		TINYINT,
		TINYTEXT,
		TO,
		TRAILING,
		TYPE,
		UNIQUE,
		UNLOCK,
		UNSIGNED,
		UPDATE,
		USAGE,
		USE,
		USING,
		VALUES,
		VARBINARY,
		VARCHAR,
		VARIABLES,
		VARYING,
		WHEN,
		WHERE,
		WITH,
		WRITE,
		YEAR,
		YEAR_MONTH,
		ZEROFILL;

		private static volatile Set<String> reservedWords = null;

		/**
		 * Case-insensitive check for if the provided string is a reserved word.
		 */
		public static boolean isReservedWord(String value) {
			Set<String> words = reservedWords;
			if(words==null) {
				ReservedWord[] values = values();
				words = new HashSet<>(values.length*4/3+1);
				for(ReservedWord word : values) words.add(word.name().toUpperCase(Locale.ROOT));
				reservedWords = words;
			}
			return words.contains(value.toUpperCase(Locale.ROOT));
		}
	}
	// </editor-fold>

	static final int
		COLUMN_PKEY=0,
		COLUMN_AO_SERVER=2,
		COLUMN_NET_BIND=5,
		COLUMN_PACKAGE=6
	;
	static final String COLUMN_AO_SERVER_name = "ao_server";
	static final String COLUMN_NAME_name = "name";

	/**
	 * The maximum length of the name.
	 */
	public static final int MAX_SERVER_NAME_LENGTH=31;

	String name;
	int ao_server;
	private int version;
	private int max_connections;
	int net_bind;
	String packageName;

	public int addMySQLDatabase(
		String name,
		Package pack
	) throws IOException, SQLException {
		return table.connector.getMysqlDatabases().addMySQLDatabase(
			name,
			this,
			pack
		);
	}

	public static void checkServerName(String name) throws IllegalArgumentException {
		// Must be a-z or 0-9 first, then a-z or 0-9 or . or _
		int len = name.length();
		if (len == 0 || len > MAX_SERVER_NAME_LENGTH) throw new IllegalArgumentException("MySQL server name should not exceed "+MAX_SERVER_NAME_LENGTH+" characters.");

		// The first character must be [a-z] or [0-9]
		char ch = name.charAt(0);
		if ((ch < 'a' || ch > 'z') && (ch<'0' || ch>'9')) throw new IllegalArgumentException("MySQL server names must start with [a-z] or [0-9]");
		// The rest may have additional characters
		for (int c = 1; c < len; c++) {
			ch = name.charAt(c);
			if (
				(ch<'a' || ch>'z')
				&& (ch<'0' || ch>'9')
				&& ch!='.'
				&& ch!='_'
			) throw new IllegalArgumentException("MySQL server names may only contain [a-z], [0-9], period (.), and underscore (_)");
		}
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case 1: return name;
			case COLUMN_AO_SERVER: return ao_server;
			case 3: return version;
			case 4: return max_connections;
			case COLUMN_NET_BIND: return net_bind;
			case COLUMN_PACKAGE: return packageName;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public String getDataDirectory() {
		return DATA_BASE_DIR+'/'+name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Gets the minor version number in X.X[-max] format.  This corresponds to the installation
	 * directory under /usr/mysql/X.X[-max] or /opt/mysql-X.X[-max]
	 */
	public String getMinorVersion() throws SQLException, IOException {
		String techVersion=getVersion().getVersion();
		int pos=techVersion.indexOf('.');
		if(pos==-1) return techVersion;
		int pos2=techVersion.indexOf('.', pos+1);
		if(pos2==-1) return techVersion;
		String S = techVersion.substring(0, pos2);
		if(techVersion.endsWith("-max")) return S+"-max";
		return S;
	}

	public TechnologyVersion getVersion() throws SQLException, IOException {
		TechnologyVersion obj=table.connector.getTechnologyVersions().get(version);
		if(obj==null) throw new SQLException("Unable to find TechnologyVersion: "+version);
		if(
			obj.getOperatingSystemVersion(table.connector).getPkey()
			!= getAOServer().getServer().getOperatingSystemVersion().getPkey()
		) {
			throw new SQLException("resource/operating system version mismatch on MySQLServer: #"+pkey);
		}
		return obj;
	}

	public AOServer getAOServer() throws SQLException, IOException {
		AOServer ao=table.connector.getAoServers().get(ao_server);
		if(ao==null) throw new SQLException("Unable to find AOServer: "+ao_server);
		return ao;
	}

	public int getMaxConnections() {
		return max_connections;
	}

	public NetBind getNetBind() throws SQLException, IOException {
		NetBind nb=table.connector.getNetBinds().get(net_bind);
		if(nb==null) throw new SQLException("Unable to find NetBind: "+net_bind);
		return nb;
	}

	public Package getPackage() throws SQLException, IOException {
		Package pk=table.connector.getPackages().get(packageName);
		if(pk==null) throw new SQLException("Unable to find Package: "+packageName);
		return pk;
	}

	public MySQLDatabase getMySQLDatabase(String name) throws IOException, SQLException {
		return table.connector.getMysqlDatabases().getMySQLDatabase(name, this);
	}

	public List<FailoverMySQLReplication> getFailoverMySQLReplications() throws IOException, SQLException {
		return table.connector.getFailoverMySQLReplications().getFailoverMySQLReplications(this);
	}

	public List<MySQLDatabase> getMySQLDatabases() throws IOException, SQLException {
		return table.connector.getMysqlDatabases().getMySQLDatabases(this);
	}

	public List<MySQLDBUser> getMySQLDBUsers() throws IOException, SQLException {
		return table.connector.getMysqlDBUsers().getMySQLDBUsers(this);
	}

	public MySQLServerUser getMySQLServerUser(String username) throws IOException, SQLException {
		return table.connector.getMysqlServerUsers().getMySQLServerUser(username, this);
	}

	public List<MySQLServerUser> getMySQLServerUsers() throws IOException, SQLException {
		return table.connector.getMysqlServerUsers().getMySQLServerUsers(this);
	}

	public List<MySQLUser> getMySQLUsers() throws IOException, SQLException {
		List<MySQLServerUser> psu=getMySQLServerUsers();
		int len=psu.size();
		List<MySQLUser> pu=new ArrayList<>(len);
		for(int c=0;c<len;c++) pu.add(psu.get(c).getMySQLUser());
		return pu;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.MYSQL_SERVERS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getInt(1);
		name=result.getString(2);
		ao_server=result.getInt(3);
		version=result.getInt(4);
		max_connections=result.getInt(5);
		net_bind=result.getInt(6);
		packageName=result.getString(7);
	}

	public boolean isMySQLDatabaseNameAvailable(String name) throws IOException, SQLException {
		return table.connector.getMysqlDatabases().isMySQLDatabaseNameAvailable(name, this);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readCompressedInt();
		name=in.readUTF().intern();
		ao_server=in.readCompressedInt();
		version=in.readCompressedInt();
		max_connections=in.readCompressedInt();
		net_bind=in.readCompressedInt();
		packageName=in.readUTF().intern();
	}

	public void restartMySQL() throws IOException, SQLException {
		table.connector.requestUpdate(false, AOServProtocol.CommandID.RESTART_MYSQL, pkey);
	}

	public void startMySQL() throws IOException, SQLException {
		table.connector.requestUpdate(false, AOServProtocol.CommandID.START_MYSQL, pkey);
	}

	public void stopMySQL() throws IOException, SQLException {
		table.connector.requestUpdate(false, AOServProtocol.CommandID.STOP_MYSQL, pkey);
	}

	@Override
	String toStringImpl() throws SQLException, IOException {
		return name+" on "+getAOServer().getHostname();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeUTF(name);
		out.writeCompressedInt(ao_server);
		out.writeCompressedInt(version);
		out.writeCompressedInt(max_connections);
		out.writeCompressedInt(net_bind);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_28)>=0) out.writeUTF(packageName);
	}

	final public static class MasterStatus {

		final private String file;
		final private String position;

		public MasterStatus(
			String file,
			String position
		) {
			this.file=file;
			this.position=position;
		}

		public String getFile() {
			return file;
		}

		public String getPosition() {
			return position;
		}
	}

	/**
	 * Gets the master status or <code>null</code> if no master status provided by MySQL.  If any error occurs, throws either
	 * IOException or SQLException.
	 */
	public MasterStatus getMasterStatus() throws IOException, SQLException {
		return table.connector.requestResult(
			true,
			new AOServConnector.ResultRequest<MasterStatus>() {
				MasterStatus result;

				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(AOServProtocol.CommandID.GET_MYSQL_MASTER_STATUS.ordinal());
					out.writeCompressedInt(pkey);
				}

				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AOServProtocol.NEXT) {
						result = new MasterStatus(
							in.readNullUTF(),
							in.readNullUTF()
						);
					} else if(code==AOServProtocol.DONE) {
						result = null;
					} else {
						AOServProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				}

				@Override
				public MasterStatus afterRelease() {
					return result;
				}
			}
		);
	}
}
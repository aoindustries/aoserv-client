package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.util.BufferManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * A <code>MySQLDatabase</code> corresponds to a unique MySQL table
 * space on one server.  The database name must be unique per server
 * and, to aid in account portability, will typically be unique
 * across the entire system.
 *
 * @see  MySQLDBUser
 *
 * @author  AO Industries, Inc.
 */
final public class MySQLDatabase extends CachedObjectIntegerKey<MySQLDatabase> implements Removable, Dumpable, JdbcProvider {

    static final int
        COLUMN_PKEY=0,
        COLUMN_MYSQL_SERVER=2,
        COLUMN_PACKAGE=3
    ;
    static final String COLUMN_NAME_name = "name";
    static final String COLUMN_MYSQL_SERVER_name = "mysql_server";

    /**
     * The classname of the JDBC driver used for the <code>MySQLDatabase</code>.
     */
    public static final String
        REDHAT_JDBC_DRIVER="com.mysql.jdbc.Driver",
        MANDRAKE_JDBC_DRIVER="com.mysql.jdbc.Driver",
        CENTOS_JDBC_DRIVER="com.mysql.jdbc.Driver"
    ;
    
    /**
     * The URL for MySQL JDBC documentation.
     * TODO: put the Mandrake documentation on http://www.aoindustries.com/docs/
     */
    public static final String
        REDHAT_JDBC_DOCUMENTATION_URL="http://www.mysql.com/documentation/connector-j/index.html",
        MANDRAKE_JDBC_DOCUMENTATION_URL="http://www.mysql.com/documentation/connector-j/index.html",
        CENTOS_JDBC_DOCUMENTATION_URL="http://www.mysql.com/documentation/connector-j/index.html"
    ;

    /**
     * The longest name allowed for a MySQL database.
     */
    public static final int MAX_DATABASE_NAME_LENGTH=64;

    /**
     * The root database for a mysql installation.
     */
    public static final String MYSQL="mysql";

    /**
     * A special database that is never removed.
     */
    public static final String INFORMATION_SCHEMA="information_schema";

    String name;
    int mysql_server;
    String packageName;

    public int addMySQLServerUser(
        MySQLServerUser msu,
        boolean canSelect,
        boolean canInsert,
        boolean canUpdate,
        boolean canDelete,
        boolean canCreate,
        boolean canDrop,
        boolean canIndex,
        boolean canAlter,
        boolean canCreateTempTable,
        boolean canLockTables,
        boolean canCreateView,
        boolean canShowView,
        boolean canCreateRoutine,
        boolean canAlterRoutine,
        boolean canExecute,
        boolean canEvent,
        boolean canTrigger
    ) throws IOException, SQLException {
        return table.connector.getMysqlDBUsers().addMySQLDBUser(
            this,
            msu,
            canSelect,
            canInsert,
            canUpdate,
            canDelete,
            canCreate,
            canDrop,
            canIndex,
            canAlter,
            canCreateTempTable,
            canLockTables,
            canCreateView,
            canShowView,
            canCreateRoutine,
            canAlterRoutine,
            canExecute,
            canEvent,
            canTrigger
        );
    }

    public void dump(PrintWriter out) throws IOException, SQLException {
        dump((Writer)out);
    }

    public void dump(final Writer out) throws IOException, SQLException {
        table.connector.requestUpdate(
            false,
            new AOServConnector.UpdateRequest() {
                public void writeRequest(CompressedDataOutputStream masterOut) throws IOException {
                    masterOut.writeCompressedInt(AOServProtocol.CommandID.DUMP_MYSQL_DATABASE.ordinal());
                    masterOut.writeCompressedInt(pkey);
                }

                public void readResponse(CompressedDataInputStream masterIn) throws IOException, SQLException {
                    /*int code;
                    byte[] buff=BufferManager.getBytes();
                    try {
                        char[] chars=BufferManager.getChars();
                        try {
                            while((code=masterIn.readByte())==AOServProtocol.NEXT) {
                                int len=masterIn.readShort();
                                masterIn.readFully(buff, 0, len);
                                for(int c=0;c<len;c++) chars[c]=(char)buff[c];
                                out.write(chars, 0, len);
                            }
                        } finally {
                            BufferManager.release(chars);
                        }
                    } finally {
                        BufferManager.release(buff);
                    }
                    if(code!=AOServProtocol.DONE) AOServProtocol.checkResult(code, masterIn);*/
                    Reader nestedIn = new InputStreamReader(new NestedInputStream(masterIn), "UTF-8");
                    try {
                        char[] chars=BufferManager.getChars();
                        try {
                            int len;
                            while((len=nestedIn.read(chars, 0, BufferManager.BUFFER_SIZE))!=-1) {
                                out.write(chars, 0, len);
                            }
                        } finally {
                            BufferManager.release(chars);
                        }
                    } finally {
                        nestedIn.close();
                    }
                }

                public void afterRelease() {
                }
            }
        );
    }

    Object getColumnImpl(int i) {
        switch(i) {
            case COLUMN_PKEY: return Integer.valueOf(pkey);
            case 1: return name;
            case COLUMN_MYSQL_SERVER: return Integer.valueOf(mysql_server);
            case COLUMN_PACKAGE: return packageName;
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public String getJdbcDriver() throws SQLException, IOException {
        int osv=getMySQLServer().getAOServer().getServer().getOperatingSystemVersion().getPkey();
        switch(osv) {
            case OperatingSystemVersion.MANDRIVA_2006_0_I586 : return MANDRAKE_JDBC_DRIVER;
            case OperatingSystemVersion.REDHAT_ES_4_X86_64 : return REDHAT_JDBC_DRIVER;
            case OperatingSystemVersion.CENTOS_5_I686_AND_X86_64: return CENTOS_JDBC_DRIVER;
            default : throw new SQLException("Unsupported OperatingSystemVersion: "+osv);
        }
    }

    public String getJdbcUrl(boolean ipOnly) throws SQLException, IOException {
        MySQLServer ms=getMySQLServer();
	AOServer ao=ms.getAOServer();
        return
            "jdbc:mysql://"
            + (ipOnly
               ?ao.getServer().getNetDevice(ao.getDaemonDeviceID().getName()).getPrimaryIPAddress().getIPAddress()
	       :ao.getHostname()
            )
            + ":"
            + ms.getNetBind().getPort().getPort()
            + "/"
            + getName()
        ;
    }

    public String getJdbcDocumentationUrl() throws SQLException, IOException {
        int osv=getMySQLServer().getAOServer().getServer().getOperatingSystemVersion().getPkey();
        switch(osv) {
            case OperatingSystemVersion.MANDRIVA_2006_0_I586 : return MANDRAKE_JDBC_DOCUMENTATION_URL;
            case OperatingSystemVersion.REDHAT_ES_4_X86_64 : return REDHAT_JDBC_DOCUMENTATION_URL;
            case OperatingSystemVersion.CENTOS_5_I686_AND_X86_64 : return CENTOS_JDBC_DOCUMENTATION_URL;
            default : throw new SQLException("Unsupported OperatingSystemVersion: "+osv);
        }
    }

    public MySQLDBUser getMySQLDBUser(MySQLServerUser msu) throws IOException, SQLException {
	return table.connector.getMysqlDBUsers().getMySQLDBUser(this, msu);
    }

    public List<MySQLDBUser> getMySQLDBUsers() throws IOException, SQLException {
        return table.connector.getMysqlDBUsers().getMySQLDBUsers(this);
    }

    public List<MySQLServerUser> getMySQLServerUsers() throws IOException, SQLException {
        return table.connector.getMysqlDBUsers().getMySQLServerUsers(this);
    }

    public String getName() {
        return name;
    }

    public Package getPackage() throws SQLException, IOException {
        Package obj=table.connector.getPackages().get(packageName);
        if(obj==null) throw new SQLException("Unable to find Package: "+packageName);
        return obj;
    }

    public MySQLServer getMySQLServer() throws SQLException, IOException {
        MySQLServer obj=table.connector.getMysqlServers().get(mysql_server);
        if(obj==null) throw new SQLException("Unable to find MySQLServer: "+mysql_server);
        return obj;
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.MYSQL_DATABASES;
    }

    public void init(ResultSet result) throws SQLException {
        pkey=result.getInt(1);
        name=result.getString(2);
        mysql_server=result.getInt(3);
        packageName=result.getString(4);
    }

    public void read(CompressedDataInputStream in) throws IOException {
        pkey=in.readCompressedInt();
        name=in.readUTF();
        mysql_server=in.readCompressedInt();
        packageName=in.readUTF().intern();
    }

    public List<CannotRemoveReason> getCannotRemoveReasons(Locale userLocale) throws SQLException, IOException {
        List<CannotRemoveReason> reasons=new ArrayList<CannotRemoveReason>();
        if(name.equals(MYSQL)) reasons.add(new CannotRemoveReason<MySQLDatabase>("Not allowed to remove the MySQL database named "+MYSQL, this));
        if(name.equals(INFORMATION_SCHEMA)) {
            String version = getMySQLServer().getVersion().getVersion();
            if(
                version.startsWith(MySQLServer.VERSION_5_0_PREFIX)
                || version.startsWith(MySQLServer.VERSION_5_1_PREFIX)
            ) reasons.add(new CannotRemoveReason<MySQLDatabase>("Not allowed to remove the MySQL database named "+INFORMATION_SCHEMA, this));
        }
        return reasons;
    }

    public void remove() throws IOException, SQLException {
    	table.connector.requestUpdateIL(
            true,
            AOServProtocol.CommandID.REMOVE,
            SchemaTable.TableID.MYSQL_DATABASES,
            pkey
    	);
    }

    @Override
    String toStringImpl(Locale userLocale) {
        return name;
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
        out.writeCompressedInt(pkey);
        out.writeUTF(name);
        if(version.compareTo(AOServProtocol.Version.VERSION_1_4)<0) out.writeCompressedInt(-1);
        else out.writeCompressedInt(mysql_server);
        out.writeUTF(packageName);
        if(version.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) {
            out.writeShort(0);
            out.writeShort(7);
        }
    }

    public enum Engine {
        CSV,
        MyISAM,
        InnoDB,
        HEAP,
        MEMORY
    }

    public static class TableStatus {

        public enum RowFormat {
            Compact,
            Dynamic,
            Fixed
        }

        public enum Collation {
            latin1_swedish_ci,
            utf8_bin,
            utf8_general_ci,
            utf8_unicode_ci
        }

        private final String name;
        private final Engine engine;
        private final Integer version;
        private final RowFormat rowFormat;
        private final Long rows;
        private final Long avgRowLength;
        private final Long dataLength;
        private final Long maxDataLength;
        private final Long indexLength;
        private final Long dataFree;
        private final Long autoIncrement;
        private final String createTime;
        private final String updateTime;
        private final String checkTime;
        private final Collation collation;
        private final String checksum;
        private final String createOptions;
        private final String comment;

        public TableStatus(
            String name,
            Engine engine,
            Integer version,
            RowFormat rowFormat,
            Long rows,
            Long avgRowLength,
            Long dataLength,
            Long maxDataLength,
            Long indexLength,
            Long dataFree,
            Long autoIncrement,
            String createTime,
            String updateTime,
            String checkTime,
            Collation collation,
            String checksum,
            String createOptions,
            String comment
        ) {
            this.name = name;
            this.engine = engine;
            this.version = version;
            this.rowFormat = rowFormat;
            this.rows = rows;
            this.avgRowLength = avgRowLength;
            this.dataLength = dataLength;
            this.maxDataLength = maxDataLength;
            this.indexLength = indexLength;
            this.dataFree = dataFree;
            this.autoIncrement = autoIncrement;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.checkTime = checkTime;
            this.collation = collation;
            this.checksum = checksum;
            this.createOptions = createOptions;
            this.comment = comment;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the engine
         */
        public Engine getEngine() {
            return engine;
        }

        /**
         * @return the version
         */
        public Integer getVersion() {
            return version;
        }

        /**
         * @return the rowFormat
         */
        public RowFormat getRowFormat() {
            return rowFormat;
        }

        /**
         * @return the rows
         */
        public Long getRows() {
            return rows;
        }

        /**
         * @return the avgRowLength
         */
        public Long getAvgRowLength() {
            return avgRowLength;
        }

        /**
         * @return the dataLength
         */
        public Long getDataLength() {
            return dataLength;
        }

        /**
         * @return the maxDataLength
         */
        public Long getMaxDataLength() {
            return maxDataLength;
        }

        /**
         * @return the indexLength
         */
        public Long getIndexLength() {
            return indexLength;
        }

        /**
         * @return the dataFree
         */
        public Long getDataFree() {
            return dataFree;
        }

        /**
         * @return the autoIncrement
         */
        public Long getAutoIncrement() {
            return autoIncrement;
        }

        /**
         * @return the createTime
         */
        public String getCreateTime() {
            return createTime;
        }

        /**
         * @return the updateTime
         */
        public String getUpdateTime() {
            return updateTime;
        }

        /**
         * @return the checkTime
         */
        public String getCheckTime() {
            return checkTime;
        }

        /**
         * @return the collation
         */
        public Collation getCollation() {
            return collation;
        }

        /**
         * @return the checksum
         */
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return the createOptions
         */
        public String getCreateOptions() {
            return createOptions;
        }

        /**
         * @return the comment
         */
        public String getComment() {
            return comment;
        }
    }

    /**
     * Gets the table status on the master server.
     */
    public List<TableStatus> getTableStatus() throws IOException, SQLException {
        return getTableStatus(null);
    }

    /**
     * Gets the table status on the master server or provided slave server.
     */
    public List<TableStatus> getTableStatus(final FailoverMySQLReplication mysqlSlave) throws IOException, SQLException {
        return table.connector.requestResult(
            true,
            new AOServConnector.ResultRequest<List<TableStatus>>() {
                private List<TableStatus> result;

                public void writeRequest(CompressedDataOutputStream out) throws IOException {
                    out.writeCompressedInt(AOServProtocol.CommandID.GET_MYSQL_TABLE_STATUS.ordinal());
                    out.writeCompressedInt(pkey);
                    out.writeCompressedInt(mysqlSlave==null ? -1 : mysqlSlave.pkey);
                }

                public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
                    int code=in.readByte();
                    if(code==AOServProtocol.NEXT) {
                        int size = in.readCompressedInt();
                        List<TableStatus> tableStatuses = new ArrayList<TableStatus>(size);
                        for(int c=0;c<size;c++) {
                            tableStatuses.add(
                                new TableStatus(
                                    in.readUTF(), // name
                                    in.readNullEnum(Engine.class), // engine
                                    in.readNullInteger(), // version
                                    in.readNullEnum(TableStatus.RowFormat.class), // rowFormat
                                    in.readNullLong(), // rows
                                    in.readNullLong(), // avgRowLength
                                    in.readNullLong(), // dataLength
                                    in.readNullLong(), // maxDataLength
                                    in.readNullLong(), // indexLength
                                    in.readNullLong(), // dataFree
                                    in.readNullLong(), // autoIncrement
                                    in.readNullUTF(), // createTime
                                    in.readNullUTF(), // updateTime
                                    in.readNullUTF(), // checkTime
                                    in.readNullEnum(TableStatus.Collation.class), // collation
                                    in.readNullUTF(), // checksum
                                    in.readNullUTF(), // createOptions
                                    in.readNullUTF() // comment
                                )
                            );
                        }
                        this.result = tableStatuses;
                    } else {
                        AOServProtocol.checkResult(code, in);
                        throw new IOException("Unexpected response code: "+code);
                    }
                }

                public List<TableStatus> afterRelease() {
                    return result;
                }
            }
        );
    }

    public static class CheckTableResult {

        public enum MsgType {
            status,
            error,
            info,
            warning,
            // From MySQL 5.1
            note,
            Error
        }

        private final String table;
        private final long duration;
        private final MsgType msgType;
        private final String msgText;

        public CheckTableResult(
            String table,
            long duration,
            MsgType msgType,
            String msgText
        ) {
            this.table = table;
            this.duration = duration;
            this.msgType = msgType;
            this.msgText = msgText;
        }

        /**
         * @return the table
         */
        public String getTable() {
            return table;
        }

        /**
         * @return the duration
         */
        public long getDuration() {
            return duration;
        }

        /**
         * @return the msgType
         */
        public MsgType getMsgType() {
            return msgType;
        }

        /**
         * @return the msgText
         */
        public String getMsgText() {
            return msgText;
        }
    }

    /**
     * Gets the table status on the master server.
     */
    public List<CheckTableResult> checkTables(final Collection<String> tableNames) throws IOException, SQLException {
        return checkTables(null, tableNames);
    }

    /**
     * Gets the table status on the master server or provided slave server.
     */
    public List<CheckTableResult> checkTables(final FailoverMySQLReplication mysqlSlave, final Collection<String> tableNames) throws IOException, SQLException {
        if(tableNames.isEmpty()) return Collections.emptyList();
        return table.connector.requestResult(
            true,
            new AOServConnector.ResultRequest<List<CheckTableResult>>() {
                private List<CheckTableResult> result;

                public void writeRequest(CompressedDataOutputStream out) throws IOException {
                    out.writeCompressedInt(AOServProtocol.CommandID.CHECK_MYSQL_TABLES.ordinal());
                    out.writeCompressedInt(pkey);
                    out.writeCompressedInt(mysqlSlave==null ? -1 : mysqlSlave.pkey);
                    int size = tableNames.size();
                    out.writeCompressedInt(size);
                    int count = 0;
                    Iterator<String> iter = tableNames.iterator();
                    while(count<size && iter.hasNext()) {
                        out.writeUTF(iter.next());
                        count++;
                    }
                    if(count!=size) throw new ConcurrentModificationException("count!=size");
                }

                public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
                    int code=in.readByte();
                    if(code==AOServProtocol.NEXT) {
                        int size = in.readCompressedInt();
                        List<CheckTableResult> checkTableResults = new ArrayList<CheckTableResult>(size);
                        for(int c=0;c<size;c++) {
                            checkTableResults.add(
                                new CheckTableResult(
                                    in.readUTF(), // table
                                    in.readLong(), // duration
                                    in.readNullEnum(CheckTableResult.MsgType.class), // msgType
                                    in.readNullUTF() // msgText
                                )
                            );
                        }
                        this.result = checkTableResults;
                    } else {
                        AOServProtocol.checkResult(code, in);
                        throw new IOException("Unexpected response code: "+code);
                    }
                }

                public List<CheckTableResult> afterRelease() {
                    return result;
                }
            }
        );
    }

    /**
     * Determines if a name is safe for use as a table/column name, the name identifier
     * should be enclosed with backticks (`).
     */
    public static boolean isSafeName(String name) {
        // Must be a-z first, then a-z or 0-9 or _ or -
        int len = name.length();
        if (len == 0) return false;
        // The first character must be [a-z] or [A-Z] or _
        char ch = name.charAt(0);
        if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && ch != '_') return false;
        // The rest may have additional characters
        for (int c = 1; c < len; c++) {
            ch = name.charAt(c);
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9') && ch != '_' && ch != '-') return false;
        }

        // Also must not be a reserved word
        /*int size=reservedWords.size();
        for(int c=0;c<size;c++) {
            if(name.equalsIgnoreCase(reservedWords.get(c).toString())) return false;
    	}*/
    	return true;
    }
}
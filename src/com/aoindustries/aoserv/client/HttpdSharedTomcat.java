package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An <code>HttpdSharedTomcat</code> stores configuration information
 * about the Jakarta Tomcat JVM under which run one or more
 * <code>HttpdTomcatSharedSite</code>s.
 *
 * @see  HttpdTomcatSharedSite
 * @see  HttpdTomcatSite
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdSharedTomcat extends CachedObjectIntegerKey<HttpdSharedTomcat> implements Disablable, Removable {

    static final int
        COLUMN_PKEY = 0,
        COLUMN_AO_SERVER = 2,
        COLUMN_LINUX_SERVER_ACCOUNT = 4,
        COLUMN_LINUX_SERVER_GROUP = 5,
        COLUMN_DISABLE_LOG = 6,
        COLUMN_TOMCAT4_WORKER = 7,
        COLUMN_TOMCAT4_SHUTDOWN_PORT = 8
    ;
    static final String COLUMN_NAME_name = "name";
    static final String COLUMN_AO_SERVER_name = "ao_server";

    /**
     * The directory that www groups are stored in.
     *
     * @deprecated  The directory for shared Tomcat installations is now operating-system specific.
     * 
     * @see  OperatingSystemVersion#getHttpdSharedTomcatsDirectory()
     * @see  OperatingSystemVersion#getHttpdSharedTomcatsDirectory(int)
     */
    @Deprecated
    public static final String WWW_GROUP_DIR="/wwwgroup";

    /**
     * The maximum number of sites allowed in one <code>HttpdSharedTomcat</code>.
     */
    public static final int MAX_SITES=LinuxGroupAccount.MAX_GROUPS-1;

    public static final int MAX_NAME_LENGTH=32;

    public static final String DEFAULT_TOMCAT_VERSION_PREFIX=HttpdTomcatVersion.VERSION_6_0_PREFIX;

    private String name;
    int ao_server;
    private int version;
    int linux_server_account;
    int linux_server_group;
    int disable_log;
    int tomcat4_worker;
    int tomcat4_shutdown_port;
    private String tomcat4_shutdown_key;
    private boolean isManual;

    public boolean canDisable() {
        return disable_log==-1;
    }
    
    public boolean canEnable() throws SQLException, IOException {
        DisableLog dl=getDisableLog();
        if(dl==null) return false;
        else return
            dl.canEnable()
            && getLinuxServerGroup().getLinuxGroup().getBusiness().disable_log==-1
            && getLinuxServerAccount().disable_log==-1
        ;
    }

    public List<CannotRemoveReason> getCannotRemoveReasons(Locale userLocale) throws SQLException, IOException {
        List<CannotRemoveReason> reasons=new ArrayList<CannotRemoveReason>();

        for(HttpdTomcatSharedSite htss : getHttpdTomcatSharedSites()) {
            HttpdSite hs=htss.getHttpdTomcatSite().getHttpdSite();
            reasons.add(new CannotRemoveReason<HttpdTomcatSharedSite>("Used by Multi-Site Tomcat website "+hs.getInstallDirectory()+" on "+hs.getAOServer().getHostname(), htss));
        }

        return reasons;
    }

    public void disable(DisableLog dl) throws IOException, SQLException {
        table.connector.requestUpdateIL(true, AOServProtocol.CommandID.DISABLE, SchemaTable.TableID.HTTPD_SHARED_TOMCATS, dl.pkey, pkey);
    }
    
    public void enable() throws IOException, SQLException {
        table.connector.requestUpdateIL(true, AOServProtocol.CommandID.ENABLE, SchemaTable.TableID.HTTPD_SHARED_TOMCATS, pkey);
    }

    public String getInstallDirectory() throws SQLException, IOException {
        return getAOServer().getServer().getOperatingSystemVersion().getHttpdSharedTomcatsDirectory()+'/'+name;
    }

    Object getColumnImpl(int i) {
        switch(i) {
            case COLUMN_PKEY: return Integer.valueOf(pkey);
            case 1: return name;
            case COLUMN_AO_SERVER: return Integer.valueOf(ao_server);
            case 3: return Integer.valueOf(version);
            case COLUMN_LINUX_SERVER_ACCOUNT: return Integer.valueOf(linux_server_account);
            case COLUMN_LINUX_SERVER_GROUP: return Integer.valueOf(linux_server_group);
            case COLUMN_DISABLE_LOG: return disable_log==-1?null:Integer.valueOf(disable_log);
            case COLUMN_TOMCAT4_WORKER: return tomcat4_worker==-1?null:Integer.valueOf(tomcat4_worker);
            case COLUMN_TOMCAT4_SHUTDOWN_PORT: return tomcat4_shutdown_port==-1?null:Integer.valueOf(tomcat4_shutdown_port);
            case 9: return tomcat4_shutdown_key;
            case 10: return isManual?Boolean.TRUE:Boolean.FALSE;
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public boolean isDisabled() {
        return disable_log!=-1;
    }

    public DisableLog getDisableLog() throws SQLException, IOException {
        if(disable_log==-1) return null;
        DisableLog obj=table.connector.getDisableLogs().get(disable_log);
        if(obj==null) throw new SQLException("Unable to find DisableLog: "+disable_log);
        return obj;
    }

    public List<HttpdTomcatSharedSite> getHttpdTomcatSharedSites() throws IOException, SQLException {
        return table.connector.getHttpdTomcatSharedSites().getHttpdTomcatSharedSites(this);
    }

    public HttpdTomcatVersion getHttpdTomcatVersion() throws SQLException, IOException {
        HttpdTomcatVersion obj=table.connector.getHttpdTomcatVersions().get(version);
        if(obj==null) throw new SQLException("Unable to find HttpdTomcatVersion: "+version);
        if(
            obj.getTechnologyVersion(table.connector).getOperatingSystemVersion(table.connector).getPkey()
            != getAOServer().getServer().getOperatingSystemVersion().getPkey()
        ) {
            throw new SQLException("resource/operating system version mismatch on HttpdSharedTomcat: #"+pkey);
        }
        return obj;
    }

    public LinuxServerAccount getLinuxServerAccount() throws SQLException, IOException {
        LinuxServerAccount obj=table.connector.getLinuxServerAccounts().get(linux_server_account);
        if(obj==null) throw new SQLException("Unable to find LinuxServerAccount: "+linux_server_account);
        return obj;
    }

    public LinuxServerGroup getLinuxServerGroup() throws SQLException, IOException {
        LinuxServerGroup obj=table.connector.getLinuxServerGroups().get(linux_server_group);
        if(obj==null) throw new SQLException("Unable to find LinuxServerGroup: "+linux_server_group);
        return obj;
    }

    public String getName() {
        return name;
    }

    public AOServer getAOServer() throws SQLException, IOException {
        AOServer obj=table.connector.getAoServers().get(ao_server);
        if(obj==null) throw new SQLException("Unable to find AOServer: "+ao_server);
        return obj;
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.HTTPD_SHARED_TOMCATS;
    }

    public HttpdWorker getTomcat4Worker() throws SQLException, IOException {
        if(tomcat4_worker==-1) return null;
        HttpdWorker hw=table.connector.getHttpdWorkers().get(tomcat4_worker);
        if(hw==null) throw new SQLException("Unable to find HttpdWorker: "+tomcat4_worker);
        return hw;
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

    public void init(ResultSet result) throws SQLException {
        int pos = 1;
        pkey=result.getInt(pos++);
        name=result.getString(pos++);
        ao_server=result.getInt(pos++);
        version=result.getInt(pos++);
        linux_server_account=result.getInt(pos++);
        linux_server_group=result.getInt(pos++);
        disable_log=result.getInt(pos++);
        if(result.wasNull()) disable_log=-1;
        tomcat4_worker=result.getInt(pos++);
        if(result.wasNull()) tomcat4_worker=-1;
        tomcat4_shutdown_port=result.getInt(pos++);
        if(result.wasNull()) tomcat4_shutdown_port=-1;
        tomcat4_shutdown_key=result.getString(pos++);
        isManual=result.getBoolean(pos++);
    }

    public boolean isManual() {
        return isManual;
    }

    /**
     * Checks the format of the name of the shared Tomcat, as used in the <code>/wwwgroup</code>
     * directory.  The name must be 12 characters or less, and comprised of
     * only <code>a-z</code>,<code>0-9</code>, or <code>-</code>.  The first
     * character must be <code>a-z</code>.
     */
    public static boolean isValidSharedTomcatName(String name) {
        int len = name.length();
        if (len == 0 || len > MAX_NAME_LENGTH)
            return false;
        // The first character must be [a-z]
        char ch = name.charAt(0);
        if (ch < 'a' || ch > 'z')
            return false;
        // The rest may have additional characters
        for (int c = 1; c < len; c++) {
            ch = name.charAt(c);
            if ((ch < 'a' || ch > 'z') && (ch < '0' || ch > '9') && ch != '-')
                return false;
        }
        return true;
    }

    public void read(CompressedDataInputStream in) throws IOException {
        pkey=in.readCompressedInt();
        name=in.readUTF();
        ao_server=in.readCompressedInt();
        version=in.readCompressedInt();
        linux_server_account=in.readCompressedInt();
        linux_server_group=in.readCompressedInt();
        disable_log=in.readCompressedInt();
        tomcat4_worker=in.readCompressedInt();
        tomcat4_shutdown_port=in.readCompressedInt();
        tomcat4_shutdown_key=in.readNullUTF();
        isManual=in.readBoolean();
    }

    public List<? extends AOServObject> getDependencies() throws IOException, SQLException {
        return createDependencyList(
            getAOServer(),
            getLinuxServerAccount(),
            getLinuxServerGroup(),
            getDisableLog(),
            getTomcat4Worker(),
            getTomcat4ShutdownPort()
        );
    }

    public List<? extends AOServObject> getDependentObjects() throws IOException, SQLException {
        return createDependencyList(
            getHttpdTomcatSharedSites()
        );
    }

    public void remove() throws IOException, SQLException {
        table.connector.requestUpdateIL(true, AOServProtocol.CommandID.REMOVE, SchemaTable.TableID.HTTPD_SHARED_TOMCATS, pkey);
    }

    public void setIsManual(boolean isManual) throws IOException, SQLException {
        table.connector.requestUpdateIL(true, AOServProtocol.CommandID.SET_HTTPD_SHARED_TOMCAT_IS_MANUAL, pkey, isManual);
    }

    @Override
    String toStringImpl(Locale userLocale) throws SQLException, IOException {
        return name+" on "+getAOServer().getHostname();
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
        out.writeCompressedInt(pkey);
        out.writeUTF(name);
        out.writeCompressedInt(ao_server);
        out.writeCompressedInt(version);
        out.writeCompressedInt(linux_server_account);
        out.writeCompressedInt(linux_server_group);
        if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_61)<=0) {
            out.writeBoolean(false); // isSecure
            out.writeBoolean(false); // isOverflow
        }
        if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) {
            out.writeShort(0);
            out.writeShort(7);
            out.writeShort(0);
            out.writeShort(7);
            out.writeShort(0);
            out.writeShort(7);
        }
        out.writeCompressedInt(disable_log);
        out.writeCompressedInt(tomcat4_worker);
        out.writeCompressedInt(tomcat4_shutdown_port);
        out.writeNullUTF(tomcat4_shutdown_key);
        out.writeBoolean(isManual);
    }
}
package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import java.io.*;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A <code>MajordomoServer</code> provides Majordomo functionality for
 * a <code>EmailDomain</code>.  Once the <code>MajordomoServer</code>
 * is established, any number of <code>MajordomoList</code>s may be
 * added to it.
 *
 * @see  EmailDomain
 * @see  MajordomoList
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class MajordomoServer extends CachedObjectIntegerKey<MajordomoServer> implements Removable {

    static final int
        COLUMN_DOMAIN = 0,
        COLUMN_LINUX_SERVER_ACCOUNT = 1,
        COLUMN_LINUX_SERVER_GROUP = 2,
        COLUMN_MAJORDOMO_PIPE_ADDRESS = 4,
        COLUMN_OWNER_MAJORDOMO_ADD = 5,
        COLUMN_MAJORDOMO_OWNER_ADD = 6
    ;
    static final String COLUMN_DOMAIN_name = "domain";

    /**
     * The directory that stores the majordomo servers.
     */
    public static final String MAJORDOMO_SERVER_DIRECTORY="/etc/mail/majordomo";

    /**
     * The username part of the email address used to directly email majordomo.
     */
    public static final String MAJORDOMO_ADDRESS="majordomo";

    /**
     * The username part of the email address used to directly email the majordomo owner.
     */
    public static final String
        OWNER_MAJORDOMO_ADDRESS="owner-majordomo",
        MAJORDOMO_OWNER_ADDRESS="majordomo-owner"
    ;

    int linux_server_account;
    int linux_server_group;
    String version;
    int majordomo_pipe_address;
    int owner_majordomo_add;
    int majordomo_owner_add;

    public int addMajordomoList(
        String listName
    ) throws SQLException, IOException {
        return table.connector.getMajordomoLists().addMajordomoList(this, listName);
    }

    public List<CannotRemoveReason> getCannotRemoveReasons(Locale userLocale) {
        return Collections.emptyList();
    }

    Object getColumnImpl(int i) {
        switch(i) {
            case COLUMN_DOMAIN: return Integer.valueOf(pkey);
            case COLUMN_LINUX_SERVER_ACCOUNT: return Integer.valueOf(linux_server_account);
            case COLUMN_LINUX_SERVER_GROUP: return Integer.valueOf(linux_server_group);
            case 3: return version;
            case COLUMN_MAJORDOMO_PIPE_ADDRESS: return Integer.valueOf(majordomo_pipe_address);
            case COLUMN_OWNER_MAJORDOMO_ADD: return Integer.valueOf(owner_majordomo_add);
            case COLUMN_MAJORDOMO_OWNER_ADD: return Integer.valueOf(majordomo_owner_add);
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public EmailDomain getDomain() throws IOException, SQLException {
        EmailDomain obj=table.connector.getEmailDomains().get(pkey);
        if(obj==null) throw new SQLException("Unable to find EmailDomain: "+pkey);
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

    public EmailPipeAddress getMajordomoPipeAddress() throws SQLException, IOException {
        EmailPipeAddress obj=table.connector.getEmailPipeAddresses().get(majordomo_pipe_address);
        if(obj==null) throw new SQLException("Unable to find EmailPipeAddress: "+majordomo_pipe_address);
        return obj;
    }

    public MajordomoList getMajordomoList(String listName) throws IOException, SQLException {
        return table.connector.getMajordomoLists().getMajordomoList(this, listName);
    }

    public List<MajordomoList> getMajordomoLists() throws IOException, SQLException {
    	return table.connector.getMajordomoLists().getMajordomoLists(this);
    }

    public EmailAddress getMajordomoOwnerAddress() throws SQLException, IOException {
        EmailAddress obj=table.connector.getEmailAddresses().get(majordomo_owner_add);
        if(obj==null) throw new SQLException("Unable to find EmailAddress: "+majordomo_owner_add);
        return obj;
    }

    public EmailAddress getOwnerMajordomoAddress() throws SQLException, IOException {
        EmailAddress obj=table.connector.getEmailAddresses().get(owner_majordomo_add);
        if(obj==null) throw new SQLException("Unable to find EmailAddress: "+owner_majordomo_add);
        return obj;
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.MAJORDOMO_SERVERS;
    }

    public MajordomoVersion getVersion() throws SQLException, IOException {
	MajordomoVersion obj=table.connector.getMajordomoVersions().get(version);
	if(obj==null) throw new SQLException("Unable to find MajordomoVersion: "+version);
	return obj;
    }

    public void init(ResultSet result) throws SQLException {
        pkey=result.getInt(1);
        linux_server_account=result.getInt(2);
        linux_server_group=result.getInt(3);
        version=result.getString(4);
        majordomo_pipe_address=result.getInt(5);
        owner_majordomo_add=result.getInt(6);
        majordomo_owner_add=result.getInt(7);
    }

    public void read(CompressedDataInputStream in) throws IOException {
        pkey=in.readCompressedInt();
        linux_server_account=in.readCompressedInt();
        linux_server_group=in.readCompressedInt();
        version=in.readUTF().intern();
        majordomo_pipe_address=in.readCompressedInt();
        owner_majordomo_add=in.readCompressedInt();
        majordomo_owner_add=in.readCompressedInt();
    }

    public List<? extends AOServObject> getDependencies() throws IOException, SQLException {
        return createDependencyList(
            getDomain(),
            getLinuxServerAccount(),
            getLinuxServerGroup(),
            getMajordomoPipeAddress(),
            getOwnerMajordomoAddress(),
            getMajordomoOwnerAddress()
        );
    }

    public List<? extends AOServObject> getDependentObjects() throws IOException, SQLException {
        return createDependencyList(
            getMajordomoLists()
        );
    }

    public void remove() throws IOException, SQLException {
    	table.connector.requestUpdateIL(
            true,
            AOServProtocol.CommandID.REMOVE,
            SchemaTable.TableID.MAJORDOMO_SERVERS,
            pkey
        );
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
        out.writeCompressedInt(pkey);
        out.writeCompressedInt(linux_server_account);
        out.writeCompressedInt(linux_server_group);
        out.writeUTF(version);
        out.writeCompressedInt(majordomo_pipe_address);
        out.writeCompressedInt(owner_majordomo_add);
        out.writeCompressedInt(majordomo_owner_add);
        if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) {
            out.writeShort(0);
            out.writeShort(7);
        }
    }
}
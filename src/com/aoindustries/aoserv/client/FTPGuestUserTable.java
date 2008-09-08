package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  FTPGuestUser
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class FTPGuestUserTable extends CachedTableStringKey<FTPGuestUser> {

    FTPGuestUserTable(AOServConnector connector) {
	super(connector, FTPGuestUser.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(FTPGuestUser.COLUMN_USERNAME_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    void addFTPGuestUser(String username) {
	connector.requestUpdateIL(
            AOServProtocol.CommandID.ADD,
            SchemaTable.TableID.FTP_GUEST_USERS,
            username
	);
    }

    List<FTPGuestUser> getFTPGuestUsers(AOServer aoServer) {
	List<FTPGuestUser> cached=getRows();
	int size=cached.size();
        List<FTPGuestUser> matches=new ArrayList<FTPGuestUser>(size);
	for(int c=0;c<size;c++) {
            FTPGuestUser obj=cached.get(c);
            if(obj.getLinuxAccount().getLinuxServerAccount(aoServer)!=null) matches.add(obj);
	}
	return matches;
    }

    public FTPGuestUser get(Object pkey) {
	return getUniqueRow(FTPGuestUser.COLUMN_USERNAME, pkey);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.FTP_GUEST_USERS;
    }

    boolean handleCommand(String[] args, InputStream in, TerminalWriter out, TerminalWriter err, boolean isInteractive) {
	String command=args[0];
	if(command.equalsIgnoreCase(AOSHCommand.ADD_FTP_GUEST_USER)) {
            if(AOSH.checkParamCount(AOSHCommand.ADD_FTP_GUEST_USER, args, 1, err)) {
                connector.simpleAOClient.addFTPGuestUser(
                    args[1]
                );
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.REMOVE_FTP_GUEST_USER)) {
            if(AOSH.checkParamCount(AOSHCommand.REMOVE_FTP_GUEST_USER, args, 1, err)) {
                connector.simpleAOClient.removeFTPGuestUser(
                    args[1]
                );
            }
            return true;
	}
	return false;
    }
}
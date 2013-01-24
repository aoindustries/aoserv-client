/*
 * Copyright 2001-2012 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  EmailAddress
 *
 * @author  AO Industries, Inc.
 */
final public class EmailAddressTable extends CachedTableIntegerKey<EmailAddress> {

    EmailAddressTable(AOServConnector connector) {
	super(connector, EmailAddress.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(EmailAddress.COLUMN_DOMAIN_name+'.'+EmailDomain.COLUMN_DOMAIN_name, ASCENDING),
        new OrderBy(EmailAddress.COLUMN_DOMAIN_name+'.'+EmailDomain.COLUMN_AO_SERVER_name+'.'+AOServer.COLUMN_HOSTNAME_name, ASCENDING),
        new OrderBy(EmailAddress.COLUMN_ADDRESS_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    int addEmailAddress(String address, EmailDomain domainObject) throws SQLException, IOException {
        if (!EmailAddress.isValidFormat(address)) throw new SQLException("Invalid email address: " + address);
        return connector.requestIntQueryIL(
            true,
            AOServProtocol.CommandID.ADD,
            SchemaTable.TableID.EMAIL_ADDRESSES,
            address,
            domainObject.pkey
    	);
    }

    public EmailAddress get(int pkey) throws IOException, SQLException {
        return getUniqueRow(EmailAddress.COLUMN_PKEY, pkey);
    }

    EmailAddress getEmailAddress(String address, EmailDomain domain) throws IOException, SQLException {
        // Uses index on domain first, then searched on address
	for(EmailAddress emailAddress : domain.getEmailAddresses()) {
            if(emailAddress.address.equals(address)) return emailAddress;
	}
	return null;
    }

    List<EmailAddress> getEmailAddresses(EmailDomain domain) throws IOException, SQLException {
        return getIndexedRows(EmailAddress.COLUMN_DOMAIN, domain.pkey);
    }

    List<EmailAddress> getEmailAddresses(AOServer ao) throws IOException, SQLException {
        int aoPKey=ao.pkey;
	List<EmailAddress> addresses = getRows();
	int len = addresses.size();
        List<EmailAddress> matches=new ArrayList<EmailAddress>(len);
	for (int c = 0; c < len; c++) {
            EmailAddress address = addresses.get(c);
            if (address.getDomain().ao_server==aoPKey) matches.add(address);
	}
	return matches;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.EMAIL_ADDRESSES;
    }

    @Override
    boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
	String command=args[0];
	if(command.equalsIgnoreCase(AOSHCommand.CHECK_EMAIL_ADDRESS)) {
            if(AOSH.checkMinParamCount(AOSHCommand.CHECK_EMAIL_ADDRESS, args, 1, err)) {
                for(int c=1;c<args.length;c++) {
                    String addr=args[c];
                    int pos=addr.indexOf('@');
                    if(pos==-1) {
                        err.print("aosh: "+AOSHCommand.CHECK_EMAIL_ADDRESS+": invalid email address: ");
                        err.println(addr);
                        err.flush();
                    } else {
                        try {
                            SimpleAOClient.checkEmailAddress(
                                addr.substring(0, pos),
                                addr.substring(pos+1)
                            );
                            if(args.length>2) {
                                out.print(addr);
                                out.print(": ");
                            }
                            out.println("true");
                        } catch(IllegalArgumentException serr) {
                            if(args.length>2) {
                                out.print(addr);
                                out.print(": ");
                            }
                            out.println(serr.getMessage());
                        }
                        out.flush();
                    }
                }
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.REMOVE_EMAIL_ADDRESS)) {
            if(AOSH.checkMinParamCount(AOSHCommand.REMOVE_EMAIL_ADDRESS, args, 2, err)) {
                if((args.length&1)!=0) {
                    for(int c=1;c<args.length;c+=2) {
                        String addr=args[c];
                        int pos=addr.indexOf('@');
                        if(pos==-1) {
                            err.print("aosh: "+AOSHCommand.REMOVE_EMAIL_ADDRESS+": invalid email address: ");
                            err.println(addr);
                            err.flush();
                        } else {
                            connector.getSimpleAOClient().removeEmailAddress(
                                addr.substring(0, pos),
                                addr.substring(pos+1),
                                args[c+1]
                            );
                        }
                    }
                } else throw new IllegalArgumentException("aosh: "+AOSHCommand.REMOVE_EMAIL_ADDRESS+": must have even number of parameters.");
            }
            return true;
	} else return false;
    }
}
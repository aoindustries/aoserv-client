package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.util.WrappedException;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  DNSZone
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class DNSZoneTable extends CachedTableStringKey<DNSZone> {

    DNSZoneTable(AOServConnector connector) {
	super(connector, DNSZone.class);
    }

    public DNSZone get(Object pkey) {
	return getUniqueRow(DNSZone.COLUMN_ZONE, pkey);
    }

    private List<String> getDNSTLDs() {
        List<DNSTLD> tlds=connector.dnsTLDs.getRows();
        List<String> strings=new ArrayList<String>(tlds.size());
        for(DNSTLD tld : tlds) strings.add(tld.getDomain());
        return strings;
    }

    void addDNSZone(Package packageObj, String zone, String ip, int ttl) {
	connector.requestUpdateIL(AOServProtocol.ADD, SchemaTable.DNS_ZONES, packageObj.name, zone, ip, ttl);
    }

    /**
     * Checks the formatting for a DNS zone.  The format of a DNS zone must be <code><i>name</i>.<i>tld</i>.</code>
     */
    public boolean checkDNSZone(String zone) {
        return checkDNSZone(zone, getDNSTLDs());
    }

    /**
     * Checks the formatting for a DNS zone.  The format of a DNS zone must be <code><i>name</i>.<i>tld</i>.</code>
     */
    public static boolean checkDNSZone(String zone, List<String> tlds) {
	int zoneLen=zone.length();

	String shortestName=null;
	int len=tlds.size();
	for(int c=0;c<len;c++) {
            String o = tlds.get(c);
            String tld='.'+o+'.';

            int tldLen=tld.length();
            if(tldLen<zoneLen) {
                if(zone.substring(zoneLen-tldLen).equals(tld)) {
                    String name=zone.substring(0, zoneLen-tldLen);
                    if(shortestName==null || name.length()<shortestName.length()) shortestName=name;
                }
            }
	}
	if(shortestName!=null) return isValidHostnamePart(shortestName);
	return false;
    }

    public String getDNSZoneForHostname(String hostname) throws IllegalArgumentException, IOException, SQLException {
        return getDNSZoneForHostname(hostname, getDNSTLDs());
    }

    /**
     * Gets the zone represented by this <code>DNSZone</code>.
     *
     * @return  the zone in the format <code><i>name</i>.<i>tld</i>.</code>
     */
    public static String getDNSZoneForHostname(String hostname, List<String> tlds) throws IllegalArgumentException, IOException, SQLException {
        int hlen = hostname.length();
	if (hlen>0 && hostname.charAt(hlen-1)=='.') {
            hostname = hostname.substring(0, --hlen);
	}
	String longestTld = null;
	int tldlen = tlds.size();
	for (int i = 0; i < tldlen; i++) {
            String o = tlds.get(i);
            String tld='.'+o;

            int len = tld.length();
            if (hlen>=len && hostname.substring(hlen-len).equals(tld)) {
                if(longestTld==null || tld.length()>longestTld.length()) {
                    longestTld=tld;
                }
            }
	}
	if (longestTld==null) throw new IllegalArgumentException("Unable to determine top level domain for hostname: "+hostname);

	String zone = hostname.substring(0, hlen-longestTld.length());
	int startpos = zone.lastIndexOf('.');
	if (startpos>=0) zone = zone.substring(startpos+1);
	return zone+longestTld+".";
    }

    List<DNSZone> getDNSZones(Package packageObj) {
        return getIndexedRows(DNSZone.COLUMN_PACKAGE, packageObj.name);
    }

    /**
     * Gets the hostname for a fully qualified hostname.  Gets a hostname in <code><i>name</i>.<i>tld</i>.</code> format.
     */
    public static String getHostTLD(String hostname, List<String> tlds) {
    	int hostnameLen=hostname.length();
        if (hostnameLen>0 && hostname.charAt(hostnameLen-1)!='.') {
            hostname = hostname+".";
            hostnameLen++;
        }

	int len=tlds.size();
	for(int c=0;c<len;c++) {
            String o = tlds.get(c);
            String tld='.'+(String)o+'.';

            int tldLen=tld.length();
            if(tldLen<hostnameLen) {
                if(hostname.substring(hostnameLen-tldLen).equals(tld)) {
                    String name=hostname.substring(0, hostnameLen-tldLen);
                    // Take only the last hostname segment
                    int pos=name.lastIndexOf('.');
                    if(pos!=-1) name=name.substring(pos+1);
                    return name+tld;
                }
            }
	}
	throw new WrappedException(new SQLException("Unable to determine the host.tld. format of "+hostname));
    }

    public String getHostTLD(String hostname) {
        return getHostTLD(hostname, getDNSTLDs());
    }

    int getTableID() {
	return SchemaTable.DNS_ZONES;
    }

    boolean handleCommand(String[] args, InputStream in, TerminalWriter out, TerminalWriter err, boolean isInteractive) {
	String command=args[0];
	if(command.equalsIgnoreCase(AOSHCommand.ADD_DNS_ZONE)) {
            if(AOSH.checkParamCount(AOSHCommand.ADD_DNS_ZONE, args, 4, err)) {
                connector.simpleAOClient.addDNSZone(
                    args[1],
                    args[2],
                    args[3],
                    AOSH.parseInt(args[4], "ttl")
                );
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.CHECK_DNS_ZONE)) {
            if(AOSH.checkParamCount(AOSHCommand.CHECK_DNS_ZONE, args, 1, err)) {
                try {
                    connector.simpleAOClient.checkDNSZone(
                        args[1]
                    );
                    out.println("true");
                } catch(IllegalArgumentException iae) {
                    out.print("aosh: "+AOSHCommand.CHECK_DNS_ZONE+": ");
                    out.println(iae.getMessage());
                }
                out.flush();
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.IS_DNS_ZONE_AVAILABLE)) {
            if(AOSH.checkParamCount(AOSHCommand.IS_DNS_ZONE_AVAILABLE, args, 1, err)) {
                try {
                    out.println(connector.simpleAOClient.isDNSZoneAvailable(args[1]));
                    out.flush();
                } catch(IllegalArgumentException iae) {
                    err.print("aosh: "+AOSHCommand.IS_DNS_ZONE_AVAILABLE+": ");
                    err.println(iae.getMessage());
                    err.flush();
                }
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.PRINT_ZONE_FILE)) {
            if(AOSH.checkParamCount(AOSHCommand.PRINT_ZONE_FILE, args, 1, err)) {
                connector.simpleAOClient.printZoneFile(
                    args[1],
                    out
                );
                out.flush();
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.REMOVE_DNS_ZONE)) {
            if(AOSH.checkParamCount(AOSHCommand.REMOVE_DNS_ZONE, args, 1, err)) {
                connector.simpleAOClient.removeDNSZone(
                    args[1]
                );
            }
            return true;
	} else if(command.equalsIgnoreCase(AOSHCommand.SET_DNS_ZONE_TTL)) {
            if(AOSH.checkParamCount(AOSHCommand.REMOVE_DNS_ZONE, args, 2, err)) {
                connector.simpleAOClient.setDNSZoneTTL(
                    args[1],
                    AOSH.parseInt(args[2], "ttl")
                );
            }
        }
	return false;
    }

    public boolean isDNSZoneAvailable(String zone) {
	return connector.requestBooleanQuery(AOServProtocol.IS_DNS_ZONE_AVAILABLE, zone);
    }

    public static boolean isValidHostnamePart(String name) {
	// Must not be an empty string
	int len=name.length();
	if(len==0) return false;

	// The first character must not be -
	if (name.charAt(0) == '-') return false;

	// Must not be all numbers
	int numCount=0;
	// All remaining characters must be [a-z,0-9,-]
	for (int c = 0; c < len; c++) {
            char ch = name.charAt(c);
            if ((ch < 'a' || ch > 'z') && (ch < '0' || ch > '9') && ch != '-') return false;
            if(ch>='0' && ch<='9') numCount++;
	}
	if(numCount==len) return false;

	return true;
    }
}

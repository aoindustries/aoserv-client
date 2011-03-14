/*
 * Copyright 2001-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.table.IndexType;
import java.rmi.RemoteException;

/**
 * An <code>NetDeviceID</code> is a simple wrapper for the
 * different names of network devices used in Linux servers.
 *
 * @see  NetDevice
 *
 * @author  AO Industries, Inc.
 */
final public class NetDeviceID extends AOServObjectStringKey implements Comparable<NetDeviceID>, DtoFactory<com.aoindustries.aoserv.client.dto.NetDeviceID> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    public static final String
        BOND0="bond0",
        LO="lo",
        ETH0="eth0",
        ETH1="eth1",
        ETH2="eth2",
        ETH3="eth3",
        ETH4="eth4",
        ETH5="eth5",
        ETH6="eth6"
    ;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private static final long serialVersionUID = -1874738491073974795L;

    final private boolean loopback;

    public NetDeviceID(AOServConnector connector, String name, boolean loopback) {
        super(connector, name);
        this.loopback = loopback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    public int compareTo(NetDeviceID other) {
        return compareIgnoreCaseConsistentWithEquals(getKey(), other.getKey());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, index=IndexType.PRIMARY_KEY, description="the unique name of the device")
    public String getName() {
    	return getKey();
    }

    @SchemaColumn(order=1, description="if the device is the loopback device")
    public boolean isLoopback() {
        return loopback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DTO">
    public NetDeviceID(AOServConnector connector, com.aoindustries.aoserv.client.dto.NetDeviceID dto) {
        this(connector, dto.getName(), dto.isLoopback());
    }

    @Override
    public com.aoindustries.aoserv.client.dto.NetDeviceID getDto() {
        return new com.aoindustries.aoserv.client.dto.NetDeviceID(getKey(), loopback);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    @DependentObjectSet
    public IndexedSet<AOServer> getAoServers() throws RemoteException {
        return getConnector().getAoServers().filterIndexed(AOServer.COLUMN_DAEMON_DEVICE_ID, this);
    }

    @DependentObjectSet
    public IndexedSet<NetDevice> getNetDevices() throws RemoteException {
    	return getConnector().getNetDevices().filterIndexed(NetDevice.COLUMN_DEVICE_ID, this);
    }
    // </editor-fold>
}

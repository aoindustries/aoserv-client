/*
 * Copyright 2001-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.table.IndexType;
import com.aoindustries.util.UnionClassSet;
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
    private static final long serialVersionUID = 1L;

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
    final private boolean isLoopback;

    public NetDeviceID(AOServConnector connector, String name, boolean isLoopback) {
        super(connector, name);
        this.isLoopback = isLoopback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    public int compareTo(NetDeviceID other) {
        return AOServObjectUtils.compareIgnoreCaseConsistentWithEquals(getKey(), other.getKey());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, name="name", index=IndexType.PRIMARY_KEY, description="the unique name of the device")
    public String getName() {
    	return getKey();
    }

    @SchemaColumn(order=1, name="is_loopback", description="if the device is the loopback device")
    public boolean isLoopback() {
        return isLoopback;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DTO">
    public NetDeviceID(AOServConnector connector, com.aoindustries.aoserv.client.dto.NetDeviceID dto) {
        this(connector, dto.getName(), dto.isIsLoopback());
    }

    @Override
    public com.aoindustries.aoserv.client.dto.NetDeviceID getDto() {
        return new com.aoindustries.aoserv.client.dto.NetDeviceID(getKey(), isLoopback);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    protected UnionClassSet<AOServObject<?>> addDependentObjects(UnionClassSet<AOServObject<?>> unionSet) throws RemoteException {
        unionSet = super.addDependentObjects(null);
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getAoServers());
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getNetDevices());
        return unionSet;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    public IndexedSet<AOServer> getAoServers() throws RemoteException {
        return getConnector().getAoServers().filterIndexed(AOServer.COLUMN_DAEMON_DEVICE_ID, this);
    }

    public IndexedSet<NetDevice> getNetDevices() throws RemoteException {
    	return getConnector().getNetDevices().filterIndexed(NetDevice.COLUMN_DEVICE_ID, this);
    }
    // </editor-fold>
}

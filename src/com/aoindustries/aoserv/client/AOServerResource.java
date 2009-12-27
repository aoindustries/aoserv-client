package com.aoindustries.aoserv.client;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.rmi.RemoteException;
import java.util.Set;

/**
 * A <code>Resource</code> that exists on an <code>AOServer</code>.  All resources on a server must
 * be removed before the related <code>BusinessServer</code> may be removed.
 *
 * @see  BusinessServer
 *
 * @author  AO Industries, Inc.
 */
final public class AOServerResource extends AOServObjectIntegerKey<AOServerResource> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final long serialVersionUID = 1L;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    final private int ao_server;

    public AOServerResource(AOServerResourceService<?,?> service, int resource, int ao_server) {
        super(service, resource);
        this.ao_server = ao_server;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    protected int compareToImpl(AOServerResource other) throws RemoteException {
        return getResource().compareTo(other.getResource());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    /**
     * Gets the resource that this represents.
     */
    @SchemaColumn(order=0, name="resource", unique=true, description="a resource id")
    public Resource getResource() throws RemoteException {
        return getService().getConnector().getResources().get(key);
    }

    /**
     * Gets the server that this resource is on.
     */
    @SchemaColumn(order=1, name="ao_server", description="the ao_server")
    public AOServer getAoServer() throws RemoteException {
        return getService().getConnector().getAoServers().get(ao_server);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    public Set<? extends AOServObject> getDependencies() throws RemoteException {
        return createDependencySet(
            getResource(),
            getAoServer()
            // TODO: getBusinessServer()
        );
    }

    @Override
    public Set<? extends AOServObject> getDependentObjects() throws RemoteException {
        return createDependencySet(
            // TODO: getDependentObjectByResourceType()
        );
    }

    /* TODO
    private AOServObject getDependentObjectByResourceType() throws RemoteException {
        String resource_type = getResource().resource_type;
        AOServObject obj;
        if(resource_type.equals(ResourceType.MYSQL_SERVER)) obj = getMySQLServer();
        else throw new AssertionError("Unexpected resource type: "+resource_type);
        if(obj==null) throw new RemoteException("Type-specific aoserver resource object not found: "+key);
        return obj;
    }*/
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    /* TODO
    private Server getServer() throws SQLException, IOException {
        Server s=getService().getConnector().getServers().get(ao_server);
        if(s==null) throw new SQLException("Unable to find Server: "+ao_server);
        return s;
    }*/

    /**
     * Gets the <code>BusinessServer</code> that this depends on.  This resource
     * must be removed before the business' access to the server may be revoked.
     * This may be filtered.
     */
    /* TODO
    public BusinessServer getBusinessServer() throws IOException, SQLException {
        return getService().getConnector().getBusinessServers().getBusinessServer(getResource().accounting, ao_server);
    }

    public MySQLServer getMySQLServer() throws IOException, SQLException {
        return getService().getConnector().getMysqlServers().get(pkey);
    }
     */
    // </editor-fold>
}

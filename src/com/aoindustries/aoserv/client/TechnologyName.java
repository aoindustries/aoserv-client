package com.aoindustries.aoserv.client;

import com.aoindustries.table.IndexType;
import java.rmi.RemoteException;
import java.util.Set;

/*
 * Copyright 2000-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */

/**
 * A <code>TechnologyName</code> represents one piece of software installed in
 * the system.
 *
 * @author  AO Industries, Inc.
 */
final public class TechnologyName extends AOServObjectStringKey<TechnologyName> implements BeanFactory<com.aoindustries.aoserv.client.beans.TechnologyName> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final long serialVersionUID = 1L;

    public static final String MYSQL = "MySQL";
    public static final String POSTGRESQL = "postgresql";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    public TechnologyName(TechnologyNameService<?,?> service, String name) {
        super(service, name);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, name="name", index=IndexType.PRIMARY_KEY, description="the name of the package")
    public String getName() {
        return key;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JavaBeans">
    public com.aoindustries.aoserv.client.beans.TechnologyName getBean() {
        return new com.aoindustries.aoserv.client.beans.TechnologyName(key);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    public Set<? extends AOServObject> getDependentObjects() throws RemoteException {
        return createDependencySet(
            getTechnologyVersions(),
            getTechnologies()
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    public Set<TechnologyVersion> getTechnologyVersions() throws RemoteException {
        return getService().getConnector().getTechnologyVersions().getIndexed(TechnologyVersion.COLUMN_NAME, this);
    }

    public Set<Technology> getTechnologies() throws RemoteException {
        return getService().getConnector().getTechnologies().getIndexed(Technology.COLUMN_NAME, this);
    }

    /* TODO
    public List<Technology> getTechnologies() throws RemoteException {
    	return connector.getTechnologies().getTechnologies(this);
    }*/
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TODO">
    /* TODO
    public TechnologyVersion getTechnologyVersion(AOServConnector connector, String version, OperatingSystemVersion osv) throws IOException, SQLException {
        return connector.getTechnologyVersions().getTechnologyVersion(this, version, osv);
    } */
    // </editor-fold>
}

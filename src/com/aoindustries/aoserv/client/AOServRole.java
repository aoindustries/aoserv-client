/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.aoserv.client.validator.AccountingCode;
import com.aoindustries.table.IndexType;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * All of the roles within the system.
 *
 * @author  AO Industries, Inc.
 */
final public class AOServRole extends AOServObjectIntegerKey<AOServRole> implements BeanFactory<com.aoindustries.aoserv.client.beans.AOServRole> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final long serialVersionUID = 1L;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private AccountingCode accounting;
    private String name;

    public AOServRole(AOServRoleService<?,?> service, int pkey, AccountingCode accounting, String name) {
        super(service, pkey);
        this.accounting = accounting;
        this.name = name;
        intern();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        intern();
    }

    private void intern() {
        accounting = intern(accounting);
        name = intern(name);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    protected int compareToImpl(AOServRole other) throws RemoteException {
        int diff = accounting==other.accounting ? 0 : AOServObjectUtils.compare(getBusiness(), other.getBusiness());
        if(diff!=0) return diff;
        return AOServObjectUtils.compareIgnoreCaseConsistentWithEquals(name, other.name);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, name="pkey", index=IndexType.PRIMARY_KEY, description="a generated unique id")
    public int getPkey() {
        return key;
    }

    static final String COLUMN_ACCOUNTING = "accounting";
    /**
     * May be filtered.
     */
    @SchemaColumn(order=1, name=COLUMN_ACCOUNTING, index=IndexType.INDEXED, description="the business that owns the role")
    public Business getBusiness() throws RemoteException {
        return getService().getConnector().getBusinesses().filterUnique(Business.COLUMN_ACCOUNTING, accounting);
    }

    @SchemaColumn(order=2, name="name", description="the per-business unique role name")
    public String getName() {
        return name;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JavaBeans">
    public com.aoindustries.aoserv.client.beans.AOServRole getBean() {
        return new com.aoindustries.aoserv.client.beans.AOServRole(key, getBean(accounting), name);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    public Set<? extends AOServObject> getDependencies() throws RemoteException {
        return AOServObjectUtils.createDependencySet(
            getBusiness()
        );
    }

    @Override
    public Set<? extends AOServObject> getDependentObjects() throws RemoteException {
        return AOServObjectUtils.createDependencySet(
            getAoservRolePermissions(),
            getBusinessAdministratorRoles()
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="i18n">
    @Override
    String toStringImpl() {
        return name;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    public IndexedSet<AOServRolePermission> getAoservRolePermissions() throws RemoteException {
        return getService().getConnector().getAoservRolePermissions().filterIndexed(AOServRolePermission.COLUMN_ROLE, this);
    }

    public IndexedSet<BusinessAdministratorRole> getBusinessAdministratorRoles() throws RemoteException {
        return getService().getConnector().getBusinessAdministratorRoles().filterIndexed(BusinessAdministratorRole.COLUMN_ROLE, this);
    }
    // </editor-fold>
}
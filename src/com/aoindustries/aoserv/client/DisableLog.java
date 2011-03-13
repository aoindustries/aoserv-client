/*
 * Copyright 2002-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.aoserv.client.validator.*;
import com.aoindustries.table.IndexType;
import com.aoindustries.util.UnionClassSet;
import com.aoindustries.util.UnionMethodSet;
import com.aoindustries.util.WrappedException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * When a resource or resources are disabled, the reason and time is logged.
 *
 * @author  AO Industries, Inc.
 */
final public class DisableLog extends AOServObjectIntegerKey implements Comparable<DisableLog>, DtoFactory<com.aoindustries.aoserv.client.dto.DisableLog> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final long serialVersionUID = 1L;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    final private long time;
    private AccountingCode accounting;
    private UserId disabledBy;
    private String disableReason;

    public DisableLog(
        AOServConnector connector,
        int pkey,
        long time,
        AccountingCode accounting,
        UserId disabledBy,
        String disableReason
    ) {
        super(connector, pkey);
        this.time = time;
        this.accounting = accounting;
        this.disabledBy = disabledBy;
        this.disableReason = disableReason;
        intern();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        intern();
    }

    private void intern() {
        accounting = intern(accounting);
        disabledBy = intern(disabledBy);
        disableReason = intern(disableReason);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    public int compareTo(DisableLog other) {
        try {
            int diff = AOServObjectUtils.compare(time, other.time);
            if(diff!=0) return diff;
            diff = accounting==other.accounting ? 0 : getBusiness().compareTo(other.getBusiness()); // OK - interned
            if(diff!=0) return diff;
            return AOServObjectUtils.compare(key, other.key);
        } catch(RemoteException err) {
            throw new WrappedException(err);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, name="pkey", index=IndexType.PRIMARY_KEY, description="a generated primary key")
    public int getPkey() {
        return key;
    }

    @SchemaColumn(order=1, name="time", description="the time the stuff was disabled")
    public Timestamp getTime() {
        return new Timestamp(time);
    }

    static final String COLUMN_ACCOUNTING = "accounting";
    @SchemaColumn(order=2, name=COLUMN_ACCOUNTING, index=IndexType.INDEXED, description="the business whos resources are being disabled")
    public Business getBusiness() throws RemoteException {
        return getConnector().getBusinesses().get(accounting);
    }

    /**
     * May be filtered.
     */
    static final String COLUMN_DISABLED_BY = "disabled_by";
    @SchemaColumn(order=3, name=COLUMN_DISABLED_BY, index=IndexType.INDEXED, description="the person who disabled the accounts")
    public BusinessAdministrator getDisabledBy() throws RemoteException {
        try {
            return getConnector().getBusinessAdministrators().get(disabledBy);
        } catch(NoSuchElementException err) {
            // Filtered
            return null;
        }
    }

    @SchemaColumn(order=4, name="disable_reason", description="the optional reason the accounts were disabled")
    public String getDisableReason() {
        return disableReason;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DTO">
    public DisableLog(AOServConnector connector, com.aoindustries.aoserv.client.dto.DisableLog dto) throws ValidationException {
        this(
            connector,
            dto.getPkey(),
            getTimeMillis(dto.getTime()),
            getAccountingCode(dto.getAccounting()),
            getUserId(dto.getDisabledBy()),
            dto.getDisableReason()
        );
    }

    @Override
    public com.aoindustries.aoserv.client.dto.DisableLog getDto() {
        return new com.aoindustries.aoserv.client.dto.DisableLog(key, time, getDto(accounting), getDto(disabledBy), disableReason);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    protected UnionClassSet<AOServObject<?>> addDependencies(UnionClassSet<AOServObject<?>> unionSet) throws RemoteException {
        unionSet = super.addDependencies(unionSet);
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getBusiness());
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getDisabledBy());
        return unionSet;
    }

    private static final Map<Class<? extends AOServObject<?>>, ? extends List<? extends UnionMethodSet.Method<? extends AOServObject<?>>>> getDependentObjectsMethods
         = getDependentObjectsMethods(DisableLog.class);

    @Override
    @SuppressWarnings("unchecked")
    public Set<? extends AOServObject<?>> getDependentObjects() throws RemoteException {
        return new UnionMethodSet<AOServObject<?>>(this, (Class)AOServObject.class, getDependentObjectsMethods);
    }

    /*
    @Override
    protected UnionClassSet<AOServObject<?>> addDependentObjects(UnionClassSet<AOServObject<?>> unionSet) throws RemoteException {
        unionSet = super.addDependentObjects(null);
        // Caused cycle in dependency DAG: unionSet =  AOServObjectUtils.addDependencySet(unionSet, getBusinesses());
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getBusinessAdministrators());
        unionSet = AOServObjectUtils.addDependencySet(unionSet, getGroupNames());
        // Caused cycle in dependency DAG: AOServObjectUtils.addDependencySet(unionSet, getUsernames());
        for(AOServService<Integer,? extends Resource> subService : getConnector().getResources().getSubServices()) {
            unionSet = AOServObjectUtils.addDependencySet(unionSet, subService.filterIndexed(Resource.COLUMN_DISABLE_LOG, this));
        }
        return unionSet;
    }
     */
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    public IndexedSet<Business> getBusinesses() throws RemoteException {
        return getConnector().getBusinesses().filterIndexed(Business.COLUMN_DISABLE_LOG, this);
    }

    @DependentObjectSet
    public IndexedSet<BusinessAdministrator> getBusinessAdministrators() throws RemoteException {
        return getConnector().getBusinessAdministrators().filterIndexed(BusinessAdministrator.COLUMN_DISABLE_LOG, this);
    }

    @DependentObjectSet
    public IndexedSet<GroupName> getGroupNames() throws RemoteException {
        return getConnector().getGroupNames().filterIndexed(GroupName.COLUMN_DISABLE_LOG, this);
    }

    public IndexedSet<Username> getUsernames() throws RemoteException {
        return getConnector().getUsernames().filterIndexed(Username.COLUMN_DISABLE_LOG, this);
    }

    @DependentObjectSet
    public IndexedSet<Resource> getResources() throws RemoteException {
        return getConnector().getResources().filterIndexed(Resource.COLUMN_DISABLE_LOG, this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TODO">
    /* TODO
    public String getDisabledByUsername() {
        return disabled_by;
    }*/

    /**
     * Determines if the current <code>AOServConnector</code> can enable
     * things disabled by this <code>DisableLog</code>.
     */
    /* TODO
    public boolean canEnable() throws SQLException, IOException {
        BusinessAdministrator disabledBy=getDisabledBy();
        return disabledBy!=null && table
            .connector
            .getThisBusinessAdministrator()
            .getUsername()
            .getBusiness()
            .isBusinessOrParentOf(
                disabledBy
                .getUsername()
                .getBusiness()
            )
        ;
    }

    public List<LinuxServerAccount> getLinuxServerAccounts() throws IOException, SQLException {
        return getConnector().getLinuxServerAccounts().getIndexedRows(LinuxServerAccount.COLUMN_DISABLE_LOG, pkey);
    }

    public List<Username> getUsernames() throws IOException, SQLException {
        return getConnector().getUsernames().getIndexedRows(Username.COLUMN_DISABLE_LOG, pkey);
    }

    public List<EmailList> getEmailLists() throws IOException, SQLException {
        return getConnector().getEmailLists().getIndexedRows(EmailList.COLUMN_DISABLE_LOG, pkey);
    }

    public List<EmailPipe> getEmailPipes() throws IOException, SQLException {
        return getConnector().getEmailPipes().getIndexedRows(EmailPipe.COLUMN_DISABLE_LOG, pkey);
    }

    public List<EmailSmtpRelay> getEmailSmtpRelays() throws IOException, SQLException {
        return getConnector().getEmailSmtpRelays().getIndexedRows(EmailSmtpRelay.COLUMN_DISABLE_LOG, pkey);
    }

    public List<HttpdSite> getHttpdSites() throws IOException, SQLException {
        return getConnector().getHttpdSites().getIndexedRows(HttpdSite.COLUMN_DISABLE_LOG, pkey);
    }

    public List<HttpdSiteBind> getHttpdSiteBinds() throws IOException, SQLException {
        return getConnector().getHttpdSiteBinds().getIndexedRows(HttpdSiteBind.COLUMN_DISABLE_LOG, pkey);
    }

    public List<HttpdSharedTomcat> getHttpdSharedTomcats() throws IOException, SQLException {
        return getConnector().getHttpdSharedTomcats().getIndexedRows(HttpdSharedTomcat.COLUMN_DISABLE_LOG, pkey);
    }

    public List<PostgresServerUser> getPostgresServerUsers() throws IOException, SQLException {
        return getConnector().getPostgresServerUsers().getIndexedRows(PostgresServerUser.COLUMN_DISABLE_LOG, pkey);
    }

    public List<PostgresUser> getPostgresUsers() throws IOException, SQLException {
        return getConnector().getPostgresUsers().getIndexedRows(PostgresUser.COLUMN_DISABLE_LOG, pkey);
    }
     */
    // </editor-fold>
}

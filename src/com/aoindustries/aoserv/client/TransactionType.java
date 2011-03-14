/*
 * Copyright 2005-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.table.IndexType;
import java.rmi.RemoteException;

/**
 * A <code>TransactionType</code> is one type that may be used
 * in a <code>Transaction</code>.  Each <code>PackageDefinition</code>
 * and <code>PackageDefinitionLimit</code> defines which type will be
 * used for billing.
 *
 * @see  PackageDefinition
 * @see  PackageDefinitionLimit
 * @see  Transaction
 *
 * @author  AO Industries, Inc.
 */
final public class TransactionType extends AOServObjectStringKey implements Comparable<TransactionType>, DtoFactory<com.aoindustries.aoserv.client.dto.TransactionType> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    public static final String
        HTTPD="httpd",
        PAYMENT="payment",
        VIRTUAL="virtual"
    ;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private static final long serialVersionUID = -1059222609721321377L;

    /**
     * If <code>true</code> this <code>TransactionType</code> represents a credit to
     * an account and will be listed in payments received reports.
     */
    final private boolean credit;

    public TransactionType(AOServConnector connector, String name, boolean credit) {
        super(connector, name);
        this.credit = credit;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    public int compareTo(TransactionType other) {
        return compareIgnoreCaseConsistentWithEquals(getKey(), other.getKey());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, index=IndexType.PRIMARY_KEY, description="the unique name of this transaction type")
    public String getName() {
        return getKey();
    }

    @SchemaColumn(order=1, description="indicates that this type of transaction represents payment or credit")
    public boolean isCredit() {
        return credit;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DTO">
    public TransactionType(AOServConnector connector, com.aoindustries.aoserv.client.dto.TransactionType dto) {
        this(
            connector,
            dto.getName(),
            dto.isCredit()
        );
    }

    @Override
    public com.aoindustries.aoserv.client.dto.TransactionType getDto() {
        return new com.aoindustries.aoserv.client.dto.TransactionType(getKey(), credit);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="i18n">
    public String getDescription() {
        return ApplicationResources.accessor.getMessage("TransactionType."+getKey()+".description");
    }

    public String getUnit() {
        return ApplicationResources.accessor.getMessage("TransactionType."+getKey()+".unit");
    }

    @Override
    String toStringImpl() {
        return ApplicationResources.accessor.getMessage("TransactionType."+getKey()+".toString");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    @DependentObjectSet
    public IndexedSet<Transaction> getTransactions() throws RemoteException {
        return getConnector().getTransactions().filterIndexed(Transaction.COLUMN_TYPE, this);
    }

    @DependentObjectSet
    public IndexedSet<PackageDefinition> getPackageDefinitionsBySetupFeeTransactionType() throws RemoteException {
        return getConnector().getPackageDefinitions().filterIndexed(PackageDefinition.COLUMN_SETUP_FEE_TRANSACTION_TYPE, this);
    }

    @DependentObjectSet
    public IndexedSet<PackageDefinition> getPackageDefinitionsByMonthlyRateTransactionType() throws RemoteException {
        return getConnector().getPackageDefinitions().filterIndexed(PackageDefinition.COLUMN_MONTHLY_RATE_TRANSACTION_TYPE, this);
    }

    @DependentObjectSet
    public IndexedSet<PackageDefinitionLimit> getPackageDefinitionLimits() throws RemoteException {
        return getConnector().getPackageDefinitionLimits().filterIndexed(PackageDefinitionLimit.COLUMN_ADDITIONAL_TRANSACTION_TYPE, this);
    }
    // </editor-fold>
}

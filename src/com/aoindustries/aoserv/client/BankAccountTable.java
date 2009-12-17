package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;
import java.util.List;

/**
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class BankAccountTable extends CachedTableStringKey<BankAccount> {

    BankAccountTable(AOServConnector connector) {
        super(connector, BankAccount.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(BankAccount.COLUMN_NAME_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public BankAccount get(String name) throws IOException, SQLException {
        return getUniqueRow(BankAccount.COLUMN_NAME, name);
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.BANK_ACCOUNTS;
    }

    List<BankAccount> getBankAccounts(Bank bank) throws IOException, SQLException {
        return getIndexedRows(BankAccount.COLUMN_BANK, bank.pkey);
    }
}
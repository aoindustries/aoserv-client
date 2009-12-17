package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.IOException;
import java.sql.SQLException;

/**
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class BankTransactionTable extends CachedTableIntegerKey<BankTransaction> {

    BankTransactionTable(AOServConnector connector) {
        super(connector, BankTransaction.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(BankTransaction.COLUMN_TIME_name+"::"+SchemaType.DATE_name, ASCENDING),
        new OrderBy(BankTransaction.COLUMN_TRANSID_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public BankTransaction get(int transid) throws IOException, SQLException {
        return getUniqueRow(BankTransaction.COLUMN_TRANSID, transid);
    }

    public SchemaTable.TableID getTableID() {
    	return SchemaTable.TableID.BANK_TRANSACTIONS;
    }
}

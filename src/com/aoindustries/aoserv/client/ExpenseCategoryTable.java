package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * For AO Industries use only.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class ExpenseCategoryTable extends CachedTableStringKey<ExpenseCategory> {

    ExpenseCategoryTable(AOServConnector connector) {
	super(connector, ExpenseCategory.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(ExpenseCategory.COLUMN_EXPENSE_CODE_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public ExpenseCategory get(Object pkey) {
	return getUniqueRow(ExpenseCategory.COLUMN_EXPENSE_CODE, pkey);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.EXPENSE_CATEGORIES;
    }
}
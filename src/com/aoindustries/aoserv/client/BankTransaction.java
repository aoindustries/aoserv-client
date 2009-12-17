package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.sql.SQLUtility;
import com.aoindustries.util.StringUtility;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class BankTransaction extends CachedObjectIntegerKey<BankTransaction> {

    static final int
        COLUMN_TRANSID = 1,
        COLUMN_BANK_ACCOUNT = 2,
        COLUMN_PROCESSOR = 3,
        COLUMN_ADMINISTRATOR = 4
    ;
    static final String COLUMN_TIME_name = "time";
    static final String COLUMN_TRANSID_name = "transid";

    private long time;
    private String
        bankAccount,
        processor,
        administrator,
        type,
        expenseCode,
        description,
        checkNo
    ;
    private int amount;
    private boolean confirmed;

    public MasterUser getAdministrator() throws SQLException, IOException {
    	MasterUser obj = table.connector.getMasterUsers().get(administrator);
    	if (obj == null) throw new SQLException("Unable to find MasterUser: " + administrator);
    	return obj;
    }

    public int getAmount() {
    	return amount;
    }

    public BankAccount getBankAccount() throws SQLException, IOException {
    	BankAccount bankAccountObject = table.connector.getBankAccounts().get(bankAccount);
        if (bankAccountObject == null) throw new SQLException("BankAccount not found: " + bankAccount);
        return bankAccountObject;
    }

    public BankTransactionType getBankTransactionType() throws SQLException, IOException {
        BankTransactionType typeObject = table.connector.getBankTransactionTypes().get(type);
        if (typeObject == null) throw new SQLException("BankTransactionType not found: " + type);
        return typeObject;
    }

    public String getCheckNo() {
    	return checkNo;
    }

    Object getColumnImpl(int i) {
        switch(i) {
            case 0: return new java.sql.Date(time);
            case COLUMN_TRANSID: return pkey;
            case COLUMN_BANK_ACCOUNT: return bankAccount;
            case COLUMN_PROCESSOR: return processor;
            case COLUMN_ADMINISTRATOR: return administrator;
            case 5: return type;
            case 6: return expenseCode;
            case 7: return description;
            case 8: return checkNo;
            case 9: return Integer.valueOf(amount);
            case 10: return confirmed?Boolean.TRUE:Boolean.FALSE;
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public String getDescription() {
    	return description;
    }

    public ExpenseCategory getExpenseCategory() throws SQLException, IOException {
        if(expenseCode==null) return null;
        ExpenseCategory cat=table.connector.getExpenseCategories().get(expenseCode);
        if (cat == null) throw new SQLException("ExpenseCategory not found: " + expenseCode);
        return cat;
    }

    public CreditCardProcessor getCreditCardProcessor() throws SQLException, IOException {
        if (processor == null) return null;
        CreditCardProcessor ccProcessor = table.connector.getCreditCardProcessors().get(processor);
        if (ccProcessor == null) throw new SQLException("CreditCardProcessor not found: " + processor);
        return ccProcessor;
    }

    public SchemaTable.TableID getTableID() {
    	return SchemaTable.TableID.BANK_TRANSACTIONS;
    }

    public long getTime() {
    	return time;
    }

    public int getTransID() {
    	return pkey;
    }

    public void init(ResultSet result) throws SQLException {
        time = result.getTimestamp(1).getTime();
        pkey = result.getInt(2);
        bankAccount = result.getString(3);
        processor = result.getString(4);
        administrator = result.getString(5);
        type = result.getString(6);
        expenseCode = result.getString(7);
        description = result.getString(8);
        checkNo = result.getString(9);
        amount = SQLUtility.getPennies(result.getString(10));
        confirmed = result.getBoolean(11);
    }

    public boolean isConfirmed() {
    	return confirmed;
    }

    public void read(CompressedDataInputStream in) throws IOException {
        time = in.readLong();
        pkey = in.readCompressedInt();
        bankAccount = in.readUTF().intern();
        processor = StringUtility.intern(in.readNullUTF());
        administrator = in.readUTF().intern();
        type = in.readUTF().intern();
        expenseCode = StringUtility.intern(in.readNullUTF());
        description = in.readUTF();
        checkNo = in.readNullUTF();
        amount = in.readCompressedInt();
        confirmed = in.readBoolean();
    }

    @Override
    String toStringImpl(Locale userLocale) {
    	return pkey+"|"+administrator+'|'+type+'|'+SQLUtility.getDecimal(amount);
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
        out.writeLong(time);
        out.writeCompressedInt(pkey);
        out.writeUTF(bankAccount);
        if(version.compareTo(AOServProtocol.Version.VERSION_1_29)<0) {
            out.writeNullUTF(null);
        } else {
            out.writeNullUTF(processor);
        }
        out.writeUTF(administrator);
        out.writeUTF(type);
        out.writeNullUTF(expenseCode);
        out.writeUTF(description);
        out.writeNullUTF(checkNo);
        out.writeCompressedInt(amount);
        out.writeBoolean(confirmed);
    }

    public List<? extends AOServObject> getDependencies() throws IOException, SQLException {
        return createDependencyList(
            getBankAccount(),
            getCreditCardProcessor(),
            getAdministrator()
        );
    }

    public List<? extends AOServObject> getDependentObjects() throws IOException, SQLException {
        return createDependencyList(
        );
    }
}
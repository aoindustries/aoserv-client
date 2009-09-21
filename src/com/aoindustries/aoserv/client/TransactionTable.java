package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.util.IntList;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  Transaction
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class TransactionTable extends AOServTable<Integer,Transaction> {

    private long accountBalancesClearCounter = 0;
    final private Map<String,Integer> accountBalances=new HashMap<String,Integer>();
    private long confirmedAccountBalancesClearCounter = 0;
    final private Map<String,Integer> confirmedAccountBalances=new HashMap<String,Integer>();

    TransactionTable(AOServConnector connector) {
        super(connector, Transaction.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(Transaction.COLUMN_TIME_name+"::"+SchemaType.DATE_name, ASCENDING),
        new OrderBy(Transaction.COLUMN_TRANSID_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    int addTransaction(
        final Business business,
        final Business sourceBusiness,
        final BusinessAdministrator business_administrator,
        final String type,
        final String description,
        final int quantity,
        final int rate,
        final PaymentType paymentType,
        final String paymentInfo,
        final CreditCardProcessor processor,
    	final byte payment_confirmed
    ) throws IOException, SQLException {
        return connector.requestResult(
            false,
            new AOServConnector.ResultRequest<Integer>() {
                int transid;
                IntList invalidateList;

                public void writeRequest(CompressedDataOutputStream out) throws IOException {
                    out.writeCompressedInt(AOServProtocol.CommandID.ADD.ordinal());
                    out.writeCompressedInt(SchemaTable.TableID.TRANSACTIONS.ordinal());
                    out.writeUTF(business.pkey);
                    out.writeUTF(sourceBusiness.pkey);
                    out.writeUTF(business_administrator.pkey);
                    out.writeUTF(type);
                    out.writeUTF(description);
                    out.writeCompressedInt(quantity);
                    out.writeCompressedInt(rate);
                    out.writeBoolean(paymentType!=null); if(paymentType!=null) out.writeUTF(paymentType.pkey);
                    out.writeNullUTF(paymentInfo);
                    out.writeNullUTF(processor==null ? null : processor.getProviderId());
                    out.writeByte(payment_confirmed);
                }

                public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
                    int code=in.readByte();
                    if(code==AOServProtocol.DONE) {
                        transid=in.readCompressedInt();
                        invalidateList=AOServConnector.readInvalidateList(in);
                    } else {
                        AOServProtocol.checkResult(code, in);
                        throw new IOException("Unexpected response code: "+code);
                    }
                }

                public Integer afterRelease() {
                    connector.tablesUpdated(invalidateList);
                    return transid;
                }
            }
        );
    }

    @Override
    public void clearCache() {
        // System.err.println("DEBUG: TransactionTable: clearCache() called");
        super.clearCache();
        synchronized(accountBalances) {
            accountBalancesClearCounter++;
            accountBalances.clear();
        }
        synchronized(confirmedAccountBalances) {
            confirmedAccountBalancesClearCounter++;
            confirmedAccountBalances.clear();
        }
    }

    int getAccountBalance(String accounting) throws IOException, SQLException {
        long clearCounter;
        synchronized(accountBalances) {
            Integer balance=accountBalances.get(accounting);
            if(balance!=null) return balance.intValue();
            clearCounter = accountBalancesClearCounter;
        }
        int balance=connector.requestIntQuery(true, AOServProtocol.CommandID.GET_ACCOUNT_BALANCE, accounting);
        synchronized(accountBalances) {
            // Only put in cache when not cleared while performing query
            if(clearCounter==accountBalancesClearCounter) accountBalances.put(accounting, Integer.valueOf(balance));
        }
        return balance;
    }

    int getAccountBalance(String accounting, long before) throws IOException, SQLException {
        return connector.requestIntQuery(true, AOServProtocol.CommandID.GET_ACCOUNT_BALANCE_BEFORE, accounting, before);
    }

    int getConfirmedAccountBalance(String accounting) throws IOException, SQLException {
        long clearCounter;
        synchronized(confirmedAccountBalances) {
            Integer balance=confirmedAccountBalances.get(accounting);
            if(balance!=null) return balance.intValue();
            clearCounter = confirmedAccountBalancesClearCounter;
        }
        int balance=connector.requestIntQuery(true, AOServProtocol.CommandID.GET_CONFIRMED_ACCOUNT_BALANCE, accounting);
        synchronized(confirmedAccountBalances) {
            // Only put in cache when not cleared while performing query
            if(clearCounter==confirmedAccountBalancesClearCounter) confirmedAccountBalances.put(accounting, Integer.valueOf(balance));
        }
        return balance;
    }

    int getConfirmedAccountBalance(String accounting, long before) throws IOException, SQLException {
        return connector.requestIntQuery(true, AOServProtocol.CommandID.GET_CONFIRMED_ACCOUNT_BALANCE_BEFORE, accounting, before);
    }

    public List<Transaction> getPendingPayments() throws IOException, SQLException {
        return getObjects(true, AOServProtocol.CommandID.GET_PENDING_PAYMENTS);
    }

    public List<Transaction> getRows() throws IOException, SQLException {
        List<Transaction> list=new ArrayList<Transaction>();
        getObjects(true, list, AOServProtocol.CommandID.GET_TABLE, SchemaTable.TableID.TRANSACTIONS);
        return list;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.TRANSACTIONS;
    }

    public Transaction get(Object transid) throws IOException, SQLException {
        return get(((Integer)transid).intValue());
    }

    public Transaction get(int transid) throws IOException, SQLException {
        return getObject(true, AOServProtocol.CommandID.GET_OBJECT, SchemaTable.TableID.TRANSACTIONS, transid);
    }

    List<Transaction> getTransactions(TransactionSearchCriteria search) throws IOException, SQLException {
        return getObjects(true, AOServProtocol.CommandID.GET_TRANSACTIONS_SEARCH, search);
    }

    List<Transaction> getTransactions(String accounting) throws IOException, SQLException {
        return getObjects(true, AOServProtocol.CommandID.GET_TRANSACTIONS_BUSINESS, accounting);
    }

    List<Transaction> getTransactions(BusinessAdministrator ba) throws IOException, SQLException {
        return getObjects(true, AOServProtocol.CommandID.GET_TRANSACTIONS_BUSINESS_ADMINISTRATOR, ba.pkey);
    }

    @Override
    final public List<Transaction> getIndexedRows(int col, Object value) throws IOException, SQLException {
        if(col==Transaction.COLUMN_TRANSID) {
            Transaction tr=get(value);
            if(tr==null) return Collections.emptyList();
            else return Collections.singletonList(tr);
        }
        if(col==Transaction.COLUMN_ACCOUNTING) return getTransactions((String)value);
        throw new UnsupportedOperationException("Not an indexed column: "+col);
    }

    protected Transaction getUniqueRowImpl(int col, Object value) throws IOException, SQLException {
        if(col!=Transaction.COLUMN_TRANSID) throw new IllegalArgumentException("Not a unique column: "+col);
        return get(value);
    }

    @Override
    boolean handleCommand(String[] args, InputStream in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
	String command=args[0];
	if(command.equalsIgnoreCase(AOSHCommand.ADD_TRANSACTION)) {
            if(AOSH.checkParamCount(AOSHCommand.ADD_TRANSACTION, args, 11, err)) {
                byte pc;
                if(args[11].equals("Y")) pc=Transaction.CONFIRMED;
                else if(args[11].equals("W")) pc=Transaction.WAITING_CONFIRMATION;
                else if(args[11].equals("N")) pc=Transaction.NOT_CONFIRMED;
                else throw new IllegalArgumentException("Unknown value for payment_confirmed, should be one of Y, W, or N: "+args[11]);
                int transid=connector.getSimpleAOClient().addTransaction(
                    args[1],
                    args[2],
                    args[3],
                    args[4],
                    args[5],
                    AOSH.parseMillis(args[6], "quantity"),
                    AOSH.parsePennies(args[7], "rate"),
                    args[8],
                    args[9],
                    args[10],
                    pc
                );
                out.println(transid);
                out.flush();
            }
            return true;
	}
	return false;
    }
}

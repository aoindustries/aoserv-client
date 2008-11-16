package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.util.StringUtility;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * A <code>TransactionSearchCriteria</code> stores all the parameters
 * for a <code>Transaction</code> search.wraps all the different
 * ways in which the transaction table may be searched.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class TransactionSearchCriteria implements Streamable {

    /**
     * Value representing any in a search.
     */
    public static final int ANY=-1;

    long after;

    long before;
    int transid;
    String business;
    String sourceBusiness;
    String business_administrator;
    String type;
    String description;
    String paymentType;
    String paymentInfo;
    byte payment_confirmed;

    /**
     * The columns that can be sorted on.
     */
    public static final byte
        SORT_TIME=0,
        SORT_TRANSID=1,
        SORT_BUSINESS=2,
        SORT_SOURCE_BUSINESS=3,
        SORT_BUSINESS_ADMINISTRATOR=4,
        SORT_TYPE=5,
        SORT_DESCRIPTION=6,
        SORT_PAYMENT_TYPE=7,
        SORT_PAYMENT_INFO=8,
        SORT_PAYMENT_CONFIRMED=9
    ;

    /**
     * The labels for each sort column.
     */
    public static final String[] sortLabels={
        "Date/Time",
        "Transaction #",
        "Accounting",
        "Source Accounting",
        "Username",
        "Transaction Type",
        "Description",
        "Payment Method",
        "Payment Info",
        "Payment Confirmation"
    };

    byte sortFirst=0;
    byte sortSecond=0;
    boolean sortDescending=true;

    public TransactionSearchCriteria() {
    }

    public TransactionSearchCriteria(
	long after,
	long before,
	int transid,
	Business business,
        Business sourceBusiness,
	BusinessAdministrator business_administrator,
	TransactionType type,
	String description,
	PaymentType paymentType,
	String paymentInfo,
	byte payment_confirmed
    ) {
	this.after = after;
	this.before = before;
	this.transid = transid;
	this.business = business==null?null:business.pkey;
        this.sourceBusiness = sourceBusiness==null?null:sourceBusiness.pkey;
	this.business_administrator = business_administrator==null?null:business_administrator.pkey;
	this.type = type==null?null:type.pkey;
	this.description = description;
	this.paymentType = paymentType==null?null:paymentType.pkey;
	this.paymentInfo = paymentInfo;
	this.payment_confirmed = payment_confirmed;
    }

    public TransactionSearchCriteria(BusinessAdministrator business_administrator) {
	// The current time
	Calendar cal = Calendar.getInstance();
	cal.setTime(new java.util.Date());

	// The current year
	int year = cal.get(Calendar.YEAR);

	before = ANY;

	// The beginning of last month starts the default search
	int month = cal.get(Calendar.MONTH);
	if (month == Calendar.JANUARY) {
            year--;
            month = Calendar.DECEMBER;
	} else month--;
	cal.set(Calendar.YEAR, year);
	cal.set(Calendar.MONTH, month);
	cal.set(Calendar.DAY_OF_MONTH, 1);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MILLISECOND, 0);
	after = cal.getTime().getTime();

	transid = ANY;
	business = business_administrator == null ? null : business_administrator.getUsername().getPackage().getBusiness().pkey;
        sourceBusiness = null;
	business_administrator = null;
	type = null;
	description = null;
	paymentType = null;
	paymentInfo = null;
	payment_confirmed = ANY;
    }

    public long getAfter() {
	return after;
    }

    public long getBefore() {
	return before;
    }

    public String getBusiness() {
	return business;
    }

    public String getSourceBusiness() {
        return sourceBusiness;
    }

    public String getBusinessAdministrator() {
	return business_administrator;
    }

    public String getDescription() {
	return description;
    }

    public byte getPaymentConfirmed() {
	return payment_confirmed;
    }

    public String getPaymentInfo() {
	return paymentInfo;
    }

    public String getPaymentType() {
	return paymentType;
    }

    public String getType() {
	return type;
    }

    public byte getSortFirst() {
	return sortFirst;
    }

    public byte getSortSecond() {
	return sortSecond;
    }

    public List<Transaction> getTransactions(AOServConnector connector) {
	return connector.transactions.getTransactions(this);
    }

    public int getTransID() {
	return transid;
    }

    public void read(CompressedDataInputStream in) throws IOException {
	after=in.readLong();
	before=in.readLong();
	transid=in.readCompressedInt();
	business=StringUtility.intern(in.readNullUTF());
        sourceBusiness=StringUtility.intern(in.readNullUTF());
	business_administrator=StringUtility.intern(in.readNullUTF());
	type=StringUtility.intern(in.readNullUTF());
	description=in.readNullUTF();
	paymentType=StringUtility.intern(in.readNullUTF());
	paymentInfo=in.readNullUTF();
	payment_confirmed=in.readByte();
	sortFirst=in.readByte();
	sortSecond=in.readByte();
	sortDescending=in.readBoolean();
    }

    public void setAfter(long after) {
	this.after=after;
    }

    public void setBefore(long before) {
	this.before=before;
    }

    public void setBusiness(String business) {
	this.business=business;
    }

    public void setSourceBusiness(String sourceBusiness) {
        this.sourceBusiness=sourceBusiness;
    }

    public void setBusinessAdministrator(String business_administrator) {
	this.business_administrator=business_administrator;
    }

    public void setDescription(String description) {
	this.description=description;
    }

    public void setPaymentConfirmed(byte payment_confirmed) {
	this.payment_confirmed=payment_confirmed;
    }

    public void setPaymentInfo(String paymentInfo) {
	this.paymentInfo=paymentInfo;
    }

    public void setPaymentType(String paymentType) {
	this.paymentType=paymentType;
    }

    public void setType(String type) {
	this.type=type;
    }

    public void setSortDescending(boolean sortDescending) {
	this.sortDescending=sortDescending;
    }

    public void setSortFirst(byte column) {
	this.sortFirst=column;
    }

    public void setSortSecond(byte column) {
	this.sortSecond=column;
    }

    public void setTransID(int transid) {
	this.transid=transid;
    }

    public boolean sortDescending() {
	return sortDescending;
    }

    /**
     * @deprecated  This is maintained only for compatibility with the <code>Streamable</code> interface.
     * 
     * @see  #write(CompressedDataOutputStream,AOServProtocol.Version)
     */
    final public void write(CompressedDataOutputStream out, String version) throws IOException {
        write(out, AOServProtocol.Version.getVersion(version));
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
	out.writeLong(after);
	out.writeLong(before);
	out.writeCompressedInt(transid);
	out.writeBoolean(business!=null); if(business!=null) out.writeUTF(business);
	out.writeBoolean(sourceBusiness!=null); if(sourceBusiness!=null) out.writeUTF(sourceBusiness);
	out.writeBoolean(business_administrator!=null); if(business_administrator!=null) out.writeUTF(business_administrator);
	out.writeBoolean(type!=null); if(type!=null) out.writeUTF(type);
	out.writeBoolean(description!=null); if(description!=null) out.writeUTF(description);
	out.writeBoolean(paymentType!=null); if(paymentType!=null) out.writeUTF(paymentType);
	out.writeBoolean(paymentInfo!=null); if(paymentInfo!=null) out.writeUTF(paymentInfo);
	out.writeByte(payment_confirmed);
	out.writeByte(sortFirst);
	out.writeByte(sortSecond);
	out.writeBoolean(sortDescending);
    }
}
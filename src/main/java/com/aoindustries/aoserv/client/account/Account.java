/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2000-2013, 2016, 2017, 2018, 2019  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-client.
 *
 * aoserv-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.client.account;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.Disablable;
import com.aoindustries.aoserv.client.SimpleAOClient;
import static com.aoindustries.aoserv.client.account.ApplicationResources.accessor;
import com.aoindustries.aoserv.client.billing.MonthlyCharge;
import com.aoindustries.aoserv.client.billing.NoticeLog;
import com.aoindustries.aoserv.client.billing.Package;
import com.aoindustries.aoserv.client.billing.PackageCategory;
import com.aoindustries.aoserv.client.billing.PackageDefinition;
import com.aoindustries.aoserv.client.billing.Transaction;
import com.aoindustries.aoserv.client.billing.TransactionType;
import com.aoindustries.aoserv.client.billing.WhoisHistoryAccount;
import com.aoindustries.aoserv.client.billing.WhoisHistoryAccountTable;
import com.aoindustries.aoserv.client.email.Domain;
import com.aoindustries.aoserv.client.email.Forwarding;
import com.aoindustries.aoserv.client.linux.GroupServer;
import com.aoindustries.aoserv.client.linux.Server;
import com.aoindustries.aoserv.client.linux.UserServer;
import com.aoindustries.aoserv.client.net.Host;
import com.aoindustries.aoserv.client.net.IpAddress;
import com.aoindustries.aoserv.client.payment.CountryCode;
import com.aoindustries.aoserv.client.payment.CreditCard;
import com.aoindustries.aoserv.client.payment.Payment;
import com.aoindustries.aoserv.client.payment.PaymentType;
import com.aoindustries.aoserv.client.payment.Processor;
import com.aoindustries.aoserv.client.pki.EncryptionKey;
import com.aoindustries.aoserv.client.reseller.Brand;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.ticket.Ticket;
import com.aoindustries.dto.DtoFactory;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.io.FastExternalizable;
import com.aoindustries.io.FastObjectInput;
import com.aoindustries.io.FastObjectOutput;
import com.aoindustries.io.TerminalWriter;
import com.aoindustries.net.Email;
import com.aoindustries.net.InetAddress;
import com.aoindustries.sql.SQLUtility;
import com.aoindustries.util.ComparatorUtils;
import com.aoindustries.util.IntList;
import com.aoindustries.util.InternUtils;
import com.aoindustries.util.Internable;
import com.aoindustries.util.SortedArrayList;
import com.aoindustries.validation.InvalidResult;
import com.aoindustries.validation.ValidResult;
import com.aoindustries.validation.ValidationException;
import com.aoindustries.validation.ValidationResult;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputValidation;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A <code>Business</code> is one distinct set of packages, resources, and permissions.
* Some businesses may have child businesses associated with them.  When that is the
 * case, the top level business is ultimately responsible for all actions taken and
 * resources used by itself and all child businesses.
 *
 * @author  AO Industries, Inc.
 */
final public class Account extends CachedObjectAccountNameKey<Account> implements Disablable, Comparable<Account> {

	/**
	 * The unique, case-insensitive identifier for an {@link Account}.  Account names must:
	 * <ul>
	 *   <li>Be non-null</li>
	 *   <li>Be non-empty</li>
	 *   <li>Be between 2 and 32 characters</li>
	 *   <li>Must start with <code>[A-Z,a-z]</code></li>
	 *   <li>Must end with <code>[A-Z,a-z,0-9]</code></li>
	 *   <li>Must contain only <code>[A-Z,a-z,0-9] and underscore(_)</code></li>
	 *   <li>May not have consecutive underscores</li>
	 * </ul>
	 *
	 * @author  AO Industries, Inc.
	 */
	final static public class Name implements
		Comparable<Name>,
		FastExternalizable,
		ObjectInputValidation,
		DtoFactory<com.aoindustries.aoserv.client.dto.AccountName>,
		Internable<Name>
	{

		public static final int MIN_LENGTH = 2;

		public static final int MAX_LENGTH = 32;

		/**
		 * Validates a name.
		 */
		public static ValidationResult validate(String name) {
			if(name==null) return new InvalidResult(accessor, "Account.Name.validate.isNull");
			int len=name.length();

			if(len<MIN_LENGTH) return new InvalidResult(accessor, "Account.Name.validate.tooShort", MIN_LENGTH, len);
			if(len>MAX_LENGTH) return new InvalidResult(accessor, "Account.Name.validate.tooLong", MAX_LENGTH, len);

			char ch=name.charAt(0);
			if(
				(ch<'A' || ch>'Z')
				&& (ch<'a' || ch>'z')
			) return new InvalidResult(accessor, "Account.Name.validate.mustStartAlpha");

			ch=name.charAt(len-1);
			if(
				(ch<'A' || ch>'Z')
				&& (ch<'a' || ch>'z')
				&& (ch<'0' || ch>'9')
			) return new InvalidResult(accessor, "Account.Name.validate.mustEndAlphanumeric");

			for(int pos=1;pos<(len-1);pos++) {
				ch=name.charAt(pos);
				if(ch=='_') {
					if(name.charAt(pos-1)=='_') return new InvalidResult(accessor, "Account.Name.validate.consecutiveUnderscores", pos-1);
				} else if(
					(ch<'A' || ch>'Z')
					&& (ch<'a' || ch>'z')
					&& (ch<'0' || ch>'9')
				) return new InvalidResult(accessor, "Account.Name.validate.invalidCharacter", ch, pos);
			}
			return ValidResult.getInstance();
		}

		private static final ConcurrentMap<String,Name> interned = new ConcurrentHashMap<>();

		/**
		 * @param name  when {@code null}, returns {@code null}
		 */
		public static Name valueOf(String name) throws ValidationException {
			if(name == null) return null;
			//Name existing = interned.get(name);
			//return existing!=null ? existing : new Name(name);
			return new Name(name);
		}

		/*
		public static Name valueOfInterned(String name) throws ValidationException {
			Name existing = interned.get(name);
			return existing!=null ? existing : new Name(name).intern();
		}*/

		private String name;
		private String upperName;

		private Name(String name) throws ValidationException {
			this.name = name;
			this.upperName = name.toUpperCase(Locale.ROOT);
			validate();
		}

		/**
		 * @param  name  Does not validate, should only be used with a known valid value.
		 * @param  upperName  Does not validate, should only be used with a known valid value.
		 */
		private Name(String name, String upperName) {
			ValidationResult result;
			assert (result = validate(name)).isValid() : result.toString();
			assert name.toUpperCase(Locale.ROOT).equals(upperName);
			this.name = name;
			this.upperName = upperName;
		}

		private void validate() throws ValidationException {
			ValidationResult result = validate(name);
			if(!result.isValid()) throw new ValidationException(result);
		}

		@Override
		public boolean equals(Object O) {
			return
				O!=null
				&& O instanceof Name
				&& upperName.equals(((Name)O).upperName)
			;
		}

		@Override
		public int hashCode() {
			return upperName.hashCode();
		}

		@Override
		public int compareTo(Name other) {
			return this==other ? 0 : ComparatorUtils.compareIgnoreCaseConsistentWithEquals(name, other.name);
		}

		@Override
		public String toString() {
			return name;
		}

		/**
		 * Gets the upper-case form of the code.  If two different names are
		 * interned and their toUpperCase is the same String instance, then they are
		 * equal in case-insensitive manner.
		 */
		public String toUpperCase() {
			return upperName;
		}

		/**
		 * Interns this name much in the same fashion as <code>String.intern()</code>.
		 *
		 * @see  String#intern()
		 */
		@Override
		public Name intern() {
			Name existing = interned.get(name);
			if(existing==null) {
				String internedName = name.intern();
				String internedUpperName = upperName.intern();
				Name addMe = (name == internedName) && (upperName == internedUpperName) ? this : new Name(internedName, internedUpperName);
				existing = interned.putIfAbsent(internedName, addMe);
				if(existing==null) existing = addMe;
			}
			return existing;
		}

		@Override
		public com.aoindustries.aoserv.client.dto.AccountName getDto() {
			return new com.aoindustries.aoserv.client.dto.AccountName(name);
		}

		// <editor-fold defaultstate="collapsed" desc="FastExternalizable">
		private static final long serialVersionUID = -4701364475901418693L;

		public Name() {
		}

		@Override
		public long getSerialVersionUID() {
			return serialVersionUID;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			FastObjectOutput fastOut = FastObjectOutput.wrap(out);
			try {
				fastOut.writeFastUTF(name);
			} finally {
				fastOut.unwrap();
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			if(name!=null) throw new IllegalStateException();
			FastObjectInput fastIn = FastObjectInput.wrap(in);
			try {
				name = fastIn.readFastUTF();
				upperName = name.toUpperCase(Locale.ROOT);
			} finally {
				fastIn.unwrap();
			}
		}

		@Override
		public void validateObject() throws InvalidObjectException {
			try {
				validate();
			} catch(ValidationException err) {
				InvalidObjectException newErr = new InvalidObjectException(err.getMessage());
				newErr.initCause(err);
				throw newErr;
			}
		}
		// </editor-fold>
	}

	static final int COLUMN_ACCOUNTING=0;
	static final String COLUMN_ACCOUNTING_name = "accounting";

	/**
	 * The maximum depth of the business tree.
	 */
	public static final int MAXIMUM_BUSINESS_TREE_DEPTH=7;

	/**
	 * The minimum payment for auto-enabling accounts, in pennies.
	 */
	public static final BigDecimal MINIMUM_PAYMENT = BigDecimal.valueOf(3000, 2);

	String contractVersion;
	private long created;

	private long canceled;

	private String cancelReason;

	Name parent;

	private boolean can_add_backup_server;
	private boolean can_add_businesses;
	private boolean can_see_prices;

	int disable_log;
	private String do_not_disable_reason;
	private boolean auto_enable;
	private boolean bill_parent;

	public int addBusinessProfile(
		String name,
		boolean isPrivate,
		String phone,
		String fax,
		String address1,
		String address2,
		String city,
		String state,
		String country,
		String zip,
		boolean sendInvoice,
		String billingContact,
		Set<Email> billingEmail,
		Profile.EmailFormat billingEmailFormat,
		String technicalContact,
		Set<Email> technicalEmail,
		Profile.EmailFormat technicalEmailFormat
	) throws IOException, SQLException {
		return table.getConnector().getAccount().getProfile().addBusinessProfile(
			this,
			name,
			isPrivate,
			phone,
			fax,
			address1,
			address2,
			city,
			state,
			country,
			zip,
			sendInvoice,
			billingContact,
			billingEmail,
			billingEmailFormat,
			technicalContact,
			technicalEmail,
			technicalEmailFormat
		);
	}

	public int addBusinessServer(
		Host server
	) throws IOException, SQLException {
		return table.getConnector().getAccount().getAccountHost().addBusinessServer(this, server);
	}

	public int addCreditCard(
		Processor processor,
		String groupName,
		String cardInfo,
		byte expirationMonth,
		short expirationYear,
		String providerUniqueId,
		String firstName,
		String lastName,
		String companyName,
		Email email,
		String phone,
		String fax,
		String customerId,
		String customerTaxId,
		String streetAddress1,
		String streetAddress2,
		String city,
		String state,
		String postalCode,
		CountryCode countryCode,
		String principalName,
		String description,
		String cardNumber
	) throws IOException, SQLException {
		return table.getConnector().getPayment().getCreditCard().addCreditCard(
			processor,
			this,
			groupName,
			cardInfo,
			expirationMonth,
			expirationYear,
			providerUniqueId,
			firstName,
			lastName,
			companyName,
			email,
			phone,
			fax,
			customerId,
			customerTaxId,
			streetAddress1,
			streetAddress2,
			city,
			state,
			postalCode,
			countryCode,
			principalName,
			description,
			cardNumber
		);
	}

	/**
	 * Adds a transaction in the pending state.
	 */
	public int addCreditCardTransaction(
		Processor processor,
		String groupName,
		boolean testMode,
		int duplicateWindow,
		String orderNumber,
		String currencyCode,
		BigDecimal amount,
		BigDecimal taxAmount,
		boolean taxExempt,
		BigDecimal shippingAmount,
		BigDecimal dutyAmount,
		String shippingFirstName,
		String shippingLastName,
		String shippingCompanyName,
		String shippingStreetAddress1,
		String shippingStreetAddress2,
		String shippingCity,
		String shippingState,
		String shippingPostalCode,
		String shippingCountryCode,
		boolean emailCustomer,
		Email merchantEmail,
		String invoiceNumber,
		String purchaseOrderNumber,
		String description,
		Administrator creditCardCreatedBy,
		String creditCardPrincipalName,
		Account creditCardAccount,
		String creditCardGroupName,
		String creditCardProviderUniqueId,
		String creditCardMaskedCardNumber,
		Byte creditCard_expirationMonth,
		Short creditCard_expirationYear,
		String creditCardFirstName,
		String creditCardLastName,
		String creditCardCompanyName,
		Email creditCardEmail,
		String creditCardPhone,
		String creditCardFax,
		String creditCardCustomerId,
		String creditCardCustomerTaxId,
		String creditCardStreetAddress1,
		String creditCardStreetAddress2,
		String creditCardCity,
		String creditCardState,
		String creditCardPostalCode,
		String creditCardCountryCode,
		String creditCardComments,
		long authorizationTime,
		String authorizationPrincipalName
	) throws IOException, SQLException {
		return table.getConnector().getPayment().getPayment().addCreditCardTransaction(
			processor,
			this,
			groupName,
			testMode,
			duplicateWindow,
			orderNumber,
			currencyCode,
			amount,
			taxAmount,
			taxExempt,
			shippingAmount,
			dutyAmount,
			shippingFirstName,
			shippingLastName,
			shippingCompanyName,
			shippingStreetAddress1,
			shippingStreetAddress2,
			shippingCity,
			shippingState,
			shippingPostalCode,
			shippingCountryCode,
			emailCustomer,
			merchantEmail,
			invoiceNumber,
			purchaseOrderNumber,
			description,
			creditCardCreatedBy,
			creditCardPrincipalName,
			creditCardAccount,
			creditCardGroupName,
			creditCardProviderUniqueId,
			creditCardMaskedCardNumber,
			creditCard_expirationMonth,
			creditCard_expirationYear,
			creditCardFirstName,
			creditCardLastName,
			creditCardCompanyName,
			creditCardEmail,
			creditCardPhone,
			creditCardFax,
			creditCardCustomerId,
			creditCardCustomerTaxId,
			creditCardStreetAddress1,
			creditCardStreetAddress2,
			creditCardCity,
			creditCardState,
			creditCardPostalCode,
			creditCardCountryCode,
			creditCardComments,
			authorizationTime,
			authorizationPrincipalName
		);
	}

	public int addDisableLog(
		String disableReason
	) throws IOException, SQLException {
		return table.getConnector().getAccount().getDisableLog().addDisableLog(this, disableReason);
	}

	public void addNoticeLog(
		String billingContact,
		Email emailAddress,
		BigDecimal balance,
		String type,
		int transid
	) throws IOException, SQLException {
		table.getConnector().getBilling().getNoticeLog().addNoticeLog(
			pkey,
			billingContact,
			emailAddress,
			balance,
			type,
			transid
		);
	}

	public int addPackage(
		Name name,
		PackageDefinition packageDefinition
	) throws IOException, SQLException {
		return table.getConnector().getBilling().getPackage().addPackage(
			name,
			this,
			packageDefinition
		);
	}

	public int addTransaction(
		Account sourceBusiness,
		Administrator business_administrator,
		TransactionType type,
		String description,
		int quantity,
		int rate,
		PaymentType paymentType,
		String paymentInfo,
		Processor processor,
		byte payment_confirmed
	) throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().addTransaction(
			this,
			sourceBusiness,
			business_administrator,
			type.getName(),
			description,
			quantity,
			rate,
			paymentType,
			paymentInfo,
			processor,
			payment_confirmed
		);
	}

	public boolean canAddBackupServer() {
		return can_add_backup_server;
	}

	public boolean canAddBusinesses() {
		return can_add_businesses;
	}

	public void cancel(String cancelReason) throws IllegalArgumentException, IOException, SQLException {
		// Automatically disable if not already disabled
		if(disable_log==-1) {
			new SimpleAOClient(table.getConnector()).disableBusiness(pkey, "Account canceled");
		}

		// Now cancel the account
		if(cancelReason!=null && (cancelReason=cancelReason.trim()).length()==0) cancelReason=null;
		final String finalCancelReason = cancelReason;
		table.getConnector().requestUpdate(
			true,
			AoservProtocol.CommandID.CANCEL_BUSINESS,
			new AOServConnector.UpdateRequest() {
				IntList invalidateList;

				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeUTF(pkey.toString());
					out.writeNullUTF(finalCancelReason);
				}

				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AoservProtocol.DONE) invalidateList=AOServConnector.readInvalidateList(in);
					else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unexpected response code: "+code);
					}
				}

				@Override
				public void afterRelease() {
					table.getConnector().tablesUpdated(invalidateList);
				}
			}
		);
	}

	public boolean canCancel() throws IOException, SQLException {
		return canceled==-1 && !isRootBusiness();
	}

	public boolean isRootBusiness() throws IOException, SQLException {
		return pkey.equals(table.getConnector().getAccount().getAccount().getRootAccount_name());
	}

	@Override
	public boolean canDisable() throws IOException, SQLException {
		// already disabled
		if(disable_log!=-1) return false;

		if(isRootBusiness()) return false;

		// packages
		for(Package pk : getPackages()) if(!pk.isDisabled()) return false;

		return true;
	}

	@Override
	public boolean canEnable() throws SQLException, IOException {
		// Cannot enable a canceled business
		if(canceled!=-1) return false;

		// Can only enable if it is disabled
		DisableLog dl=getDisableLog();
		if(dl==null) return false;
		else return dl.canEnable();
	}

	public boolean canSeePrices() {
		return can_see_prices;
	}

	@Override
	public void disable(DisableLog dl) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(true, AoservProtocol.CommandID.DISABLE, Table.TableID.BUSINESSES, dl.getPkey(), pkey.toString());
	}

	@Override
	public void enable() throws IOException, SQLException {
		table.getConnector().requestUpdateIL(true, AoservProtocol.CommandID.ENABLE, Table.TableID.BUSINESSES, pkey.toString());
	}

	public BigDecimal getAccountBalance() throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().getAccountBalance(pkey);
	}

	public BigDecimal getAccountBalance(long before) throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().getAccountBalance(pkey, before);
	}

	/**
	 * @see  #getAccountBalance()
	 */
	public String getAccountBalanceString() throws IOException, SQLException {
		return "$"+getAccountBalance();
	}

	/**
	 * @see  #getAccountBalance(long)
	 */
	public String getAccountBalanceString(long before) throws IOException, SQLException {
		return "$"+getAccountBalance(before);
	}

	public Name getName() {
		return pkey;
	}

	public boolean getAutoEnable() {
		return auto_enable;
	}

	public boolean billParent() {
		return bill_parent;
	}

	public BigDecimal getAutoEnableMinimumPayment() throws IOException, SQLException {
		BigDecimal balance=getAccountBalance();
		if(balance.signum()<0) return BigDecimal.valueOf(0, 2);
		BigDecimal minimum = balance.divide(BigDecimal.valueOf(2), RoundingMode.DOWN);
		if(minimum.compareTo(MINIMUM_PAYMENT)<0) minimum=MINIMUM_PAYMENT;
		if(minimum.compareTo(balance)>0) minimum=balance;
		return minimum;
	}

	public String getDoNotDisableReason() {
		return do_not_disable_reason;
	}

	/**
	 * Gets the <code>Business</code> in the business tree that is one level down
	 * from the top level business.
	 */
	public Account getTopLevelBusiness() throws IOException, SQLException {
		Name rootAccount_name = table.getConnector().getAccount().getAccount().getRootAccount_name();
		Account bu=this;
		Account tempParent;
		while((tempParent=bu.getParentBusiness())!=null && !tempParent.getName().equals(rootAccount_name)) bu=tempParent;
		return bu;
	}

	/**
	 * Gets the {@link Account} that is responsible for paying the bills created by this business.
	 */
	public Account getBillingAccount() throws SQLException, IOException {
		Account bu=this;
		while(bu.bill_parent) {
			bu=bu.getParentBusiness();
			if(bu==null) throw new SQLException("Unable to find the billing account for '"+pkey+'\'');
		}
		return bu;
	}

	/**
	 * Gets the <code>BusinessProfile</code> with the highest priority.
	 */
	public Profile getBusinessProfile() throws IOException, SQLException {
		return table.getConnector().getAccount().getProfile().getBusinessProfile(this);
	}

	/**
	 * Gets a list of all <code>BusinessProfiles</code> for this <code>Business</code>
	 * sorted with the highest priority profile at the zero index.
	 */
	public List<Profile> getBusinessProfiles() throws IOException, SQLException {
		return table.getConnector().getAccount().getProfile().getBusinessProfiles(this);
	}

	public AccountHost getBusinessServer(
		Host server
	) throws IOException, SQLException {
		return table.getConnector().getAccount().getAccountHost().getBusinessServer(this, server);
	}

	public List<AccountHost> getBusinessServers() throws IOException, SQLException {
		return table.getConnector().getAccount().getAccountHost().getBusinessServers(this);
	}

	public Timestamp getCanceled() {
		return canceled==-1 ? null : new Timestamp(canceled);
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public List<Account> getChildBusinesses() throws IOException, SQLException {
		return table.getConnector().getAccount().getAccount().getChildBusinesses(this);
	}

	@Override
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_ACCOUNTING: return pkey;
			case 1: return contractVersion;
			case 2: return getCreated();
			case 3: return getCanceled();
			case 4: return cancelReason;
			case 5: return parent;
			case 6: return can_add_backup_server;
			case 7: return can_add_businesses;
			case 8: return can_see_prices;
			case 9: return disable_log== -1 ? null : disable_log;
			case 10: return do_not_disable_reason;
			case 11: return auto_enable;
			case 12: return bill_parent;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public BigDecimal getConfirmedAccountBalance() throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().getConfirmedAccountBalance(pkey);
	}

	public BigDecimal getConfirmedAccountBalance(long before) throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().getConfirmedAccountBalance(pkey, before);
	}

	public String getContractVersion() {
		return contractVersion;
	}

	public Timestamp getCreated() {
		return new Timestamp(created);
	}

	public List<Processor> getCreditCardProcessors() throws IOException, SQLException {
		return table.getConnector().getPayment().getProcessor().getCreditCardProcessors(this);
	}

	public List<CreditCard> getCreditCards() throws IOException, SQLException {
		return table.getConnector().getPayment().getCreditCard().getCreditCards(this);
	}

	public Host getDefaultServer() throws IOException, SQLException {
		// May be null when the account is canceled or not using servers
		return table.getConnector().getAccount().getAccountHost().getDefaultServer(this);
	}

	@Override
	public boolean isDisabled() {
		return disable_log!=-1;
	}

	@Override
	public DisableLog getDisableLog() throws SQLException, IOException {
		if(disable_log==-1) return null;
		DisableLog obj=table.getConnector().getAccount().getDisableLog().get(disable_log);
		if(obj==null) throw new SQLException("Unable to find DisableLog: "+disable_log);
		return obj;
	}

	public List<Forwarding> getEmailForwarding() throws SQLException, IOException {
		return table.getConnector().getEmail().getForwarding().getEmailForwarding(this);
	}

	public List<com.aoindustries.aoserv.client.email.List> getEmailLists() throws IOException, SQLException {
		return table.getConnector().getEmail().getList().getEmailLists(this);
	}

	public GroupServer getLinuxServerGroup(Server aoServer) throws IOException, SQLException {
		return table.getConnector().getLinux().getGroupServer().getLinuxServerGroup(aoServer, this);
	}

	public List<com.aoindustries.aoserv.client.linux.User> getMailAccounts() throws IOException, SQLException {
		return table.getConnector().getLinux().getUser().getMailAccounts(this);
	}

	public CreditCard getMonthlyCreditCard() throws IOException, SQLException {
		return table.getConnector().getPayment().getCreditCard().getMonthlyCreditCard(this);
	}

	public List<MonthlyCharge> getMonthlyCharges() throws SQLException, IOException {
		return table.getConnector().getBilling().getMonthlyCharge().getMonthlyCharges(this);
	}

	/**
	 * Gets an approximation of the monthly rate paid by this account.  This is not guaranteed to
	 * be exactly the same as the underlying billing database processes.
	 */
	public BigDecimal getMonthlyRate() throws SQLException, IOException {
		BigDecimal total = BigDecimal.valueOf(0, 2);
		for(MonthlyCharge mc : getMonthlyCharges()) if(mc.isActive()) total = total.add(new BigDecimal(SQLUtility.getDecimal(mc.getPennies())));
		return total;
	}

	/**
	 * @see  #getMonthlyRate()
	 */
	public String getMonthlyRateString() throws SQLException, IOException {
		return "$"+getMonthlyRate();
	}

	public List<NoticeLog> getNoticeLogs() throws IOException, SQLException {
		return table.getConnector().getBilling().getNoticeLog().getNoticeLogs(this);
	}

	public List<Package> getPackages() throws IOException, SQLException {
		return table.getConnector().getBilling().getPackage().getPackages(this);
	}

	public Account getParentBusiness() throws IOException, SQLException {
		if(parent==null) return null;
		// The parent business might not be found, even when the value is set.  This is normal due
		// to filtering.
		return table.getConnector().getAccount().getAccount().get(parent);
	}

	public List<Domain> getEmailDomains() throws SQLException, IOException {
		return table.getConnector().getEmail().getDomain().getEmailDomains(this);
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.BUSINESSES;
	}

	/**
	 * Gets the total monthly rate or <code>null</code> if unavailable.
	 */
	public BigDecimal getTotalMonthlyRate() throws SQLException, IOException {
		BigDecimal sum = BigDecimal.valueOf(0, 2);
		for (Package pack : getPackages()) {
			BigDecimal monthlyRate = pack.getPackageDefinition().getMonthlyRate();
			if(monthlyRate==null) return null;
			sum = sum.add(monthlyRate);
		}
		return sum;
	}

	public List<Transaction> getTransactions() throws IOException, SQLException {
		return table.getConnector().getBilling().getTransaction().getTransactions(pkey);
	}

	/**
	 * @see  WhoisHistoryAccountTable#getWhoisHistoryAccounts(com.aoindustries.aoserv.client.account.Account)
	 */
	public List<WhoisHistoryAccount> getWhoisHistoryAccounts() throws IOException, SQLException {
		return table.getConnector().getBilling().getWhoisHistoryAccount().getWhoisHistoryAccounts(this);
	}

	/**
	 * @deprecated  Please use <code>isBusinessOrParentOf</code> instead.
	 */
	@Deprecated
	public boolean isBusinessOrParent(Account other) throws IOException, SQLException {
		return isBusinessOrParentOf(other);
	}

	/**
	 * Determines if this <code>Business</code> is the other business
	 * or a parent of it.  This is often used for access control between
	 * accounts.
	 */
	public boolean isBusinessOrParentOf(Account other) throws IOException, SQLException {
		while(other!=null) {
			if(equals(other)) return true;
			other=other.getParentBusiness();
		}
		return false;
	}

	/**
	 * Determines if this <code>Business</code> is a parent of the other business.
	 * This is often used for access control between accounts.
	 */
	public boolean isParentOf(Account other) throws IOException, SQLException {
		if(other!=null) {
			other=other.getParentBusiness();
			while(other!=null) {
				if(equals(other)) return true;
				other=other.getParentBusiness();
			}
		}
		return false;
	}

	public void move(Server from, Server to, TerminalWriter out) throws IOException, SQLException {
		if(from.equals(to)) throw new SQLException("Cannot move from AOServer "+from.getHostname()+" to AOServer "+to.getHostname()+": same AOServer");

		AccountHost fromBusinessServer=getBusinessServer(from.getServer());
		if(fromBusinessServer==null) throw new SQLException("Unable to find BusinessServer for Business="+pkey+" and Server="+from.getHostname());

		// Grant the Business access to the other server if it does not already have access
		if(out!=null) {
			out.boldOn();
			out.println("Adding Business Privileges");
			out.attributesOff();
			out.flush();
		}
		AccountHost toBusinessServer=getBusinessServer(to.getServer());
		if(toBusinessServer==null) {
			if(out!=null) {
				out.print("    ");
				out.println(to.getHostname());
				out.flush();
			}
			addBusinessServer(to.getServer());
		}

		// Add the LinuxServerGroups
		if(out!=null) {
			out.boldOn();
			out.println("Adding Linux Groups");
			out.attributesOff();
			out.flush();
		}
		List<GroupServer> fromLinuxServerGroups=new ArrayList<>();
		List<GroupServer> toLinuxServerGroups=new SortedArrayList<>();
		{
			for(GroupServer lsg : table.getConnector().getLinux().getGroupServer().getRows()) {
				Package pk=lsg.getLinuxGroup().getPackage();
				if(pk!=null && pk.getBusiness().equals(this)) {
					Server ao=lsg.getAOServer();
					if(ao.equals(from)) fromLinuxServerGroups.add(lsg);
					else if(ao.equals(to)) toLinuxServerGroups.add(lsg);
				}
			}
		}
		for (GroupServer lsg : fromLinuxServerGroups) {
			if(!toLinuxServerGroups.contains(lsg)) {
				if(out!=null) {
					out.print("    ");
					out.print(lsg.getLinuxGroup());
					out.print(" to ");
					out.println(to.getHostname());
					out.flush();
				}
				lsg.getLinuxGroup().addLinuxServerGroup(to);
			}
		}

		// Add the LinuxServerAccounts
		if(out!=null) {
			out.boldOn();
			out.println("Adding Linux Accounts");
			out.attributesOff();
			out.flush();
		}
		List<UserServer> fromLinuxServerAccounts=new ArrayList<>();
		List<UserServer> toLinuxServerAccounts=new SortedArrayList<>();
		{
			List<UserServer> lsas=table.getConnector().getLinux().getUserServer().getRows();
			for (UserServer lsa : lsas) {
				Package pk=lsa.getLinuxAccount().getUsername().getPackage();
				if(pk!=null && pk.getBusiness().equals(this)) {
					Server ao=lsa.getAOServer();
					if(ao.equals(from)) fromLinuxServerAccounts.add(lsa);
					else if(ao.equals(to)) toLinuxServerAccounts.add(lsa);
				}
			}
		}
		for (UserServer lsa : fromLinuxServerAccounts) {
			if(!toLinuxServerAccounts.contains(lsa)) {
				if(out!=null) {
					out.print("    ");
					out.print(lsa.getLinuxAccount());
					out.print(" to ");
					out.println(to.getHostname());
					out.flush();
				}
				lsa.getLinuxAccount().addLinuxServerAccount(to, lsa.getHome());
			}
		}

		// Wait for Linux Account rebuild
		if(out!=null) {
			out.boldOn();
			out.println("Waiting for Linux Account rebuild");
			out.attributesOff();
			out.print("    ");
			out.println(to.getHostname());
			out.flush();
		}
		to.waitForLinuxAccountRebuild();

		// Copy the home directory contents
		if(out!=null) {
			out.boldOn();
			out.println("Copying Home Directories");
			out.attributesOff();
			out.flush();
		}
		for (UserServer lsa : fromLinuxServerAccounts) {
			if(!toLinuxServerAccounts.contains(lsa)) {
				if(out!=null) {
					out.print("    ");
					out.print(lsa.getLinuxAccount());
					out.print(" to ");
					out.print(to.getHostname());
					out.print(": ");
					out.flush();
				}
				long byteCount=lsa.copyHomeDirectory(to);
				if(out!=null) {
					out.print(byteCount);
					out.println(byteCount==1?" byte":" bytes");
					out.flush();
				}
			}
		}

		// Copy the cron tables
		if(out!=null) {
			out.boldOn();
			out.println("Copying Cron Tables");
			out.attributesOff();
			out.flush();
		}
		for (UserServer lsa : fromLinuxServerAccounts) {
			if(!toLinuxServerAccounts.contains(lsa)) {
				if(out!=null) {
					out.print("    ");
					out.print(lsa.getLinuxAccount());
					out.print(" to ");
					out.print(to.getHostname());
					out.print(": ");
					out.flush();
				}
				String cronTable=lsa.getCronTable();
				lsa.getLinuxAccount().getLinuxServerAccount(to).setCronTable(cronTable);
				if(out!=null) {
					out.print(cronTable.length());
					out.println(cronTable.length()==1?" byte":" bytes");
					out.flush();
				}
			}
		}

		// Copy the passwords
		if(out!=null) {
			out.boldOn();
			out.println("Copying Passwords");
			out.attributesOff();
			out.flush();
		}
		for (UserServer lsa : fromLinuxServerAccounts) {
			if(!toLinuxServerAccounts.contains(lsa)) {
				if(out!=null) {
					out.print("    ");
					out.print(lsa.getLinuxAccount());
					out.print(" to ");
					out.println(to.getHostname());
					out.flush();
				}

				lsa.copyPassword(lsa.getLinuxAccount().getLinuxServerAccount(to));
			}
		}

		// Move IP Addresses
		if(out!=null) {
			out.boldOn();
			out.println("Moving IP Addresses");
			out.attributesOff();
			out.flush();
		}
		List<IpAddress> ips=table.getConnector().getNet().getIpAddress().getRows();
		for (IpAddress ip : ips) {
			InetAddress inetAddress = ip.getInetAddress();
			if(
				ip.isAlias()
				&& !inetAddress.isUnspecified()
				&& !ip.getDevice().getDeviceId().isLoopback()
				&& ip.getPackage().getAccount_name().equals(pkey)
			) {
				if(out!=null) {
					out.print("    ");
					out.println(ip);
				}
				ip.moveTo(to.getServer());
			}
		} // TODO: Continue development here



		// Remove the LinuxServerAccounts
		if(out!=null) {
			out.boldOn();
			out.println("Removing Linux Accounts");
			out.attributesOff();
			out.flush();
		}
		for (UserServer lsa : fromLinuxServerAccounts) {
			if(out!=null) {
				out.print("    ");
				out.print(lsa.getLinuxAccount());
				out.print(" on ");
				out.println(from.getHostname());
				out.flush();
			}
			lsa.remove();
		}

		// Remove the LinuxServerGroups
		if(out!=null) {
			out.boldOn();
			out.println("Removing Linux Groups");
			out.attributesOff();
			out.flush();
		}
		for (GroupServer lsg : fromLinuxServerGroups) {
			if(out!=null) {
				out.print("    ");
				out.print(lsg.getLinuxGroup());
				out.print(" on ");
				out.println(from.getHostname());
				out.flush();
			}
			lsg.remove();
		}

		// Remove access to the old server
		if(out!=null) {
			out.boldOn();
			out.println("Removing Business Privileges");
			out.attributesOff();
			out.print("    ");
			out.println(from.getHostname());
			out.flush();
		}
		fromBusinessServer.remove();
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			pkey = Name.valueOf(result.getString(1));
			contractVersion = result.getString(2);
			created = result.getTimestamp(3).getTime();
			Timestamp T = result.getTimestamp(4);
			canceled = T==null ? -1 : T.getTime();
			cancelReason = result.getString(5);
			parent = Name.valueOf(result.getString(6));
			can_add_backup_server=result.getBoolean(7);
			can_add_businesses=result.getBoolean(8);
			can_see_prices=result.getBoolean(9);
			disable_log=result.getInt(10);
			if(result.wasNull()) disable_log=-1;
			do_not_disable_reason=result.getString(11);
			auto_enable=result.getBoolean(12);
			bill_parent=result.getBoolean(13);
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		try {
			pkey=Name.valueOf(in.readUTF()).intern();
			contractVersion=InternUtils.intern(in.readNullUTF());
			created=in.readLong();
			canceled=in.readLong();
			cancelReason=in.readNullUTF();
			parent=InternUtils.intern(Name.valueOf(in.readNullUTF()));
			can_add_backup_server=in.readBoolean();
			can_add_businesses=in.readBoolean();
			can_see_prices=in.readBoolean();
			disable_log=in.readCompressedInt();
			do_not_disable_reason=in.readNullUTF();
			auto_enable=in.readBoolean();
			bill_parent=in.readBoolean();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	public void setName(Name name) throws SQLException, IOException {
		table.getConnector().requestUpdateIL(true, AoservProtocol.CommandID.SET_BUSINESS_ACCOUNTING, this.pkey.toString(), name.toString());
	}

	@Override
	public void write(CompressedDataOutputStream out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeUTF(pkey.toString());
		out.writeBoolean(contractVersion!=null); if(contractVersion!=null) out.writeUTF(contractVersion);
		out.writeLong(created);
		out.writeLong(canceled);
		out.writeNullUTF(cancelReason);
		out.writeNullUTF(Objects.toString(parent, null));
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_0_A_102)>=0) out.writeBoolean(can_add_backup_server);
		out.writeBoolean(can_add_businesses);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_0_A_122)<=0) out.writeBoolean(false);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_0_A_103)>=0) out.writeBoolean(can_see_prices);
		out.writeCompressedInt(disable_log);
		out.writeNullUTF(do_not_disable_reason);
		out.writeBoolean(auto_enable);
		out.writeBoolean(bill_parent);
	}

	public List<Ticket> getTickets() throws SQLException, IOException {
		return table.getConnector().getTicket().getTicket().getTickets(this);
	}

	/**
	 * Gets all of the encryption keys for this business.
	 */
	public List<EncryptionKey> getEncryptionKeys() throws IOException, SQLException {
		return table.getConnector().getPki().getEncryptionKey().getEncryptionKeys(this);
	}

	/**
	 * Sets the credit card that will be used monthly.  Any other selected card will
	 * be deselected.  If <code>creditCard</code> is null, none will be used automatically.
	 */
	public void setUseMonthlyCreditCard(CreditCard creditCard) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(
			true,
			AoservProtocol.CommandID.SET_CREDIT_CARD_USE_MONTHLY,
			pkey.toString(),
			creditCard==null ? -1 : creditCard.getPkey()
		);
	}

	/**
	 * Gets the most recent credit card transaction.
	 */
	public Payment getLastCreditCardTransaction() throws IOException, SQLException {
		return table.getConnector().getPayment().getPayment().getLastCreditCardTransaction(this);
	}

	/**
	 * Gets the Brand for this business or <code>null</code> if not a brand.
	 */
	public Brand getBrand() throws IOException, SQLException {
		return table.getConnector().getReseller().getBrand().getBrand(this);
	}

	public int addPackageDefinition(
		PackageCategory category,
		String name,
		String version,
		String display,
		String description,
		int setupFee,
		TransactionType setupFeeTransactionType,
		int monthlyRate,
		TransactionType monthlyRateTransactionType
	) throws IOException, SQLException {
		return table.getConnector().getBilling().getPackageDefinition().addPackageDefinition(
			this,
			category,
			name,
			version,
			display,
			description,
			setupFee,
			setupFeeTransactionType,
			monthlyRate,
			monthlyRateTransactionType
		);
	}

	public PackageDefinition getPackageDefinition(PackageCategory category, String name, String version) throws IOException, SQLException {
		return table.getConnector().getBilling().getPackageDefinition().getPackageDefinition(this, category, name, version);
	}

	public List<PackageDefinition> getPackageDefinitions(PackageCategory category) throws IOException, SQLException {
		return table.getConnector().getBilling().getPackageDefinition().getPackageDefinitions(this, category);
	}

	/**
	 * Gets all active package definitions for this business.
	 */
	public Map<PackageCategory,List<PackageDefinition>> getActivePackageDefinitions() throws IOException, SQLException {
		// Determine the active packages per category
		List<PackageCategory> allCategories = table.getConnector().getBilling().getPackageCategory().getRows();
		Map<PackageCategory,List<PackageDefinition>> categories = new LinkedHashMap<>(allCategories.size()*4/3+1);
		for(PackageCategory category : allCategories) {
			List<PackageDefinition> allDefinitions = getPackageDefinitions(category);
			List<PackageDefinition> definitions = new ArrayList<>(allDefinitions.size());
			for(PackageDefinition definition : allDefinitions) {
				if(definition.isActive()) definitions.add(definition);
			}
			if(!definitions.isEmpty()) categories.put(category, Collections.unmodifiableList(definitions));
		}
		return Collections.unmodifiableMap(categories);
	}

	@Override
	public int compareTo(Account o) {
		return pkey.compareTo(o.pkey);
	}
}

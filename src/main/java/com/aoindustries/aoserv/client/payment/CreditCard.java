/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2000-2013, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.payment;

import com.aoapps.collections.IntList;
import com.aoapps.hodgepodge.io.stream.StreamableInput;
import com.aoapps.hodgepodge.io.stream.StreamableOutput;
import com.aoapps.lang.math.SafeMath;
import com.aoapps.lang.util.InternUtils;
import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.Email;
import com.aoapps.sql.SQLStreamables;
import com.aoapps.sql.SQLUtility;
import com.aoapps.sql.UnmodifiableTimestamp;
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedObjectIntegerKey;
import com.aoindustries.aoserv.client.CannotRemoveReason;
import com.aoindustries.aoserv.client.Removable;
import com.aoindustries.aoserv.client.account.Account;
import com.aoindustries.aoserv.client.account.Administrator;
import com.aoindustries.aoserv.client.account.User;
import com.aoindustries.aoserv.client.pki.EncryptionKey;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.schema.Type;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A <code>CreditCard</code> stores credit card information.
 *
 * @author  AO Industries, Inc.
 */
final public class CreditCard extends CachedObjectIntegerKey<CreditCard> implements Removable {

	static final int
		COLUMN_PKEY=0,
		COLUMN_PROCESSOR_ID=1,
		COLUMN_ACCOUNTING=2
	;
	static final String COLUMN_ACCOUNTING_name = "accounting";
	static final String COLUMN_CREATED_name = "created";

	/**
	 * Randomizes a value by adding a random number of random characters between each character of the original String.
	 * The original string must be only comprised of 0-9, space, -, and /
	 *
	 * @see  #derandomize(String)
	 */
	static String randomize(String original) {
		SecureRandom secureRandom = AOServConnector.getSecureRandom();
		StringBuilder randomized = new StringBuilder();
		for(int c=0, len=original.length(); c<=len; c++) {
			int randChars = secureRandom.nextInt(20);
			for(int d=0;d<randChars;d++) {
				int randVal = secureRandom.nextInt(256-32-10-3); // Skipping 0-31, 32 (space), 45 (-), 47 (/), 48-57 (0-9)
				// Offset past the first 33
				randVal += 33;
				// Offset past the -
				if(randVal>=45) randVal++;
				// Offset past the / and 0-9
				if(randVal>=47) randVal += 11;
				randomized.append((char)randVal);
			}
			if(c<len) randomized.append(original.charAt(c));
		}
		return randomized.toString();
	}

	/**
	 * Derandomizes a value be stripping out all characters that are not 0-9, space, -, or /
	 *
	 * @see  #randomize(String)
	 */
	static String derandomize(String randomized) {
		// Strip all characters except 0-9, space, and -
		StringBuilder stripped = new StringBuilder(randomized.length());
		for(int c=0, len=randomized.length();c<len;c++) {
			char ch = randomized.charAt(c);
			if(
				(ch>='0' && ch<='9')
				|| ch==' '
				|| ch=='-'
				|| ch=='/'
			) stripped.append(ch);
		}
		return stripped.toString();
	}

	private String processorId;
	private Account.Name accounting;
	private String groupName;
	private String cardInfo; // TODO: Rename to maskedCardNumber
	private Byte expirationMonth;
	private Short expirationYear;
	private String providerUniqueId;
	private String firstName;
	private String lastName;
	private String companyName;
	private Email email;
	private String phone;
	private String fax;
	private String customerId;
	private String customerTaxId;
	private String streetAddress1;
	private String streetAddress2;
	private String city;
	private String state;
	private String postalCode;
	private String countryCode;
	private UnmodifiableTimestamp created;
	private User.Name createdBy;
	private String principalName;
	private boolean useMonthly;
	private boolean isActive;
	private UnmodifiableTimestamp deactivatedOn;
	private String deactivateReason;
	private String description;
	private String encrypted_card_number;
	private int encryption_card_number_from;
	private int encryption_card_number_recipient;

	// These are not pulled from the database, but are decrypted by GPG
	transient private String decryptCardNumberPassphrase;
	transient private String card_number;

	@Override
	public List<CannotRemoveReason<?>> getCannotRemoveReasons() {
		return Collections.emptyList();
	}

	/**
	 * Flags a card as declined.
	 */
	public void declined(String reason) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(
			true,
			AoservProtocol.CommandID.CREDIT_CARD_DECLINED,
			pkey,
			reason
		);
	}

	public int getId() {
		return pkey;
	}

	/**
	 * Gets the processor that is storing the credit card numbers.
	 */
	public Processor getCreditCardProcessor() throws SQLException, IOException {
		Processor ccp = table.getConnector().getPayment().getProcessor().get(processorId);
		if(ccp==null) throw new SQLException("Unable to find CreditCardProcessor: "+processorId);
		return ccp;
	}

	public Account.Name getAccount_name() {
		return accounting;
	}

	public Account getAccount() throws SQLException, IOException {
		Account obj = table.getConnector().getAccount().getAccount().get(accounting);
		if (obj == null) throw new SQLException("Unable to find Account: " + accounting);
		return obj;
	}

	/**
	 * Gets the application-specific grouping for this credit card.
	 */
	public String getGroupName() {
		return groupName;
	}

	public String getCardInfo() {
		return cardInfo;
	}

	public Byte getExpirationMonth() {
		return expirationMonth;
	}

	public Short getExpirationYear() {
		return expirationYear;
	}

	/**
	 * Gets the unique identifier that represents the CISP - compliant storage mechanism for the card
	 * number and expiration date.
	 */
	public String getProviderUniqueId() {
		return providerUniqueId;
	}

	/**
	 * Gets the default card info for a credit card number.
	 *
	 * @deprecated  Please use <code>com.aoapps.payments.CreditCard#maskCardNumber(String)</code> instead.
	 */
	@Deprecated
	public static String getCardInfo(String cardNumber) {
		String nums = "";
		int len = cardNumber.length();
		for (int c = (len - 1); c >= 0; c--) {
			char ch = cardNumber.charAt(c);
			if (ch >= '0' && ch <= '9') {
				nums = ch + nums;
				if (nums.length() >= 4) return nums;
			}
		}
		return nums;
	}

	@Override
	@SuppressWarnings("ReturnOfDateField") // UnmodifiableTimestamp
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_PROCESSOR_ID: return processorId;
			case COLUMN_ACCOUNTING: return accounting;
			case 3: return groupName;
			case 4: return cardInfo;
			case 5: return expirationMonth == null ? null : expirationMonth.shortValue(); // TODO: Add "byte" type back to AOServ?
			case 6: return expirationYear;
			case 7: return providerUniqueId;
			case 8: return firstName;
			case 9: return lastName;
			case 10: return companyName;
			case 11: return email;
			case 12: return phone;
			case 13: return fax;
			case 14: return customerId;
			case 15: return customerTaxId;
			case 16: return streetAddress1;
			case 17: return streetAddress2;
			case 18: return city;
			case 19: return state;
			case 20: return postalCode;
			case 21: return countryCode;
			case 22: return created;
			case 23: return createdBy;
			case 24: return principalName;
			case 25: return useMonthly;
			case 26: return isActive;
			case 27: return deactivatedOn;
			case 28: return deactivateReason;
			case 29: return description;
			case 30: return encrypted_card_number;
			case 31: return encryption_card_number_from;
			case 32: return encryption_card_number_recipient;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public Email getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getFax() {
		return fax;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getCustomerTaxId() {
		return customerTaxId;
	}

	public String getStreetAddress1() {
		return streetAddress1;
	}

	public String getStreetAddress2() {
		return streetAddress2;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public CountryCode getCountryCode() throws SQLException, IOException {
		CountryCode countryCodeObj = table.getConnector().getPayment().getCountryCode().get(this.countryCode);
		if (countryCodeObj == null) throw new SQLException("Unable to find CountryCode: " + this.countryCode);
		return countryCodeObj;
	}

	@SuppressWarnings("ReturnOfDateField") // UnmodifiableTimestamp
	public UnmodifiableTimestamp getCreated() {
		return created;
	}

	public Administrator getCreatedBy() throws SQLException, IOException {
		Administrator administrator = table.getConnector().getAccount().getAdministrator().get(createdBy);
		if (administrator == null) throw new SQLException("Unable to find Administrator: " + createdBy);
		return administrator;
	}

	/**
	 * Gets the application-provided principal name that stored this credit card.
	 */
	public String getPrincipalName() {
		return principalName;
	}

	@SuppressWarnings("ReturnOfDateField") // UnmodifiableTimestamp
	public UnmodifiableTimestamp getDeactivatedOn() {
		return deactivatedOn;
	}

	public String getDeactivatedOnString() {
		return SQLUtility.formatDate(deactivatedOn, Type.DATE_TIME_ZONE);
	}

	public String getDeactivateReason() {
		return deactivateReason;
	}

	public String getDescription() {
		return description;
	}

	public EncryptionKey getEncryptionCardNumberFrom() throws SQLException, IOException {
		if(encryption_card_number_from==-1) return null;
		EncryptionKey ek = table.getConnector().getPki().getEncryptionKey().get(encryption_card_number_from);
		if(ek == null) throw new SQLException("Unable to find EncryptionKey: "+encryption_card_number_from);
		return ek;
	}

	public EncryptionKey getEncryptionCardNumberRecipient() throws SQLException, IOException {
		if(encryption_card_number_recipient==-1) return null;
		EncryptionKey er = table.getConnector().getPki().getEncryptionKey().get(encryption_card_number_recipient);
		if(er == null) throw new SQLException("Unable to find EncryptionKey: "+encryption_card_number_recipient);
		return er;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.CREDIT_CARDS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			int pos = 1;
			pkey = result.getInt(pos++);
			processorId = result.getString(pos++);
			accounting = Account.Name.valueOf(result.getString(pos++));
			groupName = result.getString(pos++);
			cardInfo = result.getString(pos++);
			expirationMonth = SafeMath.castByte(result.getShort(pos++));
			if(result.wasNull()) expirationMonth = null;
			expirationYear = result.getShort(pos++);
			if(result.wasNull()) expirationYear = null;
			providerUniqueId = result.getString(pos++);
			firstName = result.getString(pos++);
			lastName = result.getString(pos++);
			companyName = result.getString(pos++);
			email = Email.valueOf(result.getString(pos++));
			phone = result.getString(pos++);
			fax = result.getString(pos++);
			customerId = result.getString(pos++);
			customerTaxId = result.getString(pos++);
			streetAddress1 = result.getString(pos++);
			streetAddress2 = result.getString(pos++);
			city = result.getString(pos++);
			state = result.getString(pos++);
			postalCode = result.getString(pos++);
			countryCode = result.getString(pos++);
			created = UnmodifiableTimestamp.valueOf(result.getTimestamp(pos++));
			createdBy = User.Name.valueOf(result.getString(pos++));
			principalName = result.getString(pos++);
			useMonthly = result.getBoolean(pos++);
			isActive = result.getBoolean(pos++);
			deactivatedOn = UnmodifiableTimestamp.valueOf(result.getTimestamp(pos++));
			deactivateReason = result.getString(pos++);
			description = result.getString(pos++);
			encrypted_card_number = result.getString(pos++);
			encryption_card_number_from = result.getInt(pos++);
			if(result.wasNull()) encryption_card_number_from = -1;
			encryption_card_number_recipient = result.getInt(pos++);
			if(result.wasNull()) encryption_card_number_recipient = -1;
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	public boolean getIsActive() {
		return isActive;
	}

	@Override
	public void read(StreamableInput in, AoservProtocol.Version protocolVersion) throws IOException {
		try {
			pkey = in.readCompressedInt();
			processorId = in.readUTF().intern();
			accounting = Account.Name.valueOf(in.readUTF()).intern();
			groupName = in.readNullUTF();
			cardInfo = in.readUTF();
			expirationMonth = in.readNullByte();
			expirationYear = in.readNullShort();
			providerUniqueId = in.readUTF();
			firstName = in.readUTF();
			lastName = in.readUTF();
			companyName = in.readNullUTF();
			email = Email.valueOf(in.readNullUTF());
			phone = in.readNullUTF();
			fax = in.readNullUTF();
			customerId = in.readNullUTF();
			customerTaxId = in.readNullUTF();
			streetAddress1 = in.readUTF();
			streetAddress2 = in.readNullUTF();
			city = in.readUTF();
			state = InternUtils.intern(in.readNullUTF());
			postalCode=in.readNullUTF();
			countryCode = in.readUTF().intern();
			created = SQLStreamables.readUnmodifiableTimestamp(in);
			createdBy = User.Name.valueOf(in.readUTF()).intern();
			principalName = in.readNullUTF();
			useMonthly = in.readBoolean();
			isActive = in.readBoolean();
			deactivatedOn = SQLStreamables.readNullUnmodifiableTimestamp(in);
			deactivateReason = in.readNullUTF();
			description = in.readNullUTF();
			encrypted_card_number = in.readNullUTF();
			encryption_card_number_from = in.readCompressedInt();
			encryption_card_number_recipient = in.readCompressedInt();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void remove() throws IOException, SQLException {
		table.getConnector().requestUpdateIL(true, AoservProtocol.CommandID.REMOVE, Table.TableID.CREDIT_CARDS, pkey);
	}

	@Override
	public String toStringImpl() {
		return providerUniqueId != null ? providerUniqueId : cardInfo;
	}

	public boolean getUseMonthly() {
		return useMonthly;
	}

	@Override
	public void write(StreamableOutput out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_29)>=0) out.writeUTF(processorId);
		out.writeUTF(accounting.toString());
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_28)<=0) out.writeCompressedInt(0);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_29)>=0) out.writeNullUTF(groupName);
		out.writeUTF(cardInfo);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_82_0) >= 0) {
			out.writeNullByte(expirationMonth);
			out.writeNullShort(expirationYear);
		}
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_28)<=0) {
			out.writeCompressedInt(0);
			out.writeCompressedInt(0);
			out.writeCompressedInt(0);
			out.writeCompressedInt(0);
			out.writeCompressedInt(0);
			if(state==null) out.writeCompressedInt(-1);
			else out.writeCompressedInt(0);
			out.writeCompressedInt(0);
		}
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_29)>=0) {
			out.writeUTF(providerUniqueId);
			out.writeUTF(firstName);
			out.writeUTF(lastName);
			out.writeNullUTF(companyName);
			out.writeNullUTF(Objects.toString(email, null));
			out.writeNullUTF(phone);
			out.writeNullUTF(fax);
			if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_82_1) >= 0) {
				out.writeNullUTF(customerId);
			}
			out.writeNullUTF(customerTaxId);
			out.writeUTF(streetAddress1);
			out.writeNullUTF(streetAddress2);
			out.writeUTF(city);
			out.writeNullUTF(state);
			out.writeNullUTF(postalCode);
			out.writeUTF(countryCode);
		}
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_83_0) < 0) {
			out.writeLong(created.getTime());
		} else {
			SQLStreamables.writeTimestamp(created, out);
		}
		out.writeUTF(createdBy.toString());
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_29)>=0) out.writeNullUTF(principalName);
		out.writeBoolean(useMonthly);
		out.writeBoolean(isActive);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_83_0) < 0) {
			out.writeLong(deactivatedOn == null ? -1 : deactivatedOn.getTime());
		} else {
			SQLStreamables.writeNullTimestamp(deactivatedOn, out);
		}
		out.writeNullUTF(deactivateReason);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_28)<=0) out.writeCompressedInt(Integer.MAX_VALUE - pkey);
		out.writeNullUTF(description);
		if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_31)>=0) {
			out.writeNullUTF(encrypted_card_number);
			out.writeCompressedInt(encryption_card_number_from);
			out.writeCompressedInt(encryption_card_number_recipient);
			if(protocolVersion.compareTo(AoservProtocol.Version.VERSION_1_82_0) < 0) {
				out.writeNullUTF(null); // encrypted_expiration
				out.writeCompressedInt(-1); // encryption_expiration_from
				out.writeCompressedInt(-1); // encryption_expiration_recipient
			}
		}
	}

	/**
	 * Updates the credit card information (not including the card number or expiration).
	 */
	public void update(
		String cardInfo,
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
		String description
	) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(
			true,
			AoservProtocol.CommandID.UPDATE_CREDIT_CARD,
			pkey,
			cardInfo,
			firstName,
			lastName,
			companyName==null ? "" : companyName,
			email==null ? "" : email.toString(),
			phone==null ? "" : phone,
			fax==null ? "" : fax,
			customerId == null ? "" : customerId,
			customerTaxId==null ? "" : customerTaxId,
			streetAddress1,
			streetAddress2==null ? "" : streetAddress2,
			city,
			state==null ? "" : state,
			postalCode==null ? "" : postalCode,
			countryCode.getCode(),
			description==null ? "" : description
		);
	}

	/**
	 * Updates the credit card number and expiration, including the masked card number.
	 * Encrypts the data if the processors has been configured to store card encrypted
	 * in the master database.
	 */
	public void updateCardNumberAndExpiration(
		final String maskedCardNumber,
		String cardNumber,
		final byte expirationMonth,
		final short expirationYear
	) throws IOException, SQLException {
		Processor processor = getCreditCardProcessor();
		final EncryptionKey encryptionFrom = processor.getEncryptionFrom();
		final EncryptionKey encryptionRecipient = processor.getEncryptionRecipient();
		final String encryptedCardNumber;
		if(encryptionFrom != null && encryptionRecipient != null) {
			// Encrypt the card number and expiration
			encryptedCardNumber = encryptionFrom.encrypt(encryptionRecipient, randomize(cardNumber));
		} else {
			encryptedCardNumber = null;
		}

		table.getConnector().requestUpdate(
			true,
			AoservProtocol.CommandID.UPDATE_CREDIT_CARD_NUMBER_AND_EXPIRATION,
			new AOServConnector.UpdateRequest() {
				private IntList invalidateList;

				@Override
				public void writeRequest(StreamableOutput out) throws IOException {
					out.writeCompressedInt(pkey);
					out.writeUTF(maskedCardNumber);
					out.writeByte(expirationMonth);
					out.writeShort(expirationYear);
					out.writeNullUTF(encryptedCardNumber);
					out.writeCompressedInt(encryptionFrom==null ? -1 : encryptionFrom.getPkey());
					out.writeCompressedInt(encryptionRecipient==null ? -1 : encryptionRecipient.getPkey());
				}

				@Override
				public void readResponse(StreamableInput in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AoservProtocol.DONE) {
						invalidateList=AOServConnector.readInvalidateList(in);
					} else {
						AoservProtocol.checkResult(code, in);
						throw new IOException("Unknown response code: "+code);
					}
				}

				@Override
				public void afterRelease() {
					table.getConnector().tablesUpdated(invalidateList);
				}
			}
		);
	}

	/**
	 * Updates the credit card expiration.  Encrypts the data if the processors
	 * has been configured to store card encrypted in the master database.
	 */
	public void updateCardExpiration(
		byte expirationMonth,
		short expirationYear
	) throws IOException, SQLException {
		table.getConnector().requestUpdateIL(
			true,
			AoservProtocol.CommandID.UPDATE_CREDIT_CARD_EXPIRATION,
			pkey,
			expirationMonth,
			expirationYear
		);
	}

	/**
	 * Reactivates a credit card.
	 */
	public void reactivate() throws IOException, SQLException {
		table.getConnector().requestUpdateIL(
			true,
			AoservProtocol.CommandID.REACTIVATE_CREDIT_CARD,
			pkey
		);
	}

	/**
	 * Gets the card number or {@code null} if not stored.
	 */
	synchronized public String getCardNumber(String passphrase) throws IOException, SQLException {
		// If a different passphrase is provided, don't use the cached values, clear, and re-decrypt
		if(decryptCardNumberPassphrase==null || !passphrase.equals(decryptCardNumberPassphrase)) {
			// Clear first just in case there is a problem in part of the decryption
			decryptCardNumberPassphrase=null;
			card_number=null;

			if(encrypted_card_number!=null) {
				// Perform the decryption
				card_number = derandomize(getEncryptionCardNumberRecipient().decrypt(encrypted_card_number, passphrase));
			}
			decryptCardNumberPassphrase=passphrase;
		}
		return card_number;
	}
}

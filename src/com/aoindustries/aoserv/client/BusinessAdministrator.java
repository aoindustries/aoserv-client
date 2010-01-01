package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.table.IndexType;
import com.aoindustries.util.Base64Coder;
import com.aoindustries.util.StringUtility;
import com.aoindustries.util.WrappedException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;

/**
 * A <code>BusinessAdministrator</code> is a username and password pair, usually
 * representing an individual or an application, that has administrative control
 * over all resources in a <code>Business</code> or any any of its child businesses.
 *
 * @see  Business
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessAdministrator extends AOServObjectStringKey<BusinessAdministrator> implements BeanFactory<com.aoindustries.aoserv.client.beans.BusinessAdministrator> /* TODO: implements PasswordProtected, Removable, Disablable, Comparable<BusinessAdministrator> */ {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final long serialVersionUID = 1L;

    /**
     * Value representing no password.
     */
    public static final String NO_PASSWORD = "*";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Password Encryption">
    /**
     * Encrypts a password.  If the password is <code>null</code>, returns <code>NO_PASSWORD</code>.
     * If the salt is <code>null</code>, a random salt will be generated.
     *
     * @deprecated  Please use hash instead
     * @see #hash(String)
     */
    public static String crypt(String password, String salt) {
        if(password==null || password.length()==0) return BusinessAdministrator.NO_PASSWORD;
        return salt==null || salt.length()==0?com.aoindustries.util.UnixCrypt.crypt(password):com.aoindustries.util.UnixCrypt.crypt(password, salt);
    }

    /**
     * Performs a one-way hash of the plaintext value using SHA-1.
     *
     * @exception  WrappedException  if any problem occurs.
     */
    public static String hash(String plaintext) throws WrappedException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(plaintext.getBytes("UTF-8"));
            return new String(Base64Coder.encode(md.digest()));
        } catch(NoSuchAlgorithmException err) {
            throw new WrappedException(err);
        } catch(UnsupportedEncodingException err) {
            throw new WrappedException(err);
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean passwordMatches(String plaintext, String ciphertext) {
        if(!NO_PASSWORD.equals(ciphertext)) {
            // Try hash first
            String hashed = hash(plaintext);
            if(hashed.equals(ciphertext)) return true;
            // Try old crypt next
            if(ciphertext.length()>=2) {
                String salt=ciphertext.substring(0,2);
                String crypted=com.aoindustries.util.UnixCrypt.crypt(plaintext, salt);
                return crypted.equals(ciphertext);
            }
        }
    	return false;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Fields">
    final private String password;
    final private String name;
    final private String title;
    final private Date birthday;
    final private boolean isPreferred;
    final private boolean isPrivate;
    final private Timestamp created;
    final private String workPhone;
    final private String homePhone;
    final private String cellPhone;
    final private String fax;
    final private String email;
    final private String address1;
    final private String address2;
    final private String city;
    final private String state;
    final private String country;
    final private String zip;
    final private Integer disableLog;
    final private boolean canSwitchUsers;
    final private String supportCode;

    public BusinessAdministrator(
        BusinessAdministratorService<?,?> service,
        String username,
        String password,
        String name,
        String title,
        Date birthday,
        boolean isPreferred,
        boolean isPrivate,
        Timestamp created,
        String workPhone,
        String homePhone,
        String cellPhone,
        String fax,
        String email,
        String address1,
        String address2,
        String city,
        String state,
        String country,
        String zip,
        Integer disableLog,
        boolean canSwitchUsers,
        String supportCode
    ) {
        super(service, username);
        this.password = password;
        this.name = name;
        this.title = title;
        this.birthday = birthday;
        this.isPreferred = isPreferred;
        this.isPrivate = isPrivate;
        this.created = created;
        this.workPhone = workPhone;
        this.homePhone = homePhone;
        this.cellPhone = cellPhone;
        this.fax = fax;
        this.email = email;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.country = StringUtility.intern(country);
        this.zip = zip;
        this.disableLog = disableLog;
        this.canSwitchUsers = canSwitchUsers;
        this.supportCode = supportCode;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Columns">
    @SchemaColumn(order=0, name="username", index=IndexType.PRIMARY_KEY, description="the unique identifier for this admin")
    public Username getUsername() throws RemoteException {
        Username usernameObject = getService().getConnector().getUsernames().get(key);
        if (usernameObject == null) throw new RemoteException("Username not found: " + key);
        return usernameObject;
    }

    @SchemaColumn(order=1, name="password", description="the encrypted password for this admin")
    public String getPassword() {
    	return password;
    }

    @SchemaColumn(order=2, name="name", description="the name of this admin")
    public String getName() {
    	return name;
    }

    @SchemaColumn(order=3, name="title", description="the admins title within their organization")
    public String getTitle() {
        return title;
    }

    @SchemaColumn(order=4, name="birthday", description="the admins birthday")
    public Date getBirthday() {
    	return birthday;
    }

    @SchemaColumn(order=5, name="is_preferred", description="if true, customers is preferred")
    public boolean isPreferred() {
    	return isPreferred;
    }

    @SchemaColumn(order=6, name="private", description="indicates if the admin should not be listed in publicly available lists")
    public boolean isPrivate() {
        return isPrivate;
    }

    @SchemaColumn(order=7, name="created", description="the time the admin entry was created")
    public Timestamp getCreated() {
    	return created;
    }

    @SchemaColumn(order=8, name="work_phone", description="the work phone number (if different than business)")
    public String getWorkPhone() {
    	return workPhone;
    }

    @SchemaColumn(order=9, name="home_phone", description="the home phone number")
    public String getHomePhone() {
    	return homePhone;
    }

    @SchemaColumn(order=10, name="cell_phone", description="the cellular phone number")
    public String getCellPhone() {
    	return cellPhone;
    }

    @SchemaColumn(order=11, name="fax", description="the fax number (if different than business)")
    public String getFax() {
    	return fax;
    }

    @SchemaColumn(order=12, name="email", description="the email address")
    public String getEmail() {
    	return email;
    }

    @SchemaColumn(order=13, name="address1", description="the street address (if different than business)")
    public String getAddress1() {
        return address1;
    }

    @SchemaColumn(order=14, name="address2", description="the street address (if different than business)")
    public String getAddress2() {
    	return address2;
    }

    @SchemaColumn(order=15, name="city", description="the city (if different than business)")
    public String getCity() {
    	return city;
    }

    @SchemaColumn(order=16, name="state", description="the state (if different than business)")
    public String getState() {
    	return state;
    }

    static final String COLUMN_COUNTRY="country";
    @SchemaColumn(order=17, name=COLUMN_COUNTRY, index=IndexType.INDEXED, description="the country (if different than business)")
    public CountryCode getCountry() throws RemoteException {
        if(country == null) return null;
        CountryCode countryCode=getService().getConnector().getCountryCodes().get(country);
        if(countryCode == null) throw new RemoteException("CountryCode not found: " + country);
        return countryCode;
    }

    @SchemaColumn(order=18, name="zip", description="the zip code (if different than business)")
    public String getZIP() {
        return zip;
    }

    static final String COLUMN_DISABLE_LOG = "disable_log";
    @SchemaColumn(order=19, name=COLUMN_DISABLE_LOG, index=IndexType.INDEXED, description="indicates that this account is disabled")
    public DisableLog getDisableLog() throws RemoteException {
        if(disableLog==null) return null;
        DisableLog obj = getService().getConnector().getDisableLogs().get(disableLog);
        if(obj==null) throw new RemoteException("Unable to find DisableLog: "+disableLog);
        return obj;
    }

    @SchemaColumn(order=20, name="can_switch_users", description="allows this person to switch users to any subaccounts")
    public boolean canSwitchUsers() {
        return canSwitchUsers;
    }

    @SchemaColumn(order=21, name="support_code", index=IndexType.UNIQUE, description="used to authenticate for email-based supprt")
    public String getSupportCode() {
        return supportCode;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JavaBeans">
    public com.aoindustries.aoserv.client.beans.BusinessAdministrator getBean() {
        return new com.aoindustries.aoserv.client.beans.BusinessAdministrator(key, password, name, title, birthday, isPreferred, isPrivate, created, workPhone, homePhone, cellPhone, fax, email, address1, address2, city, state, country, zip, disableLog, canSwitchUsers, supportCode);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependencies">
    @Override
    public Set<? extends AOServObject> getDependencies() throws RemoteException {
        return createDependencySet(
            getUsername(),
            getCountry(),
            getDisableLog()
        );
    }

    @Override
    public Set<? extends AOServObject> getDependentObjects() throws RemoteException {
        return createDependencySet(
            /* TODO
            createDependencyList(
                getMasterUser()
            ),
            getPermissions(),
            getCreditCardsByCreatedBy(),
            getCreditCardTransactionsByAuthorizationUsername(),
            getCreditCardTransactionsByCaptureUsername(),
            getCreditCardTransactionsByVoidUsername(),
             */
            getDisableLogs(),
            /*
            getMonthlyCharges(),
            getMonthlyChargesByCreatedBy(),*/
            getBusinessesByCreatedBy(),
            getResources()
            // TODO: getCreatedTickets(),
            // TODO: getCompletedSignupRequests(),
            // TODO: getTicketActions(),
            // TODO: getTicketActionsByOldAssignedTo(),
            // TODO: getTicketActionsByNewAssignedTo(),
            // TODO: getTicketAssignments(),
            // TODO: getTransactions()
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    public Set<Business> getBusinessesByCreatedBy() throws RemoteException {
        return getService().getConnector().getBusinesses().getIndexed(Business.COLUMN_CREATED_BY, this);
    }

    public Set<DisableLog> getDisableLogs() throws RemoteException {
        return getService().getConnector().getDisableLogs().getIndexed(DisableLog.COLUMN_DISABLED_BY, this);
    }

    public Set<Resource> getResources() throws RemoteException {
        return getService().getConnector().getResources().getIndexed(Resource.COLUMN_CREATED_BY, this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TODO">
    /* TODO
    public int arePasswordsSet() throws IOException, SQLException {
        return service.connector.requestBooleanQuery(true, AOServProtocol.CommandID.IS_BUSINESS_ADMINISTRATOR_PASSWORD_SET, pkey)?PasswordProtected.ALL:PasswordProtected.NONE;
    }

    public boolean canDisable() throws SQLException, IOException {
        return disableLog==null && !equals(service.connector.getThisBusinessAdministrator());
    }

    public boolean canSwitchUser(BusinessAdministrator other) throws SQLException, IOException {
        if(isDisabled() || other.isDisabled()) return false;
        Business business=getUsername().getBusiness();
        Business otherBusiness=other.getUsername().getBusiness();
        return !business.equals(otherBusiness) && business.isBusinessOrParentOf(otherBusiness);
    }

    public boolean canEnable() throws SQLException, IOException {
        DisableLog dl=getDisableLog();
        if(dl==null) return false;
        else return dl.canEnable();
    }

    public PasswordChecker.Result[] checkPassword(Locale userLocale, String password) throws IOException {
        return checkPassword(userLocale, pkey, password);
    }
    */

    /**
     * Validates a password and returns a description of the problem.  If the
     * password is valid, then <code>null</code> is returned.
     */
    /* TODO
    public static PasswordChecker.Result[] checkPassword(Locale userLocale, String username, String password) throws IOException {
	return PasswordChecker.checkPassword(userLocale, username, password, true, false);
    }*/

    /**
     * Validates a password and returns a description of the problem.  If the
     * password is valid, then <code>null</code> is returned.
     */
    /*public String checkPasswordDescribe(String password) {
	return checkPasswordDescribe(pkey, password);
    }*/

    /**
     * Validates a password and returns a description of the problem.  If the
     * password is valid, then <code>null</code> is returned.
     */
    /*public static String checkPasswordDescribe(String username, String password) {
	return PasswordChecker.checkPasswordDescribe(username, password, true, false);
    }*/

    /* TODO
    public void disable(DisableLog dl) throws IOException, SQLException {
        service.connector.requestUpdateIL(true, AOServProtocol.CommandID.DISABLE, SchemaTable.TableID.BUSINESS_ADMINISTRATORS, dl.pkey, pkey);
    }

    public void enable() throws IOException, SQLException {
        service.connector.requestUpdateIL(true, AOServProtocol.CommandID.ENABLE, SchemaTable.TableID.BUSINESS_ADMINISTRATORS, pkey);
    }

    public List<TicketAction> getTicketActions() throws IOException, SQLException {
        return service.connector.getTicketActions().getIndexedRows(TicketAction.COLUMN_ADMINISTRATOR, pkey);
    }

    public List<TicketAction> getTicketActionsByOldAssignedTo() throws IOException, SQLException {
        return service.connector.getTicketActions().getIndexedRows(TicketAction.COLUMN_OLD_ASSIGNED_TO, pkey);
    }

    public List<TicketAction> getTicketActionsByNewAssignedTo() throws IOException, SQLException {
        return service.connector.getTicketActions().getIndexedRows(TicketAction.COLUMN_NEW_ASSIGNED_TO, pkey);
    }

    public List<TicketAssignment> getTicketAssignments() throws IOException, SQLException {
        return service.connector.getTicketAssignments().getTicketAssignments(this);
    }

    public List<Ticket> getCreatedTickets() throws IOException, SQLException {
        return service.connector.getTickets().getIndexedRows(Ticket.COLUMN_CREATED_BY, pkey);
    }

    public boolean isDisabled() {
        return disableLog!=null;
    }

    public MasterUser getMasterUser() throws IOException, SQLException {
    	return service.connector.getMasterUsers().get(pkey);
    }

    public List<MonthlyCharge> getMonthlyCharges() throws IOException, SQLException {
    	return service.connector.getMonthlyCharges().getMonthlyCharges(this);
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.BUSINESS_ADMINISTRATORS;
    }

    public List<Transaction> getTransactions() throws IOException, SQLException {
        return service.connector.getTransactions().getTransactions(this);
    }

    public boolean isActiveAccounting() throws IOException, SQLException {
	MasterUser user=getMasterUser();
	return
            user!=null
            && user.isActive()
            && user.canAccessAccounting()
	;
    }

    public boolean isActiveBankAccounting() throws IOException, SQLException {
	MasterUser user=getMasterUser();
	return
            user!=null
            && user.isActive()
            && user.canAccessBankAccount()
	;
    }

    public boolean isActiveDNSAdmin() throws IOException, SQLException {
	MasterUser user=getMasterUser();
	return
            user!=null
            && user.isActive()
            && user.isDNSAdmin()
	;
    }

    public boolean isActiveTableInvalidator() throws IOException, SQLException {
	MasterUser user=getMasterUser();
	return
            user!=null
            && user.isActive()
            && user.canInvalidateTables()
	;
    }

    public boolean isActiveWebAdmin() throws IOException, SQLException {
	MasterUser user=getMasterUser();
	return
            user!=null
            && user.isActive()
            && user.isWebAdmin()
	;
    }

    public boolean passwordMatches(String plaintext) {
        return passwordMatches(plaintext, password);
    }

    public List<CannotRemoveReason> getCannotRemoveReasons(Locale userLocale) throws SQLException, IOException {
        List<CannotRemoveReason> reasons=new ArrayList<CannotRemoveReason>();

        AOServConnector conn=getService().getConnector();

        if(equals(conn.getThisBusinessAdministrator())) reasons.add(new CannotRemoveReason<BusinessAdministrator>("Not allowed to remove self", this));

        List<TicketAction> actions=getTicketActions();
        if(!actions.isEmpty()) reasons.add(new CannotRemoveReason<TicketAction>("Author of "+actions.size()+" ticket "+(actions.size()==1?"action":"actions"), actions));

        List<Ticket> tickets=getCreatedTickets();
        if(!tickets.isEmpty()) reasons.add(new CannotRemoveReason<Ticket>("Author of "+tickets.size()+' '+(tickets.size()==1?"ticket":"tickets"), tickets));

        List<Transaction> trs=getTransactions();
        if(!trs.isEmpty()) reasons.add(new CannotRemoveReason<Transaction>("Created "+trs.size()+' '+(trs.size()==1?"transaction":"transactions"), trs));

        return reasons;
    }

    public void remove() throws IOException, SQLException {
        getService().getConnector().requestUpdateIL(
            true,
            AOServProtocol.CommandID.REMOVE,
            SchemaTable.TableID.BUSINESS_ADMINISTRATORS,
            pkey
        );
    }
    */

    /**
     * Sets the password for this <code>BusinessAdministrator</code>.  All connections must
     * be over secure protocols for this method to work.  If the connections
     * are not secure, an <code>IOException</code> is thrown.
     */
    /* TODO
    public void setPassword(String plaintext) throws IOException, SQLException {
        AOServConnector connector=getService().getConnector();
        if(!connector.isSecure()) throw new IOException("Passwords for business_administrators may only be set when using secure protocols.  Currently using the "+connector.getProtocol()+" protocol, which is not secure.");
    	connector.requestUpdateIL(true, AOServProtocol.CommandID.SET_BUSINESS_ADMINISTRATOR_PASSWORD, pkey, plaintext);
    }

    public void setProfile(
        final String name,
        String title,
        final long birthday,
        final boolean isPrivate,
        final String workPhone,
        String homePhone,
        String cellPhone,
        String fax,
        final String email,
        String address1,
        String address2,
        String city,
        String state,
        String country,
        String zip
    ) throws IOException, SQLException {
        if(title!=null && title.length()==0) title=null;
        final String finalTitle = title;
        if(homePhone!=null && homePhone.length()==0) homePhone=null;
        final String finalHomePhone = homePhone;
        if(cellPhone!=null && cellPhone.length()==0) cellPhone=null;
        final String finalCellPhone = cellPhone;
        if(fax!=null && fax.length()==0) fax=null;
        final String finalFax = fax;
        if(address1!=null && address1.length()==0) address1=null;
        final String finalAddress1 = address1;
        if(address2!=null && address2.length()==0) address2=null;
        final String finalAddress2 = address2;
        if(city!=null && city.length()==0) city=null;
        final String finalCity = city;
        if(state!=null && state.length()==0) state=null;
        final String finalState = state;
        if(country!=null && country.length()==0) country=null;
        final String finalCountry = country;
        if(zip!=null && zip.length()==0) zip=null;
        final String finalZip = zip;
        getService().getConnector().requestUpdate(
            true,
            new AOServConnector.UpdateRequest() {
                IntList invalidateList;

                public void writeRequest(CompressedDataOutputStream out) throws IOException {
                    out.writeCompressedInt(AOServProtocol.CommandID.SET_BUSINESS_ADMINISTRATOR_PROFILE.ordinal());
                    out.writeUTF(pkey);
                    out.writeUTF(name);
                    out.writeBoolean(finalTitle!=null); if(finalTitle!=null) out.writeUTF(finalTitle);
                    out.writeLong(birthday);
                    out.writeBoolean(isPrivate);
                    out.writeUTF(workPhone);
                    out.writeBoolean(finalHomePhone!=null); if(finalHomePhone!=null) out.writeUTF(finalHomePhone);
                    out.writeBoolean(finalCellPhone!=null); if(finalCellPhone!=null) out.writeUTF(finalCellPhone);
                    out.writeBoolean(finalFax!=null); if(finalFax!=null) out.writeUTF(finalFax);
                    out.writeUTF(email);
                    out.writeBoolean(finalAddress1!=null); if(finalAddress1!=null) out.writeUTF(finalAddress1);
                    out.writeBoolean(finalAddress2!=null); if(finalAddress2!=null) out.writeUTF(finalAddress2);
                    out.writeBoolean(finalCity!=null); if(finalCity!=null) out.writeUTF(finalCity);
                    out.writeBoolean(finalState!=null); if(finalState!=null) out.writeUTF(finalState);
                    out.writeBoolean(finalCountry!=null); if(finalCountry!=null) out.writeUTF(finalCountry);
                    out.writeBoolean(finalZip!=null); if(finalZip!=null) out.writeUTF(finalZip);
                }

                public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
                    int code=in.readByte();
                    if(code==AOServProtocol.DONE) invalidateList=AOServConnector.readInvalidateList(in);
                    else {
                        AOServProtocol.checkResult(code, in);
                        throw new IOException("Unexpected response code: "+code);
                    }
                }

                public void afterRelease() {
                    getService().getConnector().tablesUpdated(invalidateList);
                }
            }
        );
    }
    */

    /**
     * Determines if a name can be used as a username.  The same rules apply as for
     * Username.
     *
     * @see  Username#checkUsername
     */
    /* TODO
    public static String checkUsername(String name, Locale locale) {
        return Username.checkUsername(name, locale);
    }*/

    /**
     * Determines if a name can be used as a username.  The same rules apply as for
     * Username.
     *
     * @deprecated  Please use <code>checkUsername(String)</code> to give users more details when the check fails.
     *
     * @see  Username#isValidUsername
     */
    /* TODO
    public static boolean isValidUsername(String name) {
        return Username.isValidUsername(name);
    }

    public boolean canSetPassword() {
        return disableLog==null;
    }

    public List<BusinessAdministratorPermission> getPermissions() throws IOException, SQLException {
        return getService().getConnector().getBusinessAdministratorPermissions().getPermissions(this);
    }*/

    /**
     * Checks if this business administrator has the provided permission.
     */
    /* TODO
    public boolean hasPermission(AOServPermission permission) throws IOException, SQLException {
        return hasPermission(permission.getName());
    }*/

    /**
     * Checks if this business administrator has the provided permission.
     */
    /* TODO
    public boolean hasPermission(AOServPermission.Permission permission) throws IOException, SQLException {
        return hasPermission(permission.name());
    }*/

    /**
     * Checks if this business administrator has the provided permission.
     */
    /* TODO
    public boolean hasPermission(String permission) throws IOException, SQLException {
        return getService().getConnector().getBusinessAdministratorPermissions().hasPermission(this, permission);
    }*/

    /* TODO

    public List<CreditCard> getCreditCardsByCreatedBy() throws IOException, SQLException {
        return getService().getConnector().getCreditCards().getIndexedRows(CreditCard.COLUMN_CREATED_BY, pkey);
    }

    public List<CreditCardTransaction> getCreditCardTransactionsByCreditCardCreatedBy() throws IOException, SQLException {
        return getService().getConnector().getCreditCardTransactions().getIndexedRows(CreditCardTransaction.COLUMN_CREDIT_CARD_CREATED_BY, pkey);
    }

    public List<CreditCardTransaction> getCreditCardTransactionsByAuthorizationUsername() throws IOException, SQLException {
        return getService().getConnector().getCreditCardTransactions().getIndexedRows(CreditCardTransaction.COLUMN_AUTHORIZATION_USERNAME, pkey);
    }

    public List<CreditCardTransaction> getCreditCardTransactionsByCaptureUsername() throws IOException, SQLException {
        return getService().getConnector().getCreditCardTransactions().getIndexedRows(CreditCardTransaction.COLUMN_CAPTURE_USERNAME, pkey);
    }

    public List<CreditCardTransaction> getCreditCardTransactionsByVoidUsername() throws IOException, SQLException {
        return getService().getConnector().getCreditCardTransactions().getIndexedRows(CreditCardTransaction.COLUMN_VOID_USERNAME, pkey);
    }

    public List<MonthlyCharge> getMonthlyChargesByCreatedBy() throws IOException, SQLException {
        return getService().getConnector().getMonthlyCharges().getMonthlyChargesByCreatedBy(this);
    }

    public List<SignupRequest> getCompletedSignupRequests() throws IOException, SQLException {
        return getService().getConnector().getSignupRequests().getIndexedRows(SignupRequest.COLUMN_COMPLETED_BY, pkey);
    }*/
    // </editor-fold>
}

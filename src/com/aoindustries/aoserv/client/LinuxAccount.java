package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.util.WrappedException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One user may have shell, FTP, and/or email access to any number
 * of servers.  However, some of the information is common across
 * all machines, and that set of information is contained in a
 * <code>LinuxAccount</code>.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class LinuxAccount extends CachedObjectStringKey<LinuxAccount> implements PasswordProtected, Removable, Disablable {

    static final int COLUMN_USERNAME=0;
    static final String COLUMN_USERNAME_name = "username";

    /**
     * Some commonly used system and application account usernames.
     */
    public static final String
        APACHE="apache",
        AWSTATS="awstats",
        BIN="bin",
        CYRUS="cyrus",
        EMAILMON="emailmon",
        FTP="ftp",
        FTPMON="ftpmon",
        HTTPD="httpd",
        INTERBASE="interbase",
        MAIL="mail",
        NOBODY="nobody",
        OPERATOR="operator",
        POSTGRES="postgres",
        ROOT="root"
    ;

    public static final String NO_PASSWORD_CONFIG_VALUE="!!";

    private String name;
    private String office_location;
    private String office_phone;
    private String home_phone;
    private String type;
    private String shell;
    private long created;
    int disable_log;

    public void addFTPGuestUser() {
        table.connector.ftpGuestUsers.addFTPGuestUser(pkey);
    }

    public void addLinuxGroup(LinuxGroup group) {
        table.connector.linuxGroupAccounts.addLinuxGroupAccount(group, this);
    }

    public int addLinuxServerAccount(AOServer aoServer, String home) {
        return table.connector.linuxServerAccounts.addLinuxServerAccount(this, aoServer, home);
    }

    public int arePasswordsSet() {
        return Username.groupPasswordsSet(getLinuxServerAccounts());
    }

    public boolean canDisable() {
        // Already disabled
        if(disable_log!=-1) return false;

        // linux_server_accounts
        for(LinuxServerAccount lsa : getLinuxServerAccounts()) if(lsa.disable_log==-1) return false;

        return true;
    }

    public boolean canEnable() {
        DisableLog dl=getDisableLog();
        if(dl==null) return false;
        else return dl.canEnable() && getUsername().disable_log==-1;
    }

    public PasswordChecker.Result[] checkPassword(Locale userLocale, String password) {
        return checkPassword(userLocale, pkey, type, password);
    }

    /**
     * Checks the strength of a password as required for this
     * <code>LinuxAccount</code>.  The strength requirement
     * depends on the <code>LinuxAccountType</code>.
     *
     * @see  LinuxAccountType#enforceStrongPassword(String)
     * @see  PasswordChecker#checkPassword(Locale,String,String,boolean,boolean)
     */
    public static PasswordChecker.Result[] checkPassword(Locale userLocale, String username, String type, String password) {
        boolean enforceStrong=LinuxAccountType.enforceStrongPassword(type);
        return PasswordChecker.checkPassword(userLocale, username, password, enforceStrong, !enforceStrong);
    }

    public void disable(DisableLog dl) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.DISABLE, SchemaTable.TableID.LINUX_ACCOUNTS, dl.pkey, pkey);
    }
    
    public void enable() {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.ENABLE, SchemaTable.TableID.LINUX_ACCOUNTS, pkey);
    }

    public Object getColumn(int i) {
        switch(i) {
            case COLUMN_USERNAME: return pkey;
            case 1: return name;
            case 2: return office_location;
            case 3: return office_phone;
            case 4: return home_phone;
            case 5: return type;
            case 6: return shell;
            case 7: return new java.sql.Date(created);
            case 8: return disable_log==-1?null:Integer.valueOf(disable_log);
            default: throw new IllegalArgumentException("Invalid index: "+i);
        }
    }

    public long getCreated() {
        return created;
    }

    public DisableLog getDisableLog() {
        if(disable_log==-1) return null;
        DisableLog obj=table.connector.disableLogs.get(disable_log);
        if(obj==null) throw new WrappedException(new SQLException("Unable to find DisableLog: "+disable_log));
        return obj;
    }

    public FTPGuestUser getFTPGuestUser() {
        return table.connector.ftpGuestUsers.get(pkey);
    }

    public String getHomePhone() {
        return home_phone;
    }

    public List<LinuxGroup> getLinuxGroups() {
        return table.connector.linuxGroupAccounts.getLinuxGroups(this);
    }

    public LinuxServerAccount getLinuxServerAccount(AOServer aoServer) {
        return table.connector.linuxServerAccounts.getLinuxServerAccount(aoServer, pkey);
    }

    public List<LinuxServerAccount> getLinuxServerAccounts() {
        return table.connector.linuxServerAccounts.getLinuxServerAccounts(this);
    }

    public String getName() {
        return name;
    }

    public String getOfficeLocation() {
        return office_location;
    }

    public String getOfficePhone() {
        return office_phone;
    }

    public LinuxGroup getPrimaryGroup() {
        return table.connector.linuxGroupAccounts.getPrimaryGroup(this);
    }

    public Shell getShell() {
        Shell shellObject = table.connector.shells.get(shell);
        if (shellObject == null) throw new WrappedException(new SQLException("Unable to find Shell: " + shell));
        return shellObject;
    }

    public SchemaTable.TableID getTableID() {
        return SchemaTable.TableID.LINUX_ACCOUNTS;
    }

    public LinuxAccountType getType() {
        LinuxAccountType typeObject = table.connector.linuxAccountTypes.get(type);
        if (typeObject == null) throw new IllegalArgumentException(new SQLException("Unable to find LinuxAccountType: " + type));
        return typeObject;
    }

    public Username getUsername() {
        Username usernameObject = table.connector.usernames.get(pkey);
        if (usernameObject == null) throw new WrappedException(new SQLException("Unable to find Username: " + pkey));
        return usernameObject;
    }

    public List<String> getValidHomeDirectories(AOServer ao) {
        return getValidHomeDirectories(pkey, ao);
    }

    /**
     * @deprecated  Please provide the locale for locale-specific errors.
     */
    public static List<String> getValidHomeDirectories(String username, AOServer ao) {
        return getValidHomeDirectories(username, ao, Locale.getDefault());
    }

    public static List<String> getValidHomeDirectories(String username, AOServer ao, Locale locale) {
        List<String> dirs=new ArrayList<String>();
        if(username!=null) dirs.add(LinuxServerAccount.getDefaultHomeDirectory(username, locale));

        List<HttpdSite> hss=ao.getHttpdSites();
        int hsslen=hss.size();
        for(int c=0;c<hsslen;c++) {
            HttpdSite hs=hss.get(c);
            String siteDir=hs.getInstallDirectory();
            dirs.add(siteDir);
            if(hs.getHttpdTomcatSite()!=null) dirs.add(siteDir+"/webapps");
        }

        List<HttpdSharedTomcat> hsts=ao.getHttpdSharedTomcats();
        int hstslen=hsts.size();
        for(int c=0;c<hstslen;c++) {
            HttpdSharedTomcat hst=hsts.get(c);
            dirs.add(HttpdSharedTomcat.WWW_GROUP_DIR+'/'+hst.getName());
        }
        return dirs;
    }

    public void init(ResultSet result) throws SQLException {
        pkey = result.getString(1);
        name = result.getString(2);
        office_location = result.getString(3);
        office_phone = result.getString(4);
        home_phone = result.getString(5);
        type = result.getString(6);
        shell = result.getString(7);
        created = result.getTimestamp(8).getTime();
        disable_log=result.getInt(9);
        if(result.wasNull()) disable_log=-1;
    }

    /**
     * Determines if a name can be used as a GECOS field.  A GECOS field
     * is valid if it is between 1 and 100 characters in length and uses only
     * <code>[a-z,A-Z,0-9,-,_,@, ,.,#,=,/,$,%,^,&,*,(,),?,']</code> for each
     * character.<br>
     * <br>
     * Refer to <code>man 5 passwd</code>
     * @see  #setName
     * @see  #setOfficeLocation
     * @see  #setOfficePhone
     * @see  #setHomePhone
     */
    public static String checkGECOS(String name, String display) {
        if(name!=null) {
            int len = name.length();
            if (len == 0 || len > 100) return "The "+display+" must be between 1 and 100 characters long.";

            for (int c = 0; c < len; c++) {
                char ch = name.charAt(c);
                if (
                    (ch < 'a' || ch > 'z')
                    && (ch<'A' || ch>'Z')
                    && (ch < '0' || ch > '9')
                    && ch != '-'
                    && ch != '_'
                    && ch != '@'
                    && ch != ' '
                    && ch != '.'
                    && ch != '#'
                    && ch != '='
                    && ch != '/'
                    && ch != '$'
                    && ch != '%'
                    && ch != '^'
                    && ch != '&'
                    && ch != '*'
                    && ch != '('
                    && ch != ')'
                    && ch != '?'
                    && ch != '\''
                    && ch != '+'
                ) return "Invalid character found in "+display+": "+ch;
            }
        }
        return null;
    }

    public void read(CompressedDataInputStream in) throws IOException {
        pkey=in.readUTF().intern();
        name=in.readUTF();
        office_location=in.readBoolean()?in.readUTF():null;
        office_phone=in.readBoolean()?in.readUTF():null;
        home_phone=in.readBoolean()?in.readUTF():null;
        type=in.readUTF().intern();
        shell=in.readUTF().intern();
        created=in.readLong();
        disable_log=in.readCompressedInt();
    }

    public List<CannotRemoveReason> getCannotRemoveReasons() {
        List<CannotRemoveReason> reasons=new ArrayList<CannotRemoveReason>();

        // All LinuxServerAccounts must be removable
        for(LinuxServerAccount lsa : getLinuxServerAccounts()) {
            reasons.addAll(lsa.getCannotRemoveReasons());
        }

        return reasons;
    }

    public void remove() {
        table.connector.requestUpdateIL(
            AOServProtocol.CommandID.REMOVE,
            SchemaTable.TableID.LINUX_ACCOUNTS,
            pkey
        );
    }

    public void removeLinuxGroup(LinuxGroup group) {
        table.connector.linuxGroupAccounts.getLinuxGroupAccount(group.pkey, pkey).remove();
    }

    public void setHomePhone(String phone) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.SET_LINUX_ACCOUNT_HOME_PHONE, pkey, phone==null?"":phone);
    }

    public void setName(String name) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.SET_LINUX_ACCOUNT_NAME, pkey, name);
    }

    public void setOfficeLocation(String location) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.SET_LINUX_ACCOUNT_OFFICE_LOCATION, pkey, location==null?"":location);
    }

    public void setOfficePhone(String phone) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.SET_LINUX_ACCOUNT_OFFICE_PHONE, pkey, phone==null?"":phone);
    }

    public void setPassword(String password) {
        for(LinuxServerAccount lsa : getLinuxServerAccounts()) {
            if(lsa.canSetPassword()) lsa.setPassword(password);
        }
    }

    public void setShell(Shell shell) {
        table.connector.requestUpdateIL(AOServProtocol.CommandID.SET_LINUX_ACCOUNT_SHELL, pkey, shell.pkey);
    }

    public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
        out.writeUTF(pkey);
        out.writeUTF(name);
        out.writeNullUTF(office_location);
        out.writeNullUTF(office_phone);
        out.writeNullUTF(home_phone);
        out.writeUTF(type);
        out.writeUTF(shell);
        out.writeLong(created);
        out.writeCompressedInt(disable_log);
    }

    /**
     * Determines if a name can be used as a username.  The username restrictions are
     * inherited from <code>Username</code>, with the addition of not allowing
     * <code>postmaster</code> and <code>mailer-daemon</code>.  This is to prevent a
     * user from interfering with the delivery of system messages in qmail.
     *
     * @see  Username#isValidUsername
     */
    public static boolean isValidUsername(String username) {
        return
            Username.checkUsername(username, Locale.getDefault())==null
            && !"bin".equals(username)
            && !"etc".equals(username)
            && !"lib".equals(username)
            && !"postmaster".equals(username)
            && !"mailer-daemon".equals(username)
        ;
    }
    
    public boolean canSetPassword() {
        return disable_log==-1 && getType().canSetPassword();
    }

    public void setPrimaryLinuxGroup(LinuxGroup group) {
        LinuxGroupAccount lga=table.connector.linuxGroupAccounts.getLinuxGroupAccount(group.getName(), pkey);
        if(lga==null) throw new WrappedException(new SQLException("Unable to find LinuxGroupAccount for username="+pkey+" and group="+group.getName()));
        lga.setAsPrimary();
    }
}

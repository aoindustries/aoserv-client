/*
 * Copyright 2010-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.command;

import com.aoindustries.aoserv.client.*;
import com.aoindustries.aoserv.client.validator.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author  AO Industries, Inc.
 */
final public class CheckPostgresUserPasswordCommand extends AOServCommand<List<PasswordChecker.Result>> {

    public static final String PARAM_POSTGRES_USER = "postgresUser";

    final private int postgresUser;
    private final String password;

    public CheckPostgresUserPasswordCommand(
        @Param(name=PARAM_POSTGRES_USER) PostgresUser postgresUser,
        @Param(name="password") String password
    ) {
        this.postgresUser = postgresUser.getPkey();
        this.password = password;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public int getPostgresUser() {
        return postgresUser;
    }

    public String getPassword() {
        return password;
    }

    @Override
    protected Map<String,List<String>> checkCommand(AOServConnector userConn, AOServConnector rootConn, BusinessAdministrator rootUser) throws RemoteException {
        Map<String,List<String>> errors = Collections.emptyMap();
        // Check access
        PostgresUser rootPu = rootConn.getPostgresUsers().get(postgresUser);
        if(!rootUser.canAccessPostgresUser(rootPu)) {
            errors = addValidationError(errors, PARAM_POSTGRES_USER, "Common.validate.accessDenied");
        } else {
            // No setting root password
            PostgresUserId username = rootPu.getUserId();
            if(
                username==PostgresUser.POSTGRES // OK - interned
            ) errors = addValidationError(errors, PARAM_POSTGRES_USER, "SetPostgresUserPasswordCommand.validate.noSetPostgres");
        }
        return errors;
    }

    static List<PasswordChecker.Result> checkPassword(PostgresUser pu, String password) throws IOException {
        return PasswordChecker.checkPassword(pu.getUserId().getUserId(), password, PasswordChecker.PasswordStrength.STRICT);
    }

    @Override
    public List<PasswordChecker.Result> execute(AOServConnector connector, boolean isInteractive) throws RemoteException {
        try {
            return checkPassword(connector.getPostgresUsers().get(postgresUser), password);
        } catch(IOException err) {
            throw new RemoteException(err.getMessage(), err);
        }
    }
}
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
final public class SetPostgresUserPasswordCommand extends RemoteCommand<Void> {

    private static final long serialVersionUID = 1L;

    public static final String PARAM_POSTGRES_USER = "postgresUser";
    public static final String PARAM_PLAINTEXT = "plaintext";

    final private int postgresUser;
    final private String plaintext;

    public SetPostgresUserPasswordCommand(
        @Param(name=PARAM_POSTGRES_USER) PostgresUser postgresUser,
        @Param(name=PARAM_PLAINTEXT) String plaintext
    ) {
        this.postgresUser = postgresUser.getKey();
        this.plaintext = plaintext;
    }

    public int getPostgresUser() {
        return postgresUser;
    }

    public String getPlaintext() {
        return plaintext;
    }

    @Override
    public Map<String, List<String>> validate(BusinessAdministrator connectedUser) throws RemoteException {
        Map<String,List<String>> errors = Collections.emptyMap();
        // Check access
        PostgresUser pu = connectedUser.getConnector().getPostgresUsers().get(postgresUser);
        if(!connectedUser.canAccessPostgresUser(pu)) {
            errors = addValidationError(errors, PARAM_POSTGRES_USER, ApplicationResources.accessor, "Common.validate.accessDenied");
        } else {
            // No setting root password
            PostgresUserId username = pu.getUserId();
            if(
                username==PostgresUser.POSTGRES // OK - interned
            ) errors = addValidationError(errors, PARAM_POSTGRES_USER, ApplicationResources.accessor, "SetPostgresUserPasswordCommand.validate.noSetPostgres");
            else {
                // Make sure not disabled
                if(pu.isDisabled()) errors = addValidationError(errors, PARAM_POSTGRES_USER, ApplicationResources.accessor, "SetPostgresUserPasswordCommand.validate.disabled");
                else {
                    // Check password strength
                    try {
                        if(plaintext!=null && plaintext.length()>0) errors = addValidationError(errors, PARAM_PLAINTEXT, CheckPostgresUserPasswordCommand.checkPassword(pu, plaintext));
                    } catch(IOException err) {
                        throw new RemoteException(err.getMessage(), err);
                    }
                }
            }
        }
        return errors;
    }
}

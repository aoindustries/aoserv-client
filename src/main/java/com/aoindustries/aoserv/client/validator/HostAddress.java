/*
 * Copyright 2010-2013 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.validator;

import com.aoindustries.aoserv.client.DtoFactory;
import com.aoindustries.lang.ObjectUtils;
import com.aoindustries.util.Internable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a host's address as either a <code>DomainName</code> or an <code>InetAddress</code>.
 * To not allow the IP address representation, use <code>DomainName</code> instead.
 * No DNS lookups are performed during validation.
 * 
 * @author  AO Industries, Inc.
 */
final public class HostAddress implements
    Comparable<HostAddress>,
    Serializable,
    ObjectInputValidation,
    DtoFactory<com.aoindustries.aoserv.client.dto.HostAddress>,
    Internable<HostAddress>
{

    private static final long serialVersionUID = -6323326583709666966L;

    private static boolean isIp(String address) {
        if(address==null) return false;
        int len = address.length();
        if(len==0) return false;
        // If contains all digits and periods, or contains any colon, then is an IP
        boolean allDigitsAndPeriods = true;
        for(int c=0;c<len;c++) {
            char ch = address.charAt(c);
            if(ch==':') return true;
            if(
                (ch<'0' || ch>'9')
                && ch!='.'
            ) {
                allDigitsAndPeriods = false;
                // Still need to look for any colons
            }
        }
        return allDigitsAndPeriods;
    }

    /**
     * Validates a host address, must be either a valid domain name or a valid IP address.
     * // TODO: Must be non-arpa
     */
    public static ValidationResult validate(String address) {
        if(isIp(address)) return InetAddress.validate(address);
        else return DomainName.validate(address);
    }

    private static final ConcurrentMap<DomainName,HostAddress> internedByDomainName = new ConcurrentHashMap<DomainName,HostAddress>();

    private static final ConcurrentMap<InetAddress,HostAddress> internedByInetAddress = new ConcurrentHashMap<InetAddress,HostAddress>();

    /**
     * If address is null, returns null.
     */
    public static HostAddress valueOf(String address) throws ValidationException {
        if(address==null) return null;
        return
            isIp(address)
            ? valueOf(InetAddress.valueOf(address))
            : valueOf(DomainName.valueOf(address))
        ;
    }

    /**
     * If domainName is null, returns null.
     */
    public static HostAddress valueOf(DomainName domainName) {
        if(domainName==null) return null;
        //HostAddress existing = internedByDomainName.get(domainName);
        //return existing!=null ? existing : new HostAddress(domainName);
        return new HostAddress(domainName);
    }

    /**
     * If ip is null, returns null.
     */
    public static HostAddress valueOf(InetAddress ip) {
        if(ip==null) return null;
        //HostAddress existing = internedByInetAddress.get(ip);
        //return existing!=null ? existing : new HostAddress(ip);
        return new HostAddress(ip);
    }

    final private DomainName domainName;
    final private InetAddress inetAddress;

    private HostAddress(DomainName domainName) {
        this.domainName = domainName;
        this.inetAddress = null;
    }

    private HostAddress(InetAddress inetAddress) {
        this.domainName = null;
        this.inetAddress = inetAddress;
    }

    private void validate() throws ValidationException {
        if(domainName==null && inetAddress==null) throw new ValidationException(new InvalidResult(ApplicationResources.accessor, "HostAddress.validate.bothNull"));
        if(domainName!=null && inetAddress!=null) throw new ValidationException(new InvalidResult(ApplicationResources.accessor, "HostAddress.validate.bothNonNull"));
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        validateObject();
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

    @Override
    public boolean equals(Object O) {
        if(!(O instanceof HostAddress)) return false;
        HostAddress other = (HostAddress)O;
    	return
            ObjectUtils.equals(domainName, other.domainName)
            && ObjectUtils.equals(inetAddress, other.inetAddress)
    	;
    }

    @Override
    public int hashCode() {
        return domainName!=null ? domainName.hashCode() : inetAddress.hashCode();
    }

    /**
     * Sorts IP addresses before domain names.
     */
    @Override
    public int compareTo(HostAddress other) {
        if(this==other) return 0;
        if(domainName!=null) {
            if(other.domainName!=null) return domainName.compareTo(other.domainName);
            else return 1;
        } else {
            if(other.domainName!=null) return -1;
            else return inetAddress.compareTo(other.inetAddress);
        }
    }

    @Override
    public String toString() {
        return domainName!=null ? domainName.toString() : inetAddress.toString();
    }

    public String toBracketedString() {
        return domainName!=null ? domainName.toString() : inetAddress.toBracketedString();
    }

    /**
     * Interns this host address much in the same fashion as <code>String.intern()</code>.
     *
     * @see  String#intern()
     */
    @Override
    public HostAddress intern() {
        if(domainName!=null) {
            HostAddress existing = internedByDomainName.get(domainName);
            if(existing==null) {
                DomainName internedDomainName = domainName.intern();
                HostAddress addMe = domainName==internedDomainName ? this : new HostAddress(internedDomainName);
                existing = internedByDomainName.putIfAbsent(internedDomainName, addMe);
                if(existing==null) existing = addMe;
            }
            return existing;
        } else {
            HostAddress existing = internedByInetAddress.get(inetAddress);
            if(existing==null) {
                InetAddress internedInetAddress = inetAddress.intern();
                HostAddress addMe = inetAddress==internedInetAddress ? this : new HostAddress(internedInetAddress);
                existing = internedByInetAddress.putIfAbsent(internedInetAddress, addMe);
                if(existing==null) existing = addMe;
            }
            return existing;
        }
    }

    public DomainName getDomainName() {
        return domainName;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    @Override
    public com.aoindustries.aoserv.client.dto.HostAddress getDto() {
        return new com.aoindustries.aoserv.client.dto.HostAddress(toString());
    }
}

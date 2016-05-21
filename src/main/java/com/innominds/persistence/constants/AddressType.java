/**
 * Copyright (c) 2014 Innominds. All Rights Reserved.
 */
package com.innominds.persistence.constants;

/**
 * AddressType.java
 *
 * @author ThirupathiReddy V
 *
 */
public enum AddressType {

    /** */

    OFFICE((short) 0),

    /** */
    HOME((short) 1);

    Short status;

    /**
     * Constructor
     *
     */
    private AddressType(Short status) {

        this.status = status;
    }

}

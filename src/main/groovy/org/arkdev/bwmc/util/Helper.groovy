/*
 * accounts-mission 
 *
 * Created by ppreston on 21/03/2017.
 */
package org.arkdev.bwmc.util


class Helper {

    /**
     * This will copy the errors from a domain object to a command object
     * @param domain This is the domain object which is the source of the errors
     * @param cmd This is the command object to copy the errors to
     */
    static copyErrorToCmd(domain, cmd) {

        // Add all field errors
        domain.errors.fieldErrors.each { err ->
            cmd.errors.rejectValue(err.field, err.code, err.arguments, err.defaultMessage)
        }

        // TODO - add other errors
    }
}

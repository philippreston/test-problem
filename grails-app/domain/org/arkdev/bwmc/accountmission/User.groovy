package org.arkdev.bwmc.accountmission

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@EqualsAndHashCode(includes = "username")
@ToString(cache = true, includes = ["username", "email"], includeNames = false, includePackage = false)
class User {

    transient springSecurityService

    Date dateCreated
    String christianName
    String surname
    String username
    String password
    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false
    String phone
    String email
    String skype
    Address address

    // FIXME - password limits
    static constraints = {
        christianName blank: false
        surname blank: false
        username blank: false, unique: true, size: 5..20
        password blank: false, password: true, minSize: 8
        email email: true, blank: false
        phone nullable: true, blank: true
        skype nullable: true, blank: true
        address nullable: true
    }

    static mapping = {
        password column: 'password'
        address cascade: 'all'
    }

    static transients = ['springSecurityService']

    /**
     * Before inserting need to take the plain text password and
     * encode before entering into the database
     * @return null
     */
    def beforeInsert() {
        encodePassword()
    }

    /**
     * Before completing an update action need to encode the password
     * before updating to the database - but only if changed
     * @return
     */
    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    /**
     * This will return a users displayname
     * @return Concatinated christianName and surname
     */
    String getDisplayString() {
        "${christianName} ${surname}"
    }

    /**
     * Get the set of roles this user is part of
     * @return Set of roles for this user
     */
    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this)*.role
    }

    /**
     * Is this user an admin
     * @return true if admin, false if not
     */
    boolean isAdmin() {
        getAuthorities().any{ it.authority == "ROLE_ADMIN" }
    }

    /**
     * Is this user an operator
     * @return true if operator, false if not
     */
    boolean isOperator() {
        getAuthorities().any{ it.authority == "ROLE_OPERATOR" }
    }

    /**
     * Encode the password with spring password encoder
     */
    protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ?
                springSecurityService.encodePassword(password) :
                password
    }
}

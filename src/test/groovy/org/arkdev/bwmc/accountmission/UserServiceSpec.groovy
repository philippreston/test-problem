package org.arkdev.bwmc.accountmission

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.runtime.FreshRuntime
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@FreshRuntime
@TestFor(UserService)
@Mock([User, Address, Role, UserRole])
@Unroll
class UserServiceSpec extends Specification {

    @Shared User user

    def setup() {

        println "================ START: Setting Up User (${User.count()}) ================"
        println "================ START: Setting Up Address (${Address.count()}) ================"
        println "================ START: Setting Up Role (${Role.count()}) ================"
        println "================ START: Setting Up UserRole (${UserRole.count()}) ================"

        // Set the roles
        new Role(authority: "ROLE_ADMIN").save()
        new Role(authority: "ROLE_OPERATOR").save()
        def role = new Role(authority: "ROLE_USER").save()

        def userAddress = new Address()
        userAddress.with {
            name = "Name"
            address1 = "Address 1"
            address2 = "Address 2"
            townCity = "Town"
            postcode = "BT19"
            county = "Down"
        }

        // Create an admin user
        user = new User()
        user.with {
            christianName = "Phil"
            surname = "Preston"
            email = "p@p.com"
            username = "admin"
            password = "password123"
            address = userAddress
            skype = ""
            phone = ""
        }

        println(user.properties)

        // Save the test user
        if (user.validate()) {
            println "Saving"
            user.save(flush: true, failOnError: true)
            UserRole.create(user, role)
        }
        else {
            user.errors.allErrors.eachWithIndex { i, x ->
                println "${i} - ${x}"
            }
        }

        assert user

        println "================ END: Setting Up User (${User.count()}) ================"
        println "================ END: Setting Up Address (${Address.count()}) ================"
        println "================ END: Setting Up Role (${Role.count()}) ================"
        println "================ END: Setting Up UserRole (${UserRole.count()}) ================"
    }

    def cleanup() {
        user = null
    }

    void "Test list - get all users"() {
        when: "call findAll"
        def list = service.list()

        then: "should have one user"
        list.size() == 1
    }

    void "Test find - user that exists"() {
        when: "try to find existing user"
        User found = service.find([id: user.id])

        then: "should be same username"
        found.username == user.username
    }

    void "Test find - user that does not"() {
        when: "try to find non-existant user"
        User found = service.find([id: 120])

        then: "should be null"
        !found
    }

    void "Test editable - check user that exists"() {
        when: "request editable user"
        UserCommand toEdit = service.getEditable([id: user.id])

        then: "Command Object should match User"
        toEdit.username == user.username
        toEdit.christianName == user.christianName
        toEdit.surname == user.surname
        toEdit.password == null
        toEdit.passwordCheck == null
        toEdit.name && toEdit.name == user.address.name
        toEdit.address1 && toEdit.address1 == user.address.address1
        toEdit.address2 && toEdit.address2 == user.address.address2
        toEdit.townCity && toEdit.townCity == user.address.townCity
        toEdit.postcode && toEdit.postcode == user.address.postcode
        toEdit.county && toEdit.county == user.address.county
    }

    void "Test editable - check user that does not exists"() {
        when: "request editable user"
        UserCommand toEdit = service.getEditable([id: 120])

        then: "Command Object should match User"
        !toEdit
    }

    void "Test addUser - handling null UserCommand"() {

        when: "Try to add with null object"
        service.addUser(null)

        then: "Will throw an assertion error"
        thrown(AssertionError)

    }

    void "Test addUser - handling valid UserCommand"() {

        given: "A valid object"
        UserCommand cmd = new UserCommand(
                christianName: "Christian",
                surname: "Surname",
                username: "newuname",
                password: "Password123",
                passwordCheck: "Password123",
                email: "test@test.com"
        )

        when: "calling addUser"
        User result = service.addUser(cmd)

        then: "Will return a new User with same properties"
        result.christianName == cmd.christianName
        result.surname == cmd.surname
        result.username == cmd.username
        result.email == cmd.email
    }

    void "Test addUser - handling invalid UserCommand"() {

        // Make sure have the admin user to cause failure
        assert User.count() == 1

        given: "A an object that will fail validation"
        UserCommand cmd = new UserCommand(
                christianName: "Christian",
                surname: "Surname",
                username: "admin",
                password: "Password123",
                passwordCheck: "Password123",
                email: "test@test.com"
        )

        when: "calling addUser"
        service.addUser(cmd)

        then: "Will throw an exception"
        thrown(UserServiceException)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "Test addUser - handling of admin: #admin operator: #operator"() {

        given: "A an object with different role configs"
        UserCommand cmd = new UserCommand(
                christianName: "Christian",
                surname: "Surname",
                username: "test123",
                password: "Password123",
                passwordCheck: "Password123",
                email: "test@test.com",
                isAdmin: admin,
                isOperator: operator
        )

        when: "calling addUser"
        User result = service.addUser(cmd)

        then: "Will contain the user will contain the correct roles"
        result.getAuthorities().collect { it.authority }.sort() == roles.sort()

        where:
        admin | operator | roles
        false | false    | ['ROLE_USER']
        true  | false    | ['ROLE_USER', 'ROLE_ADMIN']
        false | true     | ['ROLE_USER', 'ROLE_OPERATOR']
        true  | true     | ['ROLE_USER', 'ROLE_OPERATOR', 'ROLE_ADMIN']
    }

    void "Test updateUser - changing a user password"() {

        given: "An update to the user"
        UserCommand cmd = new UserCommand()
        cmd.with {
            id = user.id
            christianName = user.christianName
            surname = user.surname
            username = user.username
            email = user.email
            password = "newPassword"
            passwordCheck = "newPassword"
            isAdmin = user.isAdmin()
            isOperator = user.isOperator()
        }

        when: "attempt to update"
        User updated = service.updateUser(cmd)

        then: "the password should be update - but nothing else"
        updated.password == cmd.password
    }

    void "Test updateUser - changing a user detail"() {

        given: "An update to the user"
        UserCommand cmd = new UserCommand()
        cmd.with {
            id = user.id
            christianName = user.christianName
            surname = user.surname
            username = user.username
            email = "update@update.com"
            password = user.password
            isAdmin = user.isAdmin()
            isOperator = user.isOperator()
        }

        when: "attempt to update"
        User updated = service.updateUser(cmd)

        then: "the email should be update - but nothing else"
        updated.email == cmd.email
    }

    void "Test updateUser - changing a user address"() {

        given: "An update to the user"
        UserCommand cmd = new UserCommand()
        cmd.with {
            id = user.id
            christianName = user.christianName
            surname = user.surname
            username = user.username
            email = user.email
            password = user.password
            isAdmin = user.isAdmin()
            isOperator = user.isOperator()
            address1 = "Update Address 1"
        }

        when: "attempt to update"
        User updated = service.updateUser(cmd)

        then: "the address should be update - but nothing else"
        updated.address.address1 == cmd.address1
    }

    void "Test updateUser - change user to admin"() {

        given: "An update to the user"
        UserCommand cmd = new UserCommand()
        cmd.with {
            id = user.id
            christianName = user.christianName
            surname = user.surname
            username = user.username
            email = user.email
            password = user.password
            isAdmin = true
            isOperator = user.isOperator()
        }

        when: "attempt to update"
        User updated = service.updateUser(cmd)

        then: "the user is now an admin"
        updated.isAdmin()
    }

    void "Test updateUser - change user to operator"() {

        given: "An update to the user"
        UserCommand cmd = new UserCommand()
        cmd.with {
            id = user.id
            christianName = user.christianName
            surname = user.surname
            username = user.username
            email = user.email
            password = user.password
            isAdmin = user.isOperator()
            isOperator = true
        }

        when: "attempt to update"
        User updated = service.updateUser(cmd)

        then: "the user is now an admin"
        updated.isOperator()
    }

    void "Test updateUser - change operator to user"() {

    }

    void "Test updateUser - change admin to user"() {

    }


}

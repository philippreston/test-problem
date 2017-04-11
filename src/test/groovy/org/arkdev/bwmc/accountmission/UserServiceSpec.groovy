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

}

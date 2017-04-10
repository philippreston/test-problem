package org.arkdev.bwmc.accountmission

import grails.transaction.Transactional
import grails.web.databinding.DataBinder
import groovy.util.logging.Log4j
import org.arkdev.bwmc.util.Helper

@Log4j
@Transactional
class UserService implements DataBinder {

    /**
     * This will return a list of all the Users in the system
     * @return List of all Users
     */
    List<User> list() {
        User.findAll()
    }

    /**
     * This will fetch an object by id
     * @param params The params object
     * @return A user if exists or null if not
     */
    User find(def params) {
        User.findById(params?.id)
    }

    /**
     * This will return an editable User instance (Command)
     * @param params These are the parameters passed from form
     * @return The User Command object
     */
    UserCommand getEditable(def params) {
        User user = find(params)
        if (user) {
            UserCommand cmd = new UserCommand()
            bindData(cmd, user, ['operator', 'admin', 'address', 'password'])
            cmd.with {
                // Address
                name = user?.address?.name
                address1 = user?.address?.address1
                address2 = user?.address?.address2
                townCity = user?.address?.townCity
                county = user?.address?.county
                postcode = user?.address?.postcode

                // Privileges
                isAdmin = user?.admin
                isOperator = user?.operator
            }
            return cmd
        }

        return null
    }

    /**
     * This will add a new user to the system, along with appropriate roles
     * @param cmd This is the command object that has the form bindings
     * @return Will return the newly created user
     * @throws UserServiceException if validation Error
     */
    User addUser(UserCommand userCommand) {
        assert userCommand

        User newUser = new User(userCommand.properties)
        if (newUser.validate()) {
            newUser.save()

            // Deal with authorities
            Role userRole = Role.findByAuthority('ROLE_USER')
            Role operatorRole = Role.findByAuthority('ROLE_OPERATOR')
            Role adminRole = Role.findByAuthority('ROLE_ADMIN')
            assert userRole
            assert operatorRole
            assert adminRole

            // Setup roles
            UserRole.create(newUser, userRole)
            if (userCommand.isAdmin)
                UserRole.create(newUser, adminRole)
            if (userCommand.isOperator)
                UserRole.create(newUser, operatorRole)

            UserRole.withSession {
                it.flush()
                it.clear()
            }

            return newUser
        }
        else {
            Helper.copyErrorToCmd(newUser, userCommand)
            throw new UserServiceException(cmd: userCommand, message: "There were errors creating user")
        }

    }

    /**
     * This will update a new user in the system
     * @param cmd This is the command object that has the form bindings
     * @return Will return the updated user
     * @throws UserServiceException if validation Error
     */
    def updateUser(UserCommand userCommand) {
        assert userCommand

        // Get the current
        User foundUser = User.findById(userCommand.id)
        def wasAdmin = foundUser.admin
        def wasOperator = foundUser.operator

        // Bind Data
        bindData(foundUser, userCommand, ['class', 'operator', 'admin', 'address'])
        foundUser?.address?.with {
            name = userCommand.name
            address1 = userCommand.address1
            address2 = userCommand.address2
            townCity = userCommand.townCity
            county = userCommand.county
            postcode = userCommand.postcode
        }

        if (foundUser.validate()) {
            foundUser = foundUser.save(flush: true, failOnError: true)

            // Deal with authorities
            Role operatorRole = Role.findByAuthority('ROLE_OPERATOR')
            Role adminRole = Role.findByAuthority('ROLE_ADMIN')
            assert operatorRole
            assert adminRole

            // Has been made an admin
            if (userCommand.isAdmin && userCommand.isAdmin != wasAdmin) {
                UserRole.create(foundUser, adminRole)
            }

            // Has been made an operator
            if (userCommand.isOperator && userCommand.isOperator != wasOperator) {
                UserRole.create(foundUser, operatorRole)
            }

            // No longer an admin
            if (!userCommand.isAdmin && userCommand.isAdmin != wasAdmin) {
                UserRole.findByUserAndRole(foundUser, adminRole).delete()
            }

            // No longer an admin
            if (!userCommand.isOperator && userCommand.isOperator != wasOperator) {
                UserRole.findByUserAndRole(foundUser, operatorRole).delete()
            }

            UserRole.withSession {
                it.flush()
                it.clear()
            }

            return foundUser
        }
        else {
            Helper.copyErrorToCmd(foundUser, userCommand)
            log.error("Errors: ${foundUser.errors.allErrors}")
            throw new UserServiceException(cmd: userCommand, message: "There were errors updating user")
        }

    }

}

class UserServiceException extends RuntimeException {
    UserCommand cmd
    String message
}

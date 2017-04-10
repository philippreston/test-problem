package org.arkdev.bwmc.accountmission

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable

import static org.arkdev.bwmc.util.FlashStatus.*

@Secured("ROLE_ADMIN")
class UserController {

    def userService

    /**
     * This will display the list of users on the index page
     * @return Map userList which is list of all users
     */
    def index() {
        [userList: userService.list()]
    }

    /**
     * This will render the screen to edit a User
     */
    def edit() {
        UserCommand container = userService.getEditable(params)
        if (container) {
            [user: container]
        }
        else {
            log.warn "User ${params.id} could not edit"
            flash.status = WARNING
            flash.message = "User trying to edit was not found (logged)"
            redirect(action: 'index')
        }
    }

    /**
     * This will show a selected user (but not allow edit)
     */
    def show() {
        User user = userService.find(params)
        if (user) {
            [user: user]
        }
        else {
            log.warn "User ${params.id} could not show"
            flash.status = WARNING
            flash.message = "User trying to show was not found (logged)"
            redirect(action: 'index')
        }

    }

    /***
     * This will display the create new user page
     * @return a map with user -> UserCommand
     */
    def create() {
        [user: new UserCommand()]
    }

    /**
     * This will save a new user then return to index page with success flash
     * @param cmd This is the usercommand object that carries parameters
     * @return None
     */
    def save(UserCommand cmd) {

        if (cmd.hasErrors()) {
            flash.status = ERROR
            flash.message = "There were errors creating user"
            render(view: 'create', model: [user: cmd])
            return
        }

        try {
            User newUser = userService.addUser(cmd)
            flash.status = SUCCESS
            flash.message = "User ${newUser.username} was created successfully"
            redirect(action: 'index')
        }
        catch (UserServiceException ex) {
            flash.status = ERROR
            flash.message = ex.message
            render(view: 'create', model: [user: ex.cmd])
        }
    }

    /**
     * This will validate and apply the update for a User
     * @param cmd This is the usercommand object that carries parameters
     */
    def update(UserCommand cmd) {

        if (cmd.hasErrors()) {
            flash.status = ERROR
            flash.message = "There were errors updating user"
            render(view: 'edit', model: [user: cmd])
            return
        }

        try {
            User newUser = userService.updateUser(cmd)
            flash.status = SUCCESS
            flash.message = "User ${newUser.username} was created successfully"
            redirect(action: 'show', model: newUser)
        }
        catch (UserServiceException ex) {
            flash.status = ERROR
            flash.message = ex.message
            render(view: 'edit', model: [user: ex.cmd])
        }
    }

    /**
     * This will render the screen to edit a User
     */
    def delete() {
    }
}

/**
 * User Command object
 */
class UserCommand implements Validateable {
    int id
    String christianName
    String surname
    String username
    String password
    String passwordCheck
    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false
    String phone
    String skype
    String email
    String name
    String address1
    String address2
    String townCity
    String county
    String postcode
    boolean isAdmin = false
    boolean isOperator = false

    // FIXME - password import
    static constraints = {
        importFrom User
        importFrom Address
        password blank: false, password: true, minSize: 8
        passwordCheck(blank: false, password: true, validator: { pwd, uco -> return pwd == uco.password })
    }
}

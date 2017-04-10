package org.arkdev.bwmc.accountmission

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(excludes = 'dateCreated')
class Address {

    Date dateCreated
    String name
    String address1
    String address2
    String townCity
    String county
    String postcode

    static belongsTo = User

    static constraints = {
        name nullable: true, blank: true, size: 1..255
        address1 nullable: true, blank: true, size: 1..255
        address2 nullable: true, blank: true, size: 1..255
        townCity nullable: true, blank: true, size: 1..50
        county nullable: true, blank: true, size: 1..50
        postcode nullable: true, blank: true, size: 1..50
    }
}

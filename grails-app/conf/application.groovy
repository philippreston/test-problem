// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'org.arkdev.bwmc.accountmission.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'org.arkdev.bwmc.accountmission.UserRole'
grails.plugin.springsecurity.authority.className = 'org.arkdev.bwmc.accountmission.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/login/**', access: ['permitAll']],
        [pattern: '/logout/**', access: ['permitAll']],
        [pattern: '/error', access: ['permitAll']],
        [pattern: '/assets/**', access: ['permitAll']],
        [pattern: '/**/js/**', access: ['permitAll']],
        [pattern: '/**/css/**', access: ['permitAll']],
        [pattern: '/**/images/**', access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']],
        [pattern: '/users/**', access: ['ROLE_ADMIN', 'isFullyAuthenticated()']],
        [pattern: '/**', access: ['isFullyAuthenticated()']],
]

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**', filters: 'none'],
        [pattern: '/**/js/**', filters: 'none'],
        [pattern: '/**/css/**', filters: 'none'],
        [pattern: '/**/images/**', filters: 'none'],
        [pattern: '/**/favicon.ico', filters: 'none'],
        [pattern: '/**', filters: 'JOINED_FILTERS']
]


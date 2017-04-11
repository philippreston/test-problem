# test-problem

http://stackoverflow.com/questions/43227084/grails-unit-test-null-strangeness

# Running 

./grailsw test-app


# Problem 

So running `./grailsw test-app` will run all the service tests and will pass on 1 test and fails on 1

Running the failed test individually works:

`./gradlew test --tests "org.arkdev.bwmc.accountmission.UserServiceSpec.*Test updateUser - changing a user detail"`

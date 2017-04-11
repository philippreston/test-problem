# test-problem

http://stackoverflow.com/questions/43227084/grails-unit-test-null-strangeness

# Running 

./grailsw test-app


# Problem 

So running `./grailsw test-app` will run all the service tests and will fail on 4 update tests

Running the tests individually works:

`gradle test --tests "org.arkdev.bwmc.accountmission.UserServiceSpec.*Test editable - check user that does not exists"`

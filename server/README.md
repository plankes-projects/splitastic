# Setup:
Create app/application.properties from app/application.properties.template and fill data. (this location is important otherwise 'mvnw install' does not work)
* Provide spring hibernate mysql config
* Provide spring JavaMailSender config
* Provide custom config variables:
##### admin.token 
> An admin token with at least length 100. Send this in the header X-ADMIN-TOKEN for an instant login. The VUE client will do so if present in local storage key admin.token
##### server.baseurl
> eg http://localhost:8080
##### email.from
> This will be used as sender for the login email.


# Run:

> mvnw install

This will generate code based on openapi.yml in specification project and install all dependencies.

> java -jar app/target/app-0.0.1-SNAPSHOT.jar --spring.config.location=app/application.properties

Runs the app

# Notes:
* Initial project structure based on https://reflectoring.io/spring-boot-openapi/

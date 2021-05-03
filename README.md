# Logica Sandbox Manager API

Welcome to the Logica Sandbox Manager API!  

# Logica Sandbox

*Note:* If you are wanting to build and test SMART on FHIR Apps, it is recommended that you use the free cloud-hosted version of the HSPC Sandbox.

[Logica Sandbox](https://sandbox.interop.community)

### How do I get set up?
This project uses Java 11 and MySQL version 5.7.24. Please make sure that your Project SDK is set to use Java 11.

#### Step 1: Prerequisites
 * Create "sandman" schema in local mysql. Check application.yml that name has not changed.  This schema will be populated as you start your server.

#### Step 2: Maven Build

In the terminal, run the following command:

    mvn package
    
#### Step 3: Run locally or Run on Docker

###### For local installation

    ./run-local.sh

###### For Docker Installation

    cd docker/nginx
    ./build.sh
    cd ..
    ./build.sh
    docker-compose up
    
The set up process is complete and your project is running now. The service is available at: 
    http://localhost:12000/health

#### Configuration ####

Various property files configure the sandbox manager api:

 * src/main/resources/application.yml
 * src/main/resources/application-*.yml
 
### Where to go from here ###
https://healthservices.atlassian.net/wiki/display/HSPC/Healthcare+Services+Platform+Consortium

## Additional Info

### Port Assignments

| Item                    | Port  |
| ----------------------- | -----:|
| SANDMAN-API-DSTU2       | 12000 |
| SANDMAN-API-DSTU2-DEBUG | 12005 |
| MySQL                   |  3306 |

### Add an user to a sandbox

Do a PUT call through Postman
https://sandbox-api.interop.community/sandbox/REPLACE_THIS_WITH_SANDBOXID?editUserRole=REPLACE_THIS_SBM_USERID&role=USER&add=true

The authorization token and Content-Type = application/json is required to make this call.
Role could also be changed to be either ADMIN, USER, READONLY, MANAGE_USERS or MANAGE_DATA.
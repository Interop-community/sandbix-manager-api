# HSPC Sandbox Manager API

Welcome to the HSPC Sandbox Manager API!  

# HSPC Sandbox

*Note:* If you are wanting to build and test SMART on FHIR Apps, it is recommended that you use the free cloud-hosted version of the HSPC Sandbox.

[HSPC Sandbox](https://sandbox.hspconsortium.org)

### How do I get set up? ###

### Prerequisites ###
 * build "sandman" schema in local mysql. Check application.yml that name has not changed.

#### Build and Deploy ####
    mvn clean package
    ./run_local.sh

#### Configuration ####

Various property files configure the sandbox manager api:

 * src/main/resources/application.yml
 * src/main/resources/application-*.yml
 
### Where to go from here ###
https://healthservices.atlassian.net/wiki/display/HSPC/Healthcare+Services+Platform+Consortium
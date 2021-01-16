#!/bin/bash

#Create a docker network for services to communicate
docker network create logica-network

# Start the database service
docker-compose up -d sandbox-mysql;

# Seed the database
echo "Seed the database";
for i in *.sql; do
   docker exec -i communityedition_sandbox-mysql mysql -uroot -ppassword < $i;
done

# Add host table entries for sandbox services
echo "Adding host table entries";
echo "127.0.0.1  keycloak" >> /etc/hosts;
echo "127.0.0.1  sandbox-mysql" >> /etc/hosts;
echo "127.0.0.1  sandbox-manager-api" >> /etc/hosts;
echo "127.0.0.1  reference-auth" >> /etc/hosts;
echo "127.0.0.1  dstu2" >> /etc/hosts;
echo "127.0.0.1  stu3" >> /etc/hosts;
echo "127.0.0.1  r4" >> /etc/hosts;

docker-compose stop;
#!/usr/bin/env bash

jenv local 11

java \
  -Xms256M \
  -Xmx512M \
  -jar target/hspc-sandbox-manager-api*.jar

#!/usr/bin/env bash

jenv local 1.8

java \
  -Xms256M \
  -Xmx512M \
  -jar target/hspc-sandbox-manager-api*.jar

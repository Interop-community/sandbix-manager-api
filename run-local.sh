#!/usr/bin/env bash

java \
  -Xms256M \
  -Xmx512M \
  -jar target/hspc-sandbox-manager-api*.jar

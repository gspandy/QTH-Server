#!/usr/bin/env bash
mvn clean package  -am -pl nb-admin  -Dmaven.test.skip=true -Pprod
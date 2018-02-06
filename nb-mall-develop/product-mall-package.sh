#!/usr/bin/env bash
mvn clean package  -am -pl nb-mall-web  -Dmaven.test.skip=true -Pprod
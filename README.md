[![Build Status](https://travis-ci.org/gwidgets/gwt-project-generator.svg?branch=master)](https://travis-ci.org/gwidgets/gwt-project-generator)


## Disclaimer: This project is based on an old version of Spring Initialzr written in Groovy, and is not currently maintained


## Overview

gwt-project-generator is a web application that helps developers quickly bootstrap and generate a GWT application structure. gwt-project-generator is a fork of [Spring Initializr](https://github.com/spring-io/initializr) which is used to bootstrap Spring boot applications. Because the GWT ecosystem has a large number of libraries and frameworks, it might be handy to have everything aggregated in one tool. gwt-project-generator can generate a simple project structure based on Maven or Gradle. The chosen dependencies are automatically added to the build file and the inherits are added to the `.gwt.xml` module descriptor. 

##Building and Running Locally: 

Because the project has diverged from Spring Initializr, the tests needs fixing, and are broke for now, so tests should be skipped for now. 

`mvn clean install -Dmaven.tests.skip=true`

If you want to build both the library and the service (useful when deploying), you can enable the full profile:

`mvn clean install -Pfull -Dmaven.tests.skip=true`

and then form the `gwt-project-generator-service` module, you need to run: 

`mvn spring-boot:run`

## GWT versions:

gwt-project-generator can generate 2.7.0 and 2.8.0 projects. 



[![Build Status](https://travis-ci.org/gwidgets/gwt-project-generator.svg?branch=master)](https://travis-ci.org/gwidgets/gwt-project-generator)


## Overview

gwt-project-generator is a web application that helps developers quickly bootstrap and generate a GWT application. gwt-project-generator is a fork of [Spring Initializr](https://github.com/spring-io/initializr) which is used to bootstrap Spring boot applications. Our gwt-project-generator instance is running on: [https://gwt-project-generator.cfapps.io/](https://gwt-project-generator.cfapps.io/). Because the GWT ecosystem has a large number of libraries and frameworks, it might be handy to have everything aggregated in one tool. gwt-project-generator can generate a simple project structure based on Maven or Gradle. The chosen dependencies are automatically added to the build file and the inherits are added to the `.gwt.xml` module descriptor. gwt-project-generator is a "work in progress" project, so it can have bugs or misconfigurations. 

##Modules

gwt-project-generator has the following modules:

`gwt-project-generator-generator`: standalone project generation library that can be reused in many environments 

`gwt-project-generator-web`: REST endpoints and web interface

`gwt-project-generator-actuator`: optional module to provide statistics and metrics on project generation

`gwt-project-generator-service` is an additional module that represents the app instance, that can be deployed to production. 


##Building and Running Locally: 

Because the project has diverged from Spring Initializr, the tests needs fixing, and are broke for now, so tests should be skipped for now. 

`mvn clean install -Dmaven.tests.skip=true`

If you want to build both the library and the service (useful when deploying), you can enable the full profile:

`mvn clean install -Pfull -Dmaven.tests.skip=true`

and then form the `gwt-project-generator-service` module, you need to run: 

`mvn spring-boot:run`

##Adding you own dependencies:

If you are a GWT developer and would like to add a library/framework to the list you can edit the application.yml file and make a PR. 

the same goes for new features. 


## GWT versions:

gwt-project-generator can generate 2.7.0 and 2.8.0 projects. 



# Lab 3: Calling another Microservice

In the first lab we extended an existing Microservice to an OAuth 2.0 and OpenID Connect 1.0 compliant Resource Server.
Target of this lab is to add another Microservice from the Microservice we extended in lab 1.

## Learning Targets

In this lab we will add functionality to call another Microservice (resource server) with propagating the JWT.

In this lab you will learn how to:

1. How to call other Microservices using the WebClient.
2. How to authenticate at the other Microservice by propagating the existing JWT.

## Folder Contents

In the folder of lab 2 you find 2 applications:

* __initial__: This is the application we will use as starting point for this lab
* __final__: This application is the completed reference for this lab 

## Start the Lab

In this lab we will implement:

* An integration test to verify correct authentication & authorization for the ToDo API using JWT

Please start this lab with project located in _lab3/initial_.

Please also have a look at the other tests as well in the reference solution.

<hr>

This is the end of this lab and of the workshop.

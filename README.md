# cse124-lec-grpc

CSE 124 Fall 2017 grpc companion code

George Porter (gmporter@cs.ucsd.edu)

## To build the protocol buffer IDL into auto-generated stubs:

$ mvn protobuf:compile protobuf:compile-custom

## To build the code:

$ mvn package

## To run the server

$ target/atm/bin/runServer [port]

## To run the client

$ target/atm/bin/runClient [hostname] [port]

## To delete all programs and object files

$ mvn clean

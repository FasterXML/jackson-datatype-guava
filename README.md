Project to build Jackson (http://jackson.codehaus.org) module (jar)
to support JSON serialization and deserialization of
Guava (http://code.google.com/p/guava-libraries/) collection types.

[![Build Status](https://fasterxml.ci.cloudbees.com/job/jackson-datatype-guava-master/badge/icon)](https://fasterxml.ci.cloudbees.com/job/jackson-datatype-guava-master/)

## Status

As of version 2.0 module is usable although coverage is not extensive:
more support is added mainly via user contributions.

## Usage

### Maven dependency

To use module on Maven-based projects, use following dependency:

    <dependency>
      <groupId>com.fasterxml.jackson.datatype</groupId>
      <artifactId>jackson-datatype-guava</artifactId>
      <version>2.1.0</version>
    </dependency>    

(or whatever version is most up-to-date at the moment)

### Registering module

Like all standard Jackson modules (libraries that implement Module interface), registration is done as follows:

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());

after which functionality is available for all normal Jackson operations.

## More

See [Wiki](jackson-datatype-guava/wiki) for more information (javadocs, downloads).


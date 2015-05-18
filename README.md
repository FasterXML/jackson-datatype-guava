[Jackson](http://jackson.codehaus.org) module (jar)
to support JSON serialization and deserialization of
[Guava](http://code.google.com/p/guava-libraries/) data types.

## Status

[![Build Status](https://travis-ci.org/FasterXML/jackson-datatype-guava.svg)](https://travis-ci.org/FasterXML/jackson-datatype-guava)

Module has been production-ready since version 2.3.
Not all datatypes of Guava are support due to sheer size of the library; new support is added based on contributions.

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Usage

### Maven dependency

To use module on Maven-based projects, use following dependency:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.datatype</groupId>
  <artifactId>jackson-datatype-guava</artifactId>
  <version>2.4.0</version>
</dependency>
```

(or whatever version is most up-to-date at the moment)

### Registering module

Like all standard Jackson modules (libraries that implement Module interface), registration is done as follows:

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new GuavaModule());
```

after which functionality is available for all normal Jackson operations.

## More

See [Wiki](jackson-datatype-guava/wiki) for more information (javadocs, downloads).


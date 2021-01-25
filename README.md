# RDFWeaver

A Java application implementation of a multi-media data source to RDF Graph converter.

[![Build Status](https://travis-ci.org/jiefenn8/rdfweaver.svg?branch=master)](https://travis-ci.org/jiefenn8/rdfweaver)
[![codecov](https://codecov.io/gh/jiefenn8/rdfweaver/branch/master/graph/badge.svg)](https://codecov.io/gh/jiefenn8/rdfweaver)
[![Apache 2.0 License](https://img.shields.io/badge/license-apache2-green.svg) ](https://github.com/jiefenn8/rdfweaver/blob/master/LICENSE.md)

[![Release](https://img.shields.io/github/v/release/jiefenn8/rdfweaver)](https://github.com/jiefenn8/rdfweaver/releases/latest)
[![Bintray](https://img.shields.io/bintray/v/jiefenn8/rdfweaver/rdfweaver)](https://bintray.com/jiefenn8/rdfweaver/rdfweaver/_latestVersion)

## Description

A Java application implementation of a multi-media data source to RDF Graph converter using the 
[GraphLoom](https://github.com/jiefenn8/graphloom) library. Targeted to initially convert a relational database to RDF 
semantic graph with later targets in the roadmap to implement mapping from other multi-media sources. 

## Getting Started

To get started on using RDFWeaver to map your data source, ensure you have met all prerequisites and follow any 
instructions below to get the application up and running for mapping or testing purposes.

### Prerequisites

Current version of RDFWeaver is built on Java 8. Visit the [Java site](https://www.java.com/en/download/) to download 
the appropriate runtime environment.

### Downloading RDFWeaver

For each release, a zip containing the RDFWeaver fat JAR binary will be uploaded as well for immediate usage.
However, if you want to obtain just the artifact without the dependencies; download from the Bintray badge above or
add the following to the build settings.

#### Maven

Repository:
```
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>bintray-jiefenn8-rdfweaver</id>
    <name>bintray</name>
    <url>https://dl.bintray.com/jiefenn8/rdfweaver</url>
</repository>
```

Dependency:
```
<dependency>
  <groupId>com.github.jiefenn8.rdfweaver</groupId>
  <artifactId>rdfweaver-cli</artifactId>
  <version>0.1.0</version>
</dependency>
```

#### Gradle

Repository:
```
repositories {
    maven {
        url  "https://dl.bintray.com/jiefenn8/rdfweaver" 
    }
}
```

Dependency:
```
dependencies {
  implementation 'com.github.jiefenn8.rdfweaver:rdfweaver-cli:0.1.0'
}
```

### Usage example

_At this current version, all subcommands must be chained together at the end in order to work successfully. 
The order of the subcommands does not matter._

_Note: There are plans to improve this in the future._

```
java rdfweaver-console-x.x.x.jar <subcommand 1> <subcommand 2> <subcommand 3>

java rdfweaver-console-x.x.x.jar server <server args> r2rml <r2rml args> output <output args>
```

#### Server Subcommand (--server):

**Required:**
```
(-d, --driver) Database driver
(-h, --host) Database host URL
(-p, --port) Database host port
(-u, --user) Login to access database
(-pw, --password) Password for login
```
**Optional:**
```
(-db, --database) Database to use within a host (if multiple instances exist)
```
Example: 
```
server --driver=MSSQL --host=192.168.1.1 --port=1433 --user=sa --password=YourStrong@Passw0rd --database=MyDatabase01
```

#### R2RML Subcommand (--r2rml):

**Required:**
```
(-f, --file) R2RML path including filename
```
Example: 
```
r2rml --file=C:/MyFolder/MyR2RML.ttl
```
#### Output Subcommands (--output):

For file output:

**Optional:**
```
(-d, --dir) File output directory (default: /output in RDFWeaver directory)
(-n, --name) File output name (default: rdfOutput.ttl)
(-f, --format) File format to output the RDF triples as (Default: NTRIPLES) 
```
Example: 
``` 
output --dir=C:/MyOutputDirectory --name=MyRDFOutput.ttl --format=TURTLE
```

For Fuseki database output:

**Required:**
```
(-h, --host) Fuseki database host URL 
(-p, --port) Fuseki database host port
(-b, --baseName) Fuseki database endpoint base name
```
**Optional:**
```
(-g, --graphName) Fuseki database graph name to upload RDF data under
```
Example: 
```
output --host=192.168.1.1 --port=8080 --baseName=ds --graphName=default
```

## More information

For more information on RDFWeaver such as tutorial, additional usage tips or planned roadmap, visit the 
[Wiki](https://github.com/jiefenn8/rdfweaver/wiki).

Collection of other technology related projects can be found in this 
[repository](https://github.com/jiefenn8/ws-projects).

## License

This project is licensed under the terms of [Apache 2.0 License](./LICENSE.md). 

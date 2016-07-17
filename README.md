Hello IP(v4)
============

A library to fetch (and cache) public information about IPv4 ranges and use that information to classify requests
to your platform.

Use cases:
1. (Online) You want to extend a warm welcome to clients accessing your web site from a cloud service such as EC2, GCE
or Azure.
2. (Offline) You process log files containing IPs (e.g. web access logs) and want to use information about the client's
organization in your processing.

Supported range information providers:
* AWS
* Azure
* GCE
* Apnic

Usage:
1. (Online) Check example [HelloIpServlet](helloip-java-examples/src/main/java/io.mkr.helloip.examples/IpInfoServlet.java).
2. (Offline) Check example [ApacheLogIpStats](helloip-java-examples/src/main/java/io.mkr.helloip.examples/ApacheLogIpStats.java).

**(Note: As long as this project did not have its first release you need to directly use the source)**

Include
~~~~
    <dependency>
        <groupId>io.mkr.helloip</groupId>
        <artifactId>helloip-java</artifactId>
        <version>0.1</version>
    </dependency>
~~~~

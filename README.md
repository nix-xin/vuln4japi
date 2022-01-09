# vuln4japi
A vulnerable Java based REST API for demonstrating CVE-2021-44228 (log4shell).

# Motivation
Log4Shell took the internet by storm in early December 2021. A Zero Day vulnerability in the Apache Log4j logging library, capable of Remote Code Execution (RCE) that had organizations across the globe scrambling to fix/patch/mitigate their public facing Java applications. As the InfoSec community came together to provide a continued analysis and solutions to security teams, the Log4j community was busy developing patches to ensure an end to this vuln. 

I developed this simple vulnerable REST API that demonstrates the path to Remote Code Execution (RCE) by exploiting this vulnerability using the Apache Tomcat application server. I hope this proof of concept can be used for training your SecOps teams  or educating current and future application developers. Train your teams and improve your defenses, including but not limited to, SIEM rules/alerts, EDRs, SOARs.

# Disclaimer
This tutorial and source code are provided solely for eductional purposes. Please use responsibly.

Let's get started.

# What You Need
- Test Environment (VM or Hardware, preferably Linux to build)
- Java JDK (I used OpenJDK 1.8.0_312)
- Maven (Java project build and management tool; I used ver. 3.6.3)
- Marshalsec (Java Unmarshaller for JNDI Redirecting: https://github.com/mbechler/marshalsec)
- Tomcat 8 (I used version 8.0.32: https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/) 



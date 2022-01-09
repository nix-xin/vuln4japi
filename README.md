# vuln4japi
A vulnerable Java based REST API for demonstrating CVE-2021-44228 (log4shell).

# Motivation
Log4Shell took the internet by storm in early December 2021. A Zero Day vulnerability in the Apache Log4j logging library, capable of Remote Code Execution (RCE) that had organizations across the globe scrambling to fix/patch/mitigate their public facing Java applications. As the InfoSec community came together to provide a continued analysis and solutions to security teams, the Log4j community was busy developing patches to ensure an end to this vuln. 

I developed this simple vulnerable REST API that demonstrates the path to Remote Code Execution (RCE) by exploiting this vulnerability using the Apache Tomcat application server. I hope this proof of concept can be used to train your SecOps teams or educating current and future application developers. Train your teams and improve your defenses, including but not limited to, SIEM rules/alerts, EDRs, SOARs.

# Disclaimer
This tutorial and source code are provided solely for educational and training purposes. Please use ethically and responsibly.

Let's get started.

# What You Need
- Test Environment (VM or Hardware, preferably Linux to build and test)
- Java JDK (I used OpenJDK 1.8.0_312)
- Maven (Java project build and management tool; I used ver. 3.6.3)
- Marshalsec (Java Unmarshaller for JNDI Redirecting: https://github.com/mbechler/marshalsec)
- Apache Tomcat 8 (I used version 8.0.32: https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/)
- Finally, clone this repo!

# Process Flow
Once you have Java JDK and Maven installed, assuming you're on a Linux distro, change directory to your vuln4japp location and build your project.
> Note: You may want to modify certain components of the app before you build it. For example, you can modify the path to the log4j logs in the log4j2.xml file. Or you may want to change the name of the resulting war file in the pom.xml file. Its totally up to you.

```bash
cd /path/to/vuln4japp
mvn clean package -DskipTests 
```
If you dont see any build errors, you should have a newly created target directory with your vuln4japp.war file. Ok, we'll come back to this file a little later. Let's look at Tomcat briefly.

Depending on your version of Java 8, later versions of 8 may have this particular setting (com.sun.jndi.ldap.object.trustURLCodebase) set to false. This effectively disallows JNDI from loading a remote codebase via LDAP. Which is what this vulnerability is exploiting. So, we need to modify Tomcat's catalina.properties file to set this system setting to True, making Tomcat intentionally vulnerable.

Once you download apache-tomcat-8.0.32.tar.gz for Linux, untar it somewhere like the /opt directory.

```bash
tar -xvf apache-tomcat-8.0.32.tar.gz -C /opt
```
Change directory to /conf and modify catalina.properties at the bottom of the file. Note: Use your prefered text editor. I use vim in this example.

```bash
cd /opt/apache-tomcat-8.0.32/conf

vim catalina.properties
com.sun.jndi.ldap.object.trustURLCodebase=true
com.sun.jndi.rmi.object.trustURLCodebase=true
com.sun.jndi.cosnaming.object.trustURLCodebase=true
``` 
Exit your text editor and start Tomcat using the catalina.sh script inside the /bin directory.

```bash
cd /opt/apache-tomcat-8.0.32/bin
./catalina.sh start
```
Test your instance of Apache Tomcat by navigating to http://localhost:8080/ or a simple cURL command from the command line interface.

```bash
curl -vv http://localhost:8080/
```

If you see a welcome page on your browser or terminal, then it should be working. Now we can deploy our Vulnerable App.


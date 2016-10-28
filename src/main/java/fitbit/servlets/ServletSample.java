/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Vlad
 * 
 * This has overriden service() -- it checks if there is a Credential. If so, it proceeds to doGet;
 * if not,  
 * 
 * http://grepcode.com/file/repo1.maven.org/maven2/com.google.oauth-client/google-oauth-client-servlet/1.7.0-beta/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeServlet.java
 */

//https://github.com/google/google-oauth-java-client/blob/master/samples/dailymotion-cmdline-sample/src/main/java/com/google/api/services/samples/dailymotion/cmdline/DailyMotionSample.java

@WebServlet(name = "ServletSample", urlPatterns = {"/ServletSample"})
public class ServletSample extends AbstractAuthorizationCodeServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException,ServletException  {
    // do stuff
    
    
    System.out.println(getRedirectUri(request));
  }

  //for redirecting back from fitbit
  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/ServletCallbackSample");//ServletCallbackSample
    
    System.out.println("omg: "+url.build());//http://localhost:8084/ServletCallbackSample
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new NetHttpTransport(),
        new JacksonFactory(),
        new GenericUrl("https://api.fitbit.com/oauth2/token"),
        new BasicAuthentication("227T4W", "54b87a495109c3c10c06bf56754d6cc3"),
        "227T4W",
        "https://www.fitbit.com/oauth2/authorize").setScopes(Arrays.asList("activity","settings")).setCredentialDataStore(
            StoredCredential.getDefaultDataStore(
                new FileDataStoreFactory(new File("U:\\Desktop\\datastoredir"))))
        .build();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
      
   return "4PDGJ9";
   
  }
}

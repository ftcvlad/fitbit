/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import fitbit.stores.Patient;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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


//http://stackoverflow.com/questions/7722062/google-oauth2-redirect-uri-with-several-parameters -- add parameters to redirect uri
@WebServlet(name = "ServletSample", urlPatterns = {"/ServletSample"})
public class ServletSample extends HttpServlet {
 
  private final Lock lock = new ReentrantLock();
  private AuthorizationCodeFlow flow;
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException,ServletException  {
   
//      flow = initializeFlow();
//      
//       Credential credential = flow.loadCredential("4PDGJ9");
//      if (credential != null && credential.getAccessToken() != null) {
//          System.out.println("user 1 found" );
//      }
      


      
     
      
     
      
      String name = request.getParameter("firstName").trim();
      String surname = request.getParameter("secondName").trim();
      String day = request.getParameter("day");
      String month = request.getParameter("month");
      String year = request.getParameter("year");

      day = (day.length() == 1) ? ("0" + day) : day;
      month = (month.length() == 1) ? ("0" + month) : month;

      String birthDate = null;
      if (!day.equals("") || !year.equals("")) {//if not 2 empty strings
          birthDate = year + "-" + month + "-" + day;
      }

      if (name.equals("") && surname.equals("")) {
          name = "noname";
      }
        
      
      HttpSession session = request.getSession(false);
      Patient p = new Patient();
      p.setName(name);
      p.setSurname(surname);
      p.setBirthDate(birthDate);
      
//      String state
//      
//      npi.setState(state);
      session.setAttribute("newPatientInfo",p);
     
      lock.lock();
      try {
          flow = initializeFlow();

          
         // System.out.println("::: "+flow.newAuthorizationUrl().setRedirectUri(getRedirectUri(request)).set("prompt","consent").build());
          
          
          
          String redirectUri = getRedirectUri(request);
          response.sendRedirect(flow.newAuthorizationUrl().setRedirectUri(redirectUri).set("prompt","consent").build());

      } finally {
          lock.unlock();
      }
  }

  //for redirecting back from fitbit (with authorization code)

  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    
    url.setRawPath(req.getContextPath()+"/ServletCallbackSample");
    
   
    return url.build();
  }


  protected AuthorizationCodeFlow initializeFlow() throws IOException {

  
    AuthorizationCodeFlow.Builder acfb = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    new GenericUrl("https://api.fitbit.com/oauth2/token"),
                    new BasicAuthentication("227T4W", "54b87a495109c3c10c06bf56754d6cc3"),
                    "227T4W",
                    "https://www.fitbit.com/oauth2/authorize");
    
    acfb.setScopes(Arrays.asList("activity","settings"));
    
    FileDataStoreFactory ff = new FileDataStoreFactory(new File("C:\\Users\\Vlad\\Desktop\\tokens"));
    acfb.setCredentialDataStore(StoredCredential.getDefaultDataStore(ff));
    return acfb.build();
    
  }


}

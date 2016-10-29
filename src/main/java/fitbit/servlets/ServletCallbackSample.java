/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import fitbit.models.PatientManager;
import fitbit.models.User;
import fitbit.stores.FitbitTokenResponse;
import fitbit.stores.Patient;



import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author Vlad
 * This called by Fitbit after user enters credentials. It reads authorization code, requests token,
 * creates Credential and stores it and redirects to / onSuccess
 * http://grepcode.com/file/repo1.maven.org/maven2/com.google.oauth-client/google-oauth-client-servlet/1.7.0-beta/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeCallbackServlet.java?av=f
 * 
 * 
 * 
 * 
 * http://homakov.blogspot.co.uk/2013/03/redirecturi-is-achilles-heel-of-oauth.html -- why appends #_=_
 * 
 * 
 */
@WebServlet(name = "ServletCallbackSample", urlPatterns = {"/ServletCallbackSample"})
public class ServletCallbackSample extends HttpServlet  {

    private DataSource dataSource;
    
    @Override
    public void init() throws ServletException {
		try {
                        dataSource = (DataSource) new InitialContext().lookup("java:comp/env/" + "jdbc/db");
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
    }

  private final Lock lock = new ReentrantLock();

  private AuthorizationCodeFlow flow;

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    StringBuffer buf = req.getRequestURL();
    if (req.getQueryString() != null) {
      buf.append('?').append(req.getQueryString());
    }
    AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(buf.toString());
    String code = responseUrl.getCode();
    if (responseUrl.getError() != null) {
      onError(req, resp, responseUrl);
    } else if (code == null) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().print("Missing authorization code");
    } else {
      String redirectUri = getRedirectUri(req);
      lock.lock();
      try {
        if (flow == null) {
          flow = initializeFlow();
        }
        
        
        HttpResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).executeUnparsed();
        TokenResponse tokenResponse = new TokenResponse();
        FitbitTokenResponse ftr =null;
        try {
          

            ftr = response.parseAs(FitbitTokenResponse.class);
           
            
            
            tokenResponse.setAccessToken(ftr.getAccess_token());
            tokenResponse.setScope(ftr.getScope());
            tokenResponse.setExpiresInSeconds(ftr.getExpires_in());
            tokenResponse.setRefreshToken(ftr.getRefresh_token());
            tokenResponse.setTokenType(ftr.getToken_type());
            
            
            
            
        } 
        catch(IOException ioe){
            System.out.println("IO exception occured while parsing Fitbit token response!");
            return;
        }
        finally {
            response.disconnect();
        }
        
        String patientFitbitId = ftr.getUser_id();//id of the just authorized user
        
        HttpSession session = req.getSession(false);
        Patient npi = (Patient) session.getAttribute("newPatientInfo");
        npi.setFitbitId(patientFitbitId);
        
        User us = (User)session.getAttribute("user");
        String clinicianUsername = us.getUsername();
        
        //SAVE TO DATABASE
        
        
        Connection conn = null ;
        try {
            conn= dataSource.getConnection();
            PatientManager pm = new PatientManager();
            pm.savePatient(clinicianUsername, npi,conn);
            us.allPatients.add(npi);
            
            
            //SAVE TOKEN TO TOKEN STORE (FILE ON SERVER)
            Credential credential = flow.createAndStoreCredential(tokenResponse, patientFitbitId);
            onSuccess(req, resp, credential);
            
         }
        catch (SQLException sqle){
                sqle.printStackTrace();
                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Database error");
        }
        catch (Exception e){
                resp.setStatus(400);
                resp.setContentType("text/plain");
                resp.getWriter().write("Bad input --shouldn't happen!");
        }
        finally{
                if (conn != null){
                    try {conn.close();} 
                    catch (SQLException ignore) { }
                }
        }
        
        
      
      
      } finally {
        lock.unlock();
      }
    }
  }
    
    
    


    

  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
      System.out.println("TOKEN SAVED!!!"+req.getContextPath());
      resp.sendRedirect(req.getContextPath());
  }

  protected void onError(
            HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
            throws ServletException, IOException {
        System.out.println("Error -- token not saved!");

        resp.setStatus(401);
        resp.setContentType("text/plain");
        resp.getWriter().write(errorResponse.getError());
  }
/*
  This is not used (???), but should match authorization redirect_uri
  https://tools.ietf.org/html/rfc6749#section-4.1.3
  
  good read some day
  http://security.stackexchange.com/questions/44214/what-is-the-purpose-of-oauth-2-0-redirect-uri-checking
 
  and more
  http://homakov.blogspot.co.uk/2013/03/oauth1-oauth2-oauth.html
  */

  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
   // url.setRawPath("/oauth2callback");
   url.setRawPath(req.getContextPath()+"/ServletCallbackSample"); 
   
   return url.build();
  }


  protected AuthorizationCodeFlow initializeFlow() throws IOException {
     return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new NetHttpTransport(),
        new JacksonFactory(),
        new GenericUrl("https://api.fitbit.com/oauth2/token"),
        new BasicAuthentication("227T4W", "54b87a495109c3c10c06bf56754d6cc3"),
        "227T4W",
        "https://www.fitbit.com/oauth2/authorize").setScopes(Arrays.asList("activity","settings")).setCredentialDataStore(
            StoredCredential.getDefaultDataStore(
                new FileDataStoreFactory(new File("C:\\Users\\Vlad\\Desktop\\tokens"))))
        .build();

  }


//  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
//    // return user ID
//     return "4PDGJ9";//3VD94D
//  }
}

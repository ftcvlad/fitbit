/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.accessFitbitService;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.File;


import java.io.IOException;
import java.util.Arrays;

import FitbitJsonBeans.DayResponse;
import FitbitJsonBeans.MinuteData;
import FitbitJsonBeans.DaySummary;
import FitbitJsonBeans.DeviceData;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;

/**
 *
 * @author Vlad
 */
public class FitbitRequestManager {
    

    static ServletContext context; 
    
    
    public static void setContext (ServletContext con){
        context= con;
    }
    
    public static String getLastSyncDate(String activeUserEmail,String fitbitId) throws Exception{
        
        final HttpRequestFactory requestFactory = getRequestFactory(activeUserEmail+fitbitId); 
       
        if (requestFactory==null){
            throw new Exception("Could not create request factory!");
        }
        
        GenericUrl requestUrl = new GenericUrl("https://api.fitbit.com/1/user/"+fitbitId+"/devices.json");

        HttpRequest request = requestFactory.buildGetRequest(requestUrl);
        HttpResponse response = request.execute();
        
        if (response.isSuccessStatusCode()) {

            String responseAsString = response.parseAsString();
            Gson gson = new Gson();
           
            ArrayList<DeviceData> devices = gson.fromJson(responseAsString, new TypeToken<ArrayList<DeviceData>>(){}.getType());
            
            if (devices.size()>1){
                throw new Exception("There can be only 1 fitbit device associated with each account!");
            }
            else if (devices.isEmpty()){
                return "1900-00-00";//all dates are considered to be noSync if no device is linked to the account!
            }
            else{
                return devices.get(0).getLastSyncTime().substring(0,10);
                        
            }

        } else {
            throw new Exception("Issue with the server call (device): " + response.getStatusMessage());
        }
        
        
    }
   
    
    
    //http://www.programcreek.com/java-api-examples/index.php?api=com.google.api.client.auth.oauth2.StoredCredential -- good one!!!
    public static ArrayList<DayResponse> retrieveFromFitbitIntraday(String[] selDates, String fitbitId, String activeUserEmail ) throws Exception{
        
        
        final HttpRequestFactory requestFactory = getRequestFactory(activeUserEmail+fitbitId); 
       
        
        if (requestFactory==null){
            throw new Exception("Could not create request factory!");
        }
    
        ArrayList<DayResponse> allDaysData = new ArrayList<>();
        Gson gson = new Gson();
        
        try{ 
          
            for (String dateString : selDates) {


                GenericUrl requestUrl = new GenericUrl("https://api.fitbit.com/1/user/"+fitbitId+"/" + "activities/steps/date/" + dateString+ "/" + dateString + ".json");

                HttpRequest request = requestFactory.buildGetRequest(requestUrl);
                
                System.out.println("execute started: "+System.currentTimeMillis() );
                HttpResponse response = request.execute();
                System.out.println("execute ended: "+System.currentTimeMillis() );

                if (response.isSuccessStatusCode()) {

                    String responseAsString = response.parseAsString();
                    DayResponse nextDayResult = gson.fromJson(responseAsString, DayResponse.class);
                    List<MinuteData> dayDataset = nextDayResult.getActivities_steps_intraday().getDataset();


                    if (nextDayResult.getActivities_steps().get(0).getValue()!=0){
                        nextDayResult.setHasMinuteData(false);
                        
                        for (int i=0;i<  dayDataset.size();i++){
                            if (dayDataset.get(i).getValue()>0){//can be dates not synced for >7 days, steps>0, but no data. skip such
                                nextDayResult.setHasMinuteData(true);
                                break;
                            }
                        }
                        allDaysData.add(nextDayResult);
                    }


                } else {
                    System.out.println("Issue with the server call: " + response.getStatusMessage());
                }
            } 
            
        }
        catch(JsonSyntaxException jse){
            System.out.println("Poles changed places -- fitbit changed API. Json syntax exception");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            //HTTPResponseException, IOException,...
           
        }
        
        return allDaysData;
          
    }
    
    
    public static ArrayList<DayResponse> retrieveFromFitbitInterday(String[] selDates, String fitbitId, String activeUserEmail ) throws Exception{
        
        final HttpRequestFactory requestFactory = getRequestFactory(activeUserEmail+fitbitId); 
        
        if (requestFactory==null){
            throw new Exception("Could not create request factory!");
        }
    
        ArrayList<DayResponse> allDaysData = new ArrayList<>();
        Gson gson = new Gson();
        
        try{
    
            String start = selDates[0];
            String end = selDates[selDates.length-1];

            GenericUrl requestUrl = new GenericUrl("https://api.fitbit.com/1/user/"+fitbitId+"/" + "activities/steps/date/" + start+ "/" + end + ".json");

            HttpRequest request = requestFactory.buildGetRequest(requestUrl);
            HttpResponse response = request.execute();

            if (response.isSuccessStatusCode()) {

                //{"activities-steps":[{"dateTime":"2016-06-09","value":"12968"},{"dateTime":"2016-06-10","value":"1325"},...]}
                String responseAsString = response.parseAsString();
                DayResponse onlyInterdayResult = gson.fromJson(responseAsString, DayResponse.class);


                //output 0 step days (for continuity), but should be at least 1 day with steps >0

                for (DaySummary ds : onlyInterdayResult.getActivities_steps()) {
                    if (ds.getValue()!=0){
                        allDaysData.add(onlyInterdayResult);
                        break;
                    }
                }
            }
            else {
                    System.out.println("Issue with the server call: " + response.getStatusMessage());
            }
        
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        return allDaysData;
    }
    
    
     //credential -- http://grepcode.com/file/repo1.maven.org/maven2/com.google.oauth-client/google-oauth-client/1.7.0-beta/com/google/api/client/auth/oauth2/Credential.java#Credential.refreshToken%28%29
    //authorizationCodeFlow -- http://grepcode.com/file/repo1.maven.org/maven2/com.google.oauth-client/google-oauth-client/1.7.0-beta/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.java#AuthorizationCodeFlow.createAndStoreCredential%28com.google.api.client.auth.oauth2.TokenResponse%2Cjava.lang.String%29
    public static HttpRequestFactory getRequestFactory(String credKeyInStore) {
        
     AuthorizationCodeFlow flow;
        
        try{
            flow = initializeFlow();
            final Credential credential = flow.loadCredential(credKeyInStore);
            if (credential != null && credential.getAccessToken() != null) {
                
                
                System.out.println("token expires in: "+credential.getExpiresInSeconds());
                if(credential.getExpiresInSeconds()<0){
                    if (!credential.refreshToken()){
                        
                        //token couldn't refresh
                       System.out.println("-- couldn't refresh token!");
                       return null;
                    }
                    else{
                         System.out.println("-- refreshed well");
                    }
                }

                HttpRequestFactory requestFactory = new NetHttpTransport()
                  .createRequestFactory(new HttpRequestInitializer() {
                      @Override
                      public void initialize(HttpRequest request) {

                          request.getHeaders().setAccept("application/json");
                          request.getHeaders().setAuthorization("Bearer " + credential.getAccessToken());

                      }
                  });
                
                return requestFactory;
                
                
            }
            else{
                
                System.out.println("Patient not found in store!");
                return null;
                
            }   
        }
        catch(IOException ioe){
            System.out.println(ioe.getMessage());
            return null;
        }
        
    }
    
   protected static AuthorizationCodeFlow initializeFlow() throws IOException {

 
    
    URL resourceUrl = context.getResource("/WEB-INF/tokens");
    URI uri;
    try{
        uri = resourceUrl.toURI();
    }
    catch(URISyntaxException  use){
        System.out.println("shouldn't happen");
        return null;
    }
       
    AuthorizationCodeFlow.Builder acfb = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    new GenericUrl("https://api.fitbit.com/oauth2/token"),
                    new BasicAuthentication("227T4W", "54b87a495109c3c10c06bf56754d6cc3"),
                    "227T4W",
                    "https://www.fitbit.com/oauth2/authorize");
    
    acfb.setScopes(Arrays.asList("activity","settings"));
    
    FileDataStoreFactory ff = new FileDataStoreFactory(new File(uri));
   
    acfb.setCredentialDataStore(StoredCredential.getDefaultDataStore(ff));
    return acfb.build();
    
  }
    
    
}

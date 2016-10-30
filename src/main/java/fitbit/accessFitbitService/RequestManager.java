/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.accessFitbitService;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;


import java.io.IOException;

/**
 *
 * @author Vlad
 */
public class RequestManager {
    
    //do fitbit requests here?
    public static HttpResponse executeGet(HttpTransport transport, JsonFactory jsonFactory, String accessToken, GenericUrl url) throws IOException {
        Credential credential =  new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
        HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
        return requestFactory.buildGetRequest(url).execute();
    }
    
    
    public static void retrieveFromFitbit(String[] selDates,boolean intraday, String fitbitId ){
        if (intraday== true){                          
           for (int k=0;k<selDates.length;k++){
                String dateString = selDates[k];
    
                 
                  
              
               nextResult = UrlFetchApp.fetch("https://api.fitbit.com/1/user/"+userID+"/" + "activities/steps/date/" + dateString+ "/" + dateString + ".json", options);   
              
               o = JSON.parse(nextResult.getContentText());
               
               
             //  Logger.log(JSON.stringify(o));
            
               
               if (o["activities-steps"][0]["value"]!=="0"){
               
                 for (var i=0;i< o["activities-steps-intraday"]["dataset"].length;i++){
                     
                     if (o["activities-steps-intraday"]["dataset"][i]["value"]>0){//can be dates not synced for >7 days, steps>0, but no data
                         allDaysData.push(o);
                         break;
                     }
                 
                 
                 }
                 
                 
               
               }

               
                
    
           }
           
         
         
           
      }
      else if (intraday==false){                    //INTERDAY
        
//        var start = selDates[0];
//        var end = selDates[selDates.length-1];
//    
//         
//              nextResult = UrlFetchApp.fetch("https://api.fitbit.com/1/user/"+userID+"/" + "activities/steps/date/" + start+ "/" + end + ".json", options);     
//              o = JSON.parse(nextResult.getContentText());
//    
//    
//    //{"activities-steps":[{"dateTime":"2016-06-09","value":"12968"},{"dateTime":"2016-06-10","value":"1325"},{"dateTime":"2016-06-11","value":"4497"}]}
//    // {"activities-steps":[{"dateTime":"2016-06-01","value":"0"}],
//    
//    
//    
//              allDaysData.push(o);
//              var nonzeroDay = false;//output 0 step days (for continuity), but should be at least 1 day with steps >0
//              for (var i=0;i<o["activities-steps"].length;i++){
//                  if (o["activities-steps"][i]["value"]!=="0"){
//                     nonzeroDay = true;
//                  
//                  }
//              
//              }
//    
//              if (!nonzeroDay){
//                  allDaysData=[];
//              }
//      

    
      }
        
        
        
    }
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.stores;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.Key;

/**
 *
 * @author Vlad
 */
public class FitbitTokenResponse extends TokenResponse{
    
  
    @Key("user_id")
    private String user_id;
    



    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

  
    
    
    
    
    
}

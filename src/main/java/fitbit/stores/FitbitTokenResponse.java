/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.stores;

import com.google.api.client.util.Key;

/**
 *
 * @author Vlad
 */
public class FitbitTokenResponse {
    
    @Key("access_token")
    private String access_token;
    
    @Key("expires_in")
    private Long expires_in;
    
    @Key("refresh_token")
    private String refresh_token;
    
    @Key("token_type")
    private String token_type;
    
    @Key("user_id")
    private String user_id;
    
    @Key("scope")
    private String scope;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    
    
    
    
    
}

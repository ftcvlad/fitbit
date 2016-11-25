/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FitbitJsonBeans;


import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *
 * @author Vlad
 */
public class DayResponse {
    
    
    @SerializedName("activities-steps")
    private List<DaySummary> activities_steps;
    
    @SerializedName("activities-steps-intraday")
    private IntradayData  activities_steps_intraday;

    
    
    
    public List<DaySummary> getActivities_steps() {
        return activities_steps;
    }

    public void setActivities_log_steps(List<DaySummary> activities_steps) {
        this.activities_steps = activities_steps;
    }

    
    
    
    public IntradayData getActivities_steps_intraday() {
        return activities_steps_intraday;
    }

    public void setActivities_steps_intraday(IntradayData activities_steps_intraday) {
        this.activities_steps_intraday = activities_steps_intraday;
    }
    
   
    
}

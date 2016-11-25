/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FitbitJsonBeans;

import java.util.List;

/**
 *
 * @author Vlad
 */
public class IntradayData {
    
    
    private int datasetInterval;
    private List<MinuteData> dataset;

    public int getDatasetInterval() {
        return datasetInterval;
    }

    public void setDatasetInterval(int datasetInterval) {
        this.datasetInterval = datasetInterval;
    }

    public List<MinuteData> getDataset() {
        return dataset;
    }

    public void setDataset(List<MinuteData> dataset) {
        this.dataset = dataset;
    }
    
    
    
    
    
}

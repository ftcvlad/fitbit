/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.stores;
import java.util.ArrayList;

/**
 *
 * @author Vlad
 */
public class Patient {
    
    ArrayList<String> fullDates = new ArrayList<>();
    ArrayList<String> partDates = new ArrayList<>();
    ArrayList<String> nosyncDates = new ArrayList<>();
    ArrayList<String> nodataDates = new ArrayList<>();
    String name;
    String surname;
    String birthDate;
    String fitbitId;

  
    
    
    public Patient(){
        
    }

    public String getFitbitId() {
        return fitbitId;
    }

    public void setFitbitId(String fitbitId) {
        this.fitbitId = fitbitId;
    }
    

   
    public void addDate(String date, String filling){
        if (filling.equals("full")){
            fullDates.add(date);
        }
        else if (filling.equals("part")){
            partDates.add(date);
        }
        else if (filling.equals("noSync")){
            nosyncDates.add(date);
        }
        else if (filling.equals("noData")){
            nodataDates.add(date);
        }
        
    }
    
    

    
    public void setName(String name){
        this.name = name;
    }
    
    public void setSurname(String surname){
        this.surname = surname;
    }
    
    public String getName(){
        return this.name ;
    }
    
    public String getSurname(){
        return this.surname ;
    }
    
    
    public ArrayList<String> getFullDates(){
        return this.fullDates ;
    }

    
    public ArrayList<String> getPartDates(){
        return this.partDates ;
    }
    
    
     public ArrayList<String> getNosyncDates() {
        return nosyncDates;
    }

    public void setNosyncDates(ArrayList<String> nosyncDates) {
        this.nosyncDates = nosyncDates;
    }

    public ArrayList<String> getNodataDates() {
        return nodataDates;
    }

    public void setNodataDates(ArrayList<String> nodataDates) {
        this.nodataDates = nodataDates;
    }
    
    
    public void setBirthDate(String birthDate){
        this.birthDate = birthDate;
    }
    
    public String getBirthDate(){
        return this.birthDate;
    }
    
    public void setFullDates(){
        this.fullDates = new ArrayList<>(); 
    }
    public void setPartDates(){
        this.partDates = new ArrayList<>(); 
    }
    
    
    
    @Override
    public boolean equals(Object c) {
        if (!(c instanceof Patient)) {
            return false;
        }

        Patient that = (Patient)c;
        return this.fitbitId.equals(that.fitbitId);
    }
    
}

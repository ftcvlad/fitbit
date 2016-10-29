package fitbit.models;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import fitbit.stores.Patient;
import java.sql.Connection;


import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

import java.util.ArrayList;
/**
 *
 * @author Administrator
 */
public class User {
   
    String username;
    public ArrayList<Patient> allPatients;
    public User(){
        
    }
    
    public void setAllPatients(ArrayList<Patient> pat){
        allPatients = pat;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getUsername(){
        return username;
    }

    public boolean isValidUser(String username, String password, Connection conn) throws SQLException {

       
        PreparedStatement stmt;
        
        stmt = conn.prepareStatement("SELECT password from fitbit_allclinicians where username =?");
        stmt.setString(1,username);
        ResultSet rs = stmt.executeQuery();


        if (rs.isBeforeFirst()){
             while(rs.next()){
                String storedPass = rs.getString("password");
                if (storedPass.compareTo(password) == 0){
                    return true;
                }
            }
            return false;
        }
        return false;
    
    
    }
    
    
    public void registerUser(String username, String password, Connection conn) throws SQLException, SQLIntegrityConstraintViolationException {
       
        PreparedStatement stmt;

        stmt = conn.prepareStatement("INSERT INTO fitbit_allclinicians (username, password) VALUES (?,?)");
        stmt.setString(1,username );
        stmt.setString(2,password );
        stmt.executeUpdate();
    }

    public void removePatientById(String fitbitId){
        for (int i=0;i<allPatients.size();i++){
            if (allPatients.get(i).getFitbitId().equals(fitbitId)){
                    allPatients.remove(i);
                    break;
            }
        }   
    }
    
    public void removePatientById(String[] ids) throws NumberFormatException{
        
        
        for (String nextId: ids){
          
            for (int i = allPatients.size()-1; i >= 0; i--){
                    if (allPatients.get(i).getFitbitId().equals(nextId) ){
                        allPatients.remove(i);
                        break;
                    }
            }
        }
    }
    
    
     public ArrayList<Patient> addNonRepeatingPatients(ArrayList<Patient> selectedPatients){
         
         ArrayList<Patient> addedPatients = new ArrayList<>();
         for (Patient nextPatient: selectedPatients){
               if (!allPatients.contains(nextPatient)){
                   allPatients.add(nextPatient);
                   addedPatients.add(nextPatient);
               }
         }
       
         return addedPatients;
         
     }
    
    
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.models;

import fitbit.stores.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
/**
 *
 * @author Vlad
 */
public class PatientManager {
    
    
    public Patient savePatient(String activeUserEmail,Patient patient, Connection conn) throws SQLException{
      
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO fitbit_patients( Clinician, userID, birthDate, name, surname,shortlisted)"
                + " VALUES (?,?,?,?,?,1)"
                + " ON DUPLICATE KEY UPDATE Clinician = Clinician, userID= userID,birthDate=VALUES(birthDate),name=VALUES(name),surname=VALUES(surname), shortlisted=VALUES(shortlisted);");

        stmt.setString(1, activeUserEmail);
        stmt.setString(2, patient.getFitbitId());
        stmt.setString(3, patient.getBirthDate());
        stmt.setString(4, patient.getName());
        stmt.setString(5, patient.getSurname());

        int rowCount = stmt.executeUpdate();//1 for insert, 2 for update

        
        System.out.println("===============->" + rowCount);
        //since added patient could have existed before (update), need to take his dates as well
        if (rowCount==2){
            PreparedStatement stmt2 = conn.prepareStatement("SELECT fitbit_dates.Date,fitbit_dates.filling,fitbit_patients.PCpair_id, fitbit_patients.name, fitbit_patients.surname, fitbit_patients.birthDate, fitbit_patients.userID"
                    + " FROM fitbit_dates"
                    + " RIGHT JOIN fitbit_patients ON (fitbit_dates.PCpair_id=fitbit_patients.PCpair_id)"
                    + " WHERE fitbit_patients.Clinician=? AND fitbit_patients.userID=?"
                    + " ORDER BY 3 ;", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2.setString(1, activeUserEmail);
            stmt2.setString(2, patient.getFitbitId());

            return createPatientList(stmt2).get(0);//returned just 1 currently added patient with full data
        }
        else{
            return patient;
        }
        
    }
    
    
    
    
    
    
    
     public ArrayList<Patient> getShortlistedPatientsAndDates(String username, Connection conn) throws SQLException{
        
 
        PreparedStatement stmt = conn.prepareStatement("SELECT fitbit_dates.Date,fitbit_dates.filling,fitbit_patients.PCpair_id, fitbit_patients.name, fitbit_patients.surname, fitbit_patients.birthDate, fitbit_patients.userID"+
                                            " FROM fitbit_dates"+
                                            " RIGHT JOIN fitbit_patients ON (fitbit_dates.PCpair_id=fitbit_patients.PCpair_id)"+
                                            " WHERE fitbit_patients.Clinician=? AND fitbit_patients.shortlisted=1"+
                                            " ORDER BY 3 ;",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        stmt.setString(1,username);
       
        return  createPatientList(stmt);
    }
    
    

     public ArrayList<Patient> findPatientsAndDates(String nameToFind, String activeUserEmail, Connection conn) throws SQLException{
         
            String nameCondition="";
            if (!nameToFind.equals("")){
                nameCondition = "AND patients.name=?";
            }

            PreparedStatement stmt = conn.prepareStatement("SELECT fitbit_dates.Date,fitbit_dates.filling,fitbit_patients.PCpair_id, fitbit_patients.name, fitbit_patients.surname, fitbit_patients.birthDate, fitbit_patients.userID"+
                                              " FROM fitbit_dates"+
                                              " RIGHT JOIN fitbit_patients ON (fitbit_dates.PCpair_id=fitbit_patients.PCpair_id)"+
                                              " WHERE fitbit_patients.Clinician=? "+nameCondition+
                                              " ORDER BY 3 ;",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

            stmt.setString(1,activeUserEmail);
            if (!nameToFind.equals("")){
                stmt.setString(2, nameToFind);
            }

            return createPatientList(stmt);
     }
     
     
     
     private ArrayList<Patient> createPatientList(PreparedStatement stmt) throws SQLException{
        ResultSet rs = stmt.executeQuery();
      
        int previousUserId =0;  
        Patient nextPatient = new Patient();  
        ArrayList<Patient> myPatients = new  ArrayList<>();
        while(rs.next()){
             
                String date = rs.getString(1);
                String filling = rs.getString(2);
                int PCpair_id = rs.getInt(3);
               
                if (rs.isFirst()){
                   previousUserId = PCpair_id;
                }

                if (PCpair_id !=  previousUserId  ){//1ST PUT PATIENT CASE

                   
                    rs.previous();
                    nextPatient.setName(rs.getString(4));
                    nextPatient.setSurname(rs.getString(5));
                    nextPatient.setBirthDate(rs.getString(6));
                    nextPatient.setFitbitId(rs.getString(7));
                    rs.next();

                    myPatients.add(nextPatient);

                    nextPatient = new Patient();
                    previousUserId = PCpair_id;

                }

                if (filling!=null){//for NULL, NULL, userID, name, surname rows -- no dates in dates table
                    nextPatient.addDate(date,filling);
                }

                if (rs.isLast()){//2nd PUT PATIENT CASE
                   
                   nextPatient.setName(rs.getString(4));
                   nextPatient.setSurname(rs.getString(5));
                   nextPatient.setBirthDate(rs.getString(6));
                   nextPatient.setFitbitId(rs.getString(7));
                   myPatients.add(nextPatient);
                }
              

        }
        return myPatients;
     }
     
    public void deletePatients(String activeUserEmail, Connection conn, String[] idsToDelete)throws SQLException{

//            //CHECK IF PATIENTS ARE THERE (in case evil changes fitbitIds, and these would be deleted from credentialStore)
//            String whereStr="WHERE Clinician =? AND (";
//            for (String id : idsToDelete){
//                whereStr+="userID=?||";
//            }
//            whereStr = whereStr.substring(0,whereStr.length()-2);
//
//            PreparedStatement stmt = conn.prepareStatement("SELECT userID FROM fitbit_patients "+whereStr+");");
//
//            stmt.setString(1,activeUserEmail);
//            for (int i=0;i<idsToDelete.length;i++){
//                stmt.setString(i+2, idsToDelete[i]);
//            }
//
//            ResultSet rs = stmt.executeQuery();
//            ArrayList<String> patientsThatCanBeDeleted = new ArrayList<>();
//            while(rs.next()){
//                patientsThatCanBeDeleted.add(rs.getString(1)); 
//            }
            
            //DELETE PATIENTS
            String whereStr="WHERE Clinician =? AND (";
            for (String id : idsToDelete){
                whereStr+="userID=?||";
            }
            whereStr = whereStr.substring(0,whereStr.length()-2);

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM fitbit_patients "+whereStr+");");

            stmt.setString(1,activeUserEmail);
            for (int i=0;i<idsToDelete.length;i++){
                stmt.setString(i+2, idsToDelete[i]);
            }
            stmt.execute();
            
            
  
   
    }
    
    public int delistPatient(String activeUserEmail, String fitbitidToDelist,Connection conn ) throws SQLException{
        
        
        PreparedStatement stmt = conn.prepareStatement("UPDATE fitbit_patients "+
                " SET shortlisted=0"+
                " WHERE Clinician=? AND userID=?");
                
        stmt.setString(1,activeUserEmail);    
        stmt.setString(2,fitbitidToDelist);
        
        return stmt.executeUpdate();
    }
    
    
    public int enlistPatients(String activeUserEmail, ArrayList<Patient> addedPatients,Connection conn ) throws SQLException{
        
        if (addedPatients.size()>0){
                String inString="";
                for (Patient p:addedPatients){
                    inString+="?,";
                }
                inString = inString.substring(0,inString.length()-1);

                PreparedStatement stmt = conn.prepareStatement("UPDATE fitbit_patients "+
                        " SET shortlisted=1"+
                        " WHERE Clinician=? AND ((userID) IN ("+inString+"))");

                stmt.setString(1,activeUserEmail);    
                for (int i=0;i<addedPatients.size();i++){
                    stmt.setString(i+2,addedPatients.get(i).getFitbitId());   
                }

                return stmt.executeUpdate();
        }
        return 0;
        
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.models;


import FitbitJsonBeans.DayResponse;
import FitbitJsonBeans.MinuteData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;

/**
 *
 * @author Vlad
 */
public class DateManager {
    
    
    public static int removeAlreadySavedDates(String activeUserEmail, String fitbitId, Connection conn, ArrayList<String> selDates) throws SQLException, Exception{
        //AUTHENTICATION  
            int pcpair_id;
            try (PreparedStatement authStmt = conn.prepareStatement( "SELECT PCpair_id "+
                    "FROM fitbit_patients "+
                    "WHERE Clinician=? AND userID=?;")) {
                authStmt.setString(1, activeUserEmail);
                authStmt.setString(2, fitbitId);
                ResultSet authRs = authStmt.executeQuery();


                if (authRs.next()!=false){//no user-clinician (again, user modified some javascript)
                    pcpair_id = authRs.getInt(1);
                }
                else{
                    throw new Exception("Bad input: Current clinician cannot save for selected patient");    
                }
            }
        
            
        //LEAVE ONLY DATES THAT ARE NOT IN DATABASE or ARE IN DB, BUT PART or NOT SYNCED
      
            String whereStr="where PCpair_id="+pcpair_id +" AND ( ";
            for ( int i=0; i<selDates.size();i++){
                 whereStr+= "Date=?||";
            }
            whereStr = whereStr.substring(0,whereStr.length()-2);//get rid of last ||
           
           
            PreparedStatement test0Stmt = conn.prepareStatement("select Date, filling from fitbit_dates "+whereStr+");");

            for ( int i=0; i<selDates.size();i++){
                test0Stmt.setString(i+1, selDates.get(i));
            }

            ResultSet test0Rs = test0Stmt.executeQuery();


            while(test0Rs.next()){
                String dateFromDB = test0Rs.getString(1);

                int index = selDates.indexOf(dateFromDB);
                String filling = test0Rs.getString(2);
                if (index!=-1 && (filling.equals("full") || filling.equals("noData") || filling.equals("lost"))){
                    
                    selDates.remove(index);
                }
            }
            
            return pcpair_id;
        
    }
    
    
    public static HashMap<String,ArrayList<String>>  saveDates(String lastSyncDate, String activeUserEmail, Connection conn, int pcpair_id, ArrayList<DayResponse>  fitbitData, ArrayList<String> desiredDates) throws SQLException, Exception{
        
//            if (selectedData.length!=1441){
//                     throw new Exception("Bad input: != 1441");    
//            }


            ArrayList<String> fullDatesAdded = new ArrayList<>();
            ArrayList<String> partDatesAdded = new ArrayList<>();
            ArrayList<String> noSyncDatesAdded = new ArrayList<>();
            ArrayList<String> noDataDatesAdded = new ArrayList<>();
            ArrayList<String> lostDatesAdded = new ArrayList<>();

            
    //INSERT dates
            StringBuilder datesStringBuilder= new StringBuilder("Values ");

            ArrayList<String> addedDatesForInjection = new ArrayList<>();
            //steps >0
            for (int i=0;i<fitbitData.size();i++){
                String date = fitbitData.get(i).getActivities_steps().get(0).getDateTime();
                int  valueTot = fitbitData.get(i).getActivities_steps().get(0).getValue();

                if (date.compareTo(lastSyncDate)<0){
                    
                    if (fitbitData.get(i).isHasMinuteData()){
                        datesStringBuilder.append("(?,\"full\",").append(valueTot).append(",").append(pcpair_id).append("),");
                        fullDatesAdded.add(date);
                    }
                    else{//steps>0, but no minuteData => not synced for >7 days
                        datesStringBuilder.append("(?,\"lost\",").append(valueTot).append(",").append(pcpair_id).append("),");
                        lostDatesAdded.add(date);
                    }
                }
                else if (date.compareTo(lastSyncDate)==0){
                    datesStringBuilder.append("(?,\"part\",").append(valueTot).append(",").append(pcpair_id).append("),");
                    partDatesAdded.add(date);
                }
                
                addedDatesForInjection.add(date);
                desiredDates.remove(desiredDates.indexOf(date));
            }

            ArrayList<String> allPositiveStepsDates = new ArrayList<>(addedDatesForInjection);
            
            //steps ==0 
            if (desiredDates.size()>0){

                //store totalSteps ===0 dates (no data dates)
                for (int i=0;i<desiredDates.size();i++){
                      if (desiredDates.get(i).compareTo(lastSyncDate)<0){
                            datesStringBuilder.append("(?,\"noData\",").append(0).append(",").append(pcpair_id).append("),");
                            noDataDatesAdded.add(desiredDates.get(i));
                      }
                      else if (desiredDates.get(i).compareTo(lastSyncDate)==0){
                            datesStringBuilder.append("(?,\"part\",").append(0).append(",").append(pcpair_id).append("),");
                            partDatesAdded.add(desiredDates.get(i));
                      }
                      else if (desiredDates.get(i).compareTo(lastSyncDate)>0){
                            datesStringBuilder.append("(?,\"noSync\",").append(0).append(",").append(pcpair_id).append("),");
                            noSyncDatesAdded.add(desiredDates.get(i)); 
                      }
                      addedDatesForInjection.add(desiredDates.get(i));
                }

            }
            

            String datesString = datesStringBuilder.substring(0,datesStringBuilder.length()-1);


            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO fitbit_dates(Date,filling,totalSteps,PCpair_id) "+datesString+
                    " ON DUPLICATE KEY UPDATE Date = Date, filling= VALUES(filling), totalSteps=VALUES(totalSteps), PCpair_id =  PCpair_id;")) {
                for (int i=0;i<addedDatesForInjection.size();i++){
                    stmt.setString(i+1,addedDatesForInjection.get(i));
                }
                stmt.executeUpdate();
            }

            //remove no-minute-data dates before inserting steps
            for (int j = fitbitData.size()-1; j >= 0; j--) {
                    if (!fitbitData.get(j).isHasMinuteData()){
                        fitbitData.remove(j);
                    }
            }
            

            if (fitbitData.size()>0){
    //SELECT JUST INSERTED DATES
                String whereStr="WHERE PCpair_id="+pcpair_id +" AND ( ";
                
                
                
                for (int i=0; i<fitbitData.size();i++){
                  whereStr+= "Date=?||";
                }
                whereStr = whereStr.substring(0,whereStr.length()-2);//get rid of last ||
              
                
                PreparedStatement stmt2 = conn.prepareStatement("SELECT Date, date_id from fitbit_dates "+whereStr+");");
                
               
                for (int i=0;i<fitbitData.size();i++){
                    stmt2.setString(i+1, fitbitData.get(i).getActivities_steps().get(0).getDateTime());
                }

                ResultSet stmt2Rs = stmt2.executeQuery();
                
    //INSERT VALUES FOR EACH DATE (in arrToSave all have steps >0).            
                
                
                        
                int indexInFitbitData;
               
                while(stmt2Rs.next()){
                
                
                    indexInFitbitData = allPositiveStepsDates.indexOf(stmt2Rs.getString(1));
                    int date_id = stmt2Rs.getInt(2);
                 
                    
                    List<MinuteData> minuteDataPerDay =  fitbitData.get(indexInFitbitData).getActivities_steps_intraday().getDataset();
                    
                    
                    StringBuilder sb= new StringBuilder("VALUES ");
                    int minuteSteps ;
                    for ( int j=0;j<minuteDataPerDay.size();j++){
                        minuteSteps = minuteDataPerDay.get(j).getValue();
                        if (minuteSteps>0){
                            sb.append("(").append(j+1).append(",").append(date_id).append(",").append(minuteSteps).append("),");
                        }
                        
                    }
                    String valuesString = sb.substring(0,sb.length()-1);//get rid of last comma

                    //if steps taken in the middle of minute, they may increase for the minute, so, would update
                    stmt2 = conn.prepareStatement("INSERT INTO fitbit_stepsintime(Time,date_id,value) "+valuesString+
                                                    " ON DUPLICATE KEY UPDATE value=VALUES(value);");

                    stmt2.execute();
        
                }
    
            }
    

            HashMap<String,ArrayList<String>> datesAddedMap = new HashMap<>();
            datesAddedMap.put("full", fullDatesAdded);
            datesAddedMap.put("lost", lostDatesAdded);
            datesAddedMap.put("part", partDatesAdded);
            datesAddedMap.put("nodata", noDataDatesAdded);
            datesAddedMap.put("nosync", noSyncDatesAdded);
            
            return datesAddedMap;

    }
    
    
     private String incrementDate(String from_date) throws java.text.ParseException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(from_date));
        c.add(Calendar.DATE, 1);  // number of days to add
        from_date = sdf.format(c.getTime());  // dt is now the new date

        return from_date;

    }
    
    
    
    public String[][] getDates(String[] datesToGet,String activeUserEmail, Connection conn, String userId, boolean intraday) throws SQLException, Exception{
        
        
       
        int pcpair_id;
        try (PreparedStatement authStmt = conn.prepareStatement( "SELECT PCpair_id "+
                  "FROM fitbit_patients "+
                  "WHERE Clinician=? AND UserId=?;")) {
              authStmt.setString(1, activeUserEmail);
              authStmt.setString(2, userId);
              ResultSet authRs = authStmt.executeQuery();

              if (authRs.next()==false){//no user-clinician (again, user modified some javascript)
                  throw new Exception("Bad input: Current clinician cannot get selected patient's data");    
              }
              else{
                  pcpair_id = authRs.getInt(1);
              }
        }
     

        //GET SELECTED DATES THAT ARE IN DATABASE. same for interday and intraday
        String whereStr;
        if (intraday==true){
            whereStr ="WHERE (filling='full' || (filling='part' AND totalSteps>0)) AND PCpair_id="+pcpair_id+" AND (";
        }
        else{
            whereStr="WHERE totalSteps>0 AND PCpair_id="+pcpair_id+" AND (";
        }
        
        for (String s : datesToGet) {
            whereStr+= "Date=?||";
        }
        
        whereStr = whereStr.substring(0,whereStr.length()-2);//get rid of last ||
        whereStr+=")";
        
     

        PreparedStatement stmt = conn.prepareStatement("SELECT Date, date_id, totalSteps "+
                                      "FROM fitbit_dates "+ whereStr+" ORDER BY Date ASC;");

        for (int i=0; i<datesToGet.length;i++){
            stmt.setString(i+1,datesToGet[i]);
        }


        ResultSet rsDates = stmt.executeQuery();   
     
    
        int rowcountTable;
        if (rsDates.last()) {
            rowcountTable = rsDates.getRow();
            rsDates.beforeFirst(); 
        }
        else {
            return new String[0][0];
        }
        
        
        
        if (intraday == true){
                String[][] table= new String[1441][rowcountTable];
                PreparedStatement stmt2;


                //FOR EACH DATE/ROW SELECT VALUES
                int colIndex=0;
                while (rsDates.next()){

                              String date = rsDates.getString(1);
                              int date_id = rsDates.getInt(2);

                             
                              table[0][colIndex] = date;
                              stmt2 = conn.prepareStatement("SELECT value, Time "+
                                                            "FROM fitbit_stepsintime "+
                                                            "WHERE date_id="+date_id+";");

                              ResultSet rsValues = stmt2.executeQuery();

                              String val;
                              
                              //fill all with 0s
                              for (int j=1;j<=1440;j++){
                                  table[j][colIndex]="0";
                              }
                              
                              //replace for times that have steps>0 
                              while(rsValues.next()){
                                    val = ""+rsValues.getInt(1);
                                    table[rsValues.getInt(2)][colIndex] = val;
                              }
                              
                              stmt2.close();
                              colIndex++;
                }
                
                
                
                return table;

        }
        else{//interday
                String[][] table= new String[rowcountTable][2];

                int rowIndex = 0;
                while (rsDates.next()){
                     table[rowIndex][0] = rsDates.getString(1);
                     table[rowIndex][1] = ""+rsDates.getInt(3);
                     rowIndex++;
                }

               //may be 0step dates, and to do interday, should be no gaps


                String firstDate=datesToGet[0];
                String lastDate = datesToGet[datesToGet.length-1];
          //count size of arrWithZeros
                int rowCountWithZeros=1;
                String currentDate = firstDate;
                while( currentDate.compareTo(lastDate)<=0){
                    currentDate = incrementDate(currentDate);
                    rowCountWithZeros++;
                }

                String[][] arrWithZeros = new String[rowCountWithZeros][2];
                arrWithZeros[0][0] = "Date";
                arrWithZeros[0][1] = "Steps summary";


                currentDate = firstDate;
                int ind=0;//which item from table are we looking for
                rowIndex = 1;
                while(currentDate.compareTo(lastDate)<=0){

                    if ( ind<rowcountTable && table[ind][0].equals(currentDate)){
                        arrWithZeros[rowIndex][0] = table[ind][0];
                        arrWithZeros[rowIndex][1] = table[ind][1];
                        ind++;
                    }
                    else {
                         arrWithZeros[rowIndex][0] = currentDate;
                         arrWithZeros[rowIndex][1] = "0";
                    }
                    currentDate = incrementDate(currentDate);
                    rowIndex++;
                }

                return arrWithZeros;

        }
        
        
        


    }
    
 
   
    
    
    
}

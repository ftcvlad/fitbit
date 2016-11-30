/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import FitbitJsonBeans.DayResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fitbit.accessFitbitService.FitbitRequestManager;
import fitbit.models.DateManager;
import fitbit.models.User;
import fitbit.stores.Patient;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author Vlad
 */
@WebServlet(name = "SaveFromFitbit", urlPatterns = {"/SaveFromFitbit"})
public class SaveFromFitbit extends HttpServlet {

    
    
    private DataSource dataSource;
    @Override
    public void init() throws ServletException {
		try {
                        dataSource = (DataSource) new InitialContext().lookup("java:comp/env/" + "jdbc/db");
		} catch (NamingException e) {
		}
    }
    
   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        
        
        
        Gson gson = new Gson();
        ArrayList<String> selDates = gson.fromJson(request.getParameter("selDates"), new TypeToken<ArrayList<String>>(){}.getType());
        
       
        
        String fitbitId = request.getParameter("fitbitId");
        
        String currentFormattedDate= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (selDates.get(selDates.size()-1).compareTo(currentFormattedDate)>0){
            response.setContentType("text/plain");
            response.setStatus(400);
            response.getWriter().write("You can't select future dates");
            return;
        }

        HttpSession session = request.getSession(false);
        User us = (User) session.getAttribute("user");
        String activeUserEmail = us.getUsername();
        Connection conn = null;
        
        try{
            conn= dataSource.getConnection();
            
            //remove dates that are already in db
            int pcpair_id = DateManager.removeAlreadySavedDates(activeUserEmail,fitbitId, conn,selDates);
            if (selDates.isEmpty()){
                response.setContentType("text/plain");
                response.setStatus(400);
                response.getWriter().write("All of the selected dates are already saved to database!");
                return;
            }
            
            //retrieve data from Fitbit
            ArrayList<DayResponse> dataToSave = FitbitRequestManager.retrieveFromFitbitIntraday(selDates.toArray(new String[selDates.size()]),fitbitId,activeUserEmail);
            
            //get date of the last sync
            String lastSyncDate = FitbitRequestManager.getLastSyncDate(activeUserEmail, fitbitId);
            
            //save to db
            HashMap<String,ArrayList<String>> hm = DateManager.saveDates(lastSyncDate,activeUserEmail, conn, pcpair_id, dataToSave, selDates);
            
             //CREATE PROPER PATIENT WITH PROPER SESSION :(
             //UPDATE SESSION :(
             //RETURN CHANGEDFILLINGS TO SPECIAL METHOD IN JS
             
             //... AND DON'T FORGET ABOUT COMPARISON
              
            
            Patient targetPatient = null;
            for (int i=0; i<us.allPatients.size();i++){
                if (us.allPatients.get(i).getFitbitId().equals(fitbitId)){
                    targetPatient = us.allPatients.get(i);
                    
                    //UPDATED PART DATES
                    ArrayList<String> currentPartDates = targetPatient.getPartDates();
                    ArrayList<String> addedPartDates = hm.get("part");
                    for (String str : addedPartDates){
                        if (!currentPartDates.contains(str)){
                           currentPartDates.add(str);
                        }
                    }

                    //UPDATED FULL DATES
                    ArrayList<String> currentFullDates =targetPatient.getFullDates();
                    ArrayList<String> addedFullDates = hm.get("full");
                    for (String str : addedFullDates){
                        if (!currentFullDates.contains(str)){
                           currentFullDates.add(str);
                        }
                    }
                    
                    //UPDATED NOSYNC DATES
                    ArrayList<String> currentNoSyncDates = targetPatient.getNosyncDates();
                    ArrayList<String> addedNoSyncDates = hm.get("nosync");
                    for (String str : addedNoSyncDates){
                        if (!currentNoSyncDates.contains(str)){
                           currentNoSyncDates.add(str);
                        }
                    }
                    
                    //UPDATED nodata dates
                    ArrayList<String> currentNodataDates = targetPatient.getNodataDates();
                    ArrayList<String> addedNodataDates = hm.get("nodata");
                    for (String str : addedNodataDates){
                        if (!currentNodataDates.contains(str)){
                           currentNodataDates.add(str);
                        }
                    }
                    
                    break;
                }
            }
            if (targetPatient==null){
                throw new Exception("Patient not in current session-- shouln't happen");
            }
            
            hm.put("part",targetPatient.getPartDates());
            hm.put("full",targetPatient.getFullDates());
            hm.put("nodata",targetPatient.getNodataDates());
            hm.put("nosync",targetPatient.getNosyncDates());
             
             
            HashMap<String,Object> responseHM = new HashMap<>();
            responseHM.put("fillings", hm);
            responseHM.put("data",dataToSave);
            
            
            
            String jsonResponse = new Gson().toJson(responseHM);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(jsonResponse);
             
             
            
            
        }
        catch (SQLException sqle){
                sqle.printStackTrace();
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Database error");
        }
        catch(Exception  e){
             e.printStackTrace();
             response.setContentType("text/plain");
             response.setStatus(400);
             response.getWriter().write(e.getMessage());
        }
        finally{
                if (conn != null){
                    try {conn.close();} 
                    catch (SQLException ignore) { }
                }
        }
        
        
        
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

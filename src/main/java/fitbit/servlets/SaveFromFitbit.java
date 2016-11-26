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
import java.io.IOException;
import java.sql.Connection;
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
        Connection conn ;
        
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
            HashMap<String,ArrayList<String>> changeFillings = DateManager.saveDates(lastSyncDate,activeUserEmail, conn, pcpair_id, dataToSave, selDates);
            
             //CREATE PROPER PATIENT WITH PROPER SESSION :(
             //UPDATE SESSION :(
             //RETURN CHANGEDFILLINGS TO SPECIAL METHOD IN JS
             
             //... AND DON'T FORGET ABOUT COMPARISON
              
            
            
            
        }
        catch(Exception e){
            

            response.setContentType("text/plain");
            response.setStatus(400);
            response.getWriter().write(e.getMessage());
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

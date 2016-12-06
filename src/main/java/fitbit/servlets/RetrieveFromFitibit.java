/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import FitbitJsonBeans.DayResponse;
import com.google.gson.Gson;
import fitbit.accessFitbitService.FitbitRequestManager;
import fitbit.models.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 *
 * @author Vlad
 */
@WebServlet(name = "RetrieveFromFitbit", urlPatterns = {"/RetrieveFromFitbit"})
public class RetrieveFromFitibit extends HttpServlet {

   
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        
        
        String[] selDates =  new Gson().fromJson(request.getParameter("selDates"),String[].class);
        boolean intraday = Boolean.parseBoolean(request.getParameter("intraday")); 
        String fitbitId = request.getParameter("fitbitId");
        
       
      
        String currentFormattedDate= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (selDates[selDates.length-1].compareTo(currentFormattedDate)>0){
            response.setContentType("text/plain");
            response.setStatus(400);
            response.getWriter().write("You can't select future dates");
            return;
        }
       
        HttpSession session = request.getSession(false);
        User us = (User) session.getAttribute("user");
        String activeUserEmail = us.getUsername();
        
        
        try{
            
            ArrayList<DayResponse> allDaysData;
            
            
            
            if (intraday==true){
                allDaysData = FitbitRequestManager.retrieveFromFitbitIntraday(selDates,fitbitId, activeUserEmail);
                
                for (int j = allDaysData.size()-1; j >= 0; j--) {
                    if (!allDaysData.get(j).isHasMinuteData()){
                        allDaysData.remove(j);
                    }
                }
            }
            else{
                allDaysData = FitbitRequestManager.retrieveFromFitbitInterday(selDates,fitbitId, activeUserEmail);
            }
            
            String jsonResponse = new Gson().toJson(allDaysData);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(jsonResponse);
            
            
            
            
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

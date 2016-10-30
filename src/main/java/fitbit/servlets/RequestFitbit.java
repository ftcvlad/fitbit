/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import com.google.gson.Gson;
import fitbit.accessFitbitService.RequestManager;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author Vlad
 */
@WebServlet(name = "RequestFitbit", urlPatterns = {"/RequestFitbit"})
public class RequestFitbit extends HttpServlet {

   
    private DataSource dataSource;
    
    @Override
    public void init() throws ServletException {
		try {
                        dataSource = (DataSource) new InitialContext().lookup("java:comp/env/" + "jdbc/db");
			
		} catch (NamingException e) {
		}
    }
    

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
       
        RequestManager.retrieveFromFitbit(selDates,intraday,fitbitId);
 
     
  
   
        
        
        
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitbit.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.gson.Gson;
import fitbit.models.User;

/**
 *
 * @author Vlad
 */
@WebServlet(name = "mainPage", urlPatterns = {""})
public class mainPage extends HttpServlet {

    
  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        HttpSession session = request.getSession(false);
        User us = (User) session.getAttribute("user");
       
        
        //create <select> string
        String htmlString = "";
        
        for (int i=0;i<us.allPatients.size();i++){
            String fullName = us.allPatients.get(i).getName()+" "+us.allPatients.get(i).getSurname();
            String fitbitId = us.allPatients.get(i).getFitbitId();
            
            String fullDates = new Gson().toJson(us.allPatients.get(i).getFullDates()); 
            String partDates = new Gson().toJson(us.allPatients.get(i).getPartDates()); 
            String nosyncDates = new Gson().toJson(us.allPatients.get(i).getNosyncDates());
            String nodataDates = new Gson().toJson(us.allPatients.get(i).getNodataDates());
            
           
            //'  {"full":["date1","date2"...],"part":[]}  '
            htmlString+= "<option value=\""+fitbitId+"\"  data-foo=\'{\"full\":"+fullDates+",\"part\":"+partDates+",\"noData\":"+nodataDates+",\"noSync\":"+nosyncDates+"}\'>"+fullName+"</option>";

        }
        
        request.setAttribute("htmlString", htmlString);
        request.getRequestDispatcher("index.jsp").forward(request, response);
        
    }

    


    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

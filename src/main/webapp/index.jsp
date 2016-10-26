

<!DOCTYPE html>
<html>
   <head>
       
      <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
      <link rel="stylesheet" href="https://ssl.gstatic.com/docs/script/css/add-ons.css">
      <link rel="stylesheet" type="text/css" href="static/style.css">

      <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
      <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

      
       <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
      
       <link rel="stylesheet" type="text/css" href="static/tooltipsterCSS.css">
       
       
       <script src="static/tooltipster.js"></script>
       
        
       <script>console.log("-- "+new Date().getTime())</script>
            <script src="https://code.highcharts.com/highcharts.js" ></script>
            <script src="https://code.highcharts.com/highcharts-more.js" ></script>
            <script src="https://code.highcharts.com/modules/exporting.js" ></script>
       <script>console.log("-- "+new Date().getTime())</script>  
       <script src="//cdnjs.cloudflare.com/ajax/libs/dygraph/1.1.1/dygraph-combined.js" async></script>
    
        <script src="static/PapaParse.js"></script>
       <script src="static/javascript.js"></script>

   </head>
   
   
   <body>
    
     
    
      <div id="tabs">
        <div id="topRow">
           <ul>
              <li><a href="#tabs-1">Fitbit</a></li>
              <li><a href="#tabs-2">Combined with geneactiv</a></li>
           </ul>
           <div id="linksSection">
               <button class="imageButton" id="geneactivLink"  onclick="location.href='https://script.google.com/macros/s/AKfycbwRQxBTTAFIlXqBQe1NLXzXdZJ2PziG6YPIZ8uefcPtMMxGuxW4/exec'"></button>
               <button class="imageButton" id="formLink" onclick="location.href='https://script.google.com/macros/s/AKfycbxXETkjT1OnF3g7M5u7ONMqOxF7mpR2q6_rUEjM-AxGS6yiBBg/exec'"></button>
               <button class="imageButton tooltipShower" id="logout" data-tooltip-content="#logoutFrame" ></button>
                 
                <div class="tooltip_content">

                      <div id="logoutFrame">

                          <p >${sessionScope.user.username}</p>
                          <button onclick="sendLogoutRequest();" >Sign out</button>
                      </div>
                </div>
                                                                               
           </div> 
           
         </div>  
           
         
         <div id="tabs-1">
           <div class="container">
                <div id="radioButtonSetFitbit" >
                  <label for="radio1" class="radioTopLabel" >from Fitbit.com</label>
                  <input type="radio"  checked="checked" id="radio1" name="radioFitTop" value="fit" class="radioFitTop" onchange="fromFitbitSelected()">
                  <label for="radio2" class="radioTopLabel">Fitbit.com+Save to DB</label>
                  <input type="radio" id="radio2" name="radioFitTop" value="fitSave" class="radioFitTop"  onchange="fromFitbitAndDbSelected()">
                  <label for="radio3" class="radioTopLabel">from DB</label>
                  <input type="radio" id="radio3"  class="radioFitTop" value="DB" name="radioFitTop" onchange="fromDbSelected()">
                </div>
              
                
               
                <div >
                    <div id="adduserTooltipShower"  class="tooltipShower"  data-tooltip-content="#addUserFormContainer" >+</div>

                    <div id="finduserTooltipShower"  class="tooltipShower"  data-tooltip-content="#removeuserFormContainer" >&#9776;</div>
            
                    <div class="tooltip_content">
                        
                         <div id="addUserFormContainer">
                             <h3>Add user to DB <i>(all fields optional)</i></h3>
                             <p> </p>
                             <label for="addNameField">Name</label>
                             <input type="text" id="addNameField" maxlength="18"><br>
                             <label for="addSurnameField">Surname</label>
                             <input type="text" id="addSurnameField" maxlength="18"><br>
                             <label for="addAgeFieldMM">Birth Date</label>
                             <span class="sameHeightAge">
                                 <select id="addAgeFieldMM">
                                     <option value="1">January</option>
                                     <option value="2">February</option>
                                     <option value="3">March</option>
                                     <option value="4">April</option>
                                     <option value="5">May</option>
                                     <option value="6">June</option>
                                     <option value="7">July</option>
                                     <option value="8">August</option>
                                     <option value="9">September</option>
                                     <option value="10">October</option>
                                     <option value="11">November</option>
                                     <option value="12">December</option>
                                 </select>
                                 <input type="text" maxlength="2" placeholder="dd" id="addAgeFieldDD">
                                 <input type="text" maxlength="4" placeholder="yyyy" id="addAgeFieldYYYY"><br></span>
                            
                             <button type="button" id="addNewPatientBtn" onclick="getAuthUrl()">Add</button>
                             
                         </div>
                        
                  
                    </div>
                    
                    
                    
                     <div class="tooltip_content">
                        
                         <div id="removeuserFormContainer">
                                <div id="buttonHolder" >
                                     <button type="button" id="listDbUsersBtn" title="Find in database">Find</button>
                                     <button type="button" id="totalDeleteBtn" title="Delete user entirely" disabled>Delete</button>
                                     <button type="button" id="addToShortlistBtn" title="Add user to shortlist" disabled>Enlist</button>
                                     
                                     <input type="text" maxlength="18" placeholder="Search by Name (optional)" id="nameForFindInput">
                                
                                </div>
                                <div id="tableMessage">Ready</div>
                                <div id="tableHolder"></div>
                               
                         </div>
                         
                     </div>
                    
                
                
                </div>
             </div>
             <br/>      
         
            <fieldset>
               <form id="myForm">
               
                  <div class="flexDiv" >

                     <label for="radioIntraFit" >Intraday</label>
                     <input type="radio" name="interIntra" id="radioIntraFit" value="en" checked=true onchange="intraInterChange(this)"  />
                     <label for="radioInterFit">Interday</label>
                     <input type="radio" name="interIntra" id="radioInterFit" value="dis" onchange="intraInterChange(this)"/><br/>
              
              <!--frequency picker-->
                     <label for="frq" class="selectorLabel">Select frequency</label>
                     <select name="frq" id="frq">
                        <option value="1">1 min</option>
                        <option value="5">5 min</option>
                        <option value="15"  selected="selected"> 15 min</option>
                        <option value="30">30 min</option>
                        <option value="60">1 hour</option>
                     </select>
                     <br/>   
                     
                     <label class="selectorLabel" for="graphType">Graph type</label>
                     <select name="graphType" id="graphType">
                         <option value="LC" >Line</option>
                        <option value="LS" >LineStacked</option>
                        <option value="HL" selected="selected" >RadarLine</option>
                        <option value="HS">RadarStacked</option>
                        <option value="HP">RadarStackedPercent</option>
                     </select>
                     <br/>
                     
               <!--submit-->
                     
                     <button type="button" id="retrieveBtn" onclick="serverProcessForm()">Retrieve</button>
                     
                  </div>
                  
                  
                  
                  
                  <div class="flexDiv">
               <!--radio buttons-->
                     <label for="radioDayFit" >Select days</label>
                     <input type="radio" name="radioFit" id="radioDayFit" value="1" checked=true onchange="handleRadioChange(this)"  />
                     <label for="radioRangeFit">Range</label>
                     <input type="radio" name="radioFit" id="radioRangeFit" value="2" onchange="handleRadioChange(this)"/><br/>
                <!--date picker-->
                     <label for="datepicker" class="selectorLabel" id="datepickerLabel">Days selected </label><span class="dateTypeTip" data-legend="">?</span><br/>
                     <input name="pickDate" id= "datepicker" type="text" />
                  </div>
                  
                  
                  
                  
                  <div class="flexDiv">
                      <label class="selectorLabel" for="userIDselect">Shortlisted Accounts</label>
                      <select name="userId" id="userIDselect">
                            ${htmlString} 
                      </select>
                      
                      
                      <button type="button" id="removeBtn" onclick="removeFromShortList()">Delist</button>
                      <hr>

                     
                     
                  </div>
               </form>
               <div id="errorSpanFit" class="ui-state-highlight" style="color:red">Ready</div>
            </fieldset>
            
            <div id="dashboard_div2">
               <p id="range" ></p>
               <div id="slider_div"></div>
               <div id="line_chart_div" style="height:600px"></div><!-- children divs for anychart are 100% of this-->
            </div>
         </div>
         

         
         
        <div id="tabs-2">
            <fieldset>


                <div class="flexDiv">
                    <input type="file" id="file-input" /><br/>
                    <br/>
                    <li>Use Fitbit tab for settings and Fitbit errors. Geneactiv errors here </li>
                    <li>Only Line Chart is possible </li>

                    <br/>
                    <button type="button" id="retrieveBtnCombined" onclick="processBoth()">Plot</button>

                </div>
                <div id="errorSpanGENE" class="ui-state-highlight" style="color:red">Ready</div>


            </fieldset>


            <div id="dashboard_combined">
                <input type="text" id="rangeCombined" readonly style="border:0; color:#f6931f; font-weight:bold;">
                <div id="slider_divCombined"></div>
                <div id="line_chart_divCombined" style="height:600px"></div>
            </div>


        </div>
         
         
      </div>
     
   </body>
  
</html>


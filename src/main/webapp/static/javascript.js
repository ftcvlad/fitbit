
var ajaxLocked = false;

 
$(function () {

    google.charts.load('current', {'packages':['corechart', 'table']});
  
    
    google.charts.setOnLoadCallback(function(){
              
     
               var data = new google.visualization.DataTable();
               data.addColumn('string', 'Name');
               data.addColumn('string', 'Surname');
               data.addColumn('string', 'Birth date');
               data.addColumn('string', 'id');
               data.addColumn('string', 'fillings');
                              
               var table = new google.visualization.Table(document.getElementById('tableHolder'));
               
               $("#listDbUsersBtn").click(function(){findSavedPatients(table, data);});
               $("#totalDeleteBtn").click(function(){deleteSelectedUser(table, data); });
               $("#addToShortlistBtn").click(function(){addToShortlist(table, data)});

               google.visualization.events.addListener(table, 'select', function(){
               
                     if( table.getSelection().length===0){
                             $("#totalDeleteBtn").prop("disabled",true);
                             $("#addToShortlistBtn").prop("disabled",true);
                     }
                     else{
                             $("#totalDeleteBtn").prop("disabled",false);
                             $("#addToShortlistBtn").prop("disabled",false);
                     }
               });
           
                var view = new google.visualization.DataView(data);
                view.setColumns([0,1,2]); //here you set the columns you want to display
                
             
                 table.draw(view, {width: '100%',  cssClassNames:{headerRow : "tableHeader", tableRow: "tableRow", oddTableRow: "oddRow", headerCell  :"headerCell" }});
     
     });



    $("#tabs").tabs();//mb move to some other place -- loads slowly

   

    var datepickerDiv =  $("#datepicker");
   

    datepickerDiv.datepicker({
        dateFormat: "yy-mm-dd",
        maxDate: '0',//0 days from today
        autoSize: true,
        showButtonPanel: true,
        currentText: "Clear selection",
        onClose: function () {
            $(this).data('datepicker').inline = false;
        },

        onSelect: datepickerOnSelect,
        beforeShowDay: datepickerBeforeShowDay,
        beforeShow: datepickerBeforeShow
    });
    datepickerDiv.data('datepicker').arrayOfDates = [];


    


    $("#frq").selectmenu({width:150});
  
    $("#graphType").selectmenu({width:150});
    $("#userIDselect").selectmenu({width:150});
    

    $("#retrieveBtn").button({icons: {primary: "ui-icon-circle-arrow-s"}});
    $("#removeBtn").button({icons: {primary: " ui-icon-closethick"}});
    $("#addBtn").button({icons: {primary: "ui-icon-circle-plus"}});
    $("#addNewPatientBtn").button({icons: {primary: "ui-icon-circle-plus"}});
    $("#retrieveBtnCombined").button({icons: {primary: "ui-icon-circle-arrow-s"}});


    $( "#radioButtonSetFitbit" ).buttonset();

   
    $.datepicker._gotoToday = function (id) {//override and don't call original -- stay on current month

        //id is the #id of the input element, inst is the associated datepicker
        var target = $(id);
        var inst = this._getInst(target[0]);

        inst.arrayOfDates.length = 0;
        target.val("0");
        
       
        this._adjustDate(target);

    };
    

    
    $( ".dateTypeTip" ).tooltip({
      items: "[data-legend]",
      show:{duration:0},
      hide:{duration:0},
      content: function() {
      
        var element = $( this );
        if ( element.is( "[data-legend]" ) ) {
          
          return  '<span style="background-color:#e4c2be; width: 30px;height:10px; display: inline-block "></span><span> no data at Fitbit</span><br/>'+
                  '<span style="background-color:#68d898; width: 30px;height:10px; display: inline-block "></span><span> day saved to DB</span><br/>'+
                  '<span style="background-color:#78781a; width: 30px;height:10px; display: inline-block "></span><span> partly saved</span><br/>'+
                  '<span style="background-color:#bfbf90; width: 30px;height:10px; display: inline-block "></span><span> not synchronized</span><br/>'+
                  '<span style="background-color:#357ae8; width: 30px;height:10px; display: inline-block "></span><span> selected</span><br/>'+
                  '<span style="background-color:#d14836; width: 30px;height:10px; display: inline-block "></span><span> lost</span><br/>';
       }
      }
    });
    
  


   $('#adduserTooltipShower').tooltipster( createAdduserOptions());
   $('#finduserTooltipShower').tooltipster(createFinduserOptions());
  

    $('#logout').tooltipster( {
            contentAsHTML: true,
            interactive: true,
            trigger: "click",
        
            functionReady: function(){
           
                
                 $(".tooltipster-box").eq(0).parent().addClass("specialTooltipster");
            
               
            },
         
            
            side:"bottom",
            viewportAware:false,
            functionPosition: function(instance, helper, position){
                position.coord.left = position.target - position.size.width+15;
                return position;
            },
            delay:0,
            animationDuration:0,
            minWidth:175,
            maxWidth:575
           
      });


});

function createFinduserOptions(){
      return {
                contentAsHTML: true,
                interactive: true,
                trigger: "click",
                functionPosition: function(instance, helper, position){
                   position.coord.left = position.target - 485;
                    return position;
                },
                functionReady: function(instance, helper){
                   
                     $(helper.tooltip).css("min-height","300px");//set height of tooltipster's top element
                      $(helper.tooltip).css("max-height","300px");
                },
            
                side:"bottom",
                delay:0,
                animationDuration:0,
                minWidth:500,
                maxWidth:500
             };

}

function createAdduserOptions(){
      return {
            contentAsHTML: true,
            interactive: true,
            trigger: "click",
           
           
            
            side:"bottom",
            functionPosition: function(instance, helper, position){
                position.coord.left = position.target - 360;
                return position;
            },
            delay:0,
            animationDuration:0,
            minWidth:375,
            maxWidth:375
      };
}



function sendLogoutRequest(){

    if (ajaxLocked){return;}           
    ajaxLocked=true;
    jQuery.ajax({
        method: "post", 
        url: "logout", 
      
        success: function ( response,textStatus,jqXHR){
            window.location = "Login";
        },
        error: function(jqXHR, errorStatus, errorThrown) {

            if( jqXHR.responseText==="Session expired" ) {
                    window.location="Login";
            }
        },
        complete: function(){
            ajaxLocked=false;
        }
    });                
}


var datepickerBeforeShow = function(){
    return {maxDate:new Date()};
};

var datepickerBeforeShowDay = function (date) {

    var str = dateToYYYYMMDDstring(date);
  
    //this refers to input DOM element
    if ($(this).data("datepicker").arrayOfDates.indexOf(str) > -1) {
        return [true, "selected", ''];
    }
    else {
      
        var dayTypesObj = $('#userIDselect').find(":selected").data("foo");
      
       
        if (dayTypesObj!==null  ){
            if (dayTypesObj["nodata"].indexOf(str)>-1){
                return [true, "noData", ''];
            }
            else if (dayTypesObj["full"].indexOf(str)>-1){
                return [true, "full", ''];
            }
            else if (dayTypesObj["part"].indexOf(str)>-1){
                return [true, "part", ''];
            }
            else if (dayTypesObj["nosync"].indexOf(str)>-1){
                return [true, "noSync", ''];
            }
            else if (dayTypesObj["lost"].indexOf(str)>-1){
                return [true, "lost", ''];
            }
        }
      
    }
    
    return [true, '', ''];
};

var datepickerOnSelect = function (dateText, inst) {


    $(this).data('datepicker').inline = true;


    var radioButtonsSiblings = $(this).siblings('input');
    var radioVal = $(radioButtonsSiblings[0]).is(':checked') ? $(radioButtonsSiblings[0]).val() : $(radioButtonsSiblings[1]).val();

    if (radioVal === "1") {//select dates

        var index = inst.arrayOfDates.indexOf(dateText);
        if (index === -1) {
            inst.arrayOfDates.push(dateText);

        }
        else {
            inst.arrayOfDates.splice(index, 1);
        }

    }
    else if (radioVal === "2") {//select range
        var len = inst.arrayOfDates.length;

        if (len === 0) {
            inst.arrayOfDates.push(dateText);
        }
        else if (len === 1) {
            var start;
            var end;
            if (dateText >= inst.arrayOfDates[0]) {
                start = inst.arrayOfDates[0];
                end = dateText;
            }
            else {
                end = inst.arrayOfDates[0];
                start = dateText;
            }
            inst.arrayOfDates = [];
            for (; start <= end;) {
                inst.arrayOfDates.push(start);
                start = incrementDate(start);
            }

        }
        else if (len > 1) {
            inst.arrayOfDates = [];
            inst.arrayOfDates.push(dateText);

        }

    }

  
    $(this).val(inst.arrayOfDates.length);
};

function dateToYYYYMMDDstring(date) {

    var month = (date.getMonth() + 1) + "";
    if (month.length === 1) {
        month = "0" + month;
    }

    var cislo = date.getDate() + "";
    if (cislo.length === 1) {
        cislo = "0" + cislo;
    }

    var year = date.getFullYear();

    return year + "-" + month + "-" + cislo;
}

function handleRadioChange(radioClicked) {

    var respectiveInput = $(radioClicked).siblings('input').last();

    respectiveInput.data("datepicker").arrayOfDates = [];
    respectiveInput.val("0");
}



function intraInterChange(radioClicked) {

    var value = $(radioClicked).val();
    if (value === "en") {
        
        
        $('#radio2').prop("disabled", false);
        $("#radioButtonSetFitbit").buttonset("refresh");
        
        
         $("#frq").selectmenu("enable");
         $("#frq").selectmenu("refresh");
        
         $("#graphType option").not(':eq(0)').attr("disabled", false);
         $("#graphType").selectmenu("refresh");
    }
    else if (value === "dis") {
        
        
        if ($('#radio2').is(':checked')){
          $("#radio1").prop("checked", true);
          $("#frq option").attr("disabled", false);
        }
        
        $('#radio2').prop("disabled", true);
        $("#radioButtonSetFitbit").buttonset("refresh");
        
      
        //frequency
        $("#frq").selectmenu("disable");
        $("#frq").selectmenu("refresh");
        
         //graph type
        $("#graphType option").not(':eq(0)').attr("disabled", true);
        $('#graphType').val('LC');
        $("#graphType").selectmenu("refresh");
    }

}

function fromFitbitSelected(){
      $("#frq option").attr("disabled", false);
      $("#frq").selectmenu("refresh"); 
}

function fromFitbitAndDbSelected(){
      $("#frq option").not(':eq(0)').attr("disabled", true);
      $("#frq").val("1");
      $("#frq").selectmenu("refresh");
}


function fromDbSelected(){

     $("#frq option").attr("disabled", false);
     $("#frq").selectmenu("refresh");    
}


function setStatus(divElement, message, classToAdd, deferred){

      if (classToAdd==="ui-state-highlight"){
         divElement.addClass("ui-state-highlight");
         divElement.removeClass("ui-state-error");
      }
      else{
         divElement.removeClass("ui-state-highlight");
         divElement.addClass("ui-state-error");
      }
      divElement.text(message);
      
      
      if (deferred!==undefined){//undefined when not passed (ui-state-highlight), or no deferred at all (not combine)
          deferred.reject();
      }
     
}





//----------------------------------------------------------FOR GENEACTIV-------------------------------------------


function readSingleFile(deferred) {

    var errorSpanGene  = $("#errorSpanGENE");

    var file = document.getElementById("file-input").files[0];


    if (!file) {
        setStatus(errorSpanGene, "Select file!", "ui-state-error", deferred);
    }
    else {
    
        var allSelDates =[]; 
        
       
        allSelDates = $("#datepicker").data("datepicker").arrayOfDates;
        if (allSelDates.length === 0){
           deferred.reject();
           return;
        }
            
       setStatus(errorSpanGene, "Reading file...", "ui-state-highlight");
       
       var allArrayData = [];
       Papa.parse(file, {
               // worker: true,
                dynamicTyping: true,
                step: function(row) {
                    if (row.data[0].length===12){
                        allArrayData.push([row.data[0][0], row.data[0][7]]);
                    }
                },
                complete: function(results) {
                   
                     processCsvString(allArrayData, allSelDates.sort(),deferred);
                   
                }
       }); 
       
    }
}

function processCsvString(allArrayData, selDates, deferred) {

    var errorSpanGene = $("#errorSpanGENE");
    
    setStatus(errorSpanGene, "Processing data...", "ui-state-highlight");



    // depending on Combine or Gene tab select proper data!
    var intraday;
    var targetFrequency;
    
    
 
    targetFrequency = parseInt($("#frq").val())*60;//as gene wants in seconds
    intraday = $("#radioIntraFit").is(':checked') ? true : false;
      
    



   
    var traverseIndex = 0;//big go through allArrayData
    
    if (intraday === true) {

        //add time column
        var selectedData = [['TimeG']];
       
        var columnToAdd = 0;
        var csvFrequency = 0;
       


        for (var k = 0; k < selDates.length; k++) {
            var targetDate = selDates[k];


            //find first index of selected date
            var found = false;
            for (; traverseIndex < allArrayData.length; traverseIndex++) {
                if (allArrayData[traverseIndex][0] !== null && allArrayData[traverseIndex][0].substring(0, 10) === targetDate) {
                    found = true;
                    break;
                }
            }

            if (found === false) {
                traverseIndex = 0;
                continue;
            }
            else if (columnToAdd === 0) {//create time column which can be of different length

                if (traverseIndex + 1 < allArrayData.length &&
                    allArrayData[traverseIndex + 1][0] !== null &&
                    allArrayData[traverseIndex + 1][0].substring(0, 10) === targetDate) {//if there are at least 2 items for same date

                    var fst = new Date(0, 0, 0, parseInt(allArrayData[traverseIndex][0].substring(11, 13)), parseInt(allArrayData[traverseIndex][0].substring(14, 16)), parseInt(allArrayData[traverseIndex][0].substring(17, 19)), 0);
                    var snd = new Date(0, 0, 0, parseInt(allArrayData[traverseIndex + 1][0].substring(11, 13)), parseInt(allArrayData[traverseIndex + 1][0].substring(14, 16)), parseInt(allArrayData[traverseIndex + 1][0].substring(17, 19)), 0);
                    csvFrequency = Math.abs(snd.getTime() - fst.getTime()) / 1000;


                }
                else {// never happens kind of -- single record for a date
                  
                    traverseIndex = 0;
                    continue;
                }

                var startDayTime = new Date(0, 0, 0, 0, 0, 0, 0);

                var hh = "";
                var mm = "";
                var ss = "";
                for (var t = 0; t < 24 * 3600 / targetFrequency; t++) {

                    startDayTime.setSeconds(startDayTime.getSeconds() + targetFrequency);//01:00 is the sum for 00:00-01:00

                    hh = startDayTime.getHours() + "";
                    if (hh.length < 2) {
                        hh = "0" + hh;
                    }
                    mm = startDayTime.getMinutes() + "";
                    if (mm.length < 2) {
                        mm = "0" + mm;
                    }
                    ss = startDayTime.getSeconds() + "";
                    if (ss.length < 2) {
                        ss = "0" + ss;
                    }
                    selectedData.push([hh + ":" + mm + ":" + ss]);


                }
                selectedData[selectedData.length - 1] = ["24:00:00"];
            }


            //create next header
            selectedData[0].push(targetDate);
            //fill next column with nulls
            for (var i = 1; i < selectedData.length; i++) {
                selectedData[i].push(null);//??? NOT EFFECTIVE?
            }
            columnToAdd++;

            //at what index in selectedData to start putting data
            var secondsPassed = parseInt(allArrayData[traverseIndex][0].substring(11, 13)) * 3600 +
                parseInt(allArrayData[traverseIndex][0].substring(14, 16)) * 60 +
                parseInt(allArrayData[traverseIndex][0].substring(17, 19));
            var nextSelectDataIndex = Math.floor(secondsPassed / targetFrequency) + 1;//+1 for header row in selectedData


           

            var stepsForPeriod = 0;
            var everyNth = targetFrequency / csvFrequency;


            var ind = 0;
            var properStart = false;
            //for all csv rows that have targetDate

            while (traverseIndex !== allArrayData.length
            && allArrayData[traverseIndex][0] !== null //in case empty rows in the end of csv
            && allArrayData[traverseIndex][0].substring(0, 10) === targetDate) {

                

                if (!properStart) {//first period may be not full; output when first reached relevant time

                    var timeStr = allArrayData[traverseIndex][0].substring(11, 19);

                

                    if (selectedData[nextSelectDataIndex][0] <= timeStr) {//BUG :(

                        properStart = true;
                        selectedData[nextSelectDataIndex][columnToAdd] = stepsForPeriod;
                        stepsForPeriod = 0;
                        nextSelectDataIndex++;
                    }

                }
                else {
                    ind++;


                    if (ind % everyNth === 0) {
                        selectedData[nextSelectDataIndex][columnToAdd] = stepsForPeriod;
                        stepsForPeriod = 0;
                        nextSelectDataIndex++;
                    }


                }
                stepsForPeriod += allArrayData[traverseIndex][1];
                traverseIndex++;


            }

            if (stepsForPeriod !== 0) {//if finished with series, output stepsForPeriod
                selectedData[nextSelectDataIndex][columnToAdd] = stepsForPeriod;
            }

            if (traverseIndex === allArrayData.length) {
                break;
            }


        }//end of for all dates
    }//end of intraday==true
    else if (intraday === false) {

        selectedData = [["Date", "Steps summary"]];

        for ( k = 0; k < selDates.length; k++) {
            targetDate = selDates[k];


            var found = false;
            for (; traverseIndex < allArrayData.length; traverseIndex++) {
                if (allArrayData[traverseIndex][0] !== null && allArrayData[traverseIndex][0].substring(0, 10) === targetDate) {
                    found = true;
                    break;
                }
            }

            if (found === false) {
                traverseIndex = 0;
                continue;
            }



            var sumForDate = 0;

            for (; traverseIndex < allArrayData.length; traverseIndex++) {
                if (allArrayData[traverseIndex][0] !== null && allArrayData[traverseIndex][0].substring(0, 10) === targetDate) {
                    sumForDate += allArrayData[traverseIndex][1];
                }
                else{
                    break;
                }
            }

            if (sumForDate !== 0) {
                selectedData.push([targetDate, sumForDate]);
            }
            else {
                traverseIndex = 0;
            }

        }
    }


    //if no data selected at all
    if (selectedData.length === 1) {
        setStatus(errorSpanGene, "No data in specified range", "ui-state-error", deferred);
        return;
    }

    deferred.resolve(selectedData);
    
}


function incrementDate(from_date) {
    var YYYY = from_date.substring(0, 4);
    var MM = from_date.substring(5, 7);
    var DD = from_date.substring(8);
    var nextDate = new Date(parseInt(YYYY, 10), parseInt(MM, 10) - 1, parseInt(DD, 10));
    nextDate.setDate(nextDate.getDate() + 1);

    return dateToYYYYMMDDstring(nextDate);
}





//----------------------------------------------------------------------FOR FITBIT------------------------------------------------------------

//ADD NEW PATIENT
function verifyDate(){

           var monthNum = parseInt($("#addAgeFieldMM").val());
           var dayStr = $("#addAgeFieldDD").val();
           var yearStr = $("#addAgeFieldYYYY").val();
           
           try{ 
                validateDate(yearStr, monthNum, dayStr);
                return true;
           }
           catch(e){
               $('#addUserFormContainer > p:eq(0)').text(e);
               return false;//form not submitted
           }
}


//FIND PATIENTS

function findSavedPatients(table, datatable){
        if (ajaxLocked){return;}
        $("#totalDeleteBtn").prop("disabled",true);
        $("#addToShortlistBtn").prop("disabled",true);



        $("#tableMessage").text("Retrieving patient list...");


        ajaxLocked = true;
        jQuery.ajax({
            method: "get",
            url: "findPatients",
            data: {name: $("#nameForFindInput").val()},
            success: function (response, textStatus, jqXHR) {
                datatable.removeRows(0, datatable.getNumberOfRows());
                for (var i = 0; i < response.length; i++) {
                    datatable.addRow([response[i].name, response[i].surname, response[i].birthDate, response[i].fitbitId,
                        JSON.stringify({fullDates: response[i].fullDates, partDates: response[i].partDates,nosyncDates: response[i].nosyncDates,nodataDates: response[i].nodataDates, lostDates:response[i].lostDates })]);
                }
                var view = new google.visualization.DataView(datatable);
                view.setColumns([0, 1, 2]); //here you set the columns you want to display
                table.draw(view, {width: '100%', cssClassNames: {headerRow: "tableHeader", tableRow: "tableRow", oddTableRow: "oddRow", headerCell: "headerCell"}});
                $("#tableMessage").text("Done");
            },
            error: function (jqXHR, errorStatus, errorThrown) {

                if (jqXHR.responseText === "Session expired") {
                    window.location = "Login";
                }
                $("#tableMessage").text(jqXHR.responseText);

            },
            complete: function () {
                ajaxLocked = false;
            }
        });    
         
     
}


//ADD TO SHORTLIST

function addToShortlist(table, datatable){
        if (ajaxLocked){return;}
        $("#tableMessage").text("Adding selected users to shortlist...");
        var allUsers = [];

        var allSelected =  table.getSelection();
        for (var i=0; i<allSelected.length; i++){

              var row = allSelected[i].row;

              var fitbitId = datatable.getValue(row,3);
              var name = datatable.getValue(row,0);
              var surname = datatable.getValue(row,1);
              var birthDate = datatable.getValue(row,2);
              var fillings = datatable.getValue(row,4);

              var dates = JSON.parse(fillings);
              allUsers.push({fitbitId: fitbitId, name: name,surname:surname, birthDate:birthDate,fullDates: dates.fullDates, partDates: dates.partDates, nosyncDates:dates.nosyncDates, nodataDates:dates.nodataDates, lostDates:dates.lostDates  });

        }
     
       
        ajaxLocked = true;
        jQuery.ajax({
            method: "post",
            url: "addToShortlist",
            data: {patients:JSON.stringify(allUsers)},
            success: function (response, textStatus, jqXHR) {
                updateSelectEnlist(response);
            },
            error: function (jqXHR, errorStatus, errorThrown) {
                if (jqXHR.responseText === "Session expired") {
                    window.location = "Login";
                }
                $("#tableMessage").text(jqXHR.responseText);
            },
            complete: function () {
                ajaxLocked = false;
            }
        });   
  
  
}


function updateSelectEnlist(patientsToAdd){
    
    if (patientsToAdd.length>0){
    
        $.each(patientsToAdd, function (i, nextPatient) {
        
        
            $('#userIDselect').prepend($('<option>', { 
                    value: nextPatient.fitbitId,
                    text : nextPatient.name+" "+nextPatient.surname,
                    "data-foo" :  JSON.stringify({"full":nextPatient.fullDates, "part":nextPatient.partDates, "nosync":nextPatient.nosyncDates, "nodata":nextPatient.nodataDates, "lost":nextPatient.lostDates}),
                    selected: "selected"
                }));
        });
        
        $("#userIDselect").selectmenu("destroy").selectmenu({width:150});
    }
    $("#tableMessage").text("Done"); 
}


//REMOVE FROM SHORTLIST


function removeFromShortList(){
    if (ajaxLocked){return;}
    
     var fitbitId = $("#userIDselect").val();
     var errorSpanFit = $("#errorSpanFit");
     if (fitbitId===null){
          setStatus(errorSpanFit, "No id to remove!", "ui-state-error");  
     }
     else{
        ajaxLocked = true;

        jQuery.ajax({
            method: "post",
            url: "removeFromShortlist",
            data: {fitbitId:fitbitId},
            success: function (response, textStatus, jqXHR) {
                 $('#userIDselect').find('option[value="'+fitbitId+'"]').remove(); 
                 $("#userIDselect").selectmenu("destroy").selectmenu({width:150});              
                 errorSpanFit.text("Done");
            },
            error: function (jqXHR, errorStatus, errorThrown) {
                if (jqXHR.responseText === "Session expired") {
                    window.location = "Login";
                }
                 errorSpanFit.text(jqXHR.responseText);
            },
            complete: function () {
                ajaxLocked = false;
            }
        });  
        
     }
}


//DELETE PATIENT

function deleteSelectedUser(table, datatable){
        if (ajaxLocked){return;}
        $("#tableMessage").text("Deleting selected users...");
        var allUserIds = [];

        var allSelected =  table.getSelection();
        for (var i=0; i<allSelected.length; i++){
           
            allUserIds.push(datatable.getValue(allSelected[i].row,3));
        }
        
        
        ajaxLocked = true;

        jQuery.ajax({
            method: "post",
            url: "deletePatients",
            data: {idArray:JSON.stringify(allUserIds)},
            success: function (response, textStatus, jqXHR) {
                updateTableListRemove(allUserIds,datatable,table);
            },
            error: function (jqXHR, errorStatus, errorThrown) {
                if (jqXHR.responseText === "Session expired") {
                    window.location = "Login";
                }
                 $("#tableMessage").text(jqXHR.responseText);
            },
            complete: function () {
                ajaxLocked = false;
            }
        });  
}


function updateTableListRemove(idsToRemove, datatable, table){

    if (idsToRemove.length>0){
        for (var i=0;i<idsToRemove.length;i++){
            $('#userIDselect').find('option[value="'+idsToRemove[i]+'"]').remove(); 
        }
        $("#userIDselect").selectmenu("destroy").selectmenu({width:150});
       
        
        for (var j=0; j< datatable.getNumberOfRows();j++){
            var nextId = datatable.getValue(j,3);
            if (idsToRemove.indexOf(nextId)>=0){
                datatable.removeRow(j);
                j--;
            }

        }

         var view = new google.visualization.DataView(datatable);
         view.setColumns([0,1,2]); 


        table.draw(view, {width: '100%',  cssClassNames:{headerRow : "tableHeader", tableRow: "tableRow", oddTableRow: "oddRow" }});

        $("#totalDeleteBtn").prop("disabled",true);
        $("#addToShortlistBtn").prop("disabled",true);
        

    }
    $("#tableMessage").text("Done"); 
    
}





























//BIG BIG SERVERPROCESSFORM STUFF


function serverProcessForm(deferred) {//frm data is DOM form element

    if (ajaxLocked){return;}


    var errorSpanFit = $("#errorSpanFit");
    var fitbitId = $("#userIDselect").val();
    

 

    if ($("#datepicker").data("datepicker").arrayOfDates.length === 0) {
         setStatus(errorSpanFit, "No dates selected", "ui-state-error", deferred);
    }
    else if (fitbitId === null){
         setStatus(errorSpanFit, "No Id was selected. If no dropdown, add users first", "ui-state-error", deferred);
    }
    else {


        setStatus(errorSpanFit, "Retrieving...", "ui-state-highlight");
        ajaxLocked = true;


        var actionType = $('input[name=radioFitTop]:checked').val();
        var intraday = $("#radioIntraFit").is(':checked') ? true : false;
       
        if (actionType ==="fit"){
            
            jQuery.ajax({
                method: "get",
                url: "RetrieveFromFitbit",
                data: {selDates:JSON.stringify($("#datepicker").data("datepicker").arrayOfDates.sort()),
                       intraday:intraday,
                       fitbitId: fitbitId},
                success: function (response, textStatus, jqXHR) {
                       processFitbitData(intraday, response,deferred);
                },
                error: function (jqXHR, errorStatus, errorThrown) {
                    if (jqXHR.responseText === "Session expired") {
                        window.location = "Login";
                    }

                   setStatus($("#errorSpanFit"), jqXHR.responseText, "ui-state-error", deferred);
                   clearChartSliderAreaAndGetMemoryBack();

                },
                complete: function () {
                    ajaxLocked = false;
                }
            });  
            
  
            
        }
        else if (actionType ==="fitSave"){
            
            jQuery.ajax({
                method: "post",
                url: "SaveFromFitbit",
                data: {selDates:JSON.stringify($("#datepicker").data("datepicker").arrayOfDates.sort()),
                       fitbitId: fitbitId},
                success: function (response, textStatus, jqXHR) {
                         dbSaveSucceed(response,fitbitId, intraday, deferred);
                },
                error: function (jqXHR, errorStatus, errorThrown) {
                    if (jqXHR.responseText === "Session expired") {
                        window.location = "Login";
                    }

                   setStatus($("#errorSpanFit"), jqXHR.responseText, "ui-state-error", deferred);
                   clearChartSliderAreaAndGetMemoryBack();

                },
                complete: function () {
                    ajaxLocked = false;
                }
            });
            
            
            
          
        }
        else if (actionType ==="DB"){
           jQuery.ajax({
                method: "get",
                url: "RetrieveFromDb",
                data: {selDates:JSON.stringify($("#datepicker").data("datepicker").arrayOfDates.sort()),
                       intraday:intraday,
                       fitbitId: fitbitId},
                success: function (response, textStatus, jqXHR) {
                       processDatabaseData(intraday, response,deferred);
                },
                error: function (jqXHR, errorStatus, errorThrown) {
                    if (jqXHR.responseText === "Session expired") {
                        window.location = "Login";
                    }

                   setStatus($("#errorSpanFit"), jqXHR.responseText, "ui-state-error", deferred);
                   clearChartSliderAreaAndGetMemoryBack();

                },
                complete: function () {
                    ajaxLocked = false;
                }
            });
        }

    }
}



function dbSaveSucceed(response, fitbitId, intraday, deferred){
    
    
    var frqMinutes = parseInt($("#frq").val());
    
   
    
 
    var fillings = response.fillings;
    var data = response.data;
    
    
    $('#userIDselect option[value="'+fitbitId+'"]').data("foo",fillings);
    
    processFitbitData(intraday, data, deferred);
    
    
   
    
 
}






//processDatabaseData and processFitbitData are nearly identical! but different response formats + cleaner having them separate
function processDatabaseData(intraday, response, deferred){
    //[[2016-06-09,2016-06-10]["12","14"]["234","1212"]...]  for intraday -> add time column + respective frequency + parseInt
    //[[Date,Total steps][2016-06-09,"325876"][2016-06-10,"213111"]...] for interday -> parseInt
    
   
    var errorSpanFit = $("#errorSpanFit");
    if (response.length===0){
        setStatus(errorSpanFit, "Db has no data for specified range", "ui-state-highlight", deferred);
        clearChartSliderAreaAndGetMemoryBack();
        return;
    }
    
    


    
    var allDayData = response;
    var selectedData;
    
    if (intraday===true){
        
        selectedData = [['Time']];
        var frqMinutes = parseInt($("#frq").val());
       
        //ADD TIME COLUMN
        addTimeColumn(selectedData, frqMinutes);


        //GENERATE SELECTED DATA
        var totalStepsForPeriod = null;
      
        for (var i=0;i<allDayData[0].length;i++){
        
            selectedData[0].push(allDayData[0][i]);

            var dataAvailableLength = allDayData.length;
            var itemsPushed = 0;

            for (j = 1; j < dataAvailableLength; j++) {
                if (allDayData[j][i]!==null){
                    totalStepsForPeriod += parseInt(allDayData[j][i]);
                }
                if (j  % frqMinutes === 0 || j === (dataAvailableLength - 1)) {//add data every f hours or leftovers
                    selectedData[itemsPushed + 1].push(totalStepsForPeriod);
                    totalStepsForPeriod = null;
                    itemsPushed++;
                }
            }
        }
    }
    else if (intraday===false){
           
        
        for (var i=1;i<allDayData.length;i++){
            allDayData[i][1] = parseInt(allDayData[i][1]);
        }
        selectedData = allDayData;
    }
    
  
 
    
    if (deferred===undefined){
        setStatus(errorSpanFit, "Drawing data...", "ui-state-highlight");
        drawGraph($("#graphType").val(), "Fitbit", 'line_chart_div', "slider_div", "range", selectedData, frqMinutes);
        setStatus(errorSpanFit, "Done", "ui-state-highlight");
    }
    else{//if combined tab, return selectedData to callback
       deferred.resolve(selectedData);
    }
    
    
    
    
}






function processFitbitData(intraday, response, deferred){
    //response== [o,o,o,o], where o is
        //  {
        //    "activities-log-steps":[
        //        {"dateTime":"2014-09-05","value":1433}
        //    ],
        //    "activities-log-steps-intraday":{
        //        "datasetInterval":1,
        //        "dataset":[
        //            {"time":"00:00:00","value":0},
        //            {"time":"00:01:00","value":0},
        //        ]
        //    }
        //}
  
  
    var errorSpanFit = $("#errorSpanFit");
    
    if (response.length===0){
        setStatus(errorSpanFit, "No data on selected dates", "ui-state-highlight", deferred);
        clearChartSliderAreaAndGetMemoryBack();
        return;
    }
    
  
    
  
    var allDayData = response;
    

   
    
    
    var selectedData;
    
    if (intraday) {
    
        selectedData = [['Time']];
        var frqMinutes = parseInt($("#frq").val());

        //ADD TIME COLUMN
        addTimeColumn(selectedData, frqMinutes);

   
        //GENERATE SELECTED DATA

        var totalStepsForPeriod = 0;
      
        for (var i=0;i<allDayData.length;i++){
        
            selectedData[0].push(allDayData[i]["activities-steps"][0]["dateTime"]);

            var dataAvailableLength = allDayData[i]["activities-steps-intraday"]["dataset"].length;//for current day can be <1440
            var itemsPushed = 0;

            for (j = 1; j < dataAvailableLength; j++) {//ASSUME THERE IS always DATA IN THE BEGINNING OF THE DAY, BUT NOT IN END

                totalStepsForPeriod += allDayData[i]["activities-steps-intraday"]["dataset"][j]["value"];

                if (j  % frqMinutes === 0 || j === (dataAvailableLength - 1)) {//add data every f hours or leftovers

                    
                    selectedData[itemsPushed + 1].push(totalStepsForPeriod);
                   
                    totalStepsForPeriod = 0;
                    itemsPushed++;
                }

            }


            for (var k = itemsPushed + 1; k < selectedData.length; k++) {//if day not ended AND no data from 23:59--00:00, because fitbit sends it with next day's data
                selectedData[k].push(null);
            }

        }
        

    }//end of intraday if
    else {          //INTERDAY
     
 
        selectedData = [['Date', 'Steps summary']];

        for (var j = 0; j < allDayData[0]["activities-steps"].length; j++) {
            selectedData.push([allDayData[0]["activities-steps"][j]["dateTime"], 
                                        parseInt(allDayData[0]["activities-steps"][j]["value"])]);
        }
    }
    
    
   
    if (deferred===undefined){
        setStatus(errorSpanFit, "Drawing data...", "ui-state-highlight");
        drawGraph($("#graphType").val(), "Fitbit", 'line_chart_div', "slider_div", "range", selectedData, frqMinutes);
        setStatus(errorSpanFit, "Done", "ui-state-highlight");
    }
    else{//if combined tab, return selectedData to callback
       deferred.resolve(selectedData);
    }
    
    
}



function addTimeColumn(selectedData, frqMinutes){
    
        var zeroDate = new Date(0, 0, 0, 0, 0, 0, 0);
        var hh;
        var mm;
        for (var n = 0; n < 24 * 60 / frqMinutes; n++) {

            zeroDate.setMinutes(zeroDate.getMinutes() + frqMinutes);
            hh = zeroDate.getHours() + "";
            if (hh.length < 2) {
                hh = "0" + hh;
            }
            mm = zeroDate.getMinutes() + "";
            if (mm.length < 2) {
                mm = "0" + mm;
            }

            var str = hh + ":" + mm;
            str = str === "00:00" ? "24:00" : str;

            selectedData.push([str]);
        }
}





//=========================================================================================================================================
//====================================================COMMON==========================================================
//=========================================================================================================================================

function validateDate(yearStr, monthNum, dayStr){
 
           if (dayStr!=="" || yearStr!==""){
               var day = parseInt(dayStr);
               var year = parseInt(yearStr);
               
               if (!(/^\d+$/.test(dayStr)) || !(/^\d+$/.test(yearStr))){
                   throw "Date may contain numbers only";
               }
              
               if (day<=0 || year<=0){
                   throw "Date cannot be negative";
               }
               
               var maxDays;
               switch (monthNum) {
                 case 2 :
             
                     maxDays = ((year % 4 === 0 && year % 100!==0) ||year % 400 === 0) ? 29 : 28; 
                     break;
                 case 9 : case 4 : case 6 : case 11 :
                     maxDays = 30;
                     break;
                 default :
                       maxDays=  31;
             }
              
            
            
               if (day>maxDays){
                       throw (maxDays+ " days in selected month");
               }
               else if (yearStr.length!==4){
                       throw "year must have 4 digits";
               
               }
           
           }
           else{
               day="";
               year="";
           
           }
         
         
         return {month:monthNum, day:day, year:year };

}


function clearChartSliderAreaAndGetMemoryBack(){
  
        
    if (myHighchart!==null){//HIGHCHARTS
        myHighchart.destroy();
        myHighchart = null;
    }
    else if (myGoogleChart!==null){//GOOGLE CHART
        myGoogleChart.clearChart();
        myGoogleChart = null;
    }

    if (  $("#slider_div").hasClass("ui-slider") ) {//if initialized
        $("#slider_div").slider("destroy");
        $("#range").text('');
    }
       
    
}




//=========================================================================================================================================
//===============================================DRAW GRAPHS :) ============================================================================
//=========================================================================================================================================


var myGoogleChart = null;
var myHighchart = null;
function drawGraph(chartType, chartTitle, chartId, sliderId, rangeId, selectedData, frqMinutes) {

    clearChartSliderAreaAndGetMemoryBack();

    //DRAW GRAPHS
    var chartDiv = document.getElementById(chartId);
  
    if (chartType === "LC" || chartType === "LS") {
    
       
                
        var dataT = google.visualization.arrayToDataTable(selectedData);
        var options = {
            title: chartTitle,//***
            curveType: 'none',
            legend: {position: 'bottom'},
            //explorer: { keepInBounds: true},
            chartArea: {/*height: 400, width: 'auto',*/left: 70, top: 50,  width: '90%', height: '75%'},
            //hAxis: {gridlines: {count: 4}},
            hAxis: {slantedText: true, slantedTextAngle: 90, viewWindow: {}},
            width: 'auto',
            height: 600,
            // lineWidth: 1,
            vAxis: {maxValue: 200, gridlines: {count: 10}, title:"Steps"},
            // interpolateNulls: true,
            isStacked: false
        };


        if (chartTitle==="Comparison"){//skilled programming style from Vlad!!!
        
            //put Gene on secondary axis
            var ending="";
            var seriesOptions = {};
            for (var i=1;i<selectedData[0].length;i++){
                ending = selectedData[0][i].substr(selectedData[0][i].length - 3);
                if (ending==="Fit"){
                    seriesOptions[i-1] = {targetAxisIndex: 0};
                }
                else if (ending==="Gen"){
                    seriesOptions[i-1] = {targetAxisIndex: 1};
                }
            }
            options.series = seriesOptions;
            options.vAxes =  {
              0: {
                title:'Steps',
                textStyle: {color: 'red'}
              },
              1: {
                title:'Sum of vector magnitudes',
                textStyle: {color: 'green'}
              }
            };
      

        }


        if (chartType === "LC") {
            myGoogleChart = new google.visualization.LineChart(chartDiv);//***

        }
        else if (chartType === "LS") {
            options.isStacked = true;
            myGoogleChart = new google.visualization.AreaChart(chartDiv);
        }
        myGoogleChart.draw(dataT, options);
         
    }
    
    else if (chartType==="HL" || chartType==="HS" || chartType==="HP"){
    
            var myCategories = [];
            for (var i=1;i<selectedData.length;i++){
                myCategories.push(selectedData[i][0]);
            }
            
            var allSeries = [];
            for (var k=1;k<selectedData[0].length;k++){
               
               var nextData = [];
               for (i=1;i<selectedData.length;i++){
                   nextData.push(selectedData[i][k]);
               }
              
               var nextSeries = {
                    name: selectedData[0][k],
                    data: nextData,
                    pointPlacement: 'on',
                    cropThreshold:1
                };
                allSeries.push(nextSeries);
            
            }
           // selectedData.shift();//get rid of header, so that when it's used in slider, it has the same length as series
           
    
            Highcharts.seriesTypes.line.prototype.cropShoulder = 0;
            myHighchart = new Highcharts.Chart({

                chart: {
                    renderTo: chartId,
                    polar: true,
                    type: (chartType==="HL")? 'line':'area'
                },
        
                title: {
                    text: 'Fitbit: steps',
                    x: -80
                },
        
                pane: {
                    size: '80%'
                },
        
                xAxis: {
                    categories: myCategories,
                    tickmarkPlacement: 'on',
                    lineWidth: 0,
                    tickInterval:Math.round(50/frqMinutes),
                    minRange:1,
                    type:'linear',
                    endOnTick:false,
                    startOnTick:false,
                    min:0,
                    max:selectedData.length-1//-2 is not a solution for  all my problems :(
                            
                },
        
                yAxis: {
                    gridLineInterpolation: 'polygon',
                    lineWidth: 0,
                    min: 0
                },
        
                tooltip: {
                    shared: true,
                    pointFormat: '<span style="color:{series.color}">{series.name}: <b>{point.y:,.0f}</b><br/>'
                },
        
                legend: {
                    align: 'right',
                    verticalAlign: 'top',
                    y: 70,
                    layout: 'vertical'
                },
               plotOptions: {
                    line: {
                        marker: {
                            enabled: false
                        },
                        lineWidth: 1,
                        cropThreshold:1,
                        connectEnds:false
                      
                    },
                    area:{
                        marker: {
                            enabled: false
                        },
                        lineWidth: 1,
                        //cropThreshold:1,//why it is not here? how knows? who remembers?
                        connectEnds:false,
                        stacking: (chartType==="HP")? 'percent':'normal'//percent,
                        
                    }
               }, 
        
               series: allSeries
        
            });
    }
 

    var sliderDiv = $("#" + sliderId);
    var rangeDiv = $("#" + rangeId);  
    //row indexes in datatable are 0 based


    sliderDiv.slider({//***
        range: true,
        min: 1,
        max: selectedData.length - 1,
        values: [1, selectedData.length - 1],
        stop: function (event, ui) {

            if (chartType === "LC" || chartType === "LS") {
                options.hAxis.viewWindow.min = ui.values[0] - 1;
                options.hAxis.viewWindow.max = ui.values[1];//range is [min,max)

                myGoogleChart.draw(dataT, options);
            }
            else if(chartType==="HL" || chartType==="HS" ||  chartType==="HP"){

                  myHighchart=$(chartDiv).highcharts();//lol var here --> -2 hours
                  myHighchart.xAxis[0].options.tickInterval = Math.ceil((50/frqMinutes)/((selectedData.length - 1 - 1)/(ui.values[1]-1 - ui.values[0]-1)));
                  //  alert(chart.options.xAxis[0].tickInterval);
                  myHighchart.xAxis[0].setExtremes(ui.values[0] - 1, ui.values[1]-1);  
                  myHighchart.redraw();
            }
        },
        slide: function (event, ui) {

            rangeDiv.text(selectedData[ui.values[0]][0] + " - " + selectedData[ui.values[1]][0]);//***
        }

    });

    rangeDiv.text(selectedData[sliderDiv.slider("values", 0)][0] + " - " + selectedData[sliderDiv.slider("values", 1)][0]);//***

}






//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////COMBINED GENE + FITBIT//////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


function processBoth(){

  
      //http://stackoverflow.com/questions/17559207/how-to-combine-asynchronous-calls-with-synchronous-in-javascript
      var deferred1 = $.Deferred();
      var deferred2 = $.Deferred();
      
      $.when(deferred1, deferred2).done(function(dataFitbit, dataGene) {
          
          //add Fit/Gene to distinguish series' names
          for (var i=1;i<dataGene[0].length;i++){
               dataGene[0][i]+="Gen";
          }
          
          for (i=1;i<dataFitbit[0].length;i++){
               dataFitbit[0][i]+="Fit";
          }
          
          //add data together
          for (i=1; i<dataGene[0].length;i++){
            
              for (var j=0;j<dataFitbit.length;j++){
                  dataFitbit[j].push(dataGene[j][i]);
              }
          
          }
          
          
          drawGraph("LC", "Comparison", 'line_chart_divCombined', "slider_divCombined", "rangeCombined", dataFitbit);//chart type could be $("#graphType").val(), but only line makes sense
          setStatus($("#errorSpanFit"), "Done", "ui-state-highlight");
          setStatus($("#errorSpanGENE"), "Done", "ui-state-highlight");
          
      });
      
      
      serverProcessForm(deferred1);
      readSingleFile(deferred2);

}


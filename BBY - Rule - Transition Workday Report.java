<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1733343487287" id="0ad8298d938c114181939352a5375314" language="beanshell" modified="1737415202909" name="BBY - Rule - Transition Workday Report" significantModified="1737415202909">
  <Description>
    Reading details of list of terminated users shared by Workday team.
  </Description>
  <Signature returnType="String">
    <Inputs>
      <Argument name="log">
        <Description>
              The log object associated with the SailPointContext.
            </Description>
      </Argument>
      <Argument name="context" type="sailpoint.api.SailPointContext">
        <Description>
          A sailpoint.api.SailPointContext object that can be used to query the database if necessary.
        </Description>
      </Argument>
      <Argument name="item">
        <Description>
          The sailpoint.object.Notifiable to escalate.
        </Description>
      </Argument>
      <Argument name="identity" type="Identity">
        <Description>
          The sailpoint.object.Notifiable to escalate.
        </Description>
      </Argument>
    </Inputs>
  </Signature>
  <Source>
  
  import sailpoint.api.Aggregator;
  import sailpoint.api.SailPointContext;
  import sailpoint.api.SailPointFactory;
  
  import sailpoint.object.Application;
  import sailpoint.object.Attributes;
  import sailpoint.object.ResourceObject;
  import sailpoint.object.Schema;
  import sailpoint.object.TaskResult;
  import sailpoint.object.Custom;
  import sailpoint.object.EmailOptions;
  import sailpoint.object.EmailTemplate;
  import sailpoint.object.EmailFileAttachment;
  import sailpoint.object.Filter;
  import sailpoint.object.Identity;
  import sailpoint.object.LiveReport;
  import sailpoint.object.QueryOptions;
  import sailpoint.object.Sort;
  
  import java.sql.Connection;
  import java.sql.Date;
  import java.sql.DriverManager;
  import java.sql.PreparedStatement;
  import java.sql.ResultSet;
  import java.sql.SQLException;
  
  import sailpoint.task.TaskMonitor;
  import sailpoint.tools.Message;
  import sailpoint.tools.Util;
	  
  import sailpoint.plugin.PluginBaseHelper;
  import sailpoint.reporting.datasource.JavaDataSource;
  import sailpoint.task.Monitor;
  import sailpoint.tools.GeneralException;
  import org.apache.log4j.Logger;
  
  import java.text.SimpleDateFormat;
	import java.util.Calendar;
  import java.util.Date;
  
  PreparedStatement InsertStatement = null;
  PreparedStatement SelectStatement = null;
  PreparedStatement SelectStatementCount = null;
  PreparedStatement SelectStatementForReport = null;
  Connection dbConnection = null;
  ResultSet resultSet = null;
  ResultSet resultSetCount = null;
  
  String INSERT_STATEMENT = "INSERT INTO DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT"+
    "(USER_NUMBER,USER_NAME,EMPLOYEE_ID,DISPLAY_NAME,WORKER_TYPE,TERMINATION_DATE,USER_STATUS)"+
    "VALUES (?, ?, ?, ?, ?, ?, ?)";
  String SELECT_STATEMENT_FOR_REPORT = "SELECT * FROM DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT";
  String SELECT_STATEMENT_COUNT = "SELECT MAX(USER_NUMBER) FROM DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT";
  String[] VALID_COLUMN_HEADERS = { "USER NUMBER","USER NAME", "EMPLOYEE ID", "NAME", "WORKER TYPE", "TERMINATION DATE","LATEST TERM EVENT COMPLETION DATE","USER STATUS"};
  String[] VALID_COLUMN_HEADERS_FOR_REPORT = { "USER NUMBER","USER NAME", "EMPLOYEE ID", "NAME", "WORKER TYPE", "TERMINATION DATE","USER STATUS"};
  
  List indexColumns = new ArrayList();
  List indexColumns1 = new ArrayList();  
  File csvFileTermDate = null;
  File csvFileTermDateRange = null;
  Map csvFileTermDateMap = new HashMap();
  Map csvFileTermDateRangeMap = new HashMap();
  Map finalMap = new HashMap();
  
  int lineCount = 0;
  int lineCount1 = 0;
  String line = null;
  String line1 = null;
  String existingUser = null;
  
  SailPointContext context = SailPointFactory.getCurrentContext();
  Identity identity;
  String filePathTermDate = "/opt/tomcat/sp/BBY_HR_Term_Date_(for_SailPoint_Auditing).csv";
  String filePathTermDateRange = "/opt/tomcat/sp/BBY_HR_Workers_Termed_in_Date_Range_and_Eff_Date_Less_than_Completion_Date_(for_SailPoint_Auditing).csv";
  
  public Connection getDBConnection() throws SQLException, GeneralException {
    if (dbConnection != null @and !dbConnection.isClosed()) {  
			return dbConnection;
    }
		dbConnection = PluginBaseHelper.getConnection();
    dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    if (dbConnection == null) {
			throw new GeneralException("cannot get connection");
    }
    return dbConnection;
	}

  public void closeDBConnection(Connection dbConnection) {
    try{
      if (dbConnection != null @and !dbConnection.isClosed())
        dbConnection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public void generateReport(PreparedStatement SelectStatement){
    System.out.println(" --------------- Inside the Generate Report Section --------------");
    
    List indexColumns = Arrays.asList(VALID_COLUMN_HEADERS_FOR_REPORT);
    
    String csvData = Util.listToCsv(indexColumns);
    String csvData1 = null;
    StringBuilder htmlTable = new StringBuilder();
    
    htmlTable.append("&lt;table border='1' style='border-collapse: collapse; width: 100%;'>&lt;tr>");
    for (int i = 0; i &lt; indexColumns.size(); i++)
    {
      htmlTable.append("&lt;th style='border: 1px solid black; padding: 8px; background-color: #0046BE; color: white; width: 25px; height: 25px; white-space: nowrap;'>").append(indexColumns.get(i)).append("&lt;/th>");
    }
    htmlTable.append("&lt;/tr>");
    resultSet = SelectStatement.executeQuery();
    while (resultSet.next()) {
      System.out.println(" ------------   inside sendEmailReport   ------------   ");
      List resourceObjects = new ArrayList();
      htmlTable.append("&lt;tr>");
      for (int i = 0; i &lt; indexColumns.size(); i++)
      {
        if(i==0){
          System.out.println(" ------------   Taking the 1st Column of the Row   ------------   ");
          htmlTable.append("&lt;td style='border: 1px solid black; padding: 8px; white-space: nowrap;'>").append(resultSet.getInt(i+1)).append("&lt;/td>");
        }
        else{
          System.out.println(" ------------   Taking the other column of the Row   ------------   ");
        	htmlTable.append("&lt;td style='border: 1px solid black; padding: 8px; white-space: nowrap;'>").append(resultSet.getString(i+1)).append("&lt;/td>");
        }
        resourceObjects.add(resultSet.getString(i+1));
      }
      htmlTable.append("&lt;/tr>");
      System.out.println("resourceObjects --------------- >>>>>>>        "+resourceObjects);
      csvData1 = Util.listToCsv(resourceObjects);
      System.out.println("csvData1 before ending loop --------------- >>>>>>>        "+csvData1);
      csvData = csvData + "\n" + csvData1;
      System.out.println("csvData before ending loop --------------- >>>>>>>        "+csvData);
    }
    System.out.println("Final csvData ---------------   " + csvData);
    htmlTable.append("&lt;/table>");
		System.out.println(" ---------------Final HTML Table --------------          "+htmlTable.toString());
    sendEmailReport(context, htmlTable.toString(), csvData);
    resultSet.close();
    SelectStatement.close();
  }
  
  public void sendEmailReport(SailPointContext context, String htmlTable, String csvData){
    try
    {
      System.out.println("Inside mail notification method");
      System.out.println("Final csvData inside sendEmailReport ---------------   " + csvData);
      
      EmailTemplate template = context.getObjectByName(EmailTemplate.class, "Daily Terminated Users Report from Workday before Discrepancy Check");
      System.out.println("Template is ------------->>>>>>>>>>>>>>>"   + template);
      if (template == null){
        throw new Exception("Email template not found: " + strTemplate);
      }
      else{
        System.out.println("Inside template method");
        EmailOptions options = new EmailOptions();
        options.setTo("supriyo.dey@bestbuy.com");

        Map  args = new HashMap();
        args.put("htmlTable", htmlTable);
        System.out.println("Map is ::"+ args);
        options.setVariables(args);
        
        EmailFileAttachment attachment = new EmailFileAttachment("Daily_Terminated_Users_Report_from_Workday_before_Discrepancy_Check.csv", EmailFileAttachment.MimeType.MIME_CSV, csvData.toString().getBytes("UTF-8"));
				options.addAttachment(attachment);
        
        context.sendEmailNotification(template, options);  
        System.out.println("Email has been sent finally");
      }
    }
    catch(Exception e)
    {
      System.out.println("Exception Occured while sending mail"+ e);
    }
  }
  
  try{
    /*****************************************************************************
      ************************** Validation of CSV files ***************************
      *******************************************************************************/
    
    System.out.println("************* Validation of CSV files -- START************");
    csvFileTermDate = new File(filePathTermDate);
    csvFileTermDateRange = new File(filePathTermDateRange);
    if ((!csvFileTermDate.exists())) {
       System.out.println("Unable to find the bundle csv file: " + filePathTermDate);
       return;
    }
    csvFileTermDateRange = new File(filePathTermDateRange);
    if ((!csvFileTermDateRange.exists())) {
       System.out.println("Unable to find the bundle csv file: " + filePathTermDateRange);
       return;
    }
    System.out.println("************* Validation of CSV files -- END************");
    
    dbConnection = getDBConnection();
    if (dbConnection != null) {
      System.out.println("Successfully connected to database!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      SelectStatementForReport = dbConnection.prepareStatement(SELECT_STATEMENT_FOR_REPORT);
      
      /*****************************************************************************
      ************************** Setting data 3 days ago ***************************
      *******************************************************************************/
      
      System.out.println("********* Setting data 3 days ago - START ************");
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -3);
      Date threeDaysBack = cal.getTime();
      System.out.println("threeDaysBack    ---- "+threeDaysBack);
      System.out.println("********* Setting data 3 days ago - END ************");
      
      /**********************************************************************************************************************
      ************************** Reading data from file BBY_HR_Term_Date_(for_SailPoint_Auditing) ***************************
      ***********************************************************************************************************************/
      
      System.out.println("********* Reading data from file BBY_HR_Term_Date_(for_SailPoint_Auditing) - START ************");
      BufferedReader br = new BufferedReader(new FileReader(filePathTermDate));
      System.out.println("filePathTermDate is ...       "+filePathTermDate);
      String userValueTermDate;
      
      while ((line = br.readLine()) != null)
      {
        List filePathTermDateList = new ArrayList();
        System.out.println("line:::::::::          " + line);
        if (lineCount++ == 0)
        {
          indexColumns = Util.csvToList(line);
          System.out.println("indexColumns:::::::::      " + indexColumns);
        }
        else{
          List record = Util.csvToList(line);
          System.out.println("record:::::::     " + indexColumns);
          for (int i = 0; i &lt; indexColumns.size(); i++)
          {
            String attributeName = indexColumns.get(i);
            String attributeValue = record.get(i);
            System.out.println("attributeName ------  " + attributeName + "     attributeValue -------    " + attributeValue);
            
            
            if(attributeName.toLowerCase().equalsIgnoreCase("USER NAME")){
              System.out.println("***** Adding "+attributeValue+" to the csvFileTermDateMap ****** ");
              userValueTermDate = attributeValue;
              System.out.println("userValueTermDate is ::::::::::::::::::     "+userValueTermDate);
            }
            
            for (int i = 0; i &lt; VALID_COLUMN_HEADERS.length; i++){
              if(VALID_COLUMN_HEADERS[i].toLowerCase().equalsIgnoreCase(attributeName)){
                System.out.println(attributeName+ " matched with the value from VALID_COLUMN_HEADERS array *******");
                filePathTermDateList.add(attributeValue);
              }
            }
            System.out.println("filePathTermDateList is ::::::::::::::::::     "+filePathTermDateList);
          }
          csvFileTermDateMap.put(userValueTermDate,filePathTermDateList);
        }
      }
      
      System.out.println("csvFileTermDateMap ---------->>>>>>>>      "+csvFileTermDateMap);
      System.out.println("********* Reading data from file BBY_HR_Term_Date_(for_SailPoint_Auditing) - END ************");
      
      /**********************************************************************************************************************
      ******************* BBY_HR_Workers_Termed_in_Date_Range_and_Eff_Date_Less_than_Completion_Date ************************
      ***********************************************************************************************************************/
      
      System.out.println("********* BBY_HR_Workers_Termed_in_Date_Range_and_Eff_Date_Less_than_Completion_Date - START ************");
      BufferedReader br = new BufferedReader(new FileReader(filePathTermDateRange));
      System.out.println("filePathTermDate is ...       "+filePathTermDateRange);
      String userValueTermDateRange;
      
      while ((line1 = br.readLine()) != null)
      {
        List filePathTermDateRangeList = new ArrayList();
        System.out.println("line1:::::::::          " + line1);
        if (lineCount1++ == 0)
        {
          indexColumns1 = Util.csvToList(line1);
          System.out.println("indexColumns1:::::::::      " + indexColumns1);
        }
        else{
          List record = Util.csvToList(line1);
          boolean withinTwoDays = true;
          System.out.println("record:::::::     " + indexColumns1);
          for (int i = 0; i &lt; indexColumns1.size(); i++)
          {
            String attributeName = indexColumns1.get(i);
            String attributeValue = record.get(i);
            System.out.println("attributeName ------  " + attributeName + "     attributeValue -------    " + attributeValue);
            
            if(attributeName.toLowerCase().equalsIgnoreCase("USER NAME")){
              System.out.println("***** Adding "+attributeValue+" to the csvFileTermDateRangeMap ****** ");
              userValueTermDateRange = attributeValue;
              System.out.println("userValueTermDateRange is ::::::::::::::::::     "+userValueTermDateRange);
            }
            
            if(attributeName.toLowerCase().equalsIgnoreCase("LATEST TERM EVENT COMPLETION DATE")){
              System.out.println(":::::Inside LATEST TERM EVENT COMPLETION DATE loop:::::::::");
              Date termDate = simpleDateFormat.parse(attributeValue);
              if(!(termDate.after(threeDaysBack))){
                System.out.println(attributeValue+" is before 2days from today");
                withinTwoDays = false;
              }
            }
            
            for (int i = 0; i &lt; VALID_COLUMN_HEADERS.length; i++){
              if(VALID_COLUMN_HEADERS[i].toLowerCase().equalsIgnoreCase(attributeName)){
                System.out.println(attributeName+ " matched with the value from VALID_COLUMN_HEADERS array *******");
                filePathTermDateRangeList.add(attributeValue);
              }
            }
            System.out.println("filePathTermDateRangeList is ::::::::::::::::::     "+filePathTermDateRangeList);
          }
          
          System.out.println("withinTwoDays ---------->  "+withinTwoDays);
          if(withinTwoDays){
            System.out.println("filePathTermDateRangeList is added to csvFileTermDateRangeMap for the user ---------->  "+userValueTermDateRange);
          	csvFileTermDateRangeMap.put(userValueTermDateRange,filePathTermDateRangeList);
          }
        }
      }
      
      System.out.println("csvFileTermDateRangeMap ---------->>>>>>>>      "+csvFileTermDateRangeMap);
      System.out.println("********* BBY_HR_Workers_Termed_in_Date_Range_and_Eff_Date_Less_than_Completion_Date - END ************");
      
      /*********************************************************************************************************************
      ******************* Validate duplicate users and prepare final Map for insertion in DB *******************************
      ***********************************************************************************************************************/
      
      System.out.println("********* Validate duplicate users and prepare final Map for insertion in DB ************");
      finalMap.putAll(csvFileTermDateMap);
      finalMap.putAll(csvFileTermDateRangeMap);
      System.out.println("finalMap --------->>>>>   "+finalMap);
      System.out.println("********* Validate duplicate users and prepare final Map for insertion in DB - END ************");
      
      /*********************************************************************************************************************
      ******************* Insertion in DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT table **********************************
      ***********************************************************************************************************************/
      
      System.out.println("********* insertion in DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT table - START ************");
      for(Map.Entry entry : finalMap.entrySet()) {
        SelectStatementCount = dbConnection.prepareStatement(SELECT_STATEMENT_COUNT);
        resultSetCount = SelectStatementCount.executeQuery();
        int maxValue;
        while (resultSetCount.next()) {
          maxValue = resultSetCount.getInt(1);
          System.out.println("Printing maxValue - Before:::::   "+maxValue);
          maxValue = maxValue + 1;
        }
        System.out.println("Printing maxValue - After:::::   "+maxValue);
        
        InsertStatement = dbConnection.prepareStatement(INSERT_STATEMENT);
        String attributekey = entry.getKey(); 
				List values = entry.getValue();
        
        int index = values.size();
        index = index + 2;
        System.out.println("   ------------------- index is--------------------     "+index);
        System.out.println("   ------------------- Values is--------------------     "+values);
        
        for (int i = 0; i &lt; index; i++){
          int position = i+1;
          if(i == 0){
            System.out.println("  -------Adding the User Count------- for the position "+position+ " --- the value is -----"+maxValue);
            InsertStatement.setInt(position,maxValue);
          }
          else if(i == 6){
            identity = context.getObjectByName(Identity.class,attributekey);
            if(identity != null){
              System.out.println("identity --->>>>   " + identity.toString());
              String currentStatus = identity.getStringAttribute("inactive");
              if(currentStatus.toLowerCase().equalsIgnoreCase("true")){
                System.out.println("identity --->>>>   " + attributekey + "  is Inactive already in IIQ");
                currentStatus = "Inactive";
              }
              else{
                System.out.println("identity --->>>>   " + attributekey + "  is still Active in IIQ");
                currentStatus = "Active";
              }
              InsertStatement.setString(position,currentStatus);
            }
            else{
              System.out.println("User " + attributekey + " has not found in IIQ");
              InsertStatement.setString(position,"User Not Found");
            }
          }
          else{
            InsertStatement.setString(position,values.get(i-1));
            System.out.println("  -------Adding the value from columnMap list in DB ----------- for the position   "+position+ " the value is ----" + values.get(i-1));
          }
        }
        resultSetCount.close();
        System.out.println("Final InsertStatement Result2 --->>>>   " + InsertStatement.getParameterMetaData().getParameterCount());
        dbConnection.setAutoCommit(false);
        int rowcount = InsertStatement.executeUpdate();
        System.out.println("Number of rows updated for sql " + INSERT_STATEMENT + " ------- " + rowcount);
        dbConnection.commit();
        InsertStatement.close();
      }
      
      System.out.println("********* insertion in DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT table - END ************");
      
      /*********************************************************************************************************************
      ************************************ Generate Report and Send Email **************************************************
      ***********************************************************************************************************************/
      
      System.out.println("************** Generate Report and Send Email  ---  START ****************");
      generateReport(SelectStatementForReport);
      System.out.println("************** Generate Report and Send Email  ---  END ****************");
      
    }
  }
  catch (Exception exception) {
      taskResult.addMessage(new Message(Message.Type.Error, exception));
      System.out.println("Error occurred while processing orgs: " + exception.getMessage());    
      return false;
  }
  finally{
    try {
      if (dbConnection != null @and !dbConnection.isClosed()) {
        closeDBConnection(dbConnection);
        System.out.println("Database connection closed.");
      }
      if (InsertStatement != null) {
        InsertStatement.close();
      } 
      if (SelectStatement != null) {
        SelectStatement.close();
      } 
    } catch (SQLException e) {
      System.out.println("Error when closing connection: " + e.getMessage());
    }

    System.out.println("End of Organization Parsing Rule");
  }
 return "Success";

</Source>
</Rule>

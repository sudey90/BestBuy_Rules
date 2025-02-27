<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1735172088508" id="0ad8290c93e31acd81940050e2bc7481" language="beanshell" modified="1737415249520" name="BBY - Rule - Terminated Users Discrepancy Fix" significantModified="1737415249520">
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
  import sailpoint.api.Identitizer;
  import sailpoint.api.TaskManager;
  
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
  import sailpoint.object.TaskResult;
  import sailpoint.object.Link;
  
  import sailpoint.connector.Connector;
  
  import java.sql.Connection;
  import java.sql.Date;
  import java.sql.DriverManager;
  import java.sql.PreparedStatement;
  import java.sql.Statement;
  import java.sql.ResultSet;
  import java.sql.ResultSetMetaData;
  import java.sql.SQLException;
  
  import sailpoint.task.TaskMonitor;
  import sailpoint.task.Monitor;
  import sailpoint.tools.Message;
  import sailpoint.tools.Util;
	import sailpoint.tools.GeneralException;
  
  import sailpoint.plugin.PluginBaseHelper;
  import sailpoint.reporting.datasource.JavaDataSource;
  
  import org.apache.log4j.Logger;
  import java.text.SimpleDateFormat;
	import java.util.Calendar;
  import java.util.Date;
  import java.util.*;
  
  PreparedStatement InsertStatement = null;
  PreparedStatement SelectStatement = null;
  PreparedStatement SelectStatementForReport = null;
  PreparedStatement SelectStatementCount = null;
  
  Statement SelectRefreshStatement = null;
  Connection dbConnection = null;
  //Connection connection = null;
  ResultSet resultSet = null;
  ResultSet resultSetCount = null;
  ResultSetMetaData resultSetMetaData = null;
  
  Map columnMap = new HashMap();
  Map activeUserAfterWDAMap = new HashMap();
  Map refreshCompletedMap = new HashMap();
  
  List activeUserAfterWDAList = new ArrayList();
  List finalList = new ArrayList();
  List termedUserFromTask1 = new ArrayList();
  List termedUserAfterWDA = new ArrayList();
  
  String INSERT_STATEMENT = "INSERT INTO TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT"+
    "(USER_NUMBER, USER_NAME,DISPLAY_NAME,USER_STATUS,USER_STATUS_AFTER_WDDA,WDA_REQUIRED,WDA_COMPLETED,WD_STATUS,TERM_COMPLETE,REHIRED)"+
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  String SELECT_STATEMENT = "SELECT * FROM DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT";
  String SELECT_STATEMENT_FOR_REPORT = "SELECT * FROM TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT";
  String SELECT_STATEMENT_COUNT = "SELECT MAX(USER_NUMBER) FROM TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT";
  //String[] VALID_COLUMN_HEADERS = { "USER_NAME", "USER_STATUS", "USER_STATUS_AFTER_WDDA", "WDA_REQUIRED","WDA_COMPLETED","WD_STATUS","TERM_COMPLETE","REFRESH_COMPLETED","USER_STATUS_AFTER_WDA_REFRESH"};
  String[] VALID_COLUMN_HEADERS = { "USER_NUMBER" , "USER_NAME", "DISPLAY_NAME", "USER_STATUS", "USER_STATUS_AFTER_WDDA", "WDA_REQUIRED","WDA_COMPLETED","WD_STATUS","TERM_COMPLETE","REHIRED"};
  String APPNAME = "Workday";
  
  SailPointContext context = SailPointFactory.getCurrentContext();
  Identity identity;
  Date refreshStartTime;
  Date refreshEndTime;
  Date lastRefreshTime;
  
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
    
    List indexColumns = Arrays.asList(VALID_COLUMN_HEADERS);
    
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
        htmlTable.append("&lt;td style='border: 1px solid black; padding: 8px; white-space: nowrap;'>").append(resultSet.getString(i+1)).append("&lt;/td>");
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
      
      EmailTemplate template = context.getObjectByName(EmailTemplate.class, "Termination Discrepancy Report Between Workday SailPoint");
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
        
        EmailFileAttachment attachment = new EmailFileAttachment("Termination_Discrepancy_Report_Between_Workday_SailPoint.csv", EmailFileAttachment.MimeType.MIME_CSV, csvData.toString().getBytes("UTF-8"));
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
  
  /********************************************************************************************************
   ************************** Workday Aggregation Method implementation *********************************
   ***************************************************************************************************/
  
  public void workdayAggregation(List activeUserAfterWDAList, List finalList){
    String nativeIdUser=null;
    String value=null;
    for (int i = 0; i &lt; activeUserAfterWDAList.size(); i++){
      System.out.println("     ----------Workday Aggregation:Inside activeUserAfterWDAList loop--------     ");
      List record = new ArrayList();
      value=activeUserAfterWDAList.get(i);
      identity = context.getObjectByName(Identity.class,value);
      nativeIdUser=identity.getStringAttribute("employeeNumber");
      System.out.println(APPNAME+"    Aggregation : linking user "+nativeIdUser);
      ResourceObject myObj = aggregateSingleResource(APPNAME, nativeIdUser, "account");
      //record.add(nativeIdUser);
      if(myObj!=null) {
        System.out.println(APPNAME+" Aggregation : Successfully linked user   "+nativeIdUser);
        record.add("Successfully Completed");
      }else{
        System.out.println(APPNAME+" Aggregation : Linking failed for user   "+nativeIdUser);
        record.add("Unsuccessful");
      }
      activeUserAfterWDAMap.put(value,record);
      //finalList.add(VALID_COLUMN_HEADERS[i]);
    }
    System.out.println("   ------------------- activeUserAfterWDAMap --------------------     "+activeUserAfterWDAMap);
  }
  
  ResourceObject aggregateSingleResource(String appName, String nativeId, String aggType) {
    System.out.println("Start aggregateSingleResource");
    ResourceObject rObj = null;
    Application appObject = context.getObjectByName(Application.class, appName);
    String appConnName = appObject.getConnector();
    Connector appConnector = sailpoint.connector.ConnectorFactory.getConnector(appObject, null);
    try{
      rObj = appConnector.getObject("account", nativeId, null);
    }catch(Exception ex){
      System.out.println("aggregateSingleResource: User not found in Workday "+nativeId);
    }

    if(rObj!=null) {
      Attributes argMap = new Attributes();
      argMap.put("aggregationType", aggType);
      argMap.put("applications", appName);
      argMap.put("descriptionAttribute","description");
      argMap.put("descriptionLocale", "en_US");
      argMap.put("noOptimizeReaggregation", "true");
      Aggregator agg = new Aggregator(context, argMap);
      TaskResult result = agg.aggregate(appObject,rObj);
      //System.out.println("TaskResult:\n" + result.toXml());
    }
    System.out.println("End aggregateSingleResource()");
    return rObj;
  }
  
  /********************************************************************************************************
   ************************** Verification of user's latest status in Workday ******************************
   ***************************************************************************************************/
  
  public boolean verifyWorkdayDisabled(Identity identity){
    boolean flag;
    String APPLICATION_NAME;  
    List identityLinkslist = identity.getLinks();
    if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty()) {
      for(Link eachLink : identityLinkslist){
        if(null != eachLink &amp;&amp; null != eachLink.getApplication()) {
          APPLICATION_NAME = eachLink.getApplication().getName();
          if(APPLICATION_NAME.toLowerCase().equalsIgnoreCase(APPNAME) @and eachLink.isDisabled()){
            flag = true;
          }
        }
      }
    }
    return flag;
  }
  
  try{
    System.out.println("**************DB connection started****************");  
    dbConnection = getDBConnection();
    System.out.println("***********DB connection done successfully****************");
    
    int recordCount;
    System.out.println("*********dbConnection *************   "+dbConnection);
    if (dbConnection != null) {
      System.out.println("Successfully connected to database!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      System.out.println("SELECT_STATEMENT is  ---------------------->    "+SELECT_STATEMENT);
      
      SelectStatement = dbConnection.prepareStatement(SELECT_STATEMENT);
      SelectStatementForReport = dbConnection.prepareStatement(SELECT_STATEMENT_FOR_REPORT);

			resultSet = SelectStatement.executeQuery();
    	resultSetMetaData = resultSet.getMetaData();
      System.out.println("Column count ------> "+ resultSetMetaData.getColumnCount());
      System.out.println("Valid Header count ------> "+ VALID_COLUMN_HEADERS.length);  
      
      while (resultSet.next()) {
       List record = new ArrayList();
        
      /***************************************************************************************************************************************************
      ************************** Setting the records bringing from DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT ******************************************
      ****************************************************************************************************************************************************/
        
        System.out.println("************** Setting the records bringing from DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT - START****************");
        for (int i = 0; i &lt; VALID_COLUMN_HEADERS.length; i++){
          for (int j = 0; j &lt; resultSetMetaData.getColumnCount(); j++){
            String columnName = resultSetMetaData.getColumnName(j+1);
            if(VALID_COLUMN_HEADERS[i].toLowerCase().equalsIgnoreCase(columnName)){
              record.add(resultSet.getString(columnName));
              finalList.add(VALID_COLUMN_HEADERS[i]);
            }
          }
        }
        System.out.println("************** Setting the records bringing from DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT - END****************");
        
      /*************************************************************************************************
      ************************** Reversing the finalList LIST ******************************************
      ***************************************************************************************************/
        
        System.out.println("************** Reversing the finalList LIST - START****************");
        Set set = new HashSet(finalList);
        finalList.clear();
        finalList.addAll(set);
        Collections.reverse(finalList);        
        System.out.println("************** Reversing the finalList LIST - END****************");
        
        /*************************************************************************************************
      	************************** Validating user's status in SailPoint *********************************
      	***************************************************************************************************/
        
        System.out.println("************** Validating user's status in SailPoint - START ****************");
        String USER_NAME = resultSet.getString("USER_NAME");
        String USER_STATUS = resultSet.getString("USER_STATUS");
        identity = context.getObjectByName(Identity.class,USER_NAME);
        String statusafterWDA = identity.getStringAttribute("inactive");
        String WDA_REF_REQUIRED;
        System.out.println("identity ----------------------->>>>   " + identity.getStringAttribute("preferredName") + " Form Row#  "+resultSet.getRow());
        
        System.out.println("************** Validating user's status in SailPoint - START ****************");
        if(USER_STATUS.toLowerCase().equalsIgnoreCase("Terminated")){
          System.out.println("**** User:::: "+USER_NAME+" already terminated in SailPoint before Workday Delta Aggregation. No need to run further Workday Aggregation ********** ");
          termedUserFromTask1.add(USER_NAME);
          statusafterWDA = "Terminated before WDA";
          WDA_REF_REQUIRED = "No";
        }
        else{
          System.out.println("*** User:::: "+USER_NAME+" was Active in SailPoint before Workday Delta Aggregation. So need to check further status of user after Workday Delta Aggregation ********* ");
          boolean isWorkdayDisabled = verifyWorkdayDisabled(identity);
          
          if(statusafterWDA.toLowerCase().equalsIgnoreCase("true") @and isWorkdayDisabled){
            System.out.println("*** User:::: "+USER_NAME+" got terminated during Workday Delta Aggregation. No need to run further Workday Aggregation ******* ");
            statusafterWDA = "Terminated during WDA ";
            WDA_REF_REQUIRED = "No";
            // Send this list to 3rd Task
            termedUserAfterWDA.add(USER_NAME);
          }
          else{
            System.out.println("*** User:::: "+USER_NAME+" is still Active in SailPoint. So need to run further Workday Aggregation ******* ");
            statusafterWDA = "Active";
            activeUserAfterWDAList.add(USER_NAME);
            //activeUserAfterWDAMap.put(USER_NAME,activeUserAfterWDAList);
            WDA_REF_REQUIRED = "Yes";
          }
          record.add(statusafterWDA);
          record.add(WDA_REF_REQUIRED);
          columnMap.put(USER_NAME,record);
        }
         System.out.println("************** Validating user's status in SailPoint - END ****************");
      }
      
      /*************************************************************************************************
      	************************** Updating FinanList Value *********************************
      	***************************************************************************************************/
      
      System.out.println("************** Updating FinanList Value - START ****************");
      for(Map.Entry entry : columnMap.entrySet()) {
        List list = entry.getValue();
        recordCount = list.size();
      }
      System.out.println("recordCount--------------->      "+recordCount);
      if(recordCount &lt; VALID_COLUMN_HEADERS.length @and recordCount > finalList.size()){
        for (int i = 0; i &lt; recordCount; i++){
          if(i == finalList.size()){
            System.out.println("******* Adding value to the finalList list **********");
            finalList.add(VALID_COLUMN_HEADERS[i+1]);
          }
        }
      }
      System.out.println("************** Updating FinanList Value - END ****************");
      
      System.out.println(" ------------------- Final Map --------------------     "+columnMap);
      System.out.println(" ------------------- activeUserAfterWDAList --------------------     "+activeUserAfterWDAList);
      System.out.println(" ------------------- termedUserFromTask1 --------------------     "+termedUserFromTask1);
      System.out.println(" ------------------- termedUserAfterWDA --------------------------        "+termedUserAfterWDA);
      System.out.println(" ------------------- Finl List --------------------------        "+finalList);
      
      /*************************************************************************************************************************************************************
      	************************** Bulk Single Account Workday Aggregation for the Active User and add to activeUserAfterWDAMap MAP*********************************
      	************************************************************************************************************************************************************/
      
      System.out.println("************** Bulk Single Account Workday Aggregation for the Active User and add to activeUserAfterWDAMap MAP ---  START ****************");
      if (activeUserAfterWDAList != null){
      	workdayAggregation(activeUserAfterWDAList,finalList);
        
        List values = new ArrayList();
        for(Map.Entry entry : activeUserAfterWDAMap.entrySet()) {
          String attributekey = entry.getKey();
					values = entry.getValue();
          System.out.println("activeUserAfterWDAMap values --->>>>   " + values);
          identity = context.getObjectByName(Identity.class,attributekey);
        	System.out.println("identity inside activeUserAfterWDAList Loop--->>>>   " + identity.getStringAttribute("preferredName"));
          for (int i = 0; i &lt; values.size(); i++){
            String identityValue = values.get(i);
            System.out.println("identityValue of user --->>>>   " + attributekey + "is --->>>" + identityValue);
            if(identityValue.toLowerCase().equalsIgnoreCase("Successfully Completed")){
              String statusafterWDA = identity.getStringAttribute("inactive");
              System.out.println("Checking the user -------->>>>  " + attributekey);
              
              boolean isWorkdayDisabled = verifyWorkdayDisabled(identity);
              
              if(statusafterWDA.toLowerCase().equalsIgnoreCase("true") @and isWorkdayDisabled){
                System.out.println("User is Inactive::: Adding to Inactive list  " + attributekey);
                values.add("Terminated");
                values.add("Complete");
                values.add("No");
              }
              else{
                System.out.println("User is Active::: Adding to Active list  " + attributekey);
                values.add("Active");
                values.add("Incomplete");
                values.add("Yes");
              }
              
            }
            if(identityValue.toLowerCase().equalsIgnoreCase("Unsuccessful")){
              System.out.println("User's aggregation was not successful::::  " + attributekey);
              values.add("Aggregation couldnt complete");
              values.add("TBD");
              values.add("TBD");
            }
          }
          activeUserAfterWDAMap.put(attributekey,values);
        }
        System.out.println("   ------------------- values.size()--------------------     "+values.size());
        if(finalList.size() &lt; VALID_COLUMN_HEADERS.length){
          int valueSize = values.size();
          int finalListSize = finalList.size();
          for (int i = 0; i &lt; valueSize; i++){
            finalList.add(VALID_COLUMN_HEADERS[finalListSize+i+1]);
          }
        }
        System.out.println("   ------------------- Finl List After final determination   --------------------------        "+finalList);
        System.out.println("   ------------------- activeUserAfterWDAMap at the last--------------------     "+activeUserAfterWDAMap);
      }
      System.out.println("************** Bulk Single Account Workday Aggregation for the Active User and add to activeUserAfterWDAMap MAP ---  END ****************");
      
      
      /*************************************************************************************************************************************************************
      	************************** Inserting data into TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT table **********************************************
      	************************************************************************************************************************************************************/
      
      System.out.println("************** Inserting data into TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT table  ---  START ****************");
      
      for(Map.Entry entry : columnMap.entrySet()) {
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
        index = index + 1;
        System.out.println("   ------------------- index is--------------------     "+index);
        System.out.println("   ------------------- Values is--------------------     "+values);
        for(Map.Entry entry1 : activeUserAfterWDAMap.entrySet()) {
          String attributekey1 = entry1.getKey(); 
          System.out.println("   ------------------- attributekey1 is--------------------     "+attributekey1);
          List values1 = entry1.getValue();
          System.out.println("   ------------------- values1.size() is--------------------     "+values1.size());
          
          if(attributekey1.toLowerCase().equalsIgnoreCase(attributekey)){
            for (int i = 0; i &lt; index; i++){
              int position = i+1;
              if(i == 0){
                InsertStatement.setInt(position,maxValue);
                System.out.println("  -------Adding the User Count------- for the position "+position+ " --- the value is -----"+maxValue);
              }
              else{
              	InsertStatement.setString(position,values.get(i-1));
                System.out.println("  -------Adding the value from columnMap list in DB ----------- for the position   "+position+ " the value is ----" + values.get(i-1));
              }
            }
            for (int j = 0; j &lt; values1.size(); j++){
              int position = index+j+1;
              InsertStatement.setString(position,values1.get(j));
              System.out.println("  -------Adding the value from activeUserAfterWDAMap list in DB ----------- for the position  "+position+ " the value is ----" +values1.get(j));
            }
          }
          
          System.out.println("  ************* Here ***********");
        }
        resultSetCount.close();
        System.out.println("Final InsertStatement Result2 --->>>>   " + InsertStatement.getParameterMetaData().getParameterCount());
        dbConnection.setAutoCommit(false);
        int rowcount = InsertStatement.executeUpdate();
        System.out.println("Number of rows updated for sql " + INSERT_STATEMENT + " ------- " + rowcount);
        dbConnection.commit();
        InsertStatement.close();
      }
      System.out.println("************** Inserting data into TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT table  ---  END ****************");
      
      /********************************************************************************************************
      	************************** Generate Report and Send Email **********************************************
      	*******************************************************************************************************/
      
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
      // Close db connection
      if (dbConnection != null @and !dbConnection.isClosed()) {
        closeDBConnection(dbConnection);
        System.out.println("Database connection closed.");
      }
      // Close prepared statements
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

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1735239580155" id="0ad829da94031ebe81940456b9fb2b1f" language="beanshell" modified="1737440362167" name="BBY - Rule - Target Application Discrepancy Fix Terminated Users" significantModified="1737440362167">
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
  
  PreparedStatement InsertStatement = null;
  PreparedStatement SelectStatementWDTermReport = null;
  PreparedStatement SelectStatementWDIIQTermReport = null;
  PreparedStatement SelectStatementIIQTarget = null;
  Statement SelectRefreshStatement = null;
  Connection dbConnection = null;
  
  ResultSet resultSetWDTermReport = null;
  ResultSet resultSetWDIIQTermReport = null;
  ResultSet resultSetIIQTarget = null;
  ResultSetMetaData resultSetMetaData = null;
  
  Map columnMap = new HashMap();
  Map activeUserAfterWDAMap = new HashMap();
  Map refreshCompletedMap = new HashMap();
  List activeUserAfterWDAList = new ArrayList();
  List finalList = new ArrayList();
  
  List TermedUserListForLeaver_AD = new ArrayList();
  List TermedUserListForLeaver_OUD = new ArrayList();
  List TermedUserListForLeaver_OIG = new ArrayList();
  List TermedUserListForLeaver_LENEL = new ArrayList();
  List TermedUserListWithoutLeaver = new ArrayList();
  List TermedUserListWithoutRefresh = new ArrayList();
  List TermedUserListForRefresh = new ArrayList();
  List TermedUserListForRefresh_AD = new ArrayList();
  List TermedUserListForRefresh_LENEL = new ArrayList();
  List TermedUserListAfterLeaver = new ArrayList();
  List TermedUserListTable = new ArrayList();
  List TermedUserListRefrshComplete = new ArrayList();
  
  /*String INSERT_STATEMENT = "INSERT INTO TERMINATION_DISCREPANCY_REPORT_BETWEEN_SAILPOINT_TARGET"+
    "(TERM_USER_NAME,TERM_TIME,STATUS_BEFORE_REFRESH_AD,STATUS_BEFORE_REFRESH_OUD,STATUS_BEFORE_REFRESH_OIG,
    "STATUS_BEFORE_REFRESH_LENEL,REFRESH_COMPLETED,STATUS_AFTER_REFRESH_AD,STATUS_AFTER_REFRESH_OUD,STATUS_AFTER_REFRESH_OIG,"+
  "STATUS_AFTER_REFRESH_LENEL,LEAVER_REQUIRED,LEAVER_COUNT,FINAL_STATUS_AD,FINAL_STATUS_OUD,FINAL_STATUS_OIG,FINAL_STATUS_LENEL)"+
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";*/
  
  String INSERT_STATEMENT = "INSERT INTO TERMINATION_DISCREPANCY_REPORT_BETWEEN_SAILPOINT_TARGET"+
    "(TERM_USER_NAME,TERM_TIME,STATUS_BEFORE_REFRESH_AD,STATUS_BEFORE_REFRESH_LENEL,REFRESH_COMPLETED,"+
    "STATUS_AFTER_REFRESH_AD,STATUS_AFTER_REFRESH_LENEL,LEAVER_REQUIRED,FINAL_STATUS_AD,FINAL_STATUS_LENEL)"+
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  String SELECT_STATEMENT_WD_TERM_REPORT = "SELECT * FROM DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT";
  String SELECT_STATEMENT_WD_IIQ_TERM_REPORT = "SELECT * FROM TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT";
  String SELECT_STATEMENT_IIQ_TARGET = "SELECT * FROM TERMINATION_DISCREPANCY_REPORT_BETWEEN_SAILPOINT_TARGET";
  String[] VALID_COLUMN_HEADERS = { "TERM_USER_NAME", "TERM_TIME", "STATUS_BEFORE_REFRESH_AD", "STATUS_BEFORE_REFRESH_LENEL","REFRESH_COMPLETED","STATUS_AFTER_REFRESH_AD","STATUS_AFTER_REFRESH_LENEL","LEAVER_REQUIRED","FINAL_STATUS_AD","FINAL_STATUS_LENEL"};
  String APPNAME_OIG = "OIG App Target";
  String APPNAME_AD = "Active Directory NA";
  String APPNAME_LENEL = "Lenel OnGuard Badging";
  String APPNAME_OUD = "Enterprise Directory";
  
  SailPointContext context = SailPointFactory.getCurrentContext();
  Identity identity;
  Date refreshStartTime;
  Date refreshEndTime;
  Date lastRefreshTime;
  
  int leaverCount = 0;
  int refreshCount = 0;
    
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
  
  public void bulkSingleIdentityRefresh(List TermedUserListTableTask1, int refreshCount, int leaverCount){
    Map map = new HashMap();
    System.out.println("inside bulkSingleIdentityRefresh method---------------     refreshCount=  "+refreshCount+"        leaverCount=    "+leaverCount);
    map.put("promoteAttributes",true);
    map.put("refreshManagerStatus",true);
    map.put("refreshIdentityEntitlements",true);
    String filter;
    String filter1 = null;
    for(int i = 0; i &lt; TermedUserListTableTask1.size(); i++){
      String user = TermedUserListTableTask1.get(i); 
			identity = context.getObjectByName(Identity.class,user);
      String name = identity.getName();
      filter1 = "name == \""+name+"\"";
      filter = filter + " || " + filter1;        
    }
    
    filter = filter.replace("null || ","");
    map.put("filter", filter);
    System.out.println("bulkSingleIdentityRefresh ----------------     "+filter);
    
    try {
      System.out.println("*****************Start of bulk single identity refresh method for terminated users from task#1 ********************");
      refreshStartTime = new Date();
      TaskManager taskManager = new TaskManager(context);
      TaskResult result = taskManager.runSync("Single-IdentityRefresh-Jagan", map);
      refreshEndTime = new Date();
      System.out.println("*****************End of bulk single identity refresh method for terminated users from task#1 ********************");
    } catch (Exception exception) {
      System.out.println("Exception refreshing identity::"+exception.getMessage());
    }
    
    System.out.println(" ------------------- refreshStartTime ------------------   "+refreshStartTime.getTime());
    Connection connection = context.getJdbcConnection();
    for(int i = 0; i &lt; TermedUserListTableTask1.size(); i++){
      try{
        String user = TermedUserListTableTask1.get(i); 
        
        identity = context.getObjectByName(Identity.class,user);
        System.out.println("identity.getName() in refresh identity --------------    "+identity.getName());

        String SELECT_LAST_REFRESH_STATEMENT = "SELECT * FROM SPT_IDENTITY WHERE NAME = ";
        SELECT_LAST_REFRESH_STATEMENT = SELECT_LAST_REFRESH_STATEMENT + "'" +identity.getName() +"'";
        System.out.println("SELECT_LAST_REFRESH_STATEMENT ------------------------     "+SELECT_LAST_REFRESH_STATEMENT);

        SelectRefreshStatement = connection.createStatement();
        resultSet = SelectRefreshStatement.executeQuery(SELECT_LAST_REFRESH_STATEMENT);
        String lastRefreshText = null;        
        
        while(resultSet.next()){
          lastRefreshText = resultSet.getString("LAST_REFRESH");
        }
        
        resultSet.close();
        SelectRefreshStatement.close();
        
        //String lastRefreshText = identity.getAttribute("lastRefresh");
        //System.out.println("last refresh value -------    "+(String)object.getAttribute("lastRefresh"));
        long lastRefreshTime = Long.parseLong(lastRefreshText);

        System.out.println(" ------------------- lastRefreshText ------------------   "+lastRefreshText);
        System.out.println(" ------------------- lastRefreshTime ------------------   "+lastRefreshTime);
        
        if(refreshStartTime.getTime() &lt; lastRefreshTime @and lastRefreshTime &lt; refreshEndTime.getTime()){
          System.out.println(" ------------------- Refreshed Users are gettign added to the group ------------------   ");
          
          if(leaverCount > 0){
            TermedUserListAfterLeaver.add(user);
          }
          else{
            TermedUserListRefrshComplete.add(user);
          }
        }
      }
      catch (Exception exception) {
        System.out.println("Exception refreshing identity -- line#328::"+exception.getMessage());
      }
    }
    System.out.println(" ------------------- refreshEndTime ------------------   "+refreshEndTime.getTime());
    connection.close();
    System.out.println(" ------------------- Finl List aftr single identity refresh --------------------------        "+TermedUserListRefrshComplete);
  }
  
  public List performMaintenanceTask(List TermedUserListForLeaver_AD, List TermedUserListForLeaver_LENEL){
    List performMaintenanceList = new ArrayList();
    
    performMaintenanceList.add(TermedUserListForLeaver_AD);
    performMaintenanceList.add(TermedUserListForLeaver_LENEL);
    
    performMaintenanceList = removeDuplicates(performMaintenanceList);
    
    for(int i = 0; i &lt; performMaintenanceList.size(); i++){
      String user = performMaintenanceList.get(i); 
			identity = context.getObjectByName(Identity.class,user);
      String name = identity.getName();
      filter1 = "name == \""+name+"\"";
      filter = filter + " || " + filter1;        
    }
    
    filter = filter.replace("null || ","");
    map.put("filter", filter);
    System.out.println("performMaintenanceFilter ----------------     "+filter);
    
    try {
      System.out.println("*****************Start of bulk Perform Maintenance Try method ********************");
      
      TaskManager taskManager = new TaskManager(context);
      TaskResult result = taskManager.runSync("Perform Maintenance", map);
      
      System.out.println("*****************Start of bulk Perform Maintenance Try method ********************");
    } catch (Exception exception) {
      System.out.println("Exception refreshing identity::"+exception.getMessage());
    }
    
    return performMaintenanceList;
  }
  
  /* ******************************  Workday Bulk Aggregation --- Start ************************************ */
  public void workdayAggregation(List performMaintenanceList){
    String nativeIdUser=null;
    String value=null;
    for (int i = 0; i &lt; performMaintenanceList.size(); i++){
      System.out.println("     ----------Workday Aggregation:Inside performMaintenanceList loop--------     ");
      
      value=performMaintenanceList.get(i);
      identity = context.getObjectByName(Identity.class,value);
      nativeIdUser=identity.getStringAttribute("employeeNumber");
      
      System.out.println("**************Rapid Setup Value -- Start ****************");
      context.startTransaction();
      id.setAttribute("rapidSetupProcessingState","needed");
      id.setAttribute("inactive","true");
      context.commitTransaction();
      System.out.println("**************Rapid Setup Value -- End ****************");
      
      System.out.println(APPNAME+"    Aggregation : linking user "+nativeIdUser);
      ResourceObject myObj = aggregateSingleResource(APPNAME, nativeIdUser, "account");
      
      if(myObj!=null) {
        System.out.println(APPNAME+" Aggregation : Successfully linked user   "+nativeIdUser);
        
      }else{
        System.out.println(APPNAME+" Aggregation : Linking failed for user   "+nativeIdUser);
        
      }
      
    }
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
  /* ***************************************  Workday Bulk Aggregation --- End ************************************** */
  
  public void targetVerification(List TermedUserListTable, int refreshCount, int leaverCount){
    for(int i = 0; i &lt; TermedUserListTable.size(); i++){
      String user = TermedUserListTable.get(i);
      System.out.println("**************Verifying target for user **************           "+user);
      Identity targetIdentity = context.getObjectByName(Identity.class, user);
      List identityLinkslist = targetIdentity.getLinks();

      String APPLICATION_NAME;        
      if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty() ) 
      {
        for(Link eachLink : identityLinkslist) 
        { 
          if(null != eachLink &amp;&amp; null != eachLink.getApplication()) 
          {
            System.out.println("Inside eachlink::::        "+eachLink.getApplication().getName());
            System.out.println("Application status::::        "+eachLink.isDisabled());

            APPLICATION_NAME = eachLink.getApplication().getName();

            if(APPLICATION_NAME.toLowerCase().equalsIgnoreCase(APPNAME_AD) @and !eachLink.isDisabled()){
              System.out.println("**************Inside the AD application validation **************");

              if(eachLink.getAttribute("bbyHREmployeeStatus").equalsIgnoreCase("Active")){
                if(refreshCount == 0){
                  System.out.println("Adding user for Refresh Event ------------     "+user+"     -------For application-------       "+APPLICATION_NAME);
                	TermedUserListForRefresh.add(user);
                  TermedUserListForRefresh_AD.add(user);
                }
                else{
                  System.out.println("Adding user for AD Leaver Event ------------     "+user+"     -------For application-------       "+APPLICATION_NAME);
                	TermedUserListForLeaver_AD.add(user);
                }
              }
            }

            if(APPLICATION_NAME.toLowerCase().equalsIgnoreCase(APPNAME_LENEL) @and !eachLink.isDisabled()){
              System.out.println("**************Inside the Lenel Application validation**************");

              if(eachLink.getAttribute("Emp Status").equalsIgnoreCase("Active")){
                if(refreshCount == 0){
                  System.out.println("Adding user for Refresh Event ------------     "+user+"     -------For application-------       "+APPLICATION_NAME);
                	TermedUserListForRefresh.add(user);
                  TermedUserListForRefresh_LENEL.add(user);
                }
                else{
                  System.out.println("Adding user for Lenel Leaver Event ------------     "+user+"     -------For application-------       "+APPLICATION_NAME);
                	TermedUserListForLeaver_LENEL.add(user);
                }
              }
            }
            /*else{
              System.out.println("**************Inside the else validation because AD and Lenel account already terminated**************");
              if(refreshCount == 0){
                System.out.println("**************Inserting user to the TermedUserListWithoutRefresh list**************");
                TermedUserListWithoutRefresh.add(user);
              }
              else{
                System.out.println("**************Inserting user to the TermedUserListWithoutLeaver list**************");
                TermedUserListWithoutLeaver.add(user);
              }
            }*/
          }
        }
      }
      if(!(TermedUserListForRefresh.contains(user))){
        System.out.println("**************Inserting user to the TermedUserListWithoutRefresh List **************");
        
        if(refreshCount == 0){
          System.out.println("**************Inserting user to the TermedUserListWithoutLeaver list**************");
          TermedUserListWithoutRefresh.add(user);
        }
      }
      
      if((!(TermedUserListForLeaver_AD.contains(user))) @and (!(TermedUserListForLeaver_LENEL.contains(user)))){
        System.out.println("**************Inserting user to the TermedUserListWithoutLeaver OR TermedUserListWithoutRefresh List **************");
        
        if(refreshCount > 0){
          System.out.println("**************Inserting user to the TermedUserListWithoutLeaver list**************");
          TermedUserListWithoutLeaver.add(user);
        }
      }
    }
    
    System.out.println("************** Printing all the List here ************* ");
    System.out.println("TermedUserListForRefresh ------------     "+TermedUserListForRefresh);
    System.out.println("TermedUserListForRefresh_AD ------------     "+TermedUserListForRefresh_AD);
    System.out.println("TermedUserListForRefresh_LENEL ------------     "+TermedUserListForRefresh_LENEL);
    System.out.println("TermedUserListForLeaver_AD ------------     "+TermedUserListForLeaver_AD);
    System.out.println("TermedUserListForLeaver_LENEL ------------     "+TermedUserListForLeaver_LENEL);
    System.out.println("TermedUserListWithoutRefresh ------------     "+TermedUserListWithoutRefresh);
    System.out.println("TermedUserListWithoutLeaver ------------     "+TermedUserListWithoutLeaver);
    
    if(refreshCount > 0){
      if((!(null != TermedUserListForLeaver_AD &amp;&amp; !TermedUserListForLeaver_AD.isEmpty()))
         @or (!(null != TermedUserListForLeaver_LENEL &amp;&amp; !TermedUserListForLeaver_LENEL.isEmpty()))){

        System.out.println("************** Started Triggering Leaver Event ************* ");
        leaverCount++;
        System.out.println("************** Checking Leaver Count is less than 5 or not ************* ");
        if(leaverCount &lt; 3){
          System.out.println("************** Leaver Count Less than 3 ************* ");
          //triggerLeaverEvent(TermedUserListForLeaver_AD, TermedUserListForLeaver_LENEL, leaverCount);
        }
      }
    }
    refreshCount++;
  }
  
  public void triggerLeaverEvent(List TermedUserListForLeaver_AD, List TermedUserListForLeaver_LENEL, int leaverCount){

    System.out.println("************** Starting the Performence Maintenance Task under Leaver Event ************* ");
    List performMaintenanceList = performMaintenanceTask(TermedUserListForLeaver_AD, TermedUserListForLeaver_LENEL);
    System.out.println("************** Ending the Performence Maintenance Task under Leaver Event ************ ");

    System.out.println("************** Starting the Workday Bulk Aggregation under Leaver Event ************* ");
    workdayAggregation(performMaintenanceList);
    System.out.println("************** Ending the Performence Maintenance Task under Leaver Event ************* ");

    System.out.println("************** Starting the bulk single identity refresh under Leaver Event ************* ");
    bulkSingleIdentityRefresh(performMaintenanceList, leaverCount);
    System.out.println("************** Ending the bulk single identity refresh under Leaver Event ************* ");

    System.out.println("************** End Triggering Leaver Event ************* ");
    
    System.out.println("************** Start Target Verification ************* ");
    targetVerification(performMaintenanceList, refreshCount, leaverCount);
    System.out.println("************** End Target Verification ************* ");
  }
  
  
  try{
    System.out.println("**************DB connection started****************");  
    dbConnection = getDBConnection();
    System.out.println("***********DB connection done successfully****************");
    
    List TermedUserListTableTask1 = new ArrayList();
    List TermedUserListTableTask2_1 = new ArrayList();
    List TermedUserListTableTask2_2 = new ArrayList();
    
    System.out.println("dbConnection --------->>>>>>>>>   "+dbConnection);
    if (dbConnection != null) {
      System.out.println("Successfully connected to database!");
      System.out.println("1st SELECT_STATEMENT is "+SELECT_STATEMENT_WD_TERM_REPORT);
      System.out.println("2nd SELECT_STATEMENT is "+SELECT_STATEMENT_WD_IIQ_TERM_REPORT);
      System.out.println("3rd SELECT_STATEMENT is "+SELECT_STATEMENT_IIQ_TARGET);
      System.out.println("Insert Statement is "+INSERT_STATEMENT);
      
      /***************************************************************************************************************************************
      ******************************** Merging terminated users from other two tables ********************************************************
      ****************************************************************************************************************************************/
      
      SelectStatementWDTermReport = dbConnection.prepareStatement(SELECT_STATEMENT_WD_TERM_REPORT);
      SelectStatementWDIIQTermReport = dbConnection.prepareStatement(SELECT_STATEMENT_WD_IIQ_TERM_REPORT);
      SelectStatementIIQTarget = dbConnection.prepareStatement(SELECT_STATEMENT_IIQ_TARGET);
      InsertStatement = dbConnection.prepareStatement(INSERT_STATEMENT);
      resultSetMetaData = SelectStatementIIQTarget.getMetaData();

			resultSetWDTermReport = SelectStatementWDTermReport.executeQuery();
    	resultSetWDIIQTermReport = SelectStatementWDIIQTermReport.executeQuery();
      resultSetIIQTarget = SelectStatementIIQTarget.executeQuery();
        
      while (resultSetWDTermReport.next()){
        String status = resultSetWDTermReport.getString("USER_STATUS");
        if(status.toLowerCase().equalsIgnoreCase("Terminated")){
          System.out.println(" ******* adding Terminated users from Table DAILY_WORKDAY_TERMINATION_DISCREPANCY_REPORT *********");
          TermedUserListTableTask1.add(resultSetWDTermReport.getString("USER_NAME"));
          TermedUserListTable.add(resultSetWDTermReport.getString("USER_NAME"));
        }
      }
      
      while (resultSetWDIIQTermReport.next()){
        String statusAfterWDDA = resultSetWDIIQTermReport.getString("USER_STATUS_AFTER_WDDA");
        String status = resultSetWDIIQTermReport.getString("WD_STATUS");
        String USER_NAME = resultSetWDIIQTermReport.getString("USER_NAME");
        
        System.out.println(" ******* GETTING LIST OF TERMINATED USERS DURING WORKDAY DELTA AGGREGATION - START  *********");
        if(statusAfterWDDA.toLowerCase().equalsIgnoreCase("Terminated during WDA")){
          System.out.println(" ******* adding Terminated users during WDA from Table TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT *********");
          if(!(TermedUserListTableTask1.contains(USER_NAME))){
						TermedUserListTableTask2_1.add(USER_NAME);
            TermedUserListTable.add(USER_NAME);
        	}
        }
        System.out.println(" ******* GETTING LIST OF TERMINATED USERS DURING WORKDAY DELTA AGGREGATION - END  *********");
        
        /***************************************************************************************************************************************
      ******************************** GETTING LIST OF TERMINATED USERS AFTER WORKDAY AGGREGATION *********************************************
      ****************************************************************************************************************************************/
        
        System.out.println(" ******* GETTING LIST OF TERMINATED USERS AFTER WORKDAY AGGREGATION - START  *********");
        if(status.toLowerCase().equalsIgnoreCase("Terminated")){
          System.out.println(" ******* adding Terminated users from Table TERMINATION_DISCREPANCY_REPORT_BETWEEN_WORKDAY_SAILPOINT *********");
          if(!(TermedUserListTableTask1.contains(USER_NAME))){
						TermedUserListTableTask2_2.add(USER_NAME);
            TermedUserListTable.add(USER_NAME);
        	}
        }
        System.out.println(" ******* GETTING LIST OF TERMINATED USERS AFTER WORKDAY AGGREGATION - END  *********");
        
      }
      System.out.println(" ------------ TermedUserListTableTask1 -------------- " + TermedUserListTableTask1);
      System.out.println(" ------------ TermedUserListTableTask2_1 -------------- " + TermedUserListTableTask2_1);
      System.out.println(" ------------ TermedUserListTableTask2_2 -------------- " + TermedUserListTableTask2_2);
      System.out.println(" ------------ TermedUserListTable -------------- " + TermedUserListTable);
      
       /***************************************************************************************************************************************
      ******************************** Verification of Terminated users in Target System Before Refresh **************************************
      ****************************************************************************************************************************************/
      
      System.out.println("******************** Verification of Terminated users in Target System Before Refresh - Start ***************");
      targetVerification(TermedUserListTable, refreshCount, leaverCount);
      System.out.println("******************** Verification of Terminated users in Target System Before Refresh - End ***************");
      
      System.out.println(" ------------ TermedUserListForRefresh -------------- " + TermedUserListForRefresh);
      
       /***************************************************************************************************************************
      ******************************** Bulk Identity Refresh for all terminated users *********************************************
      ******************************************************************************************************************************/
      
      System.out.println("******************** Bulk Identity Refresh for all terminated users - Start ***************");
      bulkSingleIdentityRefresh(TermedUserListForRefresh, refreshCount, leaverCount);
      System.out.println("******************** Bulk Identity Refresh for all terminated users - End ***************");
      
       /******************************************************************************************************************************
      ******************************** Verification of terminated users in target system *********************************************
      *********************************************************************************************************************************/
      
      System.out.println(" ***************  Verification of terminated users in target system - START ************ ");
      targetVerification(TermedUserListForRefresh, refreshCount, leaverCount);
      System.out.println(" ***************  Verification of terminated users in target system - END ************ ");
      
      /***************************************************************************************************************************************
      ********************** Inserting the values in Table TERMINATION_DISCREPANCY_REPORT_BETWEEN_SAILPOINT_TARGET ***************************
      ****************************************************************************************************************************************/
      
      System.out.println(" ***************  Inserting the values in Table - START ************ ");
      
      for(int i = 0; i &lt; TermedUserListTable.size(); i++){
        Strng USER_NAME = TermedUserListTable.get(i);
        for(int j = 0; j &lt; VALID_COLUMN_HEADERS.length; j++){
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("TERM_USER_NAME")){
            InsertStatement.setString(j+1,TermedUserListTable.get(i));
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("TERM_TIME")){
            if(TermedUserListTableTask1.contains(USER_NAME))
            	InsertStatement.setString(j+1,"Before Workday Delta Aggregation");
            if(TermedUserListTableTask2.contains(USER_NAME))
            	InsertStatement.setString(j+1,"After Workday Delta Aggregation");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("STATUS_BEFORE_REFRESH_AD")){
            if(TermedUserListForRefresh_AD.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            if(TermedUserListWithoutRefresh.contains(USER_NAME))
              InsertStatement.setString(j+1,"Terminated");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("STATUS_BEFORE_REFRESH_LENEL")){
            if(TermedUserListForRefresh_LENEL.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            if(TermedUserListWithoutRefresh.contains(USER_NAME))
              InsertStatement.setString(j+1,"Terminated");
          }
          
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("REFRESH_COMPLETED")){
            if(TermedUserListRefrshComplete.contains(USER_NAME))
              InsertStatement.setString(j+1,"Completed");
            else
              InsertStatement.setString(j+1,"Not Completed");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("STATUS_AFTER_REFRESH_AD")){
            if(TermedUserListForLeaver_AD.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            if(TermedUserListWithoutLeaver.contains(USER_NAME))
              InsertStatement.setString(j+1,"Terminated");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("STATUS_AFTER_REFRESH_LENEL")){
            if(TermedUserListForLeaver_LENEL.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            if(TermedUserListWithoutLeaver.contains(USER_NAME))
              InsertStatement.setString(j+1,"Terminated");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("LEAVER_REQUIRED")){
            if(TermedUserListAfterLeaver.contains(USER_NAME))
            	InsertStatement.setString(j+1,"Triggered");
            else
              InsertStatement.setString(j+1,"Not Triggered");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("FINAL_STATUS_AD")){
            if(TermedUserListForLeaver_AD.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            else
              InsertStatement.setString(j+1,"Terminated");
          }
          if(VALID_COLUMN_HEADERS[j].toLowerCase().equalsIgnoreCase("FINAL_STATUS_LENEL")){
            if(TermedUserListForLeaver_LENEL.contains(USER_NAME))
              InsertStatement.setString(j+1,"Active");
            else
              InsertStatement.setString(j+1,"Terminated");
          }
        }
      }
      
      System.out.println(" ***************  Inserting the values in Table - END ************ ");
      
      resultSetWDTermReport.close();
      resultSetWDIIQTermReport.close();
    	SelectStatementWDTermReport.close();
      SelectStatementWDIIQTermReport.close();      
      
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
      if (SelectStatementWDTermReport != null) {
        SelectStatementWDTermReport.close();
      }
      if (SelectStatementWDIIQTermReport != null) {
        SelectStatementWDIIQTermReport.close();
      }
    } catch (SQLException e) {
      System.out.println("Error when closing connection: " + e.getMessage());
    }

    System.out.println("End of Organization Parsing Rule");
  }
  
 return "Success";

</Source>
</Rule>

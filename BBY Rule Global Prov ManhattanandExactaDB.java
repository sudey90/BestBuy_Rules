<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1719563733041" id="0ada671a904e125a81905dfc48314e6b" language="beanshell" modified="1737089466887" name="BBY Rule Global Prov ManhattanandExactaDB" type="JDBCProvision">
  <Description>This rule is used by the JDBC connector to do provisioning of the data .</Description>
  <Signature returnType="ProvisioningResult">
    <Inputs>
      <Argument name="log" type="org.apache.commons.logging.Log">
        <Description>
          The log object associated with the SailPointContext.
        </Description>
      </Argument>
      <Argument name="context" type="sailpoint.api.SailPointContext">
        <Description>
          A sailpoint.api.SailPointContext object that can be used to query the database if necessary.
        </Description>
      </Argument>
      <Argument name="application">
        <Description>
          The application whose data file is being processed.
        </Description>
      </Argument>
      <Argument name="schema">
        <Description>
          The Schema currently in use.
        </Description>
      </Argument>
      <Argument name="connection">
        <Description>
          A connection object to connect to database.
        </Description>
      </Argument>
      <Argument name="plan">
        <Description>
          The ProvisioningPlan created against the JDBC application.
        </Description>
      </Argument>
    </Inputs>
    <Returns>
      <Argument name="result">
        <Description>
          A Provisioning Result object is desirable to return the status.IT can be a new object or part of  Provisioning Plan
        </Description>
      </Argument>
    </Returns>
  </Signature>
  <Source>
  import java.sql.Connection;
  import java.sql.DriverManager;
  import java.sql.PreparedStatement;
  import java.sql.CallableStatement;
  import java.sql.SQLException;
  import java.sql.Types;
  import sailpoint.object.*;
  import java.util.List;
  import sailpoint.api.SailPointContext;
  import sailpoint.connector.JDBCConnector;
  import sailpoint.object.Application;
  import sailpoint.object.Link;
  import sailpoint.object.ProvisioningPlan;
  import sailpoint.api.Provisioner;
  import sailpoint.api.*;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ProvisioningPlan.PermissionRequest;
  import sailpoint.object.ProvisioningPlan.ObjectOperation;
  import sailpoint.object.ProvisioningResult;
  import sailpoint.object.Schema;
  import sailpoint.tools.xml.XMLObjectFactory;
  import org.apache.commons.logging.LogFactory;
  import org.apache.commons.logging.Log;
  import org.apache.log4j.Logger;
  import sailpoint.tools.Util;
  import sailpoint.object.*;
  import sailpoint.tools.*;
  import java.util.*;
  import java.sql.*;




  Logger logger = Logger.getLogger("bby.provrule.BBYRuleGlobalProvManhattanandExactaDB");
  String appName=application.getName();
  String[] medModAttrs={"USR_FIRST_NAME","USR_LAST_NAME","USR_UDF_JOBTITLE","USR_UDF_JOBCODE","USR_EMAIL","USR_UDF_WORKADDRESS1","USR_UDF_WORKCITY","USR_UDF_WORKCOUNTRY","USR_UDF_WORKSTATE","USR_UDF_WORKZIPCODE","USR_CUR_LOCATION"};

  System.out.println("BBY ManhattanandExactaDB Global Provisioning Rule: Application name is : "+appName);

  // Get request attribute value from provisioning plan
  public String getAttributeRequestValue(AccountRequest acctReq, String attribute) {
    if ( acctReq != null ) {
      AttributeRequest attrReq = acctReq.getAttributeRequest(attribute);
      if ( attrReq != null ) {
        return attrReq.getValue();
      }
    }
    return null;
  }

  ProvisioningResult result = new ProvisioningResult();

  if ( plan != null ) {
    System.out.println( "BBY ManhattanandExactaDB Global Provisioning Rule: plan for application "+appName+" is [" + plan.toXml() + "]" );
    List accounts = plan.getAccountRequests();
    if ( ( accounts != null ) @and ( accounts.size() > 0 ) ) {
      for ( AccountRequest account : accounts ) {
        System.out.println( "BBY ManhattanandExactaDB Global Provisioning Rule: NativeIdentity for application "+appName+" is "+account.getNativeIdentity() );
        try {
          if ( AccountRequest.Operation.Create.equals( account.getOperation() ) ) 
          {
            if (appName.equalsIgnoreCase("ManhattanandExactaDB"))
            {
              List attrReqs=account.getAttributeRequests();
              System.out.println("BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              System.out.println( "BBY JDBC Provisioning Rule: USR_LOGIN "+getAttributeRequestValue(account,"USR_LOGIN") );
              System.out.println("BBY JDBC Provisioning Rule: USR_FIRST_NAME "+getAttributeRequestValue(account,"USR_FIRST_NAME") );
              System.out.println("BBY JDBC Provisioning Rule: USR_MIDDLE_NAME "+getAttributeRequestValue(account,"USR_MIDDLE_NAME") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_LAST_NAME "+getAttributeRequestValue(account,"USR_LAST_NAME") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_PROCESSED_FLAG "+getAttributeRequestValue(account,"USR_PROCESSED_FLAG") );
              System.out.println( "BBY JDBC Provisioning Rule: RETURN_ID "+getAttributeRequestValue(account,"RETURN_ID") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKPHONE "+getAttributeRequestValue(account,"USR_UDF_WORKPHONE") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_STATUS "+getAttributeRequestValue(account,"USR_STATUS") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_JOBTITLE "+getAttributeRequestValue(account,"USR_UDF_JOBTITLE") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_JOBCODE "+getAttributeRequestValue(account,"USR_UDF_JOBCODE") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_EMAIL "+getAttributeRequestValue(account,"USR_EMAIL") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKADDRESS1 "+getAttributeRequestValue(account,"USR_UDF_WORKADDRESS1") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKADDRESS2 "+getAttributeRequestValue(account,"USR_UDF_WORKADDRESS2") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKCITY "+getAttributeRequestValue(account,"USR_UDF_WORKCITY") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKCOUNTRY "+getAttributeRequestValue(account,"USR_UDF_WORKCOUNTRY") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKSTATE "+getAttributeRequestValue(account,"USR_UDF_WORKSTATE") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_UDF_WORKZIPCODE "+getAttributeRequestValue(account,"USR_UDF_WORKZIPCODE") );
              System.out.println( "BBY JDBC Provisioning Rule: REC_UPD_USR_ID "+getAttributeRequestValue(account,"REC_UPD_USR_ID") );
              System.out.println( "BBY JDBC Provisioning Rule: REC_STAT_CDE "+getAttributeRequestValue(account,"REC_STAT_CDE") );
              System.out.println( "BBY JDBC Provisioning Rule: REC_CRT_USR_ID "+getAttributeRequestValue(account,"REC_CRT_USR_ID") );
              System.out.println( "BBY JDBC Provisioning Rule: USR_CUR_LOCATION "+getAttributeRequestValue(account,"USR_CUR_LOCATION") );

              statement=connection.prepareStatement("insert into MANPDUSR0001.USR_STG(USR_LOGIN,USR_FIRST_NAME,USR_MIDDLE_NAME,USR_LAST_NAME,USR_PROCESSED_FLAG,USR_UDF_WORKPHONE,USR_STATUS,USR_UDF_JOBTITLE,USR_UDF_JOBCODE,USR_EMAIL,USR_UDF_WORKADDRESS1,USR_UDF_WORKADDRESS2,USR_UDF_WORKCITY,USR_UDF_WORKCOUNTRY,USR_UDF_WORKSTATE,USR_UDF_WORKZIPCODE,REC_UPD_USR_ID,REC_STAT_CDE,REC_CRT_USR_ID,USR_CUR_LOCATION,TRANS_SEQ_NBR) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,MANPDUSR0001.TRANS_SEQ_NBR.NEXTVAL)");
              statement.setString ( 1, (String)account.getNativeIdentity() ); 
              statement.setString ( 2, getAttributeRequestValue(account,"USR_FIRST_NAME") );
              statement.setString ( 3, getAttributeRequestValue(account,"USR_MIDDLE_NAME") );
              statement.setString ( 4, getAttributeRequestValue(account,"USR_LAST_NAME") );
              statement.setString ( 5, getAttributeRequestValue(account,"USR_PROCESSED_FLAG") );
              statement.setString ( 6, getAttributeRequestValue(account,"USR_UDF_WORKPHONE") );
              statement.setString ( 7, getAttributeRequestValue(account,"USR_STATUS") );
              statement.setString ( 8, getAttributeRequestValue(account,"USR_UDF_JOBTITLE") );
              statement.setString ( 9, getAttributeRequestValue(account,"USR_UDF_JOBCODE") );
              statement.setString ( 10, getAttributeRequestValue(account,"USR_EMAIL") );
              statement.setString ( 11, getAttributeRequestValue(account,"USR_UDF_WORKADDRESS1") );
              statement.setString ( 12, getAttributeRequestValue(account,"USR_UDF_WORKADDRESS2") );
              statement.setString ( 13, getAttributeRequestValue(account,"USR_UDF_WORKCITY") );
              statement.setString ( 14, getAttributeRequestValue(account,"USR_UDF_WORKCOUNTRY") );
              statement.setString ( 15, getAttributeRequestValue(account,"USR_UDF_WORKSTATE") );
              statement.setString ( 16, getAttributeRequestValue(account,"USR_UDF_WORKZIPCODE") );
              statement.setString ( 17, getAttributeRequestValue(account,"REC_UPD_USR_ID") );
              statement.setString ( 18, getAttributeRequestValue(account,"REC_STAT_CDE") );
              statement.setString ( 19, getAttributeRequestValue(account,"REC_CRT_USR_ID") );
              statement.setInt ( 20, Integer.parseInt(getAttributeRequestValue(account,"USR_CUR_LOCATION")) );

              statement.execute();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );

              for (AttributeRequest accReqs:attrReqs)
              {

                if (accReqs.getName().equals("USR_CATALOG") &amp;&amp; ProvisioningPlan.Operation.Add.equals(accReqs.getOperation()))
                {
                  List ent = new ArrayList();
                  ent.add(accReqs.getValue());
                  String str1 = ent.toString();
                  boolean containsDoubleBrackets = str1.contains("[[");
                  String str2 = "";
                  if(containsDoubleBrackets){
                    str2 = str1.substring(2,str1.length()-2);
                  }
                  else{
                    str2 = str1.substring(1,str1.length()-1);
                  }
                  String entitlementList = str2.replace(", ", ",");
                  boolean containsComma = entitlementList.contains(",");
                  if(containsComma){
                    String user = account.getNativeIdentity();
                    statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_PREV_LOCATION = ? WHERE USR_LOGIN = ?");
                    statement.setInt(1,0);
                    statement.setString(2,(String)account.getNativeIdentity());
                    statement.executeUpdate();
                  }
                  statement=connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_CATALOG=? WHERE USR_LOGIN=?");
                  statement.setString ( 1, entitlementList);
                  statement.setString ( 2, (String)account.getNativeIdentity() );
                  statement.execute();
                }	
              }
            }
          }
          else if ( AccountRequest.Operation.Modify.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ManhattanandExactaDB")){	
              PreparedStatement statement=null;
              String user = account.getNativeIdentity();
              Identity targetIdentity = context.getObjectByName(Identity.class, user);
              String quoteStr="'";
              String quotecommaStr="',";
              String commaStr=",";
              List attrReqs=account.getAttributeRequests();
              int noa=attrReqs.size();
              int pa=1;
              System.out.println("BBY JDBC Provisioning Rule: Number of attributes to be modified : "+noa);		 
              if(noa>1){
                commaStr;
              }else{
                quoteStr;
              }
              StringBuilder modQuery=new StringBuilder("UPDATE MANPDUSR0001.USR_STG SET :1 WHERE USR_LOGIN=:2");
              StringBuilder  paramBuilder=new StringBuilder();
              String ModParam=null;
              //Map modAttrsMap=new HashMap();	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);

              //Set Processed Flag to N
              statement=connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_PROCESSED_FLAG=?,REC_UPD_TS=?  WHERE USR_LOGIN=?");
              statement.setString ( 1, "N" );
              statement.setTimestamp ( 2, new Timestamp(Calendar.getInstance().getTime().getTime()) );
              statement.setString ( 3, (String)account.getNativeIdentity() );
              statement.execute();
              for(AttributeRequest attrcreq:attrReqs){
                System.out.println("attrcreq::::::::::::"+attrcreq.getName());
                if (attrcreq.getName().equals(("USR_CATALOG"))  &amp;&amp; ProvisioningPlan.Operation.Add.equals(attrcreq.getOperation()))
                {

                  String user = account.getNativeIdentity();
                  Identity targetIdentity = context.getObjectByName(Identity.class, user);
                  System.out.println(" ----------- JDBC Provisioning ----- targetIdentity : "+targetIdentity);		 
                  List identityLinkslist = targetIdentity.getLinks();
                  if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty() ) { 
                    for(Link eachLink : identityLinkslist) {
                      if(null != eachLink &amp;&amp; null != eachLink.getApplication()) { 
                        if(eachLink.getApplication().getName().equalsIgnoreCase("ManhattanandExactaDB")) {

                          System.out.println(" ----------- JDBC Provisioning ----- Inside Application check ---------- ManhattanandExactaDB : ");
                          System.out.println("eachLink.getAttribute-------------------------------------------------------" +eachLink.getAttribute("USR_STATUS"));
                          String USR_STATUS2= "";
                          if(null !=eachLink.getAttribute("USR_STATUS")){
                            System.out.println("inside USR_STATUS ===================");
                            List userStatus = new ArrayList();
                            userStatus.add(eachLink.getAttribute("USR_STATUS"));
                            String beforeRemove = userStatus.toString();
                            String beforeRemove = userStatus.toString();
                            boolean containsDoubleBrackets = beforeRemove.contains("[[");
                            if(containsDoubleBrackets){
                              USR_STATUS2 = beforeRemove.substring(2,beforeRemove.length()-2);
                            }
                            else{
                              USR_STATUS2 = beforeRemove.substring(1,beforeRemove.length()-1);
                            }
                            System.out.println("Value of USR_STATUS2 :-----------------------------------------" + USR_STATUS2);

                          }

                          if(USR_STATUS2.equalsIgnoreCase("DISABLED"))
                          {
                            System.out.println("inside enable block");
                            PreparedStatement statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_STATUS='Active' WHERE USR_LOGIN = ?");
                            statement.setString ( 1, (String) account.getNativeIdentity() );
                            statement.executeUpdate();
                          }
                        }
                      }
                    }
                  }

                  System.out.println("-----------Inside Operation check 05---- ");
                  List ent = new ArrayList();
                  ent.add(attrcreq.getValue());
                  String str1 = ent.toString();
                  boolean containsDoubleBrackets = str1.contains("[[");
                  String str2= "";
                  if(containsDoubleBrackets){
                    str2 = str1.substring(2,str1.length()-2);
                  }
                  else{
                    str2 = str1.substring(1,str1.length()-1);
                  }
                  String user = account.getNativeIdentity();
                  String NewEntitlementList = str2.replace(", ", ",");
                  System.out.println(" JDBC Provisioning ----- User : "+user);		 
                  Identity targetIdentity = context.getObjectByName(Identity.class, user);
                  System.out.println(" ----------- JDBC Provisioning ----- targetIdentity : "+targetIdentity);		 
                  List identityLinkslist = targetIdentity.getLinks();
                  if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty() ) { 
                    for(Link eachLink : identityLinkslist) {
                      if(null != eachLink &amp;&amp; null != eachLink.getApplication()) { 
                        if(eachLink.getApplication().getName().equalsIgnoreCase("ManhattanandExactaDB")) {

                          System.out.println(" ----------- JDBC Provisioning ----- Inside Application check ---------- ManhattanandExactaDB : ");
                          System.out.println("eachLink.getAttribute-------------------------------------------------------" +eachLink.getAttribute("USR_CATALOG"));
                          if(null !=eachLink.getAttribute("USR_CATALOG")){
                            System.out.println("inside USR_CATALOG ===================");
                            List preEnt = new ArrayList();
                            preEnt.add(eachLink.getAttribute("USR_CATALOG"));
                            String str3 = preEnt.toString();
                            boolean containsDoubleBrackets = str3.contains("[[");
                            String str4= "";
                            if(containsDoubleBrackets){
                              str4 = str3.substring(2,str3.length()-2);
                            }
                            else{
                              str4 = str3.substring(1,str3.length()-1);
                            }
                            String user = account.getNativeIdentity();
                            System.out.println("entitlements ::::::::::: " +str4);
                            String comma = ",";
                            String oldEntitlementList = str4.replace(", ", ",");
                            String listOfEntitlement = 	oldEntitlementList.concat(comma).concat(NewEntitlementList);
                            boolean containsComma = listOfEntitlement.contains(",");
                            if(containsComma){
                              String user = account.getNativeIdentity();
                              statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_PREV_LOCATION = ? WHERE USR_LOGIN = ?");
                              statement.setInt(1,0);
                              statement.setString(2,(String)account.getNativeIdentity());
                              statement.executeUpdate();
                            }
                            statement=connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_CATALOG=? WHERE USR_LOGIN=?");
                            statement.setString ( 1, listOfEntitlement);
                            statement.setString ( 2, (String)account.getNativeIdentity() );
                            System.out.println(" ----------- JDBC Provisioning ----- statement---------- ManhattanandExactaDB : "+statement);		 
                            statement.execute();
                          }
                          else{
                            System.out.println("Else Condition ==============" +statement);
                            boolean containsComma = NewEntitlementList.contains(",");
                            if(containsComma){
                              String user = account.getNativeIdentity();
                              statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_PREV_LOCATION = ? WHERE USR_LOGIN = ?");
                              statement.setInt(1,0);
                              statement.setString(2,(String)account.getNativeIdentity());
                              statement.executeUpdate();
                            }
                            statement=connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_CATALOG=? WHERE USR_LOGIN=?");
                            statement.setString ( 1, NewEntitlementList);
                            statement.setString ( 2, (String)account.getNativeIdentity() );
                            statement.execute();
                          }
                        }
                      }
                    }
                  }
                }
                else if (attrcreq.getName().equals(("USR_CATALOG"))  &amp;&amp; ProvisioningPlan.Operation.Remove.equals(attrcreq.getOperation()))
                {
                  System.out.println("-----------Inside Operation check 05---- ");
                  List ent = new ArrayList();
                  ent.add(attrcreq.getValue());
                  String str1 = ent.toString();
                  boolean containsDoubleBrackets = str1.contains("[[");
                  String str2= "";
                  if(containsDoubleBrackets){
                    str2 = str1.substring(2,str1.length()-2);
                  }
                  else{
                    str2 = str1.substring(1,str1.length()-1);
                  }
                  String newRemoveEntitlementList = str2.replace(", ", ",");
                  String user = account.getNativeIdentity();
                  System.out.println(" JDBC Provisioning ----- User : "+user);		 
                  Identity targetIdentity = context.getObjectByName(Identity.class, user);
                  System.out.println(" ----------- JDBC Provisioning ----- targetIdentity : "+targetIdentity);		 
                  List identityLinkslist = targetIdentity.getLinks();
                  if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty() ) { 
                    for(Link eachLink : identityLinkslist) {
                      if(null != eachLink &amp;&amp; null != eachLink.getApplication()) { 
                        if(eachLink.getApplication().getName().equalsIgnoreCase("ManhattanandExactaDB")) {

                          System.out.println(" ----------- JDBC Provisioning ----- Inside Application check ---------- ManhattanandExactaDB : ");
                          System.out.println("eachLink.getAttribute-------------------------------------------------------" +eachLink.getAttribute("USR_CATALOG"));
                          if(null !=eachLink.getAttribute("USR_CATALOG")){
                            System.out.println("inside USR_CATALOG ===================");
                            List preEnt = new ArrayList();
                            preEnt.add(eachLink.getAttribute("USR_CATALOG"));
                            String str3 = preEnt.toString();
                            boolean containsDoubleBrackets = str3.contains("[[");
                            String str4= "";
                            if(containsDoubleBrackets){
                              str4 = str3.substring(2,str3.length()-2);
                            }
                            else{
                              str4 = str3.substring(1,str3.length()-1);
                            }
                            String user = account.getNativeIdentity();
                            System.out.println("entitlements ::::::::::: " +str4);
                            String oldEntitlementList = str4.replace(", ", ",");
                            if(oldEntitlementList.contains(newRemoveEntitlementList))
                            {
                              oldEntitlementList=oldEntitlementList.replaceAll(newRemoveEntitlementList, "");
                            }
                            oldEntitlementList = oldEntitlementList.trim();
                            if(oldEntitlementList.startsWith(","))
                            {
                              oldEntitlementList = oldEntitlementList.replaceFirst(",", "");
                            }
                            else if(oldEntitlementList.endsWith(","))
                            {
                              oldEntitlementList = oldEntitlementList.substring(0, oldEntitlementList.length() - 1);
                            }
                            oldEntitlementList = oldEntitlementList.trim();
                            statement=connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_CATALOG=? WHERE USR_LOGIN=?");
                            statement.setString ( 1, oldEntitlementList);
                            statement.setString ( 2, (String)account.getNativeIdentity() );
                            System.out.println(" ----------- JDBC Provisioning ----- statement---------- ManhattanandExactaDB : "+statement);		 
                            statement.execute();
                            oldEntitlementList = oldEntitlementList.trim();
                            if(oldEntitlementList.startsWith(","))
                            {
                              oldEntitlementList = oldEntitlementList.replaceFirst(",", "");
                            }
                            else if(oldEntitlementList.endsWith(","))
                            {
                              oldEntitlementList = oldEntitlementList.substring(0, oldEntitlementList.length() - 1);
                            }
                            oldEntitlementList = oldEntitlementList.trim();
                            String[] splitted = oldEntitlementList.split(",");
                            boolean result = true;
                            for(String str: splitted)
                            {
                              if(newRemoveEntitlementList.contains(str))
                              {
                                result = true;
                              }
                              else
                              {
                                result = false;
                                break;
                              }
                            }
                            if(result)
                            {
                              PreparedStatement statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_STATUS='Disabled' WHERE USR_LOGIN = ?");
                              statement.setString ( 1, (String) account.getNativeIdentity() );
                              statement.executeUpdate();
                            }
                          }
                        }
                      }
                    }
                  }
                }

                System.out.println("Before Job Code Loop jobcode removal =======================================!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(" attrcreq Name jobcode removal N ======================================="+ attrcreq.getName());
                System.out.println(" attrcreq  Operation jobcode removal ======================================="+ attrcreq.getOperation());


                for (String col:medModAttrs){
                  if(col.equals(attrcreq.getName())){
                    if((attrcreq.getName()).equals("USR_CUR_LOCATION")){
                      if(col.equals("USR_CUR_LOCATION") &amp;&amp; attrcreq.getValue()!=null){
                        int newCurLoc = Integer.parseInt(attrcreq.getValue());
                        List identityLinkslist = targetIdentity.getLinks(); 
                        if(null != identityLinkslist &amp;&amp; !identityLinkslist.isEmpty() ) {
                          int Curloc;
                          for(Link eachLink : identityLinkslist) {
                            if(null != eachLink &amp;&amp; null != eachLink.getApplication()) { 
                              if(eachLink.getApplication().getName().equalsIgnoreCase("ManhattanandExactaDB")) {
                                Curloc = Integer.parseInt(eachLink.getAttribute("USR_CUR_LOCATION"));
                              }
                            }
                          }
                          if(newCurLoc!=Curloc)
                          {
                            statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_PREV_LOCATION = ? WHERE USR_LOGIN = ?");
                            statement.setInt(1,Curloc);
                            statement.setString(2,user);
                            statement.executeUpdate();
                          }
                        }
                      }
                    }
                    System.out.println("Curloc:" + Curloc);
                    System.out.println("USR_CUR_LOCATION:" + USR_CUR_LOCATION);
                    if(attrcreq.getValue()!=null){
                      if(noa!=pa){
                        ModParam=attrcreq.getName()+"='"+attrcreq.getValue()+quotecommaStr;
                      }else{
                        ModParam=attrcreq.getName()+"='"+attrcreq.getValue()+quoteStr;
                      }
                    }
                  }

                }
                if(ModParam!=null){
                  paramBuilder.append(ModParam);
                }
                pa++;
              }

              if(Util.isNotNullOrEmpty(paramBuilder.toString())){
                modQuery.replace(modQuery.lastIndexOf(":1"),modQuery.lastIndexOf(":1")+2,paramBuilder.toString());
                modQuery.replace(modQuery.lastIndexOf(":2"),modQuery.lastIndexOf(":2")+2,"'"+user+"'");

                System.out.println("BBY JDBC Provisioning Rule: Final Modify query  is "+modQuery);

                /*
           From the request get all the attribute request
           Populate the hashmap and extract data from hashmap and form the query accordingly.
           */
                statement = connection.prepareStatement(modQuery.toString());

                statement.executeUpdate();



                result.setStatus( ProvisioningResult.STATUS_COMMITTED );
              }                           
            }
          }
          else if ( AccountRequest.Operation.Disable.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ManhattanandExactaDB")){	
              System.out.println("*****INSIDE ManhattanandExactaDB DISABLE***");
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              System.out.println("****Native Identity is ********"+(String) account.getNativeIdentity() );
              PreparedStatement statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_STATUS='Disabled',USR_CATALOG = NULL WHERE USR_LOGIN = ?");
              statement.setString ( 1, (String) account.getNativeIdentity() );
              System.out.println("Disable Query for ManhattanandExactaDB is ::::::"+statement);
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }
          }
          else if ( AccountRequest.Operation.Enable.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ManhattanandExactaDB")){	
              System.out.println("*****INSIDE ManhattanandExactaDB ENABLE***");
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              PreparedStatement statement = connection.prepareStatement("UPDATE MANPDUSR0001.USR_STG SET USR_STATUS='Active' WHERE USR_LOGIN = ?");
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }
          }
          else {
            // Unknown operation!
            logger.debug( "Unknown operation [" + account.getOperation() + "]!" );
          }
        }
        catch( SQLException e ) {
          System.out.println("BBY ManhattanandExactaDB Global Provisioning Rule: " + e );
          result.setStatus( ProvisioningResult.STATUS_FAILED );
          result.addError( e );
        }
      }
    }
  }
  System.out.println( "BBY ManhattanandExactaDB Global Provisioning Rule: result [" + result.toXml(false)+ "]");
  return result;
  </Source>
</Rule>

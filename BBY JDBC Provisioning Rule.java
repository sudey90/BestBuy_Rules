<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1705634895573" id="0ada66858d1a1a52818d1fc316d50ce6" language="beanshell" modified="1731553616681" name="BBY JDBC Provisioning Rule" type="JDBCProvision">
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
  <Source>import java.sql.Connection;
  import java.sql.DriverManager;
  import java.sql.PreparedStatement;
  import java.sql.CallableStatement;
  import java.sql.SQLException;
  import java.sql.Types;
  import java.util.List;
  import sailpoint.api.SailPointContext;
  import sailpoint.connector.JDBCConnector;
  import sailpoint.object.Application;
  import sailpoint.object.ProvisioningPlan;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ProvisioningPlan.PermissionRequest;
  import sailpoint.object.ProvisioningResult;
  import sailpoint.object.Schema;
  import sailpoint.tools.xml.XMLObjectFactory;
  import org.apache.commons.logging.LogFactory;
  import org.apache.commons.logging.Log;
  import org.apache.log4j.Logger;
  import sailpoint.tools.Util;

  Logger logger = Logger.getLogger("bby.provrule.BBYJDBCProvisioningRule");

  String appName=application.getName();
  String[] brzModAttrs={"first_name","last_name","employee_no","e_mail"};
  String[] starModAttrs={"first_name","last_name","E_Mail","SCRTY_GRP_ID","SITE_ID"};

  System.out.println("BBY JDBC Provisioning Rule: Application name is : "+appName);

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

  // Get Identity status from Cube
  public boolean getIdentityStatus() {
  return plan.getIdentity().isInactive();
  }
  
  ProvisioningResult result = new ProvisioningResult();

  if ( plan != null ) {
    System.out.println( "BBY JDBC Provisioning Rule: plan for application "+appName+" is [" + plan.toXml() + "]" );
    List accounts = plan.getAccountRequests();
    if ( ( accounts != null ) @and ( accounts.size() > 0 ) ) {
      for ( AccountRequest account : accounts ) {
        System.out.println( "BBY JDBC Provisioning Rule: NativeIdentity for application "+appName+" is "+account.getNativeIdentity() );
        try {
          if ( AccountRequest.Operation.Create.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("Breeze")){
              //---------Breeze Create Operations start------------------
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              System.out.println( "BBY JDBC Provisioning Rule: firstName "+getAttributeRequestValue(account,"first_name") );
              System.out.println( "BBY JDBC Provisioning Rule: lastName "+getAttributeRequestValue(account,"last_name") );
              System.out.println( "BBY JDBC Provisioning Rule: employeeNumber "+getAttributeRequestValue(account,"employee_no") );
              System.out.println( "BBY JDBC Provisioning Rule: emailID "+getAttributeRequestValue(account,"e_mail") );

              // Call stored procedure to create BREEZE account	  
              CallableStatement statement = connection.prepareCall("{call dbo.SP_OIMUserCreate(?,?,?,?,?,?,?)}");		  
              statement.setString ( 1, (String)account.getNativeIdentity() );
              statement.setString ( 2, getAttributeRequestValue(account,"employee_no") );		  
              statement.setString ( 3, getAttributeRequestValue(account,"first_name") );
              statement.setString ( 4, getAttributeRequestValue(account,"last_name") );
              statement.setString ( 5, getAttributeRequestValue(account,"e_mail") );
              statement.setInt ( 6, 1 );
              statement.registerOutParameter(7, Types.VARCHAR);
              statement.execute();	  

              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }else if(appName.equalsIgnoreCase("Star Resource")){
              //---------Star Resource Create Operations start------------------

              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              System.out.println( "BBY JDBC Provisioning Rule: EMPLOYEE_NO "+getAttributeRequestValue(account,"employee_no"));
              System.out.println( "BBY JDBC Provisioning Rule: SITE_ID "+getAttributeRequestValue(account,"SITE_ID"));
              System.out.println( "BBY JDBC Provisioning Rule: StarSecGrpID "+getAttributeRequestValue(account,"SCRTY_GRP_ID"));
              System.out.println( "BBY JDBC Provisioning Rule: ACTN_VARIABLE "+getAttributeRequestValue(account,"ACTN_VARIABLE"));

              statement=connection.prepareStatement("insert into user_Detail_Record_stg(LOGIN_NAME, EMPLOYEE_NO, FIRST_NAME, LAST_NAME, E_MAIL, SITE_ID, SCRTY_GRP_ID, Rec_crt_dt, Rec_upd_dt, Rec_procsd_flag, ACTN_VARIABLE) values (?,?,?,?,?,?,?,?,?,?,?)");
              statement.setString(1,(String)account.getNativeIdentity());
              statement.setString ( 2, getAttributeRequestValue(account,"employee_no") );		  
              statement.setString ( 3, getAttributeRequestValue(account,"first_name") );
              statement.setString ( 4, getAttributeRequestValue(account,"last_name") );
              statement.setString ( 5, getAttributeRequestValue(account,"e_mail") );
              statement.setString ( 6, getAttributeRequestValue(account,"SITE_ID") );
              statement.setString ( 7,getAttributeRequestValue(account,"SCRTY_GRP_ID"));
              statement.setString ( 8, getAttributeRequestValue(account,"Rec_crt_dt"));
              statement.setString ( 9, getAttributeRequestValue(account,"Rec_upd_dt"));
              statement.setString ( 10, getAttributeRequestValue(account,"Rec_procsd_flag"));
              statement.setString ( 11, getAttributeRequestValue(account,"ACTN_VARIABLE"));			  
              System.out.println( "BBY JDBC Provisioning Rule: statement "+statement );
              statement.execute();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
              System.out.println( "BBY JDBC Provisioning Rule: result "+result );
            }

          } else if ( AccountRequest.Operation.Modify.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("Breeze")){
              //---------Breeze Modify Operations start------------------
              PreparedStatement statement=null;
              String user = account.getNativeIdentity();
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
              StringBuilder modQuery=new StringBuilder("UPDATE table_employee SET :1 WHERE employee2user=(SELECT objid FROM table_user(nolock) WHERE login_name = :2)");
              StringBuilder  paramBuilder=new StringBuilder();
              String ModParam=null;
              //Map modAttrsMap=new HashMap();	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              for(AttributeRequest attrcreq:attrReqs){
                for (String col:brzModAttrs){
                  if(col.equals(attrcreq.getName())){
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
            }else if(appName.equalsIgnoreCase("Star Resource")){
              //---------Star Resource Modify Operations start------------------
              PreparedStatement statement=null;
              String user = account.getNativeIdentity();
              String quoteStr="'";
              String quotecommaStr="',";
              String commaStr=",";
              List attrReqs=account.getAttributeRequests();
              int noa=attrReqs.size();
              int pa=1;
              System.out.println("BBY JDBC Provisioning Rule: Number of attributes to be modified : "+noa);		 
              StringBuilder modQuery=new StringBuilder("UPDATE user_Detail_Record_stg SET :1 Rec_procsd_flag='N',Rec_upd_dt=sysdatetime() WHERE login_name = :2 ");
              StringBuilder  paramBuilder=new StringBuilder();
              boolean actnDisable=false;
              boolean actnEnable=false;
              String starAction=getAttributeRequestValue(account,"ACTN_VARIABLE");

              //Map modAttrsMap=new HashMap();	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] and ACTN_VARIABLE is "+starAction);
              for(AttributeRequest attrcreq:attrReqs){
                System.out.println( "BBY JDBC Provisioning Rule: attrcreq.getOperation() is "+attrcreq.getOperation());
                System.out.println( "BBY JDBC Provisioning Rule: Operation attrcreq.getOp() is "+attrcreq.getOp());
                String ModParam=null;

                for (String col:starModAttrs){
                  if(col.equals(attrcreq.getName())){
                    if(attrcreq.getValue()!=null){
                      if(noa!=pa){
                        ModParam=attrcreq.getName()+"='"+attrcreq.getValue()+quotecommaStr;
                      }else{
                        ModParam=attrcreq.getName()+"='"+attrcreq.getValue()+quotecommaStr;
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

                // check for user status before setting to UPDATE action.
                boolean term=getIdentityStatus();
                System.out.println("BBY JDBC Provisioning Rule: Identity status of user is : "+term);
                if(term){
                  paramBuilder.append("ACTN_VARIABLE='DISABLE', ");
                }else{
                  paramBuilder.append("ACTN_VARIABLE='UPDATE', ");
                }
                                
                modQuery.replace(modQuery.lastIndexOf(":1"),modQuery.lastIndexOf(":1")+2,paramBuilder.toString());
                modQuery.replace(modQuery.lastIndexOf(":2"),modQuery.lastIndexOf(":2")+2,"'"+user+"'");

                System.out.println("BBY JDBC Provisioning Rule: Final Modify query in if - is "+modQuery);


                statement = connection.prepareStatement(modQuery.toString());

                statement.executeUpdate();

                result.setStatus( ProvisioningResult.STATUS_COMMITTED );
              }
            }

          } else if ( AccountRequest.Operation.Delete.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("Breeze")){	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              PreparedStatement statement = connection.prepareStatement( (String) application.getAttributeValue( "account.deleteSQL" ) );
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }

          } else if ( AccountRequest.Operation.Disable.equals( account.getOperation() ) ) {
            //---------Disable Operation Begin------------------
            if(appName.equalsIgnoreCase("Breeze")){
              //---------Breeze Disable Operations start------------------	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              PreparedStatement statement = connection.prepareStatement("UPDATE table_user SET status=0 WHERE login_name = ?");
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }else if(appName.equalsIgnoreCase("Star Resource")){
              //---------Star Resource Disable Operations start------------------
              String starGrpID=getAttributeRequestValue(account,"SCRTY_GRP_ID");
              String starDisQuery=null;
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName+" and starGrpID is "+starGrpID);
              starDisQuery="UPDATE user_Detail_Record_stg SET SCRTY_GRP_ID=null,ACTN_VARIABLE='DISABLE',Rec_procsd_flag='N',Rec_upd_dt=sysdatetime() WHERE login_name = ?";
              PreparedStatement statement = connection.prepareStatement(starDisQuery);
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );	
            }

          } else if ( AccountRequest.Operation.Enable.equals( account.getOperation() ) ) {
            //---------Enable Operation Begin------------------	
            if(appName.equalsIgnoreCase("Breeze")){
              //---------Breeze Enable Operations start------------------	
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              PreparedStatement statement = connection.prepareStatement("UPDATE table_user SET status=1 WHERE login_name = ?");
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }else if(appName.equalsIgnoreCase("Star Resource")){
              //---------Star Resource Enable Operations start------------------
              String starGrpID=getAttributeRequestValue(account,"SCRTY_GRP_ID");
              String starEnbQuery=null;
              System.out.println( "BBY JDBC Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName+" and starGrpID is "+starGrpID);

              if(starGrpID!=null){
                starEnbQuery="UPDATE user_Detail_Record_stg SET SCRTY_GRP_ID='"+starGrpID+"',ACTN_VARIABLE='ENABLE',Rec_procsd_flag='N',Rec_upd_dt=sysdatetime() WHERE login_name = ?";
              }else{
                starEnbQuery="UPDATE user_Detail_Record_stg SET ACTN_VARIABLE='ENABLE',Rec_procsd_flag='N',Rec_upd_dt=sysdatetime() WHERE login_name = ?";
              }

              PreparedStatement statement = connection.prepareStatement(starEnbQuery);
              statement.setString ( 1, (String) account.getNativeIdentity() );
              statement.executeUpdate();
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );	              
            }
          } else if ( AccountRequest.Operation.Lock.equals( account.getOperation() ) ) {
            // Not supported.
            logger.debug( "Operation [" + account.getOperation() + "] is not supported!" );
          } else if ( AccountRequest.Operation.Unlock.equals( account.getOperation() ) ) {
            // Not supported.
            logger.debug( "Operation [" + account.getOperation() + "] is not supported!" );
          } else {
            // Unknown operation!
            logger.debug( "Unknown operation [" + account.getOperation() + "]!" );
          }
        }
        catch( SQLException e ) {
          System.out.println("BBY JDBC Provisioning Rule: " + e );
          result.setStatus( ProvisioningResult.STATUS_FAILED );
          result.addError( e );
        }
      }
    }
  }
  System.out.println( "BBY JDBC Provisioning Rule: result [" + result.toXml(false)+ "]");
  return result;</Source>
</Rule>

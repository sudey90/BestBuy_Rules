<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1713242244286" id="0ada668a8eaf106a818ee53204be0186" language="beanshell" modified="1713242715555" name="BBY Rule Global Prov ReIM" type="JDBCProvision">
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




  Logger logger = Logger.getLogger("bby.provrule.BBYRuleGlobalProvReIM");
  String appName=application.getName();
  String[] reimModAttrs={"LASTNAME","FIRSTNAME","PASSWORD","EMAIL"};

  System.out.println("BBY ReIM Global Provisioning Rule: Application name is : "+appName);

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
    System.out.println( "BBY ReIM Global Provisioning Rule: plan for application "+appName+" is [" + plan.toXml() + "]" );
    List accounts = plan.getAccountRequests();
    if ( ( accounts != null ) @and ( accounts.size() > 0 ) ) {
      for ( AccountRequest account : accounts ) {
        System.out.println( "BBY ReIM Global Provisioning Rule: NativeIdentity for application "+appName+" is "+account.getNativeIdentity() );
        try {
          if ( AccountRequest.Operation.Create.equals( account.getOperation() ) ) 
          {
            if (appName.equalsIgnoreCase("ReIM"))
            {
              // System.out.println("Reim Create Plan is :::::::::::::"+plan.toXml());
              List attrReqs=account.getAttributeRequests();
              statement=connection.prepareStatement("insert into im_user_authorization(USERNAME,LASTNAME,FIRSTNAME,PASSWORD,LANGUAGE,COUNTRY,EMAIL,BBYIM_STATUS) values (?,?,?,?,?,?,?,?)");
              statement.setString ( 1, (String)account.getNativeIdentity() );	  
              statement.setString ( 2, getAttributeRequestValue(account,"LASTNAME") );
              statement.setString ( 3, getAttributeRequestValue(account,"FIRSTNAME") );
              statement.setString ( 4, getAttributeRequestValue(account,"PASSWORD") );
              statement.setString ( 5, getAttributeRequestValue(account,"LANGUAGE") );
              statement.setString ( 6, getAttributeRequestValue(account,"COUNTRY") );
              statement.setString ( 7, getAttributeRequestValue(account,"EMAIL") );
              statement.setString ( 8, getAttributeRequestValue(account,"BBYIM_STATUS") );
              statement.execute();	  
              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
              for (AttributeRequest accReqs:attrReqs)
              {

                if (accReqs.getName().equals("BUSINESS_ROLE_NAME") &amp;&amp; ProvisioningPlan.Operation.Add.equals(accReqs.getOperation()))
                {
                  System.out.println("************Inside Business Role addition********* ");
                  Integer busrolid;
                  QueryOptions qo = new QueryOptions();
                  Filter filter = Filter.eq("application.name",appName);
                  Filter filter = Filter.eq("displayName",accReqs.getValue());
                  qo.addFilter(filter);
                  List newList = context.getObjects(ManagedAttribute.class,qo);
                  for (ManagedAttribute ma:newList)
                  {
                    busrolid =Integer.valueOf(ma.getAttribute("BUSINESS_ROLE_ID"));
                  }

                  statement=connection.prepareStatement("insert into im_business_role_member(USER_ID,BUSINESS_ROLE_ID) values (?,?)");
                  statement.setString ( 1, (String)account.getNativeIdentity() );
                  statement.setInt ( 2, busrolid );
                  statement.execute();	 

                }
              }
            }
          }
          else if ( AccountRequest.Operation.Modify.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ReIM")){
              //  System.out.println("Inside modify Operation");
              PreparedStatement statement=null;
              String user = account.getNativeIdentity();
              System.out.println("User is :::::"+user);
              String quoteStr="'";
              String quotecommaStr="',";
              String commaStr=",";
              List attrReqs=account.getAttributeRequests();
              int noa=attrReqs.size();
              int pa=1;
              System.out.println("BBY ReIM Global Provisioning Rule: Number of attributes to be modified : "+noa);		 
              if(noa>1){
                commaStr;
              }else{
                quoteStr;
              }
              StringBuilder modQuery=new StringBuilder("UPDATE im_user_authorization SET :1 WHERE USERNAME=:2");
              StringBuilder  paramBuilder=new StringBuilder();
              String ModParam=null;
              Integer busrolid;
              String busrolname="";	
              System.out.println( "BBY ReIM Global Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              // System.out.println("Reim Modiy Plan  is :::::::::::::"+plan.toXml());
              for(AttributeRequest attrcreq:attrReqs){
                // System.out.println("attrcreq::::::::::::"+attrcreq.getName());
                if (attrcreq.getName().equals(("BUSINESS_ROLE_NAME"))  &amp;&amp; ProvisioningPlan.Operation.Add.equals(attrcreq.getOperation()))
                {
                  QueryOptions qo = new QueryOptions();
                  Filter filter = Filter.eq("application.name",appName);
                  Filter filter = Filter.eq("displayName",attrcreq.getValue());
                  qo.addFilter(filter);
                  List newList = context.getObjects(ManagedAttribute.class,qo);
                  for (ManagedAttribute ma:newList)
                  {
                    busrolid =Integer.valueOf(ma.getAttribute("BUSINESS_ROLE_ID"));
                  }
                  Identity identity = context.getObjectByName(Identity.class, plan.getNativeIdentity());
                  IdentityService identityService = new IdentityService(context);
                  Link appLink = identityService.getLink(identity,application,null,account.getNativeIdentity());

                  //System.out.println("app link is ::"+ appLink + identity);
                  if(appLink.getApplication().getName().equalsIgnoreCase("ReIM")) 
                  {
                    if(null != appLink.getAttribute("BUSINESS_ROLE_ID")  &amp;&amp; null != appLink.getAttribute("BUSINESS_ROLE_NAME") )
                    {
                      //System.out.println("Exisitng role :::: "+ appLink.getAttribute("BUSINESS_ROLE_ID") + appLink.getAttribute("BUSINESS_ROLE_NAME"));
                      //Adding Logic to set Remove operation of exisitng Entitlement
                      ProvisioningPlan plan1 = new ProvisioningPlan();
                      plan1.setIdentity(identity);
                      AttributeRequest attributeRequest = new AttributeRequest();
                      AccountRequest accountRequest = new AccountRequest();
                      accountRequest.setApplication(appName);
                      accountRequest.setOperation(AccountRequest.Operation.Modify);
                      attributeRequest.setName("BUSINESS_ROLE_NAME");
                      attributeRequest.setOperation(ProvisioningPlan.Operation.Remove);
                      //busrolname =appLink.getAttribute("BUSINESS_ROLE_NAME");
                      attributeRequest.setAssignment(true);
                      attributeRequest.setValue(appLink.getAttribute("BUSINESS_ROLE_NAME"));
                      accountRequest.add(attributeRequest);
                      // System.out.println("------ accountRequest -- :: " +accountRequest);
                      plan1.add(accountRequest);
                      //  System.out.println("------ Plan is  -- :: " +plan1.toXml());
                      Provisioner provisioner = new Provisioner(context);
                      provisioner.compile(plan1);
                      //  System.out.println("------ Plan1 is::::::  -- :: " +plan1.toXml());
                      provisioner.execute(plan1);
                      //Ending Logic 
                      //Adding the new role
                      statement=connection.prepareStatement("insert into im_business_role_member(USER_ID,BUSINESS_ROLE_ID) values (?,?)");
                      statement.setString ( 1, (String)account.getNativeIdentity() );
                      statement.setInt ( 2, busrolid );
                      statement.execute();	
                    }
                    else 
                    {
                      statement=connection.prepareStatement("insert into im_business_role_member(USER_ID,BUSINESS_ROLE_ID) values (?,?)");
                      statement.setString ( 1, (String)account.getNativeIdentity() );
                      statement.setInt ( 2, busrolid );
                      statement.execute();	
                    }
                  }
                  //Check the user status and make the Account as Enabled on reh-hire Scenario
                  if (appLink.getAttribute("BBYIM_STATUS").equalsIgnoreCase("D") &amp;&amp; !identity.isInactive()) { 

                    System.out.println("User Account Status is Disabled.. So Enabling the account ");
                    ProvisioningPlan plan3 = new ProvisioningPlan();
                    plan3.setIdentity(identity);
                    AttributeRequest attributeRequest = new AttributeRequest();
                    AccountRequest accRequest = new AccountRequest();
                    accRequest.setApplication(appName);
                    accRequest.setOp(ObjectOperation.Enable);
                    plan3.add(accRequest);
                    // System.out.println("------ Plan is  -- :: " +plan3.toXml());
                    Provisioner provisioner = new Provisioner(context);
                    provisioner.compile(plan3);
                    // System.out.println("------ Plan1 is::::::  -- :: " +plan3.toXml());
                    provisioner.execute(plan3);
                  }



                }
                else if  (attrcreq.getName().equals(("BUSINESS_ROLE_NAME"))  &amp;&amp; ProvisioningPlan.Operation.Remove.equals(attrcreq.getOperation()))
                {
                  Identity identity = context.getObjectByName(Identity.class, plan.getNativeIdentity());
                  IdentityService identityService = new IdentityService(context);
                  Link appLink = identityService.getLink(identity,application,null,account.getNativeIdentity());
                  if(appLink.getApplication().getName().equalsIgnoreCase("ReIM")) 
                  {
                    // System.out.println("Inside eachlink:::: Remove Operation");
                    if(null != appLink.getAttribute("BUSINESS_ROLE_ID")  &amp;&amp; null != appLink.getAttribute("BUSINESS_ROLE_NAME") )
                    {
                      //System.out.println("Remove plan iss ::::"+ plan.toXml());
                      // System.out.println("Account Request Operation = Delete");
                      PreparedStatement statement = connection.prepareStatement(" DELETE FROM im_business_role_member WHERE USER_ID=?");
                      statement.setString(1, (String)account.getNativeIdentity());
                      // System.out.println("===================statement=======:::"+statement);
                      statement.executeUpdate();
                      // System.out.println("===================statement=executeUpdate======:::"+statement);
                      result.setStatus(ProvisioningResult.STATUS_COMMITTED);
                      // System.out.println("=================result======:::"+result);
                    }
                  }
                }
                else{
                  for (String col:reimModAttrs){
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

              }

              if(Util.isNotNullOrEmpty(paramBuilder.toString())){
                modQuery.replace(modQuery.lastIndexOf(":1"),modQuery.lastIndexOf(":1")+2,paramBuilder.toString());
                modQuery.replace(modQuery.lastIndexOf(":2"),modQuery.lastIndexOf(":2")+2,"'"+user+"'");
                // System.out.println("BBY ReIM Global Provisioning Rule: Final Modify query  is "+modQuery);
                statement = connection.prepareStatement(modQuery.toString());
                statement.executeUpdate();
                result.setStatus( ProvisioningResult.STATUS_COMMITTED );
              }
            }
          }
          else if ( AccountRequest.Operation.Disable.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ReIM")){	
              // System.out.println("*****INSIDE REIM DISABLE***");
              System.out.println( "BBY ReIM Global Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              // logger.debug( "Operation [" + account.getOperation() + "] detected." );
              // System.out.println("****Native Identity is ********"+(String) account.getNativeIdentity() );
              PreparedStatement statement = connection.prepareStatement("UPDATE im_user_authorization SET BBYIM_STATUS='D' WHERE USERNAME = ?");
              statement.setString ( 1, (String) account.getNativeIdentity() );
              //  System.out.println("Disable Query for ReIM is ::::::"+statement);
              statement.executeUpdate();

              //To Remove the Entitlement on user disable 
              Identity identity = context.getObjectByName(Identity.class, plan.getNativeIdentity());
              IdentityService identityService = new IdentityService(context);
              Link appLink = identityService.getLink(identity,application,null,account.getNativeIdentity());
              //System.out.println("app link is ::"+ appLink + identity);
              if(appLink.getApplication().getName().equalsIgnoreCase("ReIM")) 
              {
                if(null != appLink.getAttribute("BUSINESS_ROLE_ID")  &amp;&amp; null != appLink.getAttribute("BUSINESS_ROLE_NAME") )
                {
                  //System.out.println("Exisitng role :::: "+ appLink.getAttribute("BUSINESS_ROLE_ID") + appLink.getAttribute("BUSINESS_ROLE_NAME"));
                  //Adding Logic to set Remove operation of exisitng Entitlement
                  ProvisioningPlan plan1 = new ProvisioningPlan();
                  plan1.setIdentity(identity);
                  AttributeRequest attributeRequest = new AttributeRequest();
                  AccountRequest accountRequest = new AccountRequest();
                  accountRequest.setApplication(appName);
                  accountRequest.setOperation(AccountRequest.Operation.Modify);
                  attributeRequest.setName("BUSINESS_ROLE_NAME");
                  attributeRequest.setOperation(ProvisioningPlan.Operation.Remove);
                  //busrolname =appLink.getAttribute("BUSINESS_ROLE_NAME");
                  attributeRequest.setAssignment(true);
                  attributeRequest.setValue(appLink.getAttribute("BUSINESS_ROLE_NAME"));
                  accountRequest.add(attributeRequest);
                  // System.out.println("------ accountRequest -- :: " +accountRequest);
                  plan1.add(accountRequest);
                  // System.out.println("------ Plan is  -- :: " +plan1.toXml());
                  Provisioner provisioner = new Provisioner(context);
                  provisioner.compile(plan1);
                  // System.out.println("------ Plan1 is::::::  -- :: " +plan1.toXml());
                  provisioner.execute(plan1);
                }
              }

              result.setStatus( ProvisioningResult.STATUS_COMMITTED );
            }
          }
          else if ( AccountRequest.Operation.Enable.equals( account.getOperation() ) ) {
            if(appName.equalsIgnoreCase("ReIM")){	
              //System.out.println("*****INSIDE ReIM ENABLE***");
              System.out.println( "BBY ReIM Global Provisioning Rule: Operation [" + account.getOperation() + "] detected for application "+appName);
              logger.debug( "Operation [" + account.getOperation() + "] detected." );
              PreparedStatement statement = connection.prepareStatement("UPDATE im_user_authorization SET BBYIM_STATUS='A' WHERE USERNAME = ?");
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
          System.out.println("BBY ReIM Global Provisioning Rule: " + e );
          result.setStatus( ProvisioningResult.STATUS_FAILED );
          result.addError( e );
        }
      }
    }
  }
  System.out.println( "BBY ReIM Global Provisioning Rule: result [" + result.toXml(false)+ "]");
  return result;
  </Source>
</Rule>

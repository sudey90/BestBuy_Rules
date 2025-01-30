<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1675283736120" id="0ad829218604110e81860eb1263818cf" language="beanshell" modified="1736891879217" name="BBY Rule Identity Application Account Update" significantModified="1736891879217">
  <Source>

import org.apache.log4j.Logger;

import sailpoint.api.Aggregator;
import sailpoint.api.IdentityService;
import sailpoint.api.Provisioner;
import sailpoint.api.SailPointContext;

import sailpoint.connector.Connector;
import sailpoint.object.Attributes;
import sailpoint.object.Application;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ResourceObject;
import sailpoint.object.TaskResult;
import java.util.*;
 import sailpoint.api.*;
 import sailpoint.object.*;



//Logger log = Logger.getLogger("bestBuy.rule.identityApplicationUpdate");
/*
// VARIABLE DECLARATIONS
boolean isInTaskContext = (null != taskResult &amp;&amp; void != taskResult);
String applicationName = (null != config &amp;&amp; void != config) ? config.get("applicationName") : null;
String identityName = (null != config &amp;&amp; void != config) ? config.get("identityName") : null;
String accountOperation = (null != config &amp;&amp; void != config) ? config.get("accountOperation") : null;
//String appName = (null != config &amp;&amp; void != config) ? config.get("appName") : null;
//String attributeName = (null != config &amp;&amp; void != config) ? config.get("accountAttribute") : null;
//String attributeValue = (null != config &amp;&amp; void != config) ? config.get("accountAttributeValue") : null;
//------------------------------------------------------------------------------
// Static and helper variables 
//------------------------------------------------------------------------------
String AC_NEWPARENT       = "AC_NewParent";
String ACCOUNT            = "account";
String DISTINGUISHEDNAME  = "distinguishedName";
String ENABLED_USERS_OU   = "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=naqa,DC=bestbuyqa,DC=com";
String TERMINATED_USERS_OU   = "OU=Disabled,OU=Users,OU=StaleAccounts,DC=naqa,DC=bestbuyqa,DC=com";
Provisioner provisioner = new Provisioner(context);*/
  
  
/////////////////////////
// FUNCTIONS
/////////////////////////

// Logs the error and (optionally) updates the task result 
/*void logErrorAndUpdateTaskResult(String theError)
{
  log.error(theError);
  if (isInTaskContext) 
  { 
    taskResult.addMessage(new Message(Message.Type.Error, theError, null));
    taskResult.setAttribute("aggregationSuccessful", false);
  }
  return;
}*/

  
  /////////////////////////
// MAIN
/////////////////////////

/*log.debug("applicationName: " + applicationName);
log.debug("accountName: " + accountName);
if (isInTaskContext) 
{
  taskResult.setAttribute("applicationName", applicationName);
  taskResult.setAttribute("accountName", accountName);
}
  */
  
 
  
/*  
 if(accountOperation.equalsIgnoreCase("enable")){
    
    //AD DN
    String accountDN = null;

  	Identity id = context.getObjectByName(Identity.class,identityName);

  	if (id != null) 
  	{
    Application app = context.getObjectByName(Application.class, applicationName);
    if (app != null) 
    {
      IdentityService is = new IdentityService(context);
      List links = is.getLinks(id, app);
      if (links != null) 
      {
        Link link = links.get(0);
        accountDN = link.getAttribute(DISTINGUISHEDNAME);
      }
      context.decache(app);
    }
  }
    
		ProvisioningPlan plan= new ProvisioningPlan();
  	AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Enable, applicationName, null, accountDN);
  	AttributeRequest attrReq = new AttributeRequest(AC_NEWPARENT, ENABLED_USERS_OU);
   	acctReq.add(attrReq);
   	plan.add(acctReq);
   	plan.setIdentity(id);
		provisioner.execute(plan);
  }
  
  
  if(accountOperation.equalsIgnoreCase("disable")){
    
    System.out.println("Inside if");
    //AD DN
    String accountDN = null;
    
    Identity id = context.getObjectByName(Identity.class,identityName);

  	if (id != null) 
  	{
    Application app = context.getObjectByName(Application.class, applicationName);
    if (app != null) 
    {
      IdentityService is = new IdentityService(context);
      List links = is.getLinks(id, app);
      if (links != null) 
      {
        Link link = links.get(0);
        accountDN = link.getAttribute(DISTINGUISHEDNAME);
      }
      context.decache(app);
    }
  }
    
		ProvisioningPlan plan= new ProvisioningPlan();
  	AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Disable, applicationName, null, accountDN);
  	AttributeRequest attrReq = new AttributeRequest(AC_NEWPARENT, TERMINATED_USERS_OU);
   	acctReq.add(attrReq);
   	plan.add(acctReq);
   	plan.setIdentity(id);
		provisioner.execute(plan);
  }*/
  
 //if(accountOperation.equalsIgnoreCase("modify")){
 
  /*Provisioner provisioner = new Provisioner(context);
   Identity id = context.getObjectByName(Identity.class,"A3023260");
  String accountDN="";
   if (id != null) 
  	{
    Application app = context.getObjectByName(Application.class, "Active Directory NA");
    if (app != null) 
    {
      IdentityService is = new IdentityService(context);
      List links = is.getLinks(id, app);
      //return links;
      if (links != null) 
      {
        Link link = links.get(0);
        accountDN = link.getAttribute("distinguishedName");
        ;
      }
      context.decache(app);
    }
  }
   	ProvisioningPlan plan= new ProvisioningPlan();
    
   
  	AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, "Active Directory NA", null, accountDN);
  	AttributeRequest attrReq = new AttributeRequest("AC_NewParent", "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=naqa,DC=bestbuyqa,DC=com");
   	acctReq.add(attrReq);
   	plan.add(acctReq);
   	plan.setIdentity(id);
  	ProvisioningProject project = provisioner.compile(plan);
		provisioner.execute(project);
  	return project;
 //}
  */
  
    // *************** START-To update AD attrs directly in target thru link update ************************
  ProvisioningPlan plan=null;
  String empIDs="A1509415";
  String appName="Active Directory NA Non-Human Accounts";
  //String appName="OIG App Target";
  //String appName="Lenel OnGuard Badging";  
  //String appName="Enterprise Directory";    
	//String appName="PingIdentity";      
  
  String[] empIDsArr=empIDs.split(",");
  String idy=null;
		for(int i=0;i&lt;empIDsArr.length;i++) {
      idy=empIDsArr[i];
  Provisioner provisioner = new Provisioner(context); 
  
  Identity id = context.getObjectByName(Identity.class,idy);
   if (id != null) 
  	{
    Application app = context.getObjectByName(Application.class, appName);
    if (app != null) 
    {
      IdentityService is = new IdentityService(context);
      List links = is.getLinks(id, app);
      if (links != null) 
      {
        Link link = links.get(0);
        accountDN = link.getAttribute("distinguishedName"); //AD
        //accountDN = link.getAttribute("distinguishedName"); //OIG
        //accountDN = link.getAttribute("User ID"); //Lenel        
				//accountDN = link.getAttribute("username"); //Ping                
      }
      context.decache(app);
    }
  
   }
  // return accountDN;
   	plan= new ProvisioningPlan();
  	AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, appName, null, accountDN);
      
  	AttributeRequest attrReq = new AttributeRequest("bbyEDSVAType", "105" );
   	
      //AttributeRequest attrReq = new AttributeRequest("cn",id.getLastname() + "\\, " + id.getFirstname() + " "+ id.getStringAttribute("employeeNumber"));
    //AttributeRequest attrReq1 = new AttributeRequest("displayName",id.getLastname() + ", " + id.getFirstname()+"-CW"); 
      
  	//AttributeRequest attrReq = new AttributeRequest("mailNickname", id.getName());
    //AttributeRequest attrReq = new AttributeRequest("sAMAccountName", id.getName());
     // AttributeRequest attrReq = new AttributeRequest("bby0AltOwnerStr", "NA\\A1623091");
   // AttributeRequest attrReq1 = new AttributeRequest("userPrincipalName", id.getName()+"@bestbuy.com"); 
    
      
      /*******************OIG attributes*************************
    //AttributeRequest attrReq = new AttributeRequest("EffectiveEnddate",ProvisioningPlan.Operation.Remove,null); 
      
      /*******************Lenel attributes*************************
    //AttributeRequest attrReq = new AttributeRequest("User ID", "A113632");

      /*******************Lenel attributes: constraint error. Work with OUD team*************************
    //AttributeRequest attrReq = new AttributeRequest("uid", "A113632");
    //AttributeRequest attrReq = new AttributeRequest("dn", "uid=A113632,ou=People,dc=bestbuy,dc=com");
      
      /*******************Lenel attributes*************************
    //AttributeRequest attrReq = new AttributeRequest("username", "A113632");*/

      
    acctReq.add(attrReq);
    //acctReq.add(attrReq1);
   	plan.add(acctReq);
   	plan.setIdentity(id);
		provisioner.execute(plan);
      
 } 
  
 
 // *************** END- To update AD attrs directly in target thru link update ************************
  
  
  /*Identity idy = context.getObjectByName(Identity.class,"A1103706");
  Date currentDate = new Date ();
  long time = currentDate.getTime()/10000;
  long i=idy.getCreated()-System.currentTimeMillis();
  
  return i;*/
  return plan.toXml();
  
  </Source>
</Rule>

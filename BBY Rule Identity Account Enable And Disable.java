<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1737743867324" id="0ad829dc949412688194999b1dbc3983" language="beanshell" modified="1737744499266" name="BBY Rule Identity Account Enable And Disable" significantModified="1737744499266">
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
  import sailpoint.object.*;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ResourceObject;
  import sailpoint.object.TaskResult;
  import java.util.*;
  import sailpoint.tools.Util;
  import sailpoint.object.ProvisioningProject;




  String empIDs="A1517859";
    System.out.println(empIDs);
  String userName="";
  StringBuilder displayNameBuild = new StringBuilder(28);
  String jobFamily="";
  //System.out.println(appName);
  // String appName="Star Resource";
 //String appName="Lenel OnGuard Badging";  
 String appName="Active Directory NA"; 
 //String appName="Enterprise Directory";
  //String appName="Enterprise Directory for Orphan Certification";
  //String appName="PingIdentity";      
//String appName="OIG App Target";
 //String appName="Oracle EBS";
  //String appName="ManhattanandExactaDB";
  //String appName="OMS";
  //String appName="Star Resource";
  //String appName="Breeze";



  String[] empIDsArr=empIDs.split(",");
  System.out.println("empIDsArr.length "+empIDsArr.length);
  String idy=null;
  int counter=0;

  try{
    //System.out.println(idy);
    for(String idy:empIDsArr) {
      counter++;
      //idy=empIDsArr[i];
      //return idy;
      Provisioner provisioner = new Provisioner(context); 
      System.out.println("Processing AID "+idy);

      Identity id = context.getObjectByName(Identity.class,idy);
      if (id != null) 
      {
        Application app = context.getObjectByName(Application.class, appName);
        // jobFamily=id.getStringAttribute("jobFamily");
        if (app != null) 
        {
          IdentityService is = new IdentityService(context);
          List links = is.getLinks(id, app);
          if (links != null) 
          {
            Link link = links.get(0);
            userName = link.getNativeIdentity(); //AD


          }
          context.decache(app);
        }

      }
      // return accountDN;
      ProvisioningPlan plan= new ProvisioningPlan();
      //AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, appName, null, userName);
      AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Enable, appName, null, userName);
      //AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Disable, appName, null, userName);

      AttributeRequest attrReq1 = new AttributeRequest("AC_NewParent", "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=naqa,DC=bestbuyqa,DC=com");
      //AttributeRequest attrReq1 = new AttributeRequest("AC_NewParent", "OU=Disabled,OU=Users,OU=StaleAccounts,DC=naqa,DC=bestbuyqa,DC=com");
      //Identity identity= context.getObjectByName(Identity.class,userName);

      acctReq.add(attrReq1);


      plan.add(acctReq);

      plan.setIdentity(id);
      //provisioner.execute(plan);
      ProvisioningProject project = provisioner.compile(plan);
      System.out.println("Plan is ::"+ plan.toXml());
      provisioner.execute(plan);	

      //System.out.println(" End  BBY Rule Identity Application Account Update");    

      log.error("Rule ---- BBY Lenel Account Enable ---- Counter"+counter+" ---- AID "+id.getName());

    }


  }catch(Exception e){
    log.error("Error occurred in Processing the rule "+e.getMessage()); 
  }
  //return plan;
  return "success";
  /* 
 // *************** END- To fix employee number as user ID issue : PLEASE DO NOT USE FOR ANY OTHER USE CASE ************************


return "success"


    //}


  </Source>
</Rule>

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1729539697743" id="0ada678b928e12b88192b0996c4f79b7" language="beanshell" modified="1735668143674" name="BBY Rule Identity Application Password Update">
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




  String empIDs="A3025020,A3027347,A3028368,A3028551,A3029987,A3029992,A3029993,A3030064,A3035879,A3037128,A3037282,A3038261,A3038266,A3039149,A3039766,A3044257,A3048198,A3049566,A3049575,A3050184,A3050301,A3050302,A3050303,A3050305,A3050306,A3050309,A3050311,A3050316,A3050317,A3052836,A3052839,A3052842,A3056457,A3056462,A3056467,A3056479,A3056484,A3056497,A3063128,A3071134,A3073410,A3073549,A3073556,A3075257,A3076654,A3076673,A3076676,A3076677,A3076680,A3076681,A3076684,A3076685,A3077185,A3077191,A3077193,A3077194,A3077195,A3078904,A3079638,A3081906,A3086823,A3091739,A3091799,A3091801,A3091802,A3091814,A3091817,A3091818,A3091820,A3091822,A3091824,A3091826,A3091829,A3091830,A3092763,A3094594,A3096130,A3096131,A3096134,A3096137,A3096141,A3096143,A3096144,A3096146,A3096151,A3096154,A3096157,A3097725,A3099662,A3099665,A3099666,A3099669,A3099670,A3099672,A3099673,A3099674,A3099675,A3099677,A3100085,A3101372,A3101588,A439227,A462596,A532177,A538579,A6002597,A6003206,A6004976,A6005034,A890986,A899018,A928283,A938443,A989572,A998484,A998671";
  //String appName="Oracle EBS";
  System.out.println(empIDs);
  String userName="";
  StringBuilder displayNameBuild = new StringBuilder(28);
  String jobFamily="";
  //System.out.println(appName);
  String appName="Enterprise Directory";
  // String appName="Lenel OnGuard Badging";  
  //String appName="Enterprise Directory";    
  //String appName="PingIdentity";      
  // String appName="Breeze";      
int counter=0;
  String[] empIDsArr=empIDs.split(",");
  System.out.println("empIDsArr.length "+empIDsArr.length);
  String idy=null;
  //System.out.println(idy);
  try{
  for(String idy:empIDsArr) {
    //idy=empIDsArr[i];
    //return idy;
    counter++;
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
        System.out.println("{Rule} ---- {BBY Identity Application Update }---- User "+userName+" counter ::"+counter);
        context.decache(app);
      }

    }
    // return accountDN;
    ProvisioningPlan plan= new ProvisioningPlan();
    AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, appName, null, userName);
    //Identity identity= context.getObjectByName(Identity.class,id.getName());
   
   String pwd = context.decrypt(id.getPassword());
        
    AttributeRequest attrReq1 = new AttributeRequest("password", pwd.toString());
    
    acctReq.add(attrReq1);
    plan.add(acctReq);

    plan.setIdentity(id);
    //provisioner.execute(plan);
    ProvisioningProject project = provisioner.compile(plan);
  provisioner.execute(plan);	

//System.out.println(" End  BBY Rule Identity Application Account Update");    
    


  }
  }catch(Exception e){
    
    log.error("{Rule} ---- {BBY Identity Application Update }---- Error "+e.getMessage());
  }
  //return project;
return "success";
  /* 
 // *************** END- To fix employee number as user ID issue : PLEASE DO NOT USE FOR ANY OTHER USE CASE ************************


return "success"


    //}


  </Source>
</Rule>

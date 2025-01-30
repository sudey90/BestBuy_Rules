<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1662746627664" id="0ada675983231b498183236c0e500088" language="beanshell" modified="1737429552658" name="SPCONF Field Value Rules Library">
  <Source>
  import sailpoint.api.SailPointContext;      
  import sailpoint.object.Identity;
  import sailpoint.object.Custom;
  import sailpoint.object.Application;
  import java.text.DateFormat;
  import java.text.SimpleDateFormat;
  import java.util.Date;
  import sailpoint.api.PasswordGenerator;
  import sailpoint.object.PasswordPolicy;
  import sailpoint.api.IdentityService;
  import sailpoint.tools.Util;
  import sailpoint.object.Link;
  import sailpoint.object.Bundle;

  // -------------------------------------------------------
  // STATIC VARIABLES
  // -------------------------------------------------------
  String __applicationName__ = field.getApplication();
  String __appNameAD__ = "Active Directory NA";
  Custom adLookup = context.getObjectByName(Custom.class, "BBY_AD_Lookup");
  String __loggerPrefix__ = "{ADINTG}.{Rule}.{SPCONF Field Value Rules Library}.";

  // ----------------------------------------------------------------------------------
  // Method Name 		: getOffice
  // Description 		: This method pre-populates Office attribute in AD, while
  //	                  provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: Office attribute
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getOffice(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {
    logger.debug(__loggerPrefix__+"{getOffice} - Enter");
    String __office__ = "";
    String __pbxData__ = identity.getStringAttribute("cube");
    String __locationId__ = identity.getStringAttribute("locationCode");
    StringBuffer officeBuffer = new StringBuffer(6);

    if (isEmpty(__pbxData__)) {
      if (!isEmpty(__locationId__))
        __office__ = __locationId__;

    } else if ("X1100".equalsIgnoreCase(__pbxData__)) {
      if (!isEmpty(__locationId__))
        __office__ = __locationId__;

    } else if (__pbxData__.length() > 4) {
      officeBuffer.append(__pbxData__.substring(0, 2));
      officeBuffer.append("-");
      officeBuffer.append(__pbxData__.substring(2));
      __office__ = officeBuffer.toString();
    }
    logger.debug(__loggerPrefix__+"{getOffice} - Modified Office attribute value "+__office__);
    logger.debug(__loggerPrefix__+"{getOffice} - Exit");
    return __office__;

  }

  public String getCustomOffice(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    String office = identity.getStringAttribute("location");
    if (null == office)
      office = defaultValue;
    return office;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getPasswordGlobal
  // Description 		: This method generates random password based on AD password policy,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: Generates random password
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getPasswordGlobal(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug(__loggerPrefix__+"{getPasswordGlobal} - Enter");
    PasswordPolicy policy = context.getObjectByName(PasswordPolicy.class, "Active Directory NA Password Policy");

    String password = null;

    if (policy != null) {

      PasswordGenerator passGen = new PasswordGenerator(context);

      password = checkRepetitive(passGen.generatePassword(policy));

    }
    logger.debug(__loggerPrefix__+"{getPasswordGlobal} - Exit");
    return password;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getDistinguishedName
  // Description 		: This method pre-populates AD distinguishedName,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: distinguishedName
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getDistinguishedName(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {
    logger.debug(__loggerPrefix__+"{getDistinguishedName} - Enter");
    String __userDN__ = "";
    String __adUsersOU__ = "";


    if (appName.equalsIgnoreCase("Active Directory NA"))
    {
      __userDN__ = "CN=" + identity.getLastname() + "\\, " + identity.getFirstname() + " "
        + identity.getStringAttribute("employeeNumber") + "," + "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=na,DC=bestbuy,DC=com";
    }
    else if (appName.equalsIgnoreCase("Active Directory ASIA"))
    {
      __userDN__ = "CN=" + identity.getLastname() + "\\, " + identity.getFirstname() + " "
        + identity.getStringAttribute("employeeNumber") + "," + "OU=Corporate-Auto,OU=BestBuyAsia,OU=Users,OU=Users and Groups,DC=asia,DC=bestbuy,DC=com";

    }

    logger.debug(__loggerPrefix__+"{getDistinguishedName} - AD DN Value "+__userDN__);
    logger.debug(__loggerPrefix__+"{getDistinguishedName} - Exit");
    return __userDN__;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getUPN
  // Description 		: This method pre-populates AD userPrincipalName,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: userPrincipalName
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getUPN(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {
    logger.debug(__loggerPrefix__+"{getUPN} - Enter");
    String __userPrinicipalName__ = "";

    __userPrinicipalName__ = identity.getName() + "@" + "bestbuy.com";

    logger.debug(__loggerPrefix__+"{getUPN} - Updated UPN "+__userPrinicipalName__);
    logger.debug(__loggerPrefix__+"{getUPN} - Exit");
    return __userPrinicipalName__;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getDisplayName
  // Description 		: This method pre-populates AD displayName,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: displayName
  // Created On 		: 03/10/2022
  // Updated On 		: 18/09/2023
  // Comments 		: BOD's display name issue fix 
  // -----------------------------------------------------------------------------------
  public String getDisplayName(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {
    logger.debug(__loggerPrefix__+"{getDisplayName} - Enter");
    StringBuilder displayName = new StringBuilder(28);
    String adDisplayName = "";
    String jobFamily=null;
    if (!isEmpty(identity.getStringAttribute("preferredName"))) {
      if (identity.getStringAttribute("Person_Type").toLowerCase().equalsIgnoreCase("contingent worker"))
      {
        jobFamily=identity.getStringAttribute("jobFamily");
        if((jobFamily!=null @and !isEmpty(jobFamily)) @and (jobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or jobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
          adDisplayName = identity.getStringAttribute("preferredName");
        }
        else{
          adDisplayName = identity.getStringAttribute("preferredName")+"-CW";
        }
      }
      else{
        adDisplayName = identity.getStringAttribute("preferredName");
      }
    }
    else{
      if (!isEmpty(identity.getLastname()))
        displayName.append(identity.getLastname());
      if (!isEmpty(identity.getFirstname())) {
        displayName.append(", ");
        displayName.append(identity.getFirstname());
      }

      if (identity.getStringAttribute("Person_Type") != null)
      {
        if (identity.getStringAttribute("Person_Type").toLowerCase().equalsIgnoreCase("contingent worker"))
        {
          jobFamily=identity.getStringAttribute("jobFamily");
          if((jobFamily!=null @and !isEmpty(jobFamily)) @and (jobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or jobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
            adDisplayName = displayName.toString();
          }
          else{
            displayName.append("-CW");
            adDisplayName = displayName.toString();
          }
        }
      }
      else 
      {
        adDisplayName = displayName.toString();
      }
      //adDisplayName = displayName.toString();
    }
    logger.debug(__loggerPrefix__+"{getDisplayName} - AD Display Name  "+adDisplayName);
    logger.debug(__loggerPrefix__+"{getDisplayName} - Exit");
    return adDisplayName.toString();

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getManagerDN
  // Description 		: This method pre-populates managerDN,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: managerDN
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getManagerDN(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {
    logger.debug(__loggerPrefix__+"{getManagerDN}  Enter");
    String managerUserDN = "";
    Identity mgrIdy = null;
    if(identity.getManager() !=null){
      mgrIdy = context.getObject(Identity.class, identity.getManager().getName());
    }
    Application application = context.getObjectByName(Application.class, __appNameAD__);
    if (mgrIdy == null)
      managerUserDN = null;

    try {

      IdentityService is = new IdentityService(context);
      List&lt;Link> links = is.getLinks(mgrIdy, application);

      // Ensure we are not taking the priv account DN here.
      if (!Util.isEmpty(links)) {
        for (Link link : links) {

          if (link.getNativeIdentity() != null) {
            managerUserDN = link.getNativeIdentity();

          } else
            managerUserDN = null;

        }

      }

    } catch (Exception e) {

      logger.error(
        __loggerPrefix__+"{getManagerDN}  Exception " + e.getMessage());
      logger.debug(
        __loggerPrefix__+"{getManagerDN}  Exception " + e.getMessage());
    }

    logger.debug(__loggerPrefix__+"{getManagerDN} - managerDN "+managerUserDN);
    logger.debug(__loggerPrefix__+"{getManagerDN}  Exit");
    return managerUserDN;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getCompany
  // Description 		: This method pre-populates company attribute,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: company
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getCompany(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug(__loggerPrefix__+"{getCompany}  Enter");
    String __userType__ = identity.getStringAttribute("Person_Type");
    String __userCompany__ = "";

    if (__userType__ != null)
    {
      if (__userType__.equalsIgnoreCase("Contingent Worker")
          || __userType__.equalsIgnoreCase("Ex-Contingent Worker")) {
        __userCompany__ = identity.getStringAttribute("vendorName");

      }
      else {
        System.out.println("Else block of user type ");
        __userCompany__ = identity.getStringAttribute("grename");

      }
    }
    else {
      __userCompany__ = identity.getStringAttribute("grename");

    }
    logger.debug(__loggerPrefix__+"{getCompany} - Company "+__userCompany__);
    logger.debug(__loggerPrefix__+"{getCompany}  Exit");
    return __userCompany__;
  }


  // ----------------------------------------------------------------------------------
  // Method Name 		: getEDSVAType
  // Description 		: This method pre-populates EDSVAType attribute,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: EDSVAType
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getEDSVAType(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug(__loggerPrefix__+"{getEDSVAType} - Enter");
    String __edsvaType__ = "100";
    String __userJobCode__ = identity.getStringAttribute("jobCode");
    try {
      Custom __customLookup__ = context.getObjectByName(Custom.class, "BBY_Custom_Lookup");
      Map __lookupValues__ = (Map) __customLookup__.get("EDSVAType");
      String __lookUpJobCode__ = __lookupValues__.get("jobCode");

      if (__userJobCode__.equalsIgnoreCase(__lookUpJobCode__)) {
        __edsvaType__ = "102";

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    logger.debug(__loggerPrefix__+"{getEDSVAType} - EDSVAType "+__edsvaType__);
    logger.debug(__loggerPrefix__+"{getEDSVAType} - Exit");
    return __edsvaType__;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getExecAsst
  // Description 		: This method pre-populates msExchAsstName attribute,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: msExchAsstName
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getExecAsst(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug(__loggerPrefix__+"{getExecAsst} - Enter");
    String __execAssEmpNo__ = identity.getStringAttribute("executiveAssistant");
    String __execFirstName__ = "";
    String __execLastName__ = "";
    String __execPrefName__ = "";
    String __execUserType__ = "";
    String __execvendorName__ = "";
    StringBuilder executiveAsstName = new StringBuilder(28);
    String retVal = "";
    try {
      if (__execAssEmpNo__ != null &amp;&amp; !__execAssEmpNo__.equals("")) {

        Identity __execIdentity__ = context.getObject(Identity.class, "A" + __execAssEmpNo__);

        __execFirstName__ = __execIdentity__.getFirstname();

        __execLastName__ = __execIdentity__.getLastname();

        __execPrefName__ = __execIdentity__.getStringAttribute("preferredName");
        __execUserType__ = __execIdentity__.getStringAttribute("Person_Type");

        __execvendorName__ = __execIdentity__.getStringAttribute("vendorName");

        if (__execPrefName__ != null &amp;&amp; !__execPrefName__.equals("")) {
          retVal = __execPrefName__;
        }

        else {

          if (!isEmpty(__execLastName__)) {
            executiveAsstName.append(__execLastName__);
          }

          if (!isEmpty(__execFirstName__)) {
            executiveAsstName.append(", ");
            executiveAsstName.append(__execFirstName__);
          }
          if (__execUserType__.toLowerCase().indexOf("employee".toLowerCase()) &lt; 0) {
            executiveAsstName.append("-CW");
            if (!isEmpty(__execvendorName__)) {
              executiveAsstName.append("-");
              executiveAsstName.append(__execvendorName__);
            }
          }
          retVal = executiveAsstName.toString();
        }
      } else {
        retVal = "";
      }
    } catch (Exception e) {
      logger.debug(__loggerPrefix__+"{getExecAsst} - Exception "+e.getMessage());
      logger.error(__loggerPrefix__+"{getExecAsst} - Exception "+e.getMessage());
    }

    logger.debug(__loggerPrefix__+"{getExecAsst} - Executive Assistant "+retVal);
    logger.debug(__loggerPrefix__+"{getExecAsst} - Exit");
    return retVal;
  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getHRHireDate
  // Description 		: This method pre-populates hiredate attribute,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: bbyHRHireDate
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getHRHireDate(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug(__loggerPrefix__+"{getHRHireDate} - Enter");
    String __effecStartDate__ = identity.getStringAttribute("effectiveStartDate");
    String __adEffecStartDate__ = "";

    if (!isEmpty(__effecStartDate__)) {
      Date __newDate__ = new SimpleDateFormat("MM/dd/yyyy").parse(__effecStartDate__);

      DateFormat __adFormatDate__ = new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'");

      __adEffecStartDate__ = __adFormatDate__.format(__newDate__);

    }
    logger.debug(__loggerPrefix__+"{getHRHireDate} - Hire Date "+__adEffecStartDate__);
    logger.debug(__loggerPrefix__+"{getHRHireDate} - Exit");
    return __adEffecStartDate__;

  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getDescription
  // Description 		: This method pre-populates description attribute,
  //	                  while provisioning the account.
  // Method Arguments : SailpointContext, Identity, Operation,String
  // Value
  // Returns 			: description
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getDescription(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName) {

    logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDescription} - Enter");
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    String locationDescription = identity.getStringAttribute("locationDescription");

    if (null != locationDescription){
      logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDescription} - Exit");
      return "SP Disable " + dateFormat.format(date).toString() + " in AD " + locationDescription;
    }
    else{
      logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDescription} - Exit");
      return "SP Disable " + dateFormat.format(date).toString() + " in AD ";
    }
  }
  // ----------------------------------------------------------------------------------
  // Method Name 		: isEmpty
  // Description 		: This method checks if a String is empty or not
  // Method Arguments : String
  // Returns 			: Returns True or False
  // Created On 		: 03/10/2022
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public boolean isEmpty(String value) {
    return value == null || value.trim().equals("");
  } 

  public String checkRepetitive(String strValidate) {


    String methodName = "checkRepetitive()";
    char[] characters = strValidate.toCharArray();
    HashSet set=new HashSet();

    StringBuilder sb = new StringBuilder();
    logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.checkRepetitive -- Enter");

    for (char c : characters) {

      if (!set.toString().toLowerCase().contains(String.valueOf(c).toLowerCase())) {
        set.add(c);
        sb.append(c);
      }else {
        logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.checkRepetitive -- Existiing Character Set ");

      }
    }
    logger.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.checkRepetitive -- Duplicates Removed -- Exit ");

    return sb.toString();

  }
  // ----------------------------------------------------------------------------------
  // Method Name 		: getDateTime 
  // Description 		: This method returns current date and time to be used by star create and update date fields.
  // Method Arguments : 
  // Value
  // Returns 			: date timestamp
  // Created On 		: 04/11/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getDateTime(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    logger.debug(__loggerPrefix__+"{getDateTime} - Enter");
    Date __starDate__ = new Date();
    String __strStarDate__ = ""; 
    DateFormat __adFormatDate__ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    logger.debug("{getDateTime} - __starDate__.getTime() "+__starDate__.getTime());
    __strStarDate__ = __adFormatDate__.format(__starDate__.getTime());

    logger.debug("{getDateTime} - Hire Date "+__strStarDate__);

    return __strStarDate__;
  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getStarProcFlag
  // Description 		: This method returns constant value N to star table.
  // Method Arguments : 
  // Value
  // Returns 			: default to 'N'
  // Created On 		: 04/11/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------
  public String getStarProcFlag(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    return defaultValue;
  }


  // ----------------------------------------------------------------------------------
  // Method Name 		: getStarAction
  // Description 		: This method returns operation specific to star format.
  // Method Arguments : Operation
  // Value
  // Returns 			: Operation in star format
  // Created On 		: 04/11/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------  
  public String getStarAction(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    System.out.println(__loggerPrefix__ + "{getStarAction} -  operation - Create");
    return "CREATE";
  }

  public String lpad(String valueToPad, String filler, int size) {
    logger.debug(__loggerPrefix__ + "{lpad} - Enter ");
    logger.debug(__loggerPrefix__ + "{lpad} -  Input Arguments - valueToPad - " + valueToPad);
    logger.debug(__loggerPrefix__ + "{lpad} -  Input Arguments - filler - " + filler);
    logger.debug(__loggerPrefix__ + "{lpad} -  Input Arguments - size - " + size);
    while (valueToPad.length() &lt; size)
      valueToPad = filler + valueToPad; 
    logger.debug(__loggerPrefix__ + "{lpad} - Return value - valueToPad - " + valueToPad);
    logger.debug(__loggerPrefix__ + "{lpad} -  Exit ");
    return valueToPad;
  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: getStarSiteID
  // Description 		: This method returns site/location id for star application.
  // Method Arguments : SiteID
  // Value
  // Returns 			: 5 digit site id 
  // Created On 		: 04/11/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------  	
  public String getStarSiteID(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    logger.debug(__loggerPrefix__+"{getStarSiteID} - Enter");
    String locationID = identity.getStringAttribute("Location_Code");
    String starLocationID = "";
    if (locationID == null) {
      logger.debug(__loggerPrefix__ + "{getStarSiteID} - Inside null location id logic");
      starLocationID = "00000";
    } else if (locationID.equalsIgnoreCase("900010")) {
      logger.debug(__loggerPrefix__ + "{getStarSiteID} - Inside corp location id logic ");
      starLocationID = "00990";
    } else if (locationID.length() >= 6) {
      logger.debug(__loggerPrefix__ + "{getStarSiteID} -  Inside location id length 6 logic");
      starLocationID = locationID.substring(locationID.length() - 4, locationID.length());
      starLocationID = lpad(starLocationID, "0", 5);
    } else if (locationID.length() == 5) {
      logger.debug(__loggerPrefix__ + "{getStarSiteID} -  Inside location id length 5 logic");
      starLocationID = locationID.substring(1, 5);
      starLocationID = lpad(starLocationID, "0", 5);
    } else if (locationID.length() &lt; 5) {
      logger.debug(__loggerPrefix__ + "{getStarSiteID} -  Inside location id length 4 logic");
      starLocationID = lpad(locationID, "0", 5);
    } else {
      starLocationID = locationID;
    } 
    logger.debug(__loggerPrefix__ + "{getStarSiteID} -  Return value - starLocationID -" + starLocationID);
    logger.debug(__loggerPrefix__ + "{getStarSiteID} -  Exit ");
    return starLocationID;	
  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: reqStarRepair
  // Description 		: This method returns repair code for star requested role.
  // Method Arguments : role
  // Value
  // Returns 			: repair code
  // Created On 		: 04/29/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------  	
  public String reqStarRepair(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    System.out.println(__loggerPrefix__+"{reqStarRepair} - Enter");
    System.out.println(__loggerPrefix__+"{reqStarRepair} - defaultValue is "+defaultValue);
    String grpID=null;
    if(reqrole!=null){
      String roleName=reqrole.getName();
      String roleDN=reqrole.getDisplayName();
      System.out.println(__loggerPrefix__ + "{reqStarRepair} - roleName is "+roleName+" roleDN is "+roleDN);
      grpID=roleName.substring(roleName.length() - 3, roleName.length()-1);

    }else{
      System.out.println(__loggerPrefix__ + "{reqStarRepair} -  its auto prov request -");
      grpID = identity.getStringAttribute("starRepair");
    }

    System.out.println(__loggerPrefix__ + "{reqStarRepair} -  Return value - grpID -" + grpID);
    System.out.println(__loggerPrefix__ + "{reqStarRepair} -  Exit ");
    return grpID;	
  }

  // ----------------------------------------------------------------------------------
  // Method Name 		: reqIHWADGrp
  // Description 		: This method returns AD group name for IHW access.
  // Method Arguments : role
  // Value
  // Returns 			: AD group name
  // Created On 		: 12/11/2024
  // Updated On 		:
  // Comments 		:
  // -----------------------------------------------------------------------------------  	
  public String reqIHWADGrp(SailPointContext context, Identity identity, String op, String defaultValue,Bundle reqrole, String appName){
    System.out.println(__loggerPrefix__+"{reqIHWADGrp} - Enter");
    System.out.println(__loggerPrefix__+"{reqIHWADGrp} - defaultValue is "+defaultValue);

    String roleName=reqrole.getName();
    String roleDN=reqrole.getDisplayName();

    System.out.println(__loggerPrefix__ + "{reqIHWADGrp} - roleName is "+roleName+" roleDN is "+roleDN);


    System.out.println(__loggerPrefix__ + "{reqIHWADGrp} -  Exit ");
    
    return roleName;	
  }
  </Source>
</Rule>

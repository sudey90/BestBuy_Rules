<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1662746615703" id="0ada675983231b498183236bdf970067" language="beanshell" modified="1726544777156" name="AD-Attr-Transform-RuleLibrary">
  <Source>/*******
 *   Active Directory Integration Attribute Transformation Rule Library    *
 *   This rule library has methods for different attribute transformations *
 *   required for AD integration                                           *
 ******/
 	import sailpoint.object.Identity;
 	import sailpoint.object.AttributeTarget;
 	import sailpoint.api.SailPointContext;
 	import java.text.DateFormat;
	import java.text.SimpleDateFormat;
	import java.util.Date;
	import sailpoint.object.Link;
	import sailpoint.object.Application;
	import sailpoint.object.Link;
	import sailpoint.tools.Util;
	import sailpoint.api.IdentityService;
	import org.apache.log4j.Logger;
	
	
	// -------------------------------------------------------
	// STATIC VARIABLES
	// -------------------------------------------------------
	private static Logger __logger__ = Logger.getLogger("com.bby.sp.adintg.rule.Custom");

	// ----------------------------------------------------------------------------------
	// Method Name 		: getHRHireDate
	// Description 		: This method returns Hire Date in AD format when Hire Date is
    //	                  modified in Workday HR
	// Method Arguments : SailpointContext, Identity, AttributeTarget,Attribute
	// Value
	// Returns 			: HR Hire Date in AD Format
	// Created On 		: 03/10/2022
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------
	public String getHRHireDate(SailPointContext context, Identity identity, AttributeTarget target, String value) {

		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getHRHireDate}  Enter");
		String __effecStartDate__ = value;
		String __adEffecStartDate__ = "";

		try {
			if (!isEmpty(__effecStartDate__)) {

				Date __newDate__ = new SimpleDateFormat("MM/dd/yyyy").parse(__effecStartDate__);

				DateFormat __adFormatDate__ = new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'");

				__adEffecStartDate__ = __adFormatDate__.format(__newDate__);
				__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getHRHireDate}  HR StartDate "
						+ __effecStartDate__ + "AD Formatted Date " + __adEffecStartDate__);
			}

		} catch (Exception e) {

			__logger__.debug(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getHRHireDate}  Exception " + e.getMessage());
			__logger__.error(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getHRHireDate}  Exception " + e.getMessage());

		}
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getHRHireDate}  Exit");
		return __adEffecStartDate__;

	}

	// ----------------------------------------------------------------------------------
	// Method Name 		: getDistinguishedName
	// Description 		: This method updates distinguishedName attribute in AD, when
	//                    first name or lastname is modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, Application
	// Returns 			: Updated DN of the user
	// Created On 		: 03/10/2022
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------
	public String getDistinguishedName(SailPointContext context, Identity identity, Application application) {

		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDistinguishedName}  Enter");
		String __userDN__ = null;

		__userDN__ = "CN=" + identity.getLastname() + "\\, " + identity.getFirstname() + " "
				+ identity.getStringAttribute("employeeNumber") + "," + "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=na,DC=bestbuy,DC=com";

		__logger__.debug(
				"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDistinguishedName}  Updated DN " + __userDN__);
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDistinguishedName}  Exit");
		return __userDN__;

	}

		// ----------------------------------------------------------------------------------
	// Method Name 		: getDisplayName
	// Description 		: This method updates displayName attribute in AD, when first
	//                    name or lastname is modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, AttributeTarget
	// Returns 			: Updated Display Name
	// Created On 		: 03/10/2022
	// Updated On 		: 04/25/2024
	// Comments 		: Display name will be modified based on the Job Family
	// -----------------------------------------------------------------------------------

public String getDisplayName(SailPointContext context, Identity identity, AttributeTarget target) {
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDisplayName}  Enter");
    System.out.println("**************Enter DisplayName***************");
    		StringBuilder displayName = new StringBuilder(28);
		String updatedDisplayName = "";
    String jobFamily=null;

   	if (!isEmpty(identity.getStringAttribute("preferredName"))) {
      jobFamily=identity.getStringAttribute("jobFamily");
      System.out.println("**************PreferredName ***************"+identity.getStringAttribute("preferredName"));
      
      if (identity.getStringAttribute("Person_Type")!=null @and identity.getStringAttribute("Person_Type").toLowerCase().indexOf("employee".toLowerCase()) &lt; 0){
					jobFamily=identity.getStringAttribute("jobFamily");
         System.out.println("**************Person Type ***************"+identity.getStringAttribute("Person_Type"));
				if((jobFamily!=null @and !isEmpty(jobFamily)) @and (jobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or  jobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
					updatedDisplayName = identity.getStringAttribute("preferredName");
				}
				else{
					updatedDisplayName = identity.getStringAttribute("preferredName")+"-CW";
				}
        System.out.println("*******updated displayname ****"+updatedDisplayName);
			
			}
			else
			{
        System.out.println("**************Enter DisplayName***************");
			updatedDisplayName = identity.getStringAttribute("preferredName");
        System.out.println("*******updated displayname ****"+updatedDisplayName);
			
			}
			
			__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDisplayName}  updatedDisplayName"
					+ updatedDisplayName);
		} else {
	  			System.out.println("**************Enter DisplayName***************");
			if (!isEmpty(identity.getLastname()))
				displayName.append(identity.getLastname());

			if (!isEmpty(identity.getFirstname())) {
				displayName.append(", ");
				displayName.append(identity.getFirstname());
			}
			if (identity.getStringAttribute("Person_Type")!=null @and identity.getStringAttribute("Person_Type").toLowerCase().indexOf("employee".toLowerCase()) &lt; 0)
			{
        jobFamily=identity.getStringAttribute("jobFamily");
				if((jobFamily!=null @and !isEmpty(jobFamily)) @and (jobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or  jobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
					updatedDisplayName = displayName.toString();
				}
				else{
					displayName.append("-CW");
					updatedDisplayName = displayName.toString();
				}			
				//displayName.append("-CW");
			}
      else
			{
        System.out.println("**************Enter DisplayName***************");
			updatedDisplayName = displayName.toString();
        System.out.println("*******updated displayname ****"+updatedDisplayName);
			
			}
			
			__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDisplayName}  updatedDisplayName "
					+ updatedDisplayName);
		}
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getDisplayName}  Exit");
		return updatedDisplayName;
 
	}
  
  	// ----------------------------------------------------------------------------------
	// Method Name 		: getJobFamily
	// Description 		: This method updates jobfamily and displayName attribute in AD, when jobfamily
  // is modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, AttributeTarget
	// Returns 			: Updated jobfamily
	// Created On 		: 04/25/2024
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------

  /*public String getJobFamily(SailPointContext context, Identity identity, AttributeTarget target,String modJobFamily) {
        System.out.println(__loggerPrefix__+"{getJobFamily} - Enter");
		StringBuilder displayName = new StringBuilder(28);
		String adDisplayName = "";
		System.out.println(__loggerPrefix__+"{getJobFamily} - modJobFamily is "+modJobFamily);
   	
		if (!isEmpty(identity.getStringAttribute("preferredName"))) {
			
      if (identity.getStringAttribute("Person_Type").toLowerCase().equalsIgnoreCase("contingent worker")){
        if(modJobFamily!=null @and (modJobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or  modJobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
          System.out.println("inside if");
        	adDisplayName = identity.getStringAttribute("preferredName");
        }
        else{
          System.out.println("inside else");
        	adDisplayName = identity.getStringAttribute("preferredName")+"-CW";
        }
      }
      
    }else{
      if (!isEmpty(identity.getLastname()))
        displayName.append(identity.getLastname());
      if (!isEmpty(identity.getFirstname())) {
        displayName.append(", ");
        displayName.append(identity.getFirstname());
      }
		  if (identity.getStringAttribute("Person_Type").toLowerCase().equalsIgnoreCase("contingent worker")){
        if(modJobFamily!=null @and (modJobFamily.toLowerCase().equalsIgnoreCase("DATGroup-India") @or modJobFamily.toLowerCase().equalsIgnoreCase("HumanResourcesGroup-India"))){
          adDisplayName = identity.getStringAttribute("preferredName");
        }
        else{
          adDisplayName = identity.getStringAttribute("preferredName")+"-CW";
        }
		  }
		}
		
		System.out.println(__loggerPrefix__+"{getJobFamily} - AD Display Name  "+adDisplayName);
		System.out.println(__loggerPrefix__+"{getJobFamily} - Exit");
		return adDisplayName;

	}
	*/

  
	// ----------------------------------------------------------------------------------
	// Method Name 		: getManagerDN
	// Description 		: This method updates manager attribute in AD, when
    //	                  manager is modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, AttributeTarget,
	// AttributeValue
	// Returns 			: New Manager DN
	// Created On 		: 03/10/2022
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------
	public String getManagerDN(SailPointContext context, Identity identity, AttributeTarget target, String value) {

		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getManagerDN}  Enter");
		String managerUserDN = "";
		String __adUsersOU__ = "";

		Identity mgrIdy = context.getObject(Identity.class, value);

		Application application = context.getObjectByName(Application.class, target.getApplication().getName());
		if (mgrIdy == null)
			return null;

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

			__logger__.error(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getManagerDN}  Exception " + e.getMessage());
			__logger__.debug(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getManagerDN}  Exception " + e.getMessage());
		}
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getManagerDN}  Exit");
		return managerUserDN;

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

	// ----------------------------------------------------------------------------------
	// Method Name 		: getCompany
	// Description 		: This method updates company attribute in AD, when 
    //	                  modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, AttributeTarget
	// Returns 			: Modified Company attribute value
	// Created On 		: 03/10/2022
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------
	public String getCompany(SailPointContext context, Identity identity, AttributeTarget target) {
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getCompany}  Enter");
		String userType = identity.getStringAttribute("Person_Type");
		String company = "";

		if ((userType.equalsIgnoreCase("Contingent Worker")) || (userType.equalsIgnoreCase("Ex-Contingent Worker"))) {

			company = identity.getStringAttribute("vendorName");
			__logger__.debug(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getCompany}  Contractor Compay Name " + company);

		} else {
			company = identity.getStringAttribute("grename");
			__logger__.debug(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getCompany}  Employee Compay Name " + company);
		}
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getCompany}  Exit");
		return company;

	}

	// ----------------------------------------------------------------------------------
	// Method Name 		: getExecAsst
	// Description 		: This method updates executive assitant display namein AD, when
    //	                  modified in Workday HR.
	// Method Arguments : SailpointContext, Identity, AttributeTarget,AttributeValue
	// AttributeValue
	// Returns 			: Returns updated Executive Assitant Display Name
	// Created On 		: 03/10/2022
	// Updated On 		:
	// Comments 		:
	// -----------------------------------------------------------------------------------
	public String getExecAsst(SailPointContext context, Identity identity, AttributeTarget target, String value) {
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Enter");
		String __execAssEmpNo__ = identity.getStringAttribute("executiveAssistant");
		String __execFirstName__ = "";
		String __execLastName__ = "";
		String __execPrefName__ = "";
		String __execUserType__ = "";
		String __execvendorName__ = "";
		String returnVal = "";
		StringBuilder executiveAsstName = new StringBuilder(28);

		Identity __execIdentity__ = context.getObject(Identity.class, "A" + value);

		try {

			__execFirstName__ = __execIdentity__.getFirstname();
			__execLastName__ = __execIdentity__.getLastname();
			__execPrefName__ = __execIdentity__.getStringAttribute("preferredName");
			__execUserType__ = __execIdentity__.getStringAttribute("Person_Type");
			__execvendorName__ = __execIdentity__.getStringAttribute("vendorName");
			if (__execIdentity__ != null) {
				if (__execPrefName__ != null &amp;&amp; !__execPrefName__.equals("")) {
					returnVal = __execPrefName__;
					__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Modified Executive Assistant "+returnVal);
				} else {

					if (__execLastName__ != null) {
						executiveAsstName.append(__execLastName__);
					}
					if (__execFirstName__ != null) {
						executiveAsstName.append(", ");
						executiveAsstName.append(__execFirstName__);
					}

					if (__execUserType__!=null @and __execUserType__.toLowerCase().indexOf("employee".toLowerCase()) &lt; 0) {
						executiveAsstName.append("-CW");
						if (__execvendorName__ != null) {
							executiveAsstName.append("-");
							executiveAsstName.append(__execvendorName__);
						}
					}
					returnVal = executiveAsstName.toString();
					__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Modified Executive Assistant  "+returnVal);
				}
			} else {
				returnVal = "";
			}
		} catch (Exception e) {
			__logger__.debug(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Exception " + e.getMessage());
			__logger__.error(
					"{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Exception " + e.getMessage());
		}
		__logger__.debug("{ADINTG}.{Rule}.{AD-Attr-Transform-RuleLibrary}.{getExecAsst}  Exit");
		return returnVal;
	}
  </Source>
</Rule>

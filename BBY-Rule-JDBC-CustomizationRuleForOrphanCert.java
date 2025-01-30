<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1714632279594" id="0ada67938f181eac818f380c462a15b4" language="beanshell" modified="1718173646660" name="BBY-Rule-JDBC-CustomizationRuleForOrphanCert" type="ResourceObjectCustomization">
  <Description>This rule is configured on the application and is called after the connector has build a ResourceObject from the native application data.

    Initially designed for non-rule based connectors to add SPPrivileged flag to an object, but could be used to do any transformations.</Description>
  <Signature returnType="ResourceObject"/>
  <Source>
  import sailpoint.api.IdentityService;
  import sailpoint.object.Application;
  import sailpoint.object.QueryOptions;
  import sailpoint.object.Identity;
  import sailpoint.object.Attributes;
  import sailpoint.object.Link;
  import sailpoint.object.Custom;
  import sailpoint.tools.Util;
  import java.util.HashMap;
  import java.util.map;
  import java.sql.*;
  import java.util.List;
  import sailpoint.object.Filter;

  import sailpoint.tools.xml.XMLObjectFactory;
  import sailpoint.object.Identity;
  import java.util.*;
  import sailpoint.object.Capability;
  import sailpoint.object.ResourceObject;
  import sailpoint.api.PasswordGenerator;
  import sailpoint.object.PasswordPolicy;

  import java.util.regex.Matcher;
  import java.util.regex.Pattern;

  String newPassword = null;
  String genPassword =null;
  StringBuilder sb = new StringBuilder();
  String appName=application.getName();

  ResourceObject rObj = new ResourceObject(); 
  StringBuilder sb = new StringBuilder();

  try{
    if (object !=null ) {
      
      String objectType=object.getObjectType();

      if(objectType!=null @and objectType.equalsIgnoreCase("group")){
        return object;
      }


      //---------------Method to check Identity ------------------
      public static boolean checkIdentity(String identityId) {  

        String pattern = "^[aA][0-9]{5,}$";
        Pattern r = Pattern.compile(pattern);  
        Matcher m = r.matcher(identityId);

        return m.matches();
      }

      String identityToCheck = (String)object.getAttribute("USERNAME");
  
      Identity id = context.getObjectByName(Identity.class, object.getAttribute("USERNAME"));
      if (id !=null ) {
        if (id.getType() !=null &amp;&amp; id.getType().equalsIgnoreCase("Service")) {
          //------------------ Update Link Status -----------------
          List identityLinkslist = id.getLinks(); 
          if(null != identityLinkslist @and !identityLinkslist.isEmpty()) { 
            for(Link eachLink : identityLinkslist) { 
              if(null != eachLink @and null != eachLink.getApplication()) { 

                if(eachLink.getApplication().getName().equalsIgnoreCase("ResaRMS")) { 

                  String accStatus = eachLink.getAttribute("ACCOUNT_STATUS");
                  if(null != accStatus @and accStatus.equalsIgnoreCase("OPEN")) {
                    object.put("IIQDisabled", false);
                    return object;
                  }else{
                    object.put("IIQDisabled", true);
                    return object;
                  }

                }
              }
            }
          }
        }

        if (id.getType() !=null &amp;&amp; id.getType().equalsIgnoreCase("Service")) {
          System.out.println("inside service account check: ");


          return object;
        }

        if (id.getType() !=null &amp;&amp; id.getType().equalsIgnoreCase("User")) {

          return null;
        }

      }

  
      //--------------  if Identity not found in IIQ  create a Identity cube :-----------------------------------
      if (id ==null ) {
 System.out.println("inside Identity is Null check: ");
        Identity identity = new Identity();

        String username = (String)object.getAttribute("USERNAME");

        String identityToCheck = (String)object.getAttribute("USERNAME");

        if (!checkIdentity(identityToCheck)) {

          identity.setFirstname(username);
          identity.setLastname(username);
          identity.setName(username);
          identity.setDisplayName(username);
          identity.setType("service");

          //----------------------Setting Identity Password----------------------------------

          PasswordPolicy policy = context.getObjectByName(PasswordPolicy.class, "Active Directory NA Password Policy");

          if (policy != null) {
            PasswordGenerator passGen = new PasswordGenerator(context);
            genPassword=passGen.generatePassword(policy);

            char[] characters = genPassword.toCharArray();
            HashSet set=new HashSet();
            for (char c : characters) {

              if (!set.toString().toLowerCase().contains(String.valueOf(c).toLowerCase())) {
                set.add(c);
                sb.append(c);
              }else {

              }
            }

          }
          newPassword=sb.toString();
          identity.setPassword(newPassword);

          context.saveObject(identity);

          context.commitTransaction();

        }
      }

    }
    //   return object;

  } catch (Exception e) {
    e.printStackTrace();
  }


  </Source>
</Rule>

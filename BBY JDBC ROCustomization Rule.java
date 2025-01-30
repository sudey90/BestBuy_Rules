<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1705634148506" id="0ada66858d1a1a52818d1fb7b09a0cc7" language="beanshell" modified="1719912604432" name="BBY JDBC ROCustomization Rule" type="ResourceObjectCustomization">
  <Description>This rule is configured on the application and is called after the connector has build a ResourceObject from the native application data.

Initially designed for non-rule based connectors to add SPPrivileged flag to an object, but could be used to do any transformations.</Description>
  <Signature returnType="ResourceObject">
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
      <Argument name="object">
        <Description>
          The ResourceObject built by the connector.
        </Description>
      </Argument>
      <Argument name="application">
        <Description>
          Application that references the connector.
        </Description>
      </Argument>
      <Argument name="connector">
        <Description>
          The connector object.
        </Description>
      </Argument>
      <Argument name="state">
        <Description>
          A Map containing state information.
        </Description>
      </Argument>
    </Inputs>
    <Returns>
      <Argument name="resourceObject">
        <Description>
          The updated resource object.
        </Description>
      </Argument>
    </Returns>
  </Signature>
  <Source>String appName=application.getName();
  System.out.println("BBY JDBC ROCustomization Rule: Application is :"+application.getName());
  if(appName.equalsIgnoreCase("Breeze")){
    int userStatus = object.get("status");
    System.out.println("BBY JDBC ROCustomization Rule: userStatus in "+appName+ " is : "+userStatus);
    if (null != userStatus) {
      if (userStatus==1) {
        object.put("IIQDisabled", false);
      }else {
        object.put("IIQDisabled", true);
      }
    }
  }
  else if(appName.equalsIgnoreCase("RSDB")){
    String userStatus= object.getAttribute("ACCOUNT_STATUS");
    System.out.println("BBY JDBC ROCustomization Rule: userStatus in "+appName+ " is : "+userStatus);
    if (null != userStatus) {
      if (userStatus=="OPEN") {
        object.put("IIQDisabled", false);
      }else {
        object.put("IIQDisabled", true);
      }
    }
  }
  else if(appName.equalsIgnoreCase("ReIM")){
    String userStatus= object.getAttribute("BBYIM_STATUS");
    System.out.println("BBY JDBC ROCustomization Rule: userStatus in "+appName+ " is : "+userStatus);
    if (null != userStatus) {
      if (userStatus.equals("A")) {
        object.put("IIQDisabled", false);
      }else {
        object.put("IIQDisabled", true);
      }
    }
  }
  else if(appName.equalsIgnoreCase("ManhattanandExactaDB")){
    String userStatus= object.getAttribute("USR_STATUS");
    System.out.println("BBY JDBC ROCustomization Rule: userStatus in "+appName+ " is : "+userStatus);
    if (null != userStatus) {
      if (userStatus.equals("Active")) {
        object.put("IIQDisabled", false);
      }else {
        object.put("IIQDisabled", true);
      }
    }
  }
  else if(appName.equalsIgnoreCase("Star Resource")){
    String userStatus = object.get("ACTN_VARIABLE");
    System.out.println("BBY JDBC ROCustomization Rule: userStatus in "+appName+ " is : "+userStatus);
    if (null!=userStatus) {
      if (userStatus.equalsIgnoreCase("DISABLE")) {
        object.put("IIQDisabled", true);
      }else {
        object.put("IIQDisabled", false);
      }
    }
  }

  return object;</Source>
</Rule>

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1657899565883" id="0ad829d4879912dc8187e127a74004bf" language="beanshell" modified="1683136415492" name="Rule-ADLeam-ROCustomization" type="ResourceObjectCustomization">
  <Description>This rule is configured on the application and is called after the connector has build a ResourceObject from the native application data.

Initially designed for non-rule based connectors to add SPPrivileged flag to an object, but could be used to do any transformations.</Description>
  <ReferencedRules>
    <Reference class="sailpoint.object.Rule" id="0ad8294484ad1acd8184bfd65bfb1dd4" name="RuleLibrary-BBY-WDAggregation-ErrorEvents"/>
  </ReferencedRules>
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
  <Source>import sailpoint.connector.Connector;
  import sailpoint.object.Identity;
  import sailpoint.object.QueryOptions;
  import sailpoint.object.Filter;
  import sailpoint.object.*;
  import java.text.SimpleDateFormat;
  import java.util.Date;
  import java.util.TimeZone;


String sAMAccountName=(String)object.getAttribute("sAMAccountName");
System.out.println(" Start of Rule-AD LEAM-ROCustomization for user ID ---> "+sAMAccountName);

String ldapTimestamp_str=(String)object.getAttribute("lastLogonTimestamp");
System.out.println(" Last Login  for user ID ---> "+ ldapTimestamp_str);
  

  Long ldapTimestamp_long=Long.parseLong(ldapTimestamp_str);
  Date date = new Date((ldapTimestamp_long / 10000) - 11644473600000L);
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
  String formattedDate = sdf.format(date);
  System.out.println(formattedDate);

/*
 Long ldapTS_mills = Long.parseLong(ldapTimestamp_str)/10000000;
  System.out.println("ldapTS_mills =" + String.valueOf(ldapTS_mills));
  Long unix_mills = (((1970-1601) * 365) - 3 + Math.round((1970-1601)/4) ) * 86400L;
  System.out.println("unix_mills =" + String.valueOf(unix_mills));
  Long ldapTimestamp_long = ldapTS_mills-unix_mills;
  System.out.println("ldapTimestamp_long =" + String.valueOf(ldapTimestamp_long));

  //Long ldapTimestamp_long = (Long.parseLong(ldapTimestamp_str)/1000L) - 11644473600000L;
  Date lastLoginDate = new Date(ldapTimestamp_long * 1000);
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
  String formattedDate = sdf.format(lastLoginDate);
  System.out.println(formattedDate);
  System.out.println("lastLoginDate Date =" + String.valueOf(lastLoginDate));
  System.out.println("Formatted lastLoginDate Date =" + String.valueOf(formattedDate));

*/
  object.put("lastLogonTimestamp",String.valueOf(formattedDate));
//object.put("lastLogonTimestamp",ldapTimestamp_str);

  return object;</Source>
</Rule>

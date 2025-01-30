<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1732212148440" id="0ad82989934f1d2b81934fe3c8d8063d" language="beanshell" modified="1732742725827" name="BBY Password Expiry Attribute SYnc" significantModified="1732742725827">
  <Signature returnType="String">
    <Inputs>
      <Argument name="context">
        <Description>
          A sailpoint.api.SailPointContext object that can be used to
          query the database to save application object and task definitions.
        </Description>
      </Argument>
      <Argument name="log">
        <Description>
          The log object for logging.
        </Description>
      </Argument>
      <Argument name="inputParams">
        <Description>
          The input params object is map of attributes received from Task UI.
        </Description>
      </Argument>
      <Argument name="taskResult">
        <Description>
          The task result object required for updating the result.
        </Description>
      </Argument>
    </Inputs>
  </Signature>
  <Source>	  
  import sailpoint.object.Identity;
  import java.text.SimpleDateFormat;
  import java.util.ArrayList;
  import java.util.Date;
  import java.util.HashMap;
  import java.util.List;
  import sailpoint.object.Application;
  import sailpoint.object.EmailOptions;
  import sailpoint.object.EmailTemplate;
  import sailpoint.object.*;
  import sailpoint.api.*;
  import sailpoint.tools.GeneralException;



  public Date toDate(String timeStump) {
    Date toDate = null;
    if (timeStump != null) {
      long llastLogonTimestamp = Long.parseLong(timeStump);
      //toDate = new Date(127877417297554938L/10000-llastLogonTimestamp);
      long mills = (llastLogonTimestamp / 10000000);
      long unix = (((1970 - 1601) * 365) - 3 + Math.round((1970 - 1601) / 4)) * 86400L;
      long timeStamp = mills - unix;
      toDate = new Date(timeStamp * 1000L);
      // toDate = new Date(llastLogonTimestamp*1000);        
      System.out.print("*******TO DATE  ***************" + toDate);
    }
    return toDate;
  }



  public void syncExpiryDate() throws GeneralException {
    System.out.println("Enter method syncExpiryDate");
    String aboutToExpireQuery =
      "SELECT name, Password_expiry_date FROM spt_identity WHERE inactive = 0 and type in ('User')  and name ='A976026'";

    // Fetch users whose passwords are about to expire
    System.out.println("SQL query for about to expire users is :: " + aboutToExpireQuery);
    ArrayList aboutToExpireUsers = getUsers(aboutToExpireQuery);
    System.out.println("Iterating the users using for loop");
    for (HashMap user : aboutToExpireUsers) {
      String identityName = user.get("name");
      System.out.println("Identity name for which the discrepancy check needed :" + identityName);
      String msDsAdAttribute = getMsDsAdAttribute(identityName);
      if (msDsAdAttribute == null) {
        System.out.println("msDsAdAttribute is null for user: " + identityName);
        continue;
      }
      String formatDate = new SimpleDateFormat("MM/dd/yyyy").format(toDate(msDsAdAttribute));
      String passwordExpiryDate = user.get("Password_expiry_date");
      System.out.println("Password Expiry Date from AD attribute :: " + formatDate);
      System.out.println("Password Expiry Date from IIQ :: " + passwordExpiryDate);
      if (formatDate != null @and passwordExpiryDate != null @and !formatDate.equals(passwordExpiryDate)) {
        System.out.println("Password Expiry Date is not matching with AD attribute for user :: " + identityName);

        Identity iden = context.getObjectByName(Identity.class, identityName);
        if (iden != null) {
          iden.setAttribute("passwordExpiryDate", formatDate);
          context.saveObject(iden);
          context.commitTransaction();
          context.decache();
        } else {
          System.out.println("Identity object is null for user: " + identityName);
        }
      }

    }

  }

  public List getUsers(String sqlQuery) {
    System.out.print("syncExpiryDate | getUsers Method : Fetching users to find the Discrepencies");
    ArrayList users = new ArrayList();
    try {
      Iterator iterator = context.search("sql:" + sqlQuery.toString(), null, null);
      while ((null != iterator) @and iterator.hasNext()) {
        Object[] thisRecord = (Object[]) iterator.next();
        String userLogin = (String) thisRecord[0];
        String PasswordExpiryDate = (String) thisRecord[1];
        HashMap userDetails = new HashMap();
        userDetails.put("name", userLogin);
        userDetails.put("Password_expiry_date", PasswordExpiryDate);
        users.add(userDetails);
      }

    } catch (GeneralException e) {
      System.out.println("General Exception Occured " + e.getMessage());
      throw new RuntimeException("Exception Occured while getting users::" + e.getMessage());
    }
    System.out.print("syncExpiryDate | getUsers Method : Fetching users to find the Discrepencies Completed: "+users);
    return users;
  }

  public String getMsDsAdAttribute(String idenName) {
    System.out.println("Enter method getMsDsAdAttribute" + idenName);
    Identity identity1 = context.getObject(Identity.class, idenName);
    System.out.println("Enter If Condition of getMsDsAdAttribute" + identity1);
    String attrValue = null;

    if (identity1 != null) {
      Application app = context.getObjectByName(Application.class, "Active Directory NA");
      if (app != null) {
        IdentityService is = new IdentityService(context);
        List links = is.getLinks(identity1, app);
        System.out.println("Links for the User: " + links);
        if (links != null @and  !links.isEmpty()) {
          Link link = links.get(0);
          attrValue = link.getAttribute("msDS-UserPasswordExpiryTimeComputed");
        }
        context.decache();
      }
    }
    System.out.println("Exit method getMsDsAdAttribute: " + attrValue);
    return attrValue;
  }

  syncExpiryDate();



  </Source>
</Rule>

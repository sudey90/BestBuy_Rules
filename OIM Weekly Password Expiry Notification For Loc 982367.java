<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1731489476965" id="0ad8292f931f19cf819324d0b16508ae" language="beanshell" modified="1731571460616" name="OIM Weekly Password Expiry Notification For Loc 982367" significantModified="1731571460616">
  <Description>This rule is used by the JDBC connector to do provisioning of the data .</Description>
  <Signature returnType="String">
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
      <Argument name="item">
        <Description>
          The sailpoint.object.Notifiable to escalate.
        </Description>
      </Argument>
      <Argument name="identity" type="Identity">
        <Description>
          The sailpoint.object.Notifiable to escalate.
        </Description>
      </Argument>
    </Inputs>
  </Signature>
  <Source>
  import java.sql.Connection;
  import java.sql.PreparedStatement;
  import java.sql.ResultSet;
  import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.Iterator;
  import java.sql.SQLException;
  import java.util.List;
  import java.util.Map;
  import net.sf.jasperreports.engine.JRException;
  import net.sf.jasperreports.engine.JRField;
  import org.apache.log4j.Logger;
  import sailpoint.api.SailPointContext;
  import sailpoint.api.SailPointFactory;
  import sailpoint.object.Attributes;
  import sailpoint.object.Custom;
  import sailpoint.object.EmailOptions;
  import sailpoint.object.EmailTemplate;
  import sailpoint.object.Filter;
  import sailpoint.object.Identity;
  import sailpoint.object.LiveReport;
  import sailpoint.object.QueryOptions;
  import sailpoint.object.Sort;
  import sailpoint.plugin.PluginBaseHelper;
  import sailpoint.reporting.datasource.JavaDataSource;
  import sailpoint.task.Monitor;
  import sailpoint.tools.GeneralException;
  import sailpoint.tools.Util;
  import sailpoint.object.Application;
  import sailpoint.object.IdentityEntitlement;
  import sailpoint.object.EmailFileAttachment;
  import java.util.*;
  import java.text.SimpleDateFormat;
  import java.util.Calendar;



  PreparedStatement ps = null;
  ResultSet resultSet = null;
  Connection conn = null;  
  SailPointContext context = SailPointFactory.getCurrentContext();
  List data = new ArrayList();


  try {

    conn = context.getJdbcConnection();
    if (null != conn) {

      System.out.println ("Connection is not null");

      String query = "select name AS AID,FIRSTNAME as FIRSTNAME, LASTNAME as LASTNAME, EXTENDED3 as LOCATION,to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy') " +
        " as PASSWORD_EXPIRY_DATE" +
        " from spt_identity" +
        " where EXTENDED3 in ('982367')  "; //+
      // " AND  inactive = 0  and TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy')) &lt; trunc(SYSDATE)+7 and " +
      // " TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy')) > trunc(SYSDATE)";

      ps = conn.prepareStatement(query);
      System.out.println("Query is ::"+ query);
      resultSet = ps.executeQuery();
      System.out.println("**********Connected to DB Sucessfully @and Executed the Query ***********");
      while (resultSet.next()) {
        System.out.println("Record is "+ resultSet.getString("AID"));
        String[] row = { resultSet.getString("AID"), resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"),resultSet.getString("LOCATION"),resultSet.getString("PASSWORD_EXPIRY_DATE")  };
        data.add(row);
      }
      resultSet.close();
      ps.close();
      conn.close();
      System.out.println ("Records ::" + data);
      writeDataToCsv(data);
    }
  }
  catch (Exception e) {
    System.out.println("Exception Occured"+ e);
  }



  public void writeDataToCsv(List data)
  {
    StringBuilder csvData = new StringBuilder();
    csvData.append("\"AID\",\"FIRSTNAME\",\"LASTNAME\",\"LOCATION\",\"PASSWORD_EXPIRY_DATE\"\n");
    for (String[] row : data) {
      csvData.append("\"").append(row[0]).append("\",")
        .append("\"").append(row[1]).append("\",")
        .append("\"").append(row[2]).append("\",")
        .append("\"").append(row[3]).append("\",")
        .append("\"").append(row[4]).append("\"\n");

      System.out.println("End of CSV");
    }
    String fileName = "Password Expiry Notification For Loc 982367"+".csv";
    sendEmail(csvData, fileName, "sabitha.r@bestbuy.com", context);
  }



  public void sendEmail(StringBuilder csvData, String fileName, String strToAddress, SailPointContext context) throws Exception {
    // Prepare email arguments
    Map mailargs = new HashMap();
    EmailTemplate template = context.getObjectByName(EmailTemplate.class, "OIM Weekly Password Expiry Notification For Loc 982367");
    if (template == null) {
      throw new Exception("Email template not found: " );
      System.out.println("Email template not found: " );
    }
    System.out.println("Hello 1");
    // Create email options and add CSV attachment
    EmailOptions mailopts = new EmailOptions();
    mailopts.setTo("sabitha.r@bestbuy.com");
    System.out.println("Hello 2");
    EmailFileAttachment attachment = new EmailFileAttachment(fileName, EmailFileAttachment.MimeType.MIME_CSV, csvData.toString().getBytes("UTF-8"));
    mailopts.addAttachment(attachment);
    System.out.println("Hello 3");
    // Send the email
    context.sendEmailNotification(template, mailopts);
  }


  </Source>
</Rule>

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1733988671080" id="0ad8294093ac119b8193b9c75e683c1c" language="beanshell" modified="1737551662925" name="Test SAB Report Rule" significantModified="1737551662925">
  <Source>
  import java.io.File;
  import java.io.FileWriter;
  import java.io.IOException;
  import java.text.SimpleDateFormat;
  import java.util.ArrayList;
  import java.util.Calendar;
  import java.util.Arrays;
  import java.util.Date;
  import java.util.HashMap;
  import java.util.Iterator;
  import java.util.List;
  import java.util.Map;
  import java.util.StringTokenizer;
  import sailpoint.api.Explanator;
  import sailpoint.api.SailPointContext;
  import sailpoint.object.Attributes;
  import sailpoint.object.Custom;
  import sailpoint.object.Entitlement;
  import sailpoint.object.Filter;
  import sailpoint.object.Identity;
  import sailpoint.object.IdentityEntitlement;
  import sailpoint.object.LiveReport;
  import sailpoint.object.ManagedAttribute;
  import sailpoint.object.ManagedAttribute.Type;
  import sailpoint.object.QueryOptions;
  import sailpoint.object.Sort;
  import sailpoint.task.Monitor;
  import sailpoint.tools.GeneralException;
  import sailpoint.tools.Util;
  import java.util.HashMap;
  import sailpoint.object.EmailFileAttachment;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
  
  import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;

 
public Object identintityEntitelmentListCNValue(List keyList)
  {
   
       System.out.println("Enter identintityEntitelmentListCNValue method keyList " + keyList);

	  List identityEntitlementListValues = new ArrayList();
		
    try {
    	if(keyList != null) {
        
        
    		
    		int length = keyList.size();
    		
               System.out.println("Enter identintityEntitelmentListCNValue method length " + length);

    		for (int i = 0 ; i  &lt; length ; i++) {
                
    			String val = keyList.get(i);
                         System.out.println("Enter identintityEntitelmentListCNValue method length " + val);


                Filter attributeFilter = Filter.eq("attribute","memberOf");
                Filter typeFilter = Filter.eq("type","group");
                Filter appFilter = Filter.eq("application.name","Active Directory NA");

                Filter value1 = Filter.eq("displayName",val);


                Filter andfilter = Filter.and(attributeFilter,typeFilter,value1,appFilter);

                QueryOptions qo =  new QueryOptions();

                qo.addFilter(andfilter);
             List managedAttributes = context.getObjects(ManagedAttribute.class,qo);
          
                 System.out.println("Enter identintityEntitelmentListCNValue method managedAttributes " + managedAttributes.size());

             
             if(managedAttributes != null) {
          	   for(ManagedAttribute managedAttribute : managedAttributes) {
                 
                                  System.out.println("Enter identintityEntitelmentListCNValue method managedAttribute " + managedAttribute.getValue());

          		   identityEntitlementListValues.add(managedAttribute.getValue());
          		   
          		   
          	   }
          	   
             }
    					   
    				   }
        
                 System.out.println("Enter identintityEntitelmentListCNValue method identityEntitlementListValues " + identityEntitlementListValues);

          
    		return identityEntitlementListValues;
           
    	}
    	 
    
    }catch (Exception ex) {
    	
    }
   return identityEntitlementListValues;
  }
 public Object writeDataToCsv(List usersList)
  {
    String filename = null;
 String outputCSVPath ="/tmp/";
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("UTF-8");
      sb.append("\n");
      sb.append("UniqueName");
      sb.append(",");
      sb.append("User_UniqueName");
      sb.append(",");
      sb.append("PasswordAdapter");
      sb.append(",");
      sb.append("PurchasingUnit");
      sb.append("\n");
      for ( Map recorde : usersList) {

        if(recorde != null) {
          sb.append(recorde.get("UniqueName"));
          sb.append(",");
          sb.append(recorde.get("User_UniqueName"));
          sb.append(",");
          sb.append(recorde.get("Password Adapter"));
          sb.append(",");
          sb.append(recorde.get("Purchasing Unit"));
          sb.append("\n");
        }

      }
      File src = new File(outputCSVPath);
      if (src.isDirectory()) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        filename = "SAPAribaUsers-" + format.format(new Date()) + ".csv";
        File file = new File(outputCSVPath, filename);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        System.out.println("[SAPAribaReportsScheduler] :: generateCSVFile()-> File generated sucessfully at : "
                           + outputCSVPath + "And File Name is: " + filename);
      } else {
        throw new IOException("Output directory path should not include file name.");
      }
      
      sendEmailReport(context,filename,usersList);
      System.out.println("[SAPAribaReportsScheduler] :: generateCSVFile - END");
     
      return filename;

    }catch (Exception ex) {

    }
    return filename;

  }
 public void sendEmailReport(SailPointContext context, String filename, List usersList)
  {
	  String[] VALID_COLUMN_HEADERS_FOR_REPORT = { "UniqueName","User_UniqueName","PasswordAdapter","PurchasingUnit"};

	    List indexColumns = Arrays.asList(VALID_COLUMN_HEADERS_FOR_REPORT);
	    
	    String csvData = Util.listToCsv(indexColumns);
	    String csvData1 = null;
	    
	      List resourceObjects = new ArrayList();
		
    try {
for ( Map recorde : usersList) {
   String csn = null;
	  List resourceObject = new ArrayList();
			if(recorde != null) {
				 resourceObject.add(recorde.get("UniqueName"));
				 resourceObject.add(recorde.get("User_UniqueName"));
				 resourceObject.add(recorde.get("Password Adapter"));
				 resourceObject.add(recorde.get("Purchasing Unit"));
				if(resourceObject != null) {
					 csn =Util.listToCsv(resourceObject);
					if(csvData1 != null) {
						 csvData1 = csvData1+ "\n" + csn;
          }else{
            csvData1 = csn;
          }
				 }
				 
			}
			
		}
      

csvData = csvData + "\n" + csvData1;
EmailTemplate template = context.getObjectByName(EmailTemplate.class, "Daily sap Report");
if (template == null){
    throw new Exception("Email template not found: ");
  }
  else{
    System.out.println("Inside template method");
    EmailOptions options = new EmailOptions();
    options.setTo("BBY-DL-IDENTITYGOV@bestbuy.com");

    
    EmailFileAttachment attachment = new EmailFileAttachment(filename, EmailFileAttachment.MimeType.MIME_CSV, csvData.toString().getBytes("UTF-8"));
			options.addAttachment(attachment);
    
    context.sendEmailNotification(template, options);  
    System.out.println("Email has been sent finally");
  }
    }catch (Exception ex) {
    	
    }
	
   
  }
  public Object uniqueName(IdentityEntitlement identityEntitlement) throws GeneralException {
    String value = null;

    System.out.println("uniqueName");
    if(identityEntitlement != null) {
      String appId = identityEntitlement.getApplication().getId();
      String val = identityEntitlement.getValue().toString();
      System.out.println("uniqueName"+val);

      Filter attributeFilter = Filter.eq("attribute","memberOf");
      Filter typeFilter = Filter.eq("type","group");
      Filter appFilter = Filter.eq("application.name","Active Directory NA");

      Filter value1 = Filter.eq("value",val);


      Filter andfilter = Filter.and(attributeFilter,typeFilter,value1,appFilter);

      QueryOptions qo =  new QueryOptions();

      qo.addFilter(andfilter);
      List managedAttributes = context.getObjects(ManagedAttribute.class,qo);

      if(managedAttributes != null) {
        for(ManagedAttribute managedAttribute : managedAttributes) {
          return  managedAttribute.getDisplayName();

        }

      }


    }

    return value;

  }





  public Object identintityEntitelmentList() throws GeneralException {

    System.out.println("Enter identintityEntitelmentList method ");


    List identityEntitlementListValues = new ArrayList();

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -60);


    Date startValue = cal.getTime(); 
    System.out.println("Enter identintityEntitelmentList method startValue " + startValue);

    Calendar cal1 = Calendar.getInstance();

    Date endValue = cal1.getTime(); 

    System.out.println("Enter identintityEntitelmentList method endValue " + startValue);

    Custom objectByName = context.getObjectByName(Custom.class,"BBY-Custom-sap");

    Attributes attributes = objectByName.getAttributes();
    System.out.println("Enter identintityEntitelmentList method endValue " + attributes.getKeys());
    List keys = (List) identintityEntitelmentListCNValue(attributes.getKeys());
    System.out.println("Enter identintityEntitelmentList method keys " + keys);

    Filter startFilter = Filter.ge("created",startValue);
    Filter endFilter = Filter.le("created",endValue);
    Filter contains = Filter.in("value", keys);


    Filter andfilter = Filter.and(startFilter,endFilter,contains);





    QueryOptions qo =  new QueryOptions();

    qo.addFilter(andfilter);

    identityEntitlementListValues = context.getObjects(IdentityEntitlement.class,qo);


    System.out.println("Enter identintityEntitelmentList method identityEntitlementListValues " + identityEntitlementListValues);


    return identityEntitlementListValues;

  }

  public Object  prepare() throws GeneralException {

    System.out.println("Enter the prepare method");


    List  identityEntitlementListValues  = (List) identintityEntitelmentList();
    System.out.println("identityEntitlementListValues" + identityEntitlementListValues);

    List records = new ArrayList();
    if(identityEntitlementListValues != null) {
      for(IdentityEntitlement identityEntitlement :identityEntitlementListValues) {

        System.out.println("identityEntitlement" + identityEntitlement);


        if(identityEntitlement != null) {

          Custom objectByName = context.getObjectByName(Custom.class,"BBY-Custom-sap");

          Attributes attributes = objectByName.getAttributes();


          System.out.println("attributes" + attributes);
          System.out.println("Entitlement" + identityEntitlement.getValue());
          System.out.println("Entitlement true or false" + attributes.containsKey(identityEntitlement.getValue().toString()));
           String uniqueName = (String)uniqueName(identityEntitlement);


          if(uniqueName != null &amp;&amp; attributes.containsKey(uniqueName) ) {
            System.out.println("insideif");



            List keyValues = (List) attributes.get(uniqueName);




            System.out.println("keyValues" + keyValues);

            int length = keyValues.size();
            System.out.println("length" + length);



            for (int i = 0 ; i &lt; length ; i++) {

              Map recorde = new  HashMap();


              recorde.put("UniqueName",uniqueName);

              recorde.put("Password Adapter","Password Adapter");
              recorde.put("User_UniqueName",identityEntitlement.getIdentity().getName());

              recorde.put("Purchasing Unit",keyValues.get(i));

              //return recorde;

              records.add(recorde);


            }



          }




        }

      }


    }

    return records;


  }

  System.out.println("identityEntitlement" );


  List recordsValuese = prepare();

  String fileValue = (String) writeDataToCsv(recordsValuese);


  return fileValue;
  </Source>
</Rule>

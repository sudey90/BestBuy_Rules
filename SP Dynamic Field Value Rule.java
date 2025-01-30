<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1662746604653" id="0ada675983231b498183236bb46c0032" language="beanshell" modified="1719893806465" name="SP Dynamic Field Value Rule" type="FieldValue">
  <Description>
    This rule uses information in the field that calls the rule to dynamically
    call a method in the library SPCONF Field Value Rules Library. Each field
    that calls this rule must have a corresponding information in the Custom object
    "SPCONF Field Value Mappings Custom"
  </Description>
  <ReferencedRules>
    <Reference class="sailpoint.object.Rule" id="0ada675983231b498183236c0e500088" name="SPCONF Field Value Rules Library"/>
    <Reference class="sailpoint.object.Rule" id="0ada675983231b498183236c10310089" name="SP Field Value Rules Library"/>
  </ReferencedRules>
  <Signature returnType="Object"/>
  <Source>
  import org.apache.commons.logging.Log;
  import org.apache.commons.logging.LogFactory;
  import sailpoint.api.SailPointContext;
  import sailpoint.object.Custom;
  import sailpoint.tools.GeneralException;
  import sailpoint.object.Script;     
  import sailpoint.object.Bundle;

  import java.util.List;
  import java.util.ArrayList;

  private Log logger = LogFactory.getLog("rule.SP.FieldValue.RulesLibrary");

  Object returnVal=null;
  String appName=null;
  ArrayList combinedListFields = new ArrayList();

  System.out.println("Enter Field Value Selector");
  System.out.println("Have field: " + field);

  try
  {
    Custom mappingObj = context.getObjectByName(Custom.class, "SPCONF Field Value Mappings Custom");
    String fieldName = field.getName();
    //String appName = field.getApplication();
System.out.println("Field Name is : " + fieldName);
    if(application!=null){
      System.out.println("application is : " + application);
      appName = application.getName();
    }else if (template!=null){
      System.out.println("template is : " + template.getName());
      appName = template.getApplication().getName();
    }


    System.out.println("Application name in SP Dynamic Field Value Rule is "+appName);
    String appCommonFields="ApplicationCommonFields";
    String templateOp = field.getTemplate();

    String op = "Create";
    if (templateOp != null) {
      op = templateOp;
    }
    System.out.println("In SP Dynamic Field Value Rule Object argument operation value is "+operation.toString()+" and templateOp is  "+op);
    if (mappingObj != null &amp;&amp; mappingObj.get(appName) != null &amp;&amp; mappingObj.get(appCommonFields) != null) {
      Object appMapping = mappingObj.get(appName);

      Object appCommonMapping = mappingObj.get(appCommonFields);
      List&lt;Field> listField = appMapping;
      List&lt;Field> commonField = appCommonMapping;
      //listField.addAll(appCommonMapping);
      combinedListFields.addAll(listField);
      combinedListFields.addAll(commonField);

      Field selectedField = null;
      for (Field f : combinedListFields) {
        if (f.getName() != null &amp;&amp; f.getName().equals(fieldName)) {
          selectedField = f;
          break;
        }
      }

      if (selectedField != null) {
        switch (selectedField.getType().toLowerCase()) {
          case "attribute":
          returnVal = getIdentityAttribute(context, identity, op, fieldName, selectedField.getValue().toString(), selectedField.getDefaultValueAsString());
          break;
          case "rule":
          returnVal = getRuleResult(context, identity, (Object)selectedField.getValue(), selectedField.getDefaultValueAsString(), op,role,appName);
          break;
          case "velocity":
          returnVal = getVelocityResult(selectedField.getValue(), identity);
          if(Util.isNullOrEmpty(Util.otoa(returnVal)) &amp;&amp;selectedField.getValue().toString().contains("email") &amp;&amp; "Breeze".equals(appName) ){
            System.out.println("SP Dynamic Field Value Rule : velocity attribute: return value not available for "+selectedField.getValue());
            returnVal="yet_to_create@bby.com";
          }

          
          if(Util.isNotNullOrEmpty(Util.otoa(returnVal)) @and selectedField.getValue().contains("firstname") @and "Star Resource".equals(appName) ){
            System.out.println("SP Dynamic Field Value Rule : velocity attribute: return value for "+selectedField.getValue());
            returnVal=returnVal.toUpperCase();
            if (returnVal != null  @and returnVal.contains(" ")) {
              returnVal = returnVal.replaceAll("\\s+", "");
            }							  
          }

          if(Util.isNotNullOrEmpty(Util.otoa(returnVal)) @and selectedField.getValue().contains("lastname") @and "Star Resource".equals(appName) ){
            System.out.println("SP Dynamic Field Value Rule : velocity attribute: return value for "+selectedField.getValue());	
            returnVal=returnVal.toUpperCase();    
            if (returnVal != null  @and returnVal.contains(" ")) {
              returnVal = returnVal.split(" ")[0].replaceAll("\\s+", "");
            } 
          }

          break;
          default:
          throw new GeneralException("Invalid option in Field Value");

        }
        if(returnVal!=null)
          System.out.println("SP Dynamic Field Value Rule : returnVal of "+selectedField.getValue()+"is "+returnVal.toString());
      }
    }
    else{
      //For legacy compatibility
      returnVal = executeLegacy(context,identity,field).toString();
    }
  }
  catch (GeneralException err){logger.error("Error in SP Dynamic Field Value Rule " + err.getMessage());}

  logger.trace("Result: " + returnVal);
  logger.trace("Exit Field Value Selector");

  return returnVal;


  </Source>
</Rule>

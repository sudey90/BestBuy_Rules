<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1732210001692" id="0ad82989934f1d2b81934fc3071c037d" language="beanshell" modified="1732657219119" name="Rule-AD-EDSVAType-FieldValue" significantModified="1732657219119" type="FieldValue">
  <Description>This rule can be used to generate a field value (eg - an account name) using data from the given Identity. If this rule is run in the context of a workflow step then the arguments passed into the step will also be available. Also, any field values that have been processed so far from the policy related to the Application/Role will be available.</Description>
  <Signature returnType="String"/>
  <Source>import sailpoint.object.Application;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AccountRequest.Operation;

  String edsVAType="100";

  System.out.println("Active Directory account operation : Calculating edsVAType for operation :  "+accountRequest.getOperation());
  System.out.println("Active Directory account operation : Confirming application name in account request :  "+accountRequest.getApplicationName());
  
    if(accountRequest.getOperation().equals(AccountRequest.Operation.Enable)){
    System.out.println("Active Directory Enable Account");
    Application iiqApp = context.getObjectByName(Application.class, "IIQ");
    String usrSecSus=identity.getStringAttribute("securitysuspended");
    //System.out.println("Active Directory Enable Account : project is :  "+project.toXml());
    //System.out.println("Active Directory Enable Account : IIQ account request in project :securitysuspended is :  "+project.getCreateOrModifyRequest(iiqApp).getAttributeRequest("securitysuspended").getValue());
    if(project.getPlan("IIQ")!=null){
      System.out.println("Active Directory Enable Account : IIQ provisioning plan in project is :  "+project.getPlan("IIQ").toXml()); 
      AttributeRequest chkSecSus=project.getPlan("IIQ").getAccountRequests().get(0).getAttributeRequest("securitysuspended");
      if(chkSecSus!=null){
        String secSus=chkSecSus.getValue();
        System.out.println("Active Directory Enable Account : IIQ account request in project :securitysuspended is :  "+secSus);
        if(secSus!=null @and secSus.equalsIgnoreCase("No")){
          edsVAType="100";

        }
      }

    }else if(usrSecSus!=null @and usrSecSus.equalsIgnoreCase("accsuspended")){
      edsVAType="888";
    }
  }else if(accountRequest.getOperation().equals(AccountRequest.Operation.Disable)){
    System.out.println("Active Directory Disable Account");
    Application iiqApp = context.getObjectByName(Application.class, "IIQ");
    //System.out.println("Active Directory Disable Account : project is :  "+project.toXml());
    //System.out.println("Active Directory Disable Account : IIQ account request in project :securitysuspended is :  "+project.getCreateOrModifyRequest(iiqApp).getAttributeRequest("securitysuspended").getValue());
    if(project.getPlan("IIQ")!=null){
      System.out.println("Active Directory Disable Account : IIQ provisioning plan in project is :  "+project.getPlan("IIQ").toXml()); 
      AttributeRequest chkSecSus=project.getPlan("IIQ").getAccountRequests().get(0).getAttributeRequest("securitysuspended");
      if(chkSecSus!=null){
        String secSus=chkSecSus.getValue();
        System.out.println("Active Directory Disable Account : IIQ account request in project :securitysuspended is :  "+secSus);
        if(secSus!=null @and secSus.equalsIgnoreCase("accsuspended")){
          edsVAType="888";
        }
      }else{
        edsVAType="999";
      }

    }else{
      edsVAType="999";
    }
  }


  System.out.println("Active Directory account operation : Final edsVAType is :  "+edsVAType); 
  
  return edsVAType;</Source>
</Rule>

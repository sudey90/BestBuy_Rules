<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1732654883330" id="0ad82989934f1d2b81936a47620272d1" language="beanshell" modified="1732657006322" name="Rule-AD-HideExchange-FieldValue" significantModified="1732657006322" type="FieldValue">
  <Description>This rule can be used to generate a field value (eg - an account name) using data from the given Identity. If this rule is run in the context of a workflow step then the arguments passed into the step will also be available. Also, any field values that have been processed so far from the policy related to the Application/Role will be available.</Description>
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
      <Argument name="identity" type="Identity">
        <Description>
          The Identity object that represents the user needing the field value.
        </Description>
      </Argument>
      <Argument name="link" type="Link">
        <Description>
          The sailpoint.object.Link that is being acted upon. If the link is not applicable,
          this value will be null.
        </Description>
      </Argument>
      <Argument name="group" type="AccountGroupDTO">
        <Description>
          The sailpoint.web.group.AccountGroupDTO that is being acted upon. If the AccountGroupDTO
          is not applicable, the value will be null.
        </Description>
      </Argument>
      <Argument name="project" type="ProvisioningProject">
        <Description>
          The provisioning project being acted upon. If a provisioning project is not applicable,
          the value will be null.
        </Description>
      </Argument>
      <Argument name="accountRequest" type="ProvisioningPlan.AccountRequest">
        <Description>
          The account request. If an account request is not applicable, the value will be null.
        </Description>
      </Argument>
      <Argument name="objectRequest" type="ProvisioningPlan.ObjectRequest">
        <Description>
          The object request. If an object request is not applicable, the value will be null.
        </Description>
      </Argument>
      <Argument name="role" type="Bundle">
        <Description>
          The role with the template we are compiling. If the role is
          not applicable, the value will be null.
        </Description>
      </Argument>
      <Argument name="application" type="Application">
        <Description>
          The sailpont.object.Application with the template we are compiling. If the application
          is not applicable, the value will be null.
        </Description>
      </Argument>
      <Argument name="template" type="Template">
        <Description>
          The Template that contains this field.
        </Description>
      </Argument>
      <Argument name="field" type="Field">
        <Description>
          The current field being computed.
        </Description>
      </Argument>
      <Argument name="current" type="Object">
        <Description>
          The current value corresponding to the identity or account attribute that the field represents.
          If no current value is set, this value will be null.
        </Description>
      </Argument>
      <Argument name="operation" type="ProvisioningPlan.Operation">
        <Description>
          The operation being performed.
        </Description>
      </Argument>
    </Inputs>
    <Returns>
      <Argument name="value">
        <Description>
          The string value created.
        </Description>
      </Argument>
    </Returns>
  </Signature>
  <Source>import sailpoint.object.Application;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AccountRequest.Operation;

  String hideExchange="false";

  System.out.println("Active Directory account operation : Calculating hideExchange for operation :  "+accountRequest.getOperation());
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
          hideExchange="false";

        }
      }

    }else if(usrSecSus!=null @and usrSecSus.equalsIgnoreCase("accsuspended")){
      hideExchange="false";
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
          hideExchange="false";
        }
      }else{
        hideExchange="true";
      }

    }else{
      hideExchange="true";
    }
  }


  System.out.println("Active Directory account operation : Final hideExchange is :  "+hideExchange); 
  
  return hideExchange;</Source>
</Rule>

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1662746613657" id="0ada675983231b498183236bd7990057" language="beanshell" modified="1726544777156" name="AD-Attr-TransformationRule" type="IdentityAttributeTarget">
  <Description>Identity attribute target rules are used to transform identity attribute values that are being pushed to targets.</Description>
  <ReferencedRules>
    <Reference class="sailpoint.object.Rule" id="0ada675983231b498183236bdf970067" name="AD-Attr-Transform-RuleLibrary"/>
  </ReferencedRules>
  <Signature returnType="Object">
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
      <Argument name="value" type="Object">
        <Description>
           The value of the identity attribute.  Note that this could be single value or a list of values.
        </Description>
      </Argument>
      <Argument name="sourceIdentityAttribute">
        <Description>
           The sailpoint.object.ObjectAttribute for this target.
        </Description>
      </Argument>
      <Argument name="sourceIdentityAttributeName">
        <Description>
           The name of the identity attribute for this target.
        </Description>
      </Argument>
      <Argument name="sourceAttributeRequest">
        <Description>
           The sailpoint.object.ProvisioningPlan.AttributeRequest that is
           setting the attribute on the identity.
        </Description>
      </Argument>
      <Argument name="target">
        <Description>
           The sailpoint.object.AttributeTarget that is being processed.
        </Description>
      </Argument>
      <Argument name="link">
        <Description>
           The sailpoint.object.Link that is being processed.
        </Description>
      </Argument>
      <Argument name="identity">
        <Description>
           The sailpoint.object.Identity that is being processed.
        </Description>
      </Argument>
      <Argument name="project">
        <Description>
           The sailpoint.object.ProvisioningProject that has the changes that
           are being requested.
        </Description>
      </Argument>
    </Inputs>
    <Returns>
      <Argument name="attributeValue">
        <Description>
          The transformed value that will be pushed to the target.
        </Description>
      </Argument>
    </Returns>
  </Signature>
  <Source>import sailpoint.object.Identity;
  
  
   String userDN = "";
   

    switch (sourceIdentityAttributeName) {

      case "manager": 
      attributeValue = getManagerDN(context,identity,target,value);
      break;
      
      case "effectiveStartDate":
       String effectiveStartDate = value;
      attributeValue = getHRHireDate(context,identity,target, effectiveStartDate);
      break;
	  
	  case "executiveAssistant":
	  attributeValue = getExecAsst(context,identity,target,value);
	  break;
	  
	  case "displayName":
	  attributeValue = getDisplayName(context,identity,target);
	  break;
	  
	  case "grename":
	  attributeValue = getCompany(context,identity,target);
	  break;
	  
	  case "vendorName":
	  attributeValue = getCompany(context,identity,target);
	  break;
      
      default: 
      break;
    }
 
 
 return attributeValue;</Source>
</Rule>

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1722029085251" id="0ada66a590e810068190f0ee9243409e" language="beanshell" modified="1737710239425" name="BBY Rule AD Account Update">
  <Source>



  import org.apache.log4j.Logger;

  import sailpoint.api.Aggregator;
  import sailpoint.api.IdentityService;
  import sailpoint.api.Provisioner;
  import sailpoint.api.SailPointContext;
  
  import sailpoint.connector.Connector;
  import sailpoint.object.Attributes;
  import sailpoint.object.Application;
  import sailpoint.object.Identity;
  import sailpoint.object.Link;
  import sailpoint.object.ProvisioningPlan;
  import sailpoint.object.*;
  import sailpoint.object.ProvisioningPlan.AccountRequest;
  import sailpoint.object.ProvisioningPlan.AttributeRequest;
  import sailpoint.object.ResourceObject;
  import sailpoint.object.TaskResult;
  import java.util.*;
  import sailpoint.tools.Util;

  String accountDN = null;

  Provisioner provisioner= new Provisioner(context);

  Identity id = context.getObjectByName(Identity.class,"A3118997");

  if (id != null) 
  {
    Application app = context.getObjectByName(Application.class, "Active Directory NA");
    if (app != null) 
    {
      IdentityService is = new IdentityService(context);
      List links = is.getLinks(id, app);
      if (links != null) 
      {
        Link link = links.get(0);
        accountDN = link.getAttribute("distinguishedName");
      }
      context.decache(app);
    }
  }

  ProvisioningPlan plan= new ProvisioningPlan();
  AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, "Active Directory NA", null, accountDN);
  //AttributeRequest attrReq = new AttributeRequest("cn", "Nedunuri, Sai Lakshmi Jahnavi 3029291" );
  //acctReq.add(new ProvisioningPlan.AttributeRequest("cn", ProvisioningPlan.Operation.Set, "Pickhart, Christy 3005922"));
 // acctReq.add(new ProvisioningPlan.AttributeRequest("AC_NewParent", ProvisioningPlan.Operation.Set, "OU=Default,OU=Users,OU=Best Buy,OU=Users and Groups,DC=na,DC=bestbuy,DC=com"));
  acctReq.add(new ProvisioningPlan.AttributeRequest("AC_NewParent",ProvisioningPlan.Operation.Set,"OU=Disabled,OU=Users,OU=StaleAccounts,DC=na,DC=bestbuy,DC=com");
  acctReq.add(attrReq);
  plan.add(acctReq);
  plan.setIdentity(id);

  //return plan;
  // provisioner.setNoLocking(true);
  //provisioner.setOptimisticProvisioning(true);
  ProvisioningProject project = provisioner.compile(plan);
  provisioner.execute(plan);	
  return project;



  </Source>
</Rule>

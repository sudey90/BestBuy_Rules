<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1726091193040" id="0ad8299a91e117d48191e30d6ad00bbc" language="beanshell" modified="1732685572276" name="BBY Password Warning Rule" significantModified="1732685572276">
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
         import sailpoint.tools.GeneralException;
          import java.util.Calendar;
          import java.util.List;
          import java.util.Date;
          import java.text.SimpleDateFormat;
          import sailpoint.api.SailPointContext;
          import sailpoint.api.TaskManager;
          import java.util.HashMap;
          import java.util.Map;
          import sailpoint.object.EmailTemplate;
          import sailpoint.object.Attributes;
          import sailpoint.object.EmailOptions;
          import sailpoint.object.Filter;
          import sailpoint.object.QueryOptions;
          import sailpoint.object.Identity;
          import sailpoint.object.Link;
          import java.util.concurrent.TimeUnit;
          import sailpoint.object.TaskResult;
          import sailpoint.object.TaskResult.CompletionStatus;
          import sailpoint.object.TaskSchedule;
          import sailpoint.task.AbstractTaskExecutor;
					import sailpoint.tools.*;

          boolean hasCompletedSuccessfully = false;
          boolean isStillPending = false;

          boolean isTaskPending = false;
          String completionStatusValue = null;
          Date startTime = new Date();

          //to put into CustomObject
          int maxRetryCount = 3;
          String taskName = "Active Directory NA Delta Target Aggregation";
          int waitForNextCheckCycle = 1000*60*5; // 5 mins to wait for next check
          int maxPendingCheckTimeLimit = 1000*60*30; //30 Minutes to keep checking in each retry cycle
          TaskResult taskResult = null;

          public boolean hasMaxPendingCheckTimeBreached(Date endTime){
            long diffInMillies = Math.abs(endTime.getTime() - startTime.getTime());
            long diff = TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return (diff > maxPendingCheckTimeLimit)?true:false;
          }


          public void setCurrentTaskResultProperties() throws GeneralException {
            System.out.println("setCurrentTaskResultProperties Started.");
            taskResult = context.getObjectByName(TaskResult.class, taskName);
            if(null != taskResult){
              Attributes taskResultAttr = taskResult.getAttributes();
              isTaskPending = (null != taskResultAttr.get("eventsPending")) ? true : false;
              if (!isTaskPending) {
                CompletionStatus completionStatus = taskResult.getCompletionStatus();
                if(null != completionStatus){
                  completionStatusValue = completionStatus.name();
                  System.out.println("completionStatusValue: " + completionStatusValue);
                }else{
                  isTaskPending = true;
                }
              }
            }
            context.decache();
            System.out.println("setCurrentTaskResultProperties Ended.");
          }

          public String getAggregationTaskStatus() throws GeneralException {
            System.out.println("*****************Start of getAggregationTaskStatus method ********************");
            setCurrentTaskResultProperties();
            String taskStaus = "";
            if (isTaskPending) {
              taskStaus = "PENDING";
            } else {
              if ("SUCCESS".equalsIgnoreCase(completionStatusValue)) {
                taskStaus = "SUCCESS";
              } else if ("TERMINATED".equalsIgnoreCase(completionStatusValue)) {
                taskStaus = "TERMINATED";
              } else if ("ERROR".equalsIgnoreCase(completionStatusValue)) {
                taskStaus = "ERROR";
              }
            }
            System.out.println("*****************End of getAggregationTaskStatus method ********************");
            return taskStaus;  		
          }

          public void runScheduleTask() throws GeneralException {
            System.out.println("*****************Start of runScheduleTask method ********************");
            TaskManager taskManager = new TaskManager(context);
            taskManager.run(taskName, new Attributes());
            System.out.println("*****************End of runScheduleTask method ********************");
          }

         private void sendPwdExpiryNotification(ArrayList users,String templateName, boolean isAboutToExpire){
            System.out.println("sendPwdExpiryNotification Started.");
            EmailTemplate template = context.getObjectByName(EmailTemplate.class, templateName);
            int counter=0;
           	
            try {
              for (HashMap user : users) {
                String email = (String) user.get("email");
                String userLogin = user.get("name");
                if (email == null) {
                    System.out.println("Email is null for user: " + userLogin + ". Skipping...");
                    continue;
                }
                
                
                EmailOptions option = new EmailOptions();
                HashMap variables = new HashMap();
                variables.put("USR_LOGIN", userLogin);
                variables.put("firstName", user.get("firstname"));
                variables.put("lastName", user.get("lastname")); 

                if (isAboutToExpire) {
                  variables.put("interval", user.get("DAYS_LEFT"));
                  variables.put("EXPIRE_DATE", user.get("Password_expiry_date"));
                } 
                option.setVariables(variables);
                option.setTo(email);
                option.setSendImmediate(true);
                option.setNoRetry(true);
                context.sendEmailNotification(template, option);
                counter++;
                System.out.println("email sent successfully for UserLogin " +userLogin +"  And the Count :"+counter + " Out of :"+users.size());
               
                //System.out.println("Count :"+counter);
              }
            } catch (GeneralException e) {
              System.out.println("Exception Occured " + e.getMessage());
              
            }
			    }

          public int daysLeftInExpiry(Date userExpiryDateAsDate) {
            return (int) TimeUnit.MILLISECONDS.toDays(userExpiryDateAsDate.getTime() - (new Date()).getTime());
          }

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
  
          public List getUsers(String sqlQuery) {
            System.out.print("BBYPasswordWarning | getUsers Method : Fetching users to send email");
            ArrayList users = new ArrayList();
            try {
           Iterator iterator = context.search("sql:" + sqlQuery.toString(), null, null);
            
              while ((null != iterator) @and iterator.hasNext()) {
                Object[] thisRecord = (Object[]) iterator.next();
                String userLogin = (String) thisRecord[0];
                String email = (String) thisRecord[1];
                String daysLeft = thisRecord[2].toString();
                String firstName = (String) thisRecord[3];
                String lastName = (String) thisRecord[4];
                String PasswordExpiryDate = (String) thisRecord[5];                
                HashMap userDetails = new HashMap();
                userDetails.put("name", userLogin);
                userDetails.put("email", email);
                userDetails.put("DAYS_LEFT", daysLeft);
                userDetails.put("firstname", firstName);
                userDetails.put("lastname", lastName);
                userDetails.put("Password_expiry_date", PasswordExpiryDate);
                users.add(userDetails);
              }
 						sailpoint.tools.Util.flushIterator(iterator);
            } catch (GeneralException e) {
              System.out.println("General Exception Occured " + e.getMessage());
             
            }
            
           	 finally {
              //sailpoint.tools.Util.flushIterator(iterator);
              context.decache();
									}
            return users;
          }

            public void sendPasswrodNotification() throws GeneralException {
              String aboutToExpireQuery = "SELECT name, email, TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy'))-TRUNC(SYSDATE) AS DAYS_LEFT, firstname, lastname, Password_expiry_date FROM spt_identity WHERE inactive = 0 and  type in ('Dummy') and TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy')) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE+6)";
          //    String expiredQuery = "SELECT name, email, TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy'))-TRUNC(SYSDATE) AS DAYS_LEFT, firstname, lastname, Password_expiry_date FROM spt_identity WHERE inactive = 0 and name ='A3101768' and type in ('User') and TRUNC(to_DATE(PASSWORD_EXPIRY_DATE,'MM/dd/yyyy')) = TRUNC(SYSDATE)-2";      

              // Fetch users whose passwords are about to expire
              System.out.println("SQL query for about to expire users is :: " + aboutToExpireQuery);
              ArrayList aboutToExpireUsers = getUsers(aboutToExpireQuery);                        
              System.out.println("About to expire user list is " + aboutToExpireUsers);
              sendPwdExpiryNotification(aboutToExpireUsers, "BBY Password Warning Notification", true);

              // Fetch users whose passwords have already expired
           //   System.out.println("SQL query for expired users is :: " + expiredQuery);
             // ArrayList expiredUsers = getUsers(expiredQuery);                        
              //System.out.println("Expired user list is " + expiredUsers);
              //sendPwdExpiryNotification(expiredUsers, "BBY Password Expiry Notification", false);
            }

          public void triggerWarningNotification() throws GeneralException {
            System.out.println("*****************Start of triggerWarningNotification method ********************");
            String taskResultStatus = getAggregationTaskStatus();
            switch (taskResultStatus) {
              case "PENDING":
              System.out.println("*****************Entered into PENDING CASE ********************");
              isStillPending = true;
              hasCompletedSuccessfully = false;
              break;
              case "SUCCESS":
              isStillPending = false;
              hasCompletedSuccessfully = true;
              System.out.println("*****************Entered into SUCCESS CASE ********************");
              System.out.println("*****************Trigger the Email Notification ********************");
              sendPasswrodNotification();
              break;
              case "TERMINATED":
              isStillPending = false;
              hasCompletedSuccessfully = false;
              System.out.println("*****************Entered into TERMINATED CASE ********************");
              runScheduleTask();
              break;
              case "ERROR":
              isStillPending = false;
              hasCompletedSuccessfully = false;
              System.out.println("*****************Entered into ERROR CASE ********************");
              runScheduleTask();
              break;
            }
            System.out.println("*****************End of triggerWarningNotification method ********************");
          }

          public String getDateAsString(Date date, String format) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
          }

          public Date addDays(Date date) {
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.add(Calendar.HOUR, 90 * 24);
            return now.getTime();
          }


          private void sendNotificationToSupport(){
            System.out.println("sendNotificationToSupport Started.");
            try {
              EmailTemplate template = null;

              template = context.getObjectByName(EmailTemplate.class, "BBY Newhire Creation Notification");//BBY AD Delta Job Failure Notification
							toEmail = "vijaysagar.thatipamula@bestbuy.com";
              System.out.println("*****TEMPLATE******"+template);
              EmailOptions options = new EmailOptions();
              options.setTo(toEmail);
              Map args = new HashMap();
              context.sendEmailNotification(template, options);
            } catch (GeneralException e) {
              System.out.println("Exception: " + e.getLocalizedMessage());
            }
            System.out.println("sendNotificationToSupport Ended.");
          }

          int retryCount = 1;
          do {
            System.out.println("\n\nCycle "+retryCount+" Running");
            triggerWarningNotification();

            if(hasCompletedSuccessfully) {
              break;
            }else {
              if(!isStillPending) {  
                System.out.println("Cycle "+retryCount+" Completed");
                //resetting startTime for current cycle
                startTime = new Date();
                retryCount++;  

              }
              Thread.sleep(waitForNextCheckCycle);
            }

            if(hasMaxPendingCheckTimeBreached(new Date())){
              System.out.println("Task completing abnormally due to max Pending Check Time Breached.");
              break;
            }

          }while(retryCount &lt;= maxRetryCount);

          if(retryCount > maxRetryCount){
            System.out.println("Trigger Email to Identity Governance DevOPS Team");
            sendNotificationToSupport();
          }
  </Source>
</Rule>
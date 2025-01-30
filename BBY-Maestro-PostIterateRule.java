<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule created="1718173537771" id="0ada67128ff211bb81900b1f95eb50d1" language="beanshell" name="BBY-Maestro-PostIterateRule" type="PostIterate">
  <Description>This rule is called after the connector processes the data in a file.

Stats is a Map passed in by the connector with the information it has gathered. Custom keys can be added here if necessary for validation.</Description>
  <Signature returnType="void">
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
      <Argument name="application">
        <Description>
          Application being iterated.
        </Description>
      </Argument>
      <Argument name="schema">
        <Description>
          Schema requested during iteration.
        </Description>
      </Argument>
      <Argument name="stats">
        <Description>
          A map of the stats for the current file that was just iterated.
          Contains keys:
            fileName : (String) filename of the file about to be processed
            absolutePath : (String) absolute filename
            length : (Long) length in bytes
            lastModified : (Long) last time the file was updated Java GMT
            columnNames : (List) column names that were used during the iteration
            objectsIterated : (Long) total number of objects iterated during this run
        </Description>
      </Argument>
    </Inputs>
  </Signature>
  <Source>  
	import java.io.*;
	import java.nio.file.Files;
	import java.nio.file.*;
	import java.util.Date;
	import java.text.DateFormat;
	import java.text.SimpleDateFormat;	
	import org.apache.log4j.Logger;
	import org.apache.log4j.Level;

	Logger maestroLogger = Logger.getLogger("bby.maestro.MaestroPostIterateRule");

	maestroLogger.debug("Start BBY-Maestro-PostIterateRule");

	try{

		maestroLogger.debug("stats-----"+stats);  
		String fileName = stats.get("fileName");
		String accountFileName=application.getAttributeValue("file");
		String groupFileName=application.getAttributeValue("group.file");


		maestroLogger.debug("fileName-----"+fileName);
		maestroLogger.debug("accountFileName-----"+accountFileName);  
		maestroLogger.debug("groupFileName-----"+groupFileName);    

		List columnNames = new ArrayList();

		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
		Date date = new Date();

		String d = dateFormat.format(date).toString();

		String userSourcePath = application.getAttributeValue("file");
		String groupSourcePath = application.getAttributeValue("group.file");


		maestroLogger.debug("userSourcePath-----"+userSourcePath);

		Path userSourceParentPath = Paths.get(userSourcePath);


		String destinationPathVal = userSourceParentPath.getParent().toString()+"/ProcessedFolder/test.xml";	

		destinationPathVal = destinationPathVal.replaceAll("/FeedFolder", "");

		maestroLogger.debug("destinationPathVal-----"+destinationPathVal);

		Path destinationPath = Paths.get(destinationPathVal);


		maestroLogger.debug("destinationPath parent-----"+destinationPath.getParent().toString());

		if(accountFileName.contains(fileName)){
			Path temp = Files.move(Paths.get(userSourcePath),Paths.get(destinationPath.getParent().toString()+"/Processed_"+fileName));
			maestroLogger.debug("temp-----"+temp);
			if(temp != null)
			{
				maestroLogger.debug("Account moved successfully");
			}
			else
			{
				maestroLogger.debug("Failed to move account file");
			}
		}

		if(groupFileName.contains(fileName)){
			Path groupTemp = Files.move(Paths.get(groupSourcePath),Paths.get(destinationPath.getParent().toString()+"/Processed_"+fileName));
			maestroLogger.debug("groupTemp-----"+groupTemp);
			if(groupTemp != null)
			{
				maestroLogger.debug("group moved successfully");
			}
			else
			{
				maestroLogger.debug("group to move account file");
			}
		}
	}catch(Exception e){
		maestroLogger.error("error Maestro post iterate rule-----"+e.toString());
	}
	maestroLogger.debug("endMaestro post iterate rule-----");
</Source>
</Rule>

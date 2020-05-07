package com.mark59.servermetrics;


import java.io.File;
import java.io.FileNotFoundException;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAO;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.pojos.WebServerMetricsResponsePojo;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.TargetServerFunctions;


public class ApachePOIExcelRead {
    //private static final String FILE_NAME = "Mark59ServerProfiles.xlsx";

    public static void main(String[] args) {
    	
    	
    	System.out.println(">>>>trying connections");
    	String reqServerProfileName = "localhost_HOSTID";
//    	String reqServerProfileName = "ulldkr013.auiag.corp";
    	String reqTestMode = "N";

        try { // CellType.STRING is assumed

        	Resource resource = new ClassPathResource(
        			AppConstantsServerMetricsWeb.MARK59_SERVER_PROFILES_EXCEL_FILE);    
        	File excelFile = resource.getFile();
        	System.out.println("File excelFile = " + excelFile.getPath()  ); 

        	Workbook workbook = new XSSFWorkbook(excelFile.getPath() );
            
        	Sheet serverprofilesSheet 		  = workbook.getSheet("serverprofiles");
        	Sheet servercommandlinksSheet	  = workbook.getSheet("servercommandlinks");
        	Sheet commandsSheet 			  = workbook.getSheet("commands");
        	Sheet commandparserlinksSheet 	  = workbook.getSheet("commandparserlinks");
        	Sheet commandresponseparsersSheet = workbook.getSheet("commandresponseparsers");
        	
        	ServerProfilesDAO serverProfilesDAO 				= new ServerProfilesDAOexcelWorkbookImpl(serverprofilesSheet); 
        	ServerCommandLinksDAO serverCommandLinksDAO 		= new ServerCommandLinksDAOexcelWorkbookImpl(servercommandlinksSheet);    	
        	CommandsDAO commandsDAO 							= new CommandsDAOexcelWorkbookImpl(commandsSheet);     	
        	CommandParserLinksDAO commandParserLinksDAO 		= new CommandParserLinksDAOexcelWorkbookImpl(commandparserlinksSheet);
        	CommandResponseParsersDAO commandResponseParsersDAO = new CommandResponseParsersDAOexcelWorkbookImpl(commandresponseparsersSheet);
        	
        	
 	    	WebServerMetricsResponsePojo response = 
 	    			TargetServerFunctions.serverResponse(reqServerProfileName, reqTestMode, 
 					serverProfilesDAO, serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO);	

 	    	
 			System.out.println("response : " + response );	
 			System.out.println("<<<<<");	    	
            
 			workbook.close();
//            
//            Iterator<Row> iterator = datatypeSheet.iterator();
//            while (iterator.hasNext()) {
//                Row currentRow = iterator.next();
//                Iterator<Cell> cellIterator = currentRow.iterator();
//                while (cellIterator.hasNext()) {  
//                    Cell currentCell = cellIterator.next();
//                    System.out.print(currentCell.getStringCellValue() + "<--");       	// CellType.STRING is assumed
//                }
//                System.out.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

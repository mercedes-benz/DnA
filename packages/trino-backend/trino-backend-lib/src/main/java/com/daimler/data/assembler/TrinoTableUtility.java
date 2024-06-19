package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.mb.dna.datalakehouse.dto.DataLakeTableColumnDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
import com.mb.dna.datalakehouse.dto.GenerateTableStmtResponseVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrinoTableUtility {

	private static String createTable = "CREATE TABLE IF NOT EXISTS ";
	
	public GenerateTableStmtResponseVO generateCreateTableStatement(List<String> connectorSpecificColTypes, String catalog, String schema, String externalLocationForSchema, DatalakeTableVO tableDetails){
		try {
		GenerateTableStmtResponseVO generateStmtResponse = new GenerateTableStmtResponseVO();
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		String createTableStatement = "";
		boolean canCreate = false;
		if(tableDetails!=null) {
			List<DataLakeTableColumnDetailsVO> columns = tableDetails.getColumns();
			if(columns!=null && !columns.isEmpty()) {
				String tableName = tableDetails.getTableName();
				String tableLocation = externalLocationForSchema + "/" + tableName;
				for(int i=0; i<columns.size(); i++) {
					DataLakeTableColumnDetailsVO columnDetails = columns.get(i);
					if(connectorSpecificColTypes.contains(columnDetails.getDataType())){
						if(i==0) {
							createTableStatement = createTable + catalog + "." + schema + "." + tableName + " (";
							canCreate = true;
						}
						createTableStatement += " " + columnDetails.getColumnName() + " " + columnDetails.getDataType();
						if(i< (columns.size()-1))
							createTableStatement += ",";
						else
							createTableStatement += ")";
					}else {
						MessageDescription tempWarning = new MessageDescription("Datatype "+ columnDetails.getDataType() + " is not supported, couldnt add column " + columnDetails.getColumnName() + ". Please add again by altering table " + tableDetails.getTableName());
						warnings.add(tempWarning);
					}
				}
				if(canCreate) {
					String format= "ORC".equalsIgnoreCase(tableDetails.getDataFormat()) ? "ORC" : "PARQUET";
					createTableStatement += " WITH (  location = '" + tableLocation + "')";
					generateStmtResponse.setGeneratedTable(tableDetails);
				}
				generateStmtResponse.setTableStmt(createTableStatement);
				responseMsg.setSuccess("SUCCESS");
			}else {
				MessageDescription tempWarning = new MessageDescription("No column information provided, could not generate create table statement for table " + tableDetails.getTableName());
				warnings.add(tempWarning);
			}
		}
		responseMsg.setWarnings(warnings);
		generateStmtResponse.setTableStmt(createTableStatement);
		generateStmtResponse.setResponseMsg(responseMsg);
		return generateStmtResponse;
		}catch(Exception e) {
			log.error("Failed to generate create table statements for given catalog {} and schema {}  due to exception {} ", catalog, schema, e.getMessage());
			return null;
		}
	}
}

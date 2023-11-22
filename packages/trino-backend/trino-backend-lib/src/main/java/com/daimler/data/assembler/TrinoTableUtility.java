package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoConnectorRepo;
import com.mb.dna.datalakehouse.dto.DataLakeTableColumnDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
import com.mb.dna.datalakehouse.dto.GenerateTableStmtResponseVO;

@Component
public class TrinoTableUtility {

	@Autowired
	private TrinoConnectorRepo connectorRepo;
	
	public List<String> connectorSpecificDataTypes(String connector){
		Optional<TrinoConnectorNsql> connectorDetails = connectorRepo.findById(connector);
		if(connectorDetails!=null && connectorDetails.get()!=null && connectorDetails.get().getData()!=null && connectorDetails.get().getData().getDataTypes()!=null) {
			return connectorDetails.get().getData().getDataTypes();
		}
		return null;
	}
	
	public GenerateTableStmtResponseVO generateCreateTableStatement(String catalog, DatalakeTableVO tableDetails){
		
		GenerateTableStmtResponseVO generateStmtResponse = new GenerateTableStmtResponseVO();
		GenericMessage responseMsg = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		
		if(tableDetails!=null) {
			List<DataLakeTableColumnDetailsVO> columns = tableDetails.getColumns();
			//column type validation
			//add to warning for the invalid columns
			if(columns!=null && !columns.isEmpty()) {
				
			}
		}
		
		return null;
	}
}

package com.daimler.data.service.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.ForecastAssembler;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.File;
import com.daimler.data.db.repo.forecast.ForecastCustomRepository;
import com.daimler.data.db.repo.forecast.ForecastRepository;
import com.daimler.data.dto.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BaseForecastService extends BaseCommonService<ForecastVO, ForecastNsql, String> implements ForecastService{

	@Autowired
	private StorageServicesClient storageClient;
	
	@Autowired
	private ForecastCustomRepository customRepo;
	@Autowired
	private ForecastRepository jpaRepo;
	@Autowired
	private ForecastAssembler assembler;

	public BaseForecastService() {
		super();
	}
	
	@Override
	public List<InputFileVO> getSavedFiles(String id) {
		List<File> files = customRepo.getSavedFiles(id);
		return assembler.toFilesVO(files);
	}

	@Override
	public List<ForecastVO> getAll( int limit,  int offset, String user) {
		List<ForecastNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}

	@Override
	@Transactional
	public ForecastVO createForecast(ForecastVO vo) throws Exception {
		CreateBucketResponseWrapperDto bucketCreationResponse = storageClient.createBucket(vo.getBucketName(),vo.getCreatedBy(),vo.getCollaborators(), null);
		if(bucketCreationResponse!= null && "SUCCESS".equalsIgnoreCase(bucketCreationResponse.getStatus())) {
			return super.create(vo);
		}else {
			throw new Exception("Failed while creating bucket for Forecast project artifacts to be stored.");
		}
	}
	
	
	
}

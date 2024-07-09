package com.daimler.data.service.storage;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.data.db.entities.StorageNsql;
import com.daimler.data.db.repo.storage.IStorageRepository;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.minio.client.DnaMinioClient;
import com.daimler.data.util.ConstantsUtility;

import io.minio.messages.Bucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConditionalOnProperty(value="storage.startup.createdDateService", havingValue = "true", matchIfMissing = false)
@Component
public class FixCreateDateStartUpService {
	
	@Autowired
	private DnaMinioClient dnaMinioClient;
	
	@Autowired
	private IStorageRepository jpaRepo;

	
	@PostConstruct
	public void init() {
			MinioGenericResponse minioResponse = dnaMinioClient.getAllBuckets("", true);
			List<StorageNsql> entities = jpaRepo.findAll();
			HttpStatus httpStatus = minioResponse.getHttpStatus();
			if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				log.info("Success from list buckets minio client");
				if (!ObjectUtils.isEmpty(minioResponse.getBuckets())) {
					List<Bucket> buckets = minioResponse.getBuckets();
					for(StorageNsql entity : entities) {
						
					try {
						if(entity!=null && entity.getData()!=null) {
							if(entity.getData().getCreatedDate()==null) {
								Optional<Bucket> matchingBucket = buckets.stream().filter(n-> entity.getData().getBucketName().equalsIgnoreCase(n.name())).findFirst();
								if(matchingBucket.isPresent()) {
									entity.getData().setCreatedDate(Date.from(matchingBucket.get().creationDate().toInstant()));
									jpaRepo.save(entity);
									log.info("Update bucket {} with created date successfully during startup", entity.getData().getBucketName());
									}
								}
							}
						}catch(Exception e) {
							log.error("Failed to update created date of bucket {} during startup", entity.getData().getBucketName());
						}
					}
				}
			
		} else {
			log.info("Failure from list buckets minio client");
		}
	}
}

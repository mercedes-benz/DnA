package com.mb.dna.data.service;

import com.mb.dna.data.db.repo.DataikuRepository;
import com.mb.dna.data.utility.DataikuAssembler;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataikuServiceImpl implements DataikuService	{

	@Inject
	private DataikuRepository dataikuRepo;
	
	@Inject
	private DataikuAssembler assembler;
	
	public DataikuServiceImpl() {
		super();
	}
	
	public DataikuServiceImpl(DataikuRepository dataikuRepo, DataikuAssembler assembler) {
		super();
		this.dataikuRepo = dataikuRepo;
		this.assembler = assembler;
	}
	
	
	
}

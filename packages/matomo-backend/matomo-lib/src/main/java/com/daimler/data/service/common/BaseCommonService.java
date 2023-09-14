package com.daimler.data.service.common;

import com.daimler.data.assembler.GenericAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class BaseCommonService<V, T, ID> implements CommonService<V, T, ID>{
    @Autowired
    private JpaRepository<T, ID> jpaRepo;
    @Autowired
    private GenericAssembler<V, T> assembler;
    @Override
    @Transactional
    public V create(V vo) {
        T entity = assembler.toEntity(vo);
        T savedEntity = jpaRepo.save(entity);
        return assembler.toVo(savedEntity);
    }

    @Override
    @Transactional
    public V getById(ID id) {
        Optional<T> entityOptional = jpaRepo.findById(id);
        T entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
        return assembler.toVo(entity);
    }

}

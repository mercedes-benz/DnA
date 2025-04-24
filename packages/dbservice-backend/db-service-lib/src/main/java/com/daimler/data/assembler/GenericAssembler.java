package com.daimler.data.assembler;

public interface GenericAssembler<V, E> {

	V toVo(E e);

	E toEntity(V vo);

}

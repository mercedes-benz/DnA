package com.daimler.data.service.common;

public interface CommonService<V, T, ID> {
    V create(V vo);
    V getById(ID id);
}

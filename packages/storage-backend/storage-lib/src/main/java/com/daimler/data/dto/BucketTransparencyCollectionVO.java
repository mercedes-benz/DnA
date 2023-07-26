package com.daimler.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketTransparencyCollectionVO {
    private Integer totalCount;
    private List<BucketTransparencyVO> records;
}

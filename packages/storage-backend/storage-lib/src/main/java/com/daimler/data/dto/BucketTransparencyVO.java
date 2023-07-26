package com.daimler.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketTransparencyVO {
    private String name;
    private Long bucketSize;
}

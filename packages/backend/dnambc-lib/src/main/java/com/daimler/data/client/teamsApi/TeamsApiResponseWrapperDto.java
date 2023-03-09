package com.daimler.data.client.teamsApi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TeamsApiResponseWrapperDto {
    private List<TeamsApiResponseDto> entries;
    private Integer totalHits;
}

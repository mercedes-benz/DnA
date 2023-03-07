package com.daimler.data.client.teamsApi;

import com.daimler.data.db.jsonb.UserInfo;

import java.util.List;

public interface TeamsApiClient {
    public TeamsApiResponseWrapperDto getTeamsApiUserInfoDetails(String searchTerm);
}

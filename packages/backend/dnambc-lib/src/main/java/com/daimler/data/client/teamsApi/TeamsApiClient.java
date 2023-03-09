package com.daimler.data.client.teamsApi;


import com.daimler.data.dto.userinfo.UsersCollection;

public interface TeamsApiClient {
    public UsersCollection getTeamsApiUserInfoDetails(String searchTerm);
}

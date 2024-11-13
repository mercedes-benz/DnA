package com.daimler.data.service.common;

public interface DeployService {

    void deployFromVersion();

    void deployFromBanch();
    
    void restartDeployedApplication();

}

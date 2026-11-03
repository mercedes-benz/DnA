package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.dto.HarborDashboardResponse;
import com.daimler.data.service.HarborDashboardService;

/**
 * Controller is responsible to integrate Grafana Log Server Dashboard
 * Using the Onboarding rest api provided by HarborDashboard
 * 
 * HarborDashboardController
 */
@RestController
@RequestMapping("/api")
public class HarborDashboardController {

    @Autowired
    private HarborDashboardService harborDashboardService;

    @GetMapping("/harbor-dashboard")
    public ResponseEntity<Map<String, Object>> getHarborDashboardData(
            @RequestParam(required = false, name = "project_name") String projectName,
            @RequestParam(required = false, name = "user_emails[]") List<String> userEmails,
            @RequestParam(required = false, name = "workspace_ids[]") List<String> workspaceIds) {

        HarborDashboardResponse responseBody = harborDashboardService.fetchDashboardData(projectName, userEmails, workspaceIds);

        if (responseBody == null) {
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("dashboard_id", null);
            emptyResponse.put("dashboard_uid", null);
            emptyResponse.put("dashboard_url", null);
            emptyResponse.put("dashboard_created", false);
            emptyResponse.put("permissions_updated_for", new ArrayList<>());
            emptyResponse.put("not_onboarded_users", new ArrayList<>());
            return ResponseEntity.ok(emptyResponse);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dashboard_id", responseBody.getDashboardId());
        responseMap.put("dashboard_uid", responseBody.getDashboardUid());
        responseMap.put("dashboard_url", responseBody.getDashboardUrl());
        responseMap.put("dashboard_created", responseBody.getDashboardCreated());
        responseMap.put("permissions_updated_for", responseBody.getPermissionsUpdatedFor());
        responseMap.put("not_onboarded_users", responseBody.getNotOnboardedUsers());
        return ResponseEntity.ok(responseMap);
    }
}

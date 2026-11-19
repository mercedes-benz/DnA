package com.daimler.data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.dto.ServiceDowntimeVO;
import com.daimler.data.service.ServiceDowntimeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class ServiceDowntimeController {

    @Autowired
    private ServiceDowntimeService serviceDowntimeService;

    @GetMapping("/downtime/active")
    public ResponseEntity<List<ServiceDowntimeVO>> getActiveDowntimes() {
        return ResponseEntity.ok(serviceDowntimeService.getActiveDowntimes());
    }
}

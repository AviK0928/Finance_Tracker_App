package com.example.Finance_Tracker.Sync.controller;

import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Sync.dto.SyncRequestDTO;
import com.example.Finance_Tracker.Sync.dto.SyncResponseDTO;
import com.example.Finance_Tracker.Sync.service.SyncService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<SyncResponseDTO> sync(@Valid @RequestBody SyncRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        SyncResponseDTO response = syncService.syncData(userId, request);
        return ResponseEntity.ok(response);
    }

}
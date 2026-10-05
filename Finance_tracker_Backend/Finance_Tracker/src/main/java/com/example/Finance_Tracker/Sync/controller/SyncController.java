package com.example.Finance_Tracker.Sync.controller;

import com.example.Finance_Tracker.Sync.dto.SyncResponseDTO;
import com.example.Finance_Tracker.Sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /** Changes since {@code cursor}, or everything without one. Send the returned cursor next time. */
    @GetMapping
    public SyncResponseDTO sync(@RequestParam(required = false) String cursor) {
        return syncService.sync(cursor);
    }
}

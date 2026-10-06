package com.example.Finance_Tracker.Device.controller;

import com.example.Finance_Tracker.Device.dto.DeviceRegistrationDTO;
import com.example.Finance_Tracker.Device.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Push registration of the app install the request comes from. */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    /** Register or refresh this device's push token (after login and whenever Firebase issues a new one). */
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody DeviceRegistrationDTO dto) {
        deviceTokenService.register(dto);
        return ResponseEntity.noContent().build();
    }

    /** Stop pushes to this device (on logout). 204 even when the token is unknown. */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregister(@PathVariable String token) {
        deviceTokenService.unregister(token);
        return ResponseEntity.noContent().build();
    }
}

package com.example.Finance_Tracker.Core.mail;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MailProperties {
    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.from.address}")
    private String fromAddress;
}
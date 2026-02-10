package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.logging.ClientLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client-logs")
public class ClientLogController {

    private static final Logger logger = LoggerFactory.getLogger(ClientLogController.class);

    @PostMapping
    public void receiveClientLog(@RequestBody ClientLogRequest logRequest) {

        String prefix = "[CLIENT LOG] [" + logRequest.getCorrelationId() + "] ";

        switch (logRequest.getLevel().toLowerCase()) {
            case "error":
                logger.error(prefix + logRequest.getMessage() + " | URL: " + logRequest.getUrl()
                             + " | Status: " + logRequest.getStatus()
                             + " | Details: " + logRequest.getDetails());
                break;

            case "warn":
                logger.warn(prefix + logRequest.getMessage());
                break;

            default:
                logger.info(prefix + logRequest.getMessage());
                break;
        }
    }
}

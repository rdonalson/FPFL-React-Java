package com.financialplanner.moduleapi.controllers;


//import com.financialplanner.modulecommonbc.ledger.LedgerRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.moduledisplaybc.model.Ledger;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.service.LedgerReadoutService;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/display")
public class DisplayController {

    private final LedgerReadoutService ledgerReadoutService;
    private final ApiResponseFactory responseFactory;

    public DisplayController(LedgerReadoutService ledgerReadoutService, ApiResponseFactory responseFactory) {
        this.ledgerReadoutService = ledgerReadoutService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/ledger")
    public ResponseEntity<ApiResponse<List<LedgerDto>>> buildLedger(@RequestBody LedgerRequest request) {

        List<LedgerDto> ledger = ledgerReadoutService.buildLedgerReadout(request);
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<LedgerDto>> body = responseFactory.success(ledger, "Items retrieved successfully");
        return ResponseEntity.ok(body);
    }
}

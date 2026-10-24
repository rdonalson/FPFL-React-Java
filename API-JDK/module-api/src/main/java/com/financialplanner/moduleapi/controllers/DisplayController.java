package com.financialplanner.moduleapi.controllers;


//import com.financialplanner.modulecommonbc.ledger.LedgerRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.moduledisplaybc.model.Ledger;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.service.LedgerReadoutService;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.dtos.paydown.DebtPaydownResponse;
import com.financialplanner.moduleapi.mappers.DebtMapper;
import com.financialplanner.moduleapi.security.CustomUserDetails;
import com.financialplanner.modulecommonbc.exception.ForbiddenOperationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/display")
public class DisplayController {

    private final LedgerReadoutService ledgerReadoutService;
    private final ApiResponseFactory responseFactory;
    private final DebtMapper debtMapper;

    public DisplayController(LedgerReadoutService ledgerReadoutService, ApiResponseFactory responseFactory,
                             DebtMapper debtMapper) {
        this.ledgerReadoutService = ledgerReadoutService;
        this.responseFactory = responseFactory;
        this.debtMapper = debtMapper;
    }

    @PostMapping("/ledger")
    public ResponseEntity<ApiResponse<List<LedgerDto>>> buildLedger(@RequestBody LedgerRequest request) {

        List<LedgerDto> ledger = ledgerReadoutService.buildLedgerReadout(request);
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<LedgerDto>> body = responseFactory.success(ledger, "Items retrieved successfully");
        return ResponseEntity.ok(body);
    }

    /**
     * Debt Paydown: each tracked debt's balance per day, its payments, allocations and interest, and the
     * projected payoff date, over the same window as the ledger (same request body). {@code userId} defaults
     * to the caller; only an admin may ask for someone else's.
     */
    @PostMapping("/paydown")
    public ResponseEntity<ApiResponse<List<DebtPaydownResponse>>> buildPaydown(
        @RequestBody LedgerRequest request, @AuthenticationPrincipal CustomUserDetails principal) {

        UUID caller = DebtController.userId(principal);
        UUID userId = request.userId() != null ? request.userId() : caller;
        if (!userId.equals(caller) && !DebtController.isAdmin(principal)) {
            throw new ForbiddenOperationException("You can only see your own debt paydown");
        }

        LedgerRequest scoped = new LedgerRequest(userId, request.ledgerStartDate(), request.ledgerEndDate(),
                                                 request.includeGrouping());
        List<DebtPaydownResponse> debts = debtMapper.toPaydownResponse(ledgerReadoutService.buildPaydown(scoped));
        return ResponseEntity.ok(responseFactory.success(debts, "Paydown retrieved successfully"));
    }
}

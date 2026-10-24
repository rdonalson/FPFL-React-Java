package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.debt.DebtRequest;
import com.financialplanner.moduleapi.dtos.debt.DebtResponse;
import com.financialplanner.moduleapi.mappers.DebtMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.security.CustomUserDetails;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import com.financialplanner.moduleitemsbc.domain.service.DebtService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Debt Paydown: the debits a user is paying down and the credits allocated to them. Signed-in users only
 * (JWT); each user sees and changes only their own debts (admins any). The owner always comes from the
 * access token.
 */
@RestController
@RequestMapping("/debts")
public class DebtController {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final DebtService service;
    private final DebtMapper mapper;
    private final ApiResponseFactory responseFactory;

    public DebtController(DebtService service, DebtMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /** Lists the caller's tracked debts with their allocations. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DebtResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        List<DebtResponse> debts = mapper.toResponse(service.list(userId(principal)));
        return ResponseEntity.ok(responseFactory.success(debts, "Debts fetched successfully"));
    }

    /** Gets one of the caller's tracked debts. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtResponse>> get(@PathVariable("id") Long id,
                                                         @AuthenticationPrincipal CustomUserDetails principal) {
        Debt debt = service.get(id, userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success(mapper.toResponse(debt), "Debt " + id + " retrieved successfully"));
    }

    /** Starts tracking one of the caller's debits, with any allocations. */
    @PostMapping
    public ResponseEntity<ApiResponse<DebtResponse>> create(@RequestBody DebtRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        DebtResponse response = mapper.toResponse(service.create(mapper.toDraft(request), userId(principal)));
        URI location = URI.create("/debts/" + response.id());
        return ResponseEntity.created(location)
                             .body(responseFactory.created(response, "Debt created successfully", location.toString()));
    }

    /** Changes a debt's balance, APR, balance date and allocations. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtResponse>> update(@PathVariable("id") Long id,
                                                            @RequestBody DebtRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        Debt debt = service.update(id, mapper.toDraft(request), userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success(mapper.toResponse(debt), "Debt updated successfully"));
    }

    /** Stops tracking a debt (the debit itself stays). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        service.delete(id, userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success("Debt " + id + " deleted successfully"));
    }

    static UUID userId(CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            throw new InvalidCredentialsException("Sign in to use Debt Paydown");
        }
        return principal.getUser().getUserId();
    }

    static boolean isAdmin(CustomUserDetails principal) {
        return principal != null && principal.getRoleNames() != null && principal.getRoleNames().contains(ADMIN_ROLE);
    }
}

package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.account.AccountRequest;
import com.financialplanner.moduleapi.dtos.account.AccountResponse;
import com.financialplanner.moduleapi.dtos.account.PurchaseRequest;
import com.financialplanner.moduleapi.mappers.AccountMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.security.CallerAccess;
import com.financialplanner.moduleapi.security.CustomUserDetails;
import com.financialplanner.moduleitemsbc.domain.service.CreditAccountService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPurchase;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Cards &amp; Loans: standalone credit cards and loans with their own purchases, paid by the user's credits and
 * debits. Same routes and messages as API-NET's {@code AccountsController}. Signed-in users only (JWT); each
 * user sees and changes only their own accounts (admins any). The owner always comes from the access token.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreditAccountService service;
    private final AccountMapper mapper;
    private final ApiResponseFactory responseFactory;

    public AccountController(CreditAccountService service, AccountMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /** Lists the caller's cards and loans with their purchases and payments. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        List<AccountResponse> accounts = mapper.toResponse(service.list(userId(principal)));
        return ResponseEntity.ok(responseFactory.success(accounts, "Accounts fetched successfully"));
    }

    /** Gets one of the caller's cards or loans. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> get(@PathVariable("id") Long id,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        CreditAccount account = service.get(id, userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success(mapper.toResponse(account),
                                                         "Account " + id + " retrieved successfully"));
    }

    /** Creates a card or loan, with any paying credits and debits. */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(@RequestBody AccountRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails principal) {
        AccountResponse response = mapper.toResponse(service.create(mapper.toDraft(request), userId(principal)));
        URI location = URI.create("/accounts/" + response.id());
        return ResponseEntity.created(location)
                             .body(responseFactory.created(response, "Account created successfully", location.toString()));
    }

    /** Changes a card or loan's details and its payments (replaced as a set). */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(@PathVariable("id") Long id,
                                                               @RequestBody AccountRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails principal) {
        CreditAccount account = service.update(id, mapper.toDraft(request), userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success(mapper.toResponse(account), "Account updated successfully"));
    }

    /** Deletes a card or loan with its purchases and payments (the paying items stay). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        service.delete(id, userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success("Account " + id + " deleted successfully"));
    }

    /** An account's purchases. */
    @GetMapping("/{id}/purchases")
    public ResponseEntity<ApiResponse<List<AccountResponse.PurchaseResponse>>> purchases(
        @PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        AccountResponse account = mapper.toResponse(service.get(id, userId(principal), isAdmin(principal)));
        return ResponseEntity.ok(responseFactory.success(account.purchases(), "Purchases fetched successfully"));
    }

    /** Adds a purchase to an account. */
    @PostMapping("/{id}/purchases")
    public ResponseEntity<ApiResponse<AccountResponse.PurchaseResponse>> addPurchase(
        @PathVariable("id") Long id, @RequestBody PurchaseRequest request,
        @AuthenticationPrincipal CustomUserDetails principal) {
        AccountPurchase purchase = service.addPurchase(id, mapper.toDraft(request), userId(principal), isAdmin(principal));
        URI location = URI.create("/accounts/" + id + "/purchases/" + purchase.getId());
        return ResponseEntity.created(location)
                             .body(responseFactory.created(AccountMapper.toResponse(purchase),
                                                           "Purchase created successfully", location.toString()));
    }

    /** Changes a purchase. */
    @PutMapping("/{id}/purchases/{purchaseId}")
    public ResponseEntity<ApiResponse<AccountResponse.PurchaseResponse>> updatePurchase(
        @PathVariable("id") Long id, @PathVariable("purchaseId") Long purchaseId, @RequestBody PurchaseRequest request,
        @AuthenticationPrincipal CustomUserDetails principal) {
        AccountPurchase purchase = service.updatePurchase(id, purchaseId, mapper.toDraft(request), userId(principal),
                                                          isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success(AccountMapper.toResponse(purchase),
                                                         "Purchase updated successfully"));
    }

    /** Deletes a purchase. */
    @DeleteMapping("/{id}/purchases/{purchaseId}")
    public ResponseEntity<ApiResponse<Void>> deletePurchase(@PathVariable("id") Long id,
                                                            @PathVariable("purchaseId") Long purchaseId,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        service.deletePurchase(id, purchaseId, userId(principal), isAdmin(principal));
        return ResponseEntity.ok(responseFactory.success("Purchase " + purchaseId + " deleted successfully"));
    }

    static UUID userId(CustomUserDetails principal) {
        return CallerAccess.userId(principal);
    }

    static boolean isAdmin(CustomUserDetails principal) {
        return CallerAccess.isAdmin(principal);
    }
}

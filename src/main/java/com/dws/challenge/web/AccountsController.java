package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferMoney;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.TransferAmountException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;

  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
  
  
  @PostMapping(path = "/transfer" , consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferMoney(@RequestBody @Valid TransferMoney transferMoney) {
    log.info("Transfer Money {}", transferMoney);
    BigDecimal  zero = new BigDecimal(0);
    String transferNotification = null;
    
    try {
    if (!(transferMoney.getAmount().compareTo(zero) == 1)) { 
        throw new TransferAmountException("Transfer Amount cannot be less then 0");
    } 
    NotificationService emailNotification = new EmailNotificationService();
    
    transferNotification = accountsService.transferAmount(transferMoney,emailNotification);
    }catch (Exception transExp) {
        return new ResponseEntity<>(transExp.getMessage(), HttpStatus.BAD_REQUEST);
      }

    return new ResponseEntity<>(transferNotification,HttpStatus.OK);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

}

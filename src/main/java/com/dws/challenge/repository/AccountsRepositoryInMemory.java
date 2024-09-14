package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferMoney;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAccountIdException;
import com.dws.challenge.exception.TransferAmountException;
import com.dws.challenge.service.NotificationService;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
   

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

	@Override
	public  String transferAmount(TransferMoney transferMoney, NotificationService notifyService) {
		BigDecimal transferAmount = transferMoney.getAmount();
		Account fromAccount = accounts.get(transferMoney.getAccountFromId());
		Account toAccount = accounts.get(transferMoney.getAccountToId());
		    
		    if (fromAccount == null) {
		        throw new InvalidAccountIdException("Account From ID");
		    }
		    if (toAccount == null) {
		        throw new InvalidAccountIdException("Account To ID");
		    }
		    
		    synchronized (fromAccount) {
		        synchronized (toAccount) {
		            if (fromAccount.getBalance().compareTo(transferAmount) < 0) {
		                throw new TransferAmountException("Insufficient balance to transfer the amount");
		            }
		            
		            try {
		                
		                fromAccount.setBalance(fromAccount.getBalance().subtract(transferAmount));
		                toAccount.setBalance(toAccount.getBalance().add(transferAmount));
		                
		                // Notifications after successful transfer
		                notifyService.notifyAboutTransfer(fromAccount, " INR "+ transferAmount +" has been Debited from your account towards Account Id: "+toAccount.getAccountId() );
		    			notifyService.notifyAboutTransfer(toAccount, " INR "+ transferAmount +" has been Credited to your account from Account Id: "+fromAccount.getAccountId() );
		            } catch (Exception e) {
		                // Handle exceptions properly
		                e.printStackTrace();
		                throw new TransferAmountException("Error occurred during transfer: " + e.getMessage());
		            }
		        }
		    }
		
		
		return " INR "+ transferAmount +" has been Transfered from AccountId: "+ fromAccount.getAccountId() +" towards Account Id: "+toAccount.getAccountId();
	}

}

package com.dws.challenge.validation;

import java.math.BigDecimal;

import com.dws.challenge.domain.TransferMoney;
import com.dws.challenge.exception.TransferAmountException;

public class TransferValidation {
	BigDecimal  zero = new BigDecimal(0);
	public void transferAmountValidation(TransferMoney transferMoney) {
		
		if (!(transferMoney.getAmount().compareTo(zero) == 1)) { 
            throw new TransferAmountException("Transfer Amount cannot be less then 0");
        } 

	}
}

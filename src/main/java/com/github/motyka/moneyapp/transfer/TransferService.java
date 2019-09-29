package com.github.motyka.moneyapp.transfer;

import com.github.motyka.moneyapp.account.AccountService;
import com.github.motyka.moneyapp.exception.ConstrainViolationException;
import com.github.motyka.moneyapp.exception.MoneyAppException;
import com.github.motyka.moneyapp.exception.ResourceNotFoundException;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.math.BigDecimal;
import java.util.Objects;

public class TransferService {

    private AccountService accountService;

    public TransferService(AccountService accountService) {
        this.accountService = accountService;
    }

    public Transfer create(Transfer transfer) {
        validateTransfer(transfer);

        try {
            Base.openTransaction();
            accountService.transferFunds(transfer.getSenderId(), transfer.getRecipientId(), transfer.getAmount());
            transfer.setSucceed(true);
        } catch (ResourceNotFoundException rnfe) {
            // in case Sender or Recipient doesn't exist
            // the Transfer can't be saved with an error message because of the database constrains
            // ResourceNotFoundException is converted into ConstrainViolationException
            // because POST should not return 404 in this case, but 400
            Base.rollbackTransaction();
            throw new ConstrainViolationException("Sender or Recipient doesn't exist", rnfe);
        } catch (MoneyAppException mae) {
            transfer.setSucceed(false);
            transfer.setErrorMessage(mae.getMessage());
        }
        transfer.saveIt();
        Base.commitTransaction();
        return transfer;
    }

    public Transfer findById(Long id) {
        Transfer transfer = Transfer.findById(id);
        if (Objects.isNull(transfer)) {
            throw new ResourceNotFoundException("Transfer doesn't exist, id: " + id);
        }
        return transfer;
    }

    public LazyList<Transfer> findAll() {
        return Transfer.findAll();
    }

    private void validateTransfer(Transfer transfer) {
        if (Objects.nonNull(transfer.getId())) {
            throw new ConstrainViolationException("New Transfer should not have ID already set");
        }

        if (Objects.isNull(transfer.getRecipientId())) {
            throw new ConstrainViolationException("Recipient's ID can't be null");
        }

        if (Objects.isNull(transfer.getSenderId())) {
            throw new ConstrainViolationException("Sender's ID can't be null");
        }

        if (transfer.getSenderId().equals(transfer.getRecipientId())) {
            throw new ConstrainViolationException("Sender's and Recipient's IDs must be different");
        }

        BigDecimal amount = transfer.getAmount();
        if (Objects.isNull(amount) || BigDecimal.ZERO.compareTo(amount) > 0) {
            throw new ConstrainViolationException("Amount must be greater than 0");
        }
    }
}

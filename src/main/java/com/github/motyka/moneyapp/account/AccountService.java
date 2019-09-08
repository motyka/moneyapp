package com.github.motyka.moneyapp.account;

import com.github.motyka.moneyapp.exception.ConstrainViolationException;
import com.github.motyka.moneyapp.exception.OperationFailedException;
import com.github.motyka.moneyapp.exception.ResourceNotFoundException;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.StaleModelException;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountService {

    public Account create(Account account) {
        if (Objects.isNull(account.getId())) {
            account.setActive(true);
            account.saveIt();
            return account;
        } else {
            throw new ConstrainViolationException("New Account should not have ID already set");
        }
    }

    public Account findById(Long id) {
        return findAndValidateAccount(id);
    }

    public LazyList<Account> findAll() {
        return Account.find("active = ?", true);
    }

    public void delete(Long id) {
        Account account = findAndValidateAccount(id);

        if (BigDecimal.ZERO.equals(account.getBalance())) {
            account.setActive(false);
            account.saveIt();
        } else {
            throw new ConstrainViolationException("Can't delete an account with non 0 balance: " + account.getBalance());
        }
    }

    public void transferFunds(Long senderId, Long recipientId, BigDecimal amount) {
        validateAmount(amount);
        Account sender = findAndValidateAccount(senderId);
        Account recipient = findAndValidateAccount(recipientId);

        Base.openTransaction();
        try {
            if (hasEnoughFunds(sender, amount)) {
                changeBalance(sender, amount.negate());
                changeBalance(recipient, amount);
            } else {
                throw new ConstrainViolationException("Not enough funds");
            }
            Base.commitTransaction();
        } catch (StaleModelException sme) {
            Base.rollbackTransaction();
            throw new OperationFailedException("Optimistic locking failed, one of the Accounts was concurrently modified", sme);
        }
    }

    private void validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount can't be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConstrainViolationException("Amount must be greater than 0");
        }
    }

    private Account findAndValidateAccount(Long id) {
        Account account = Account.findById(id);
        if (exists(account)) {
            return account;
        } else {
            throw new ResourceNotFoundException("Account doesn't exist, id: " + id);
        }
    }

    private boolean exists(Account account) {
        return Objects.nonNull(account) && account.isActive();
    }

    private boolean hasEnoughFunds(Account sender, BigDecimal amount) {
        return sender.getBalance().compareTo(amount) >= 0;
    }

    private void changeBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        account.saveIt();
    }
}

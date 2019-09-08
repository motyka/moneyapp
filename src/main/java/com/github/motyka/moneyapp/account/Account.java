package com.github.motyka.moneyapp.account;

import org.javalite.activejdbc.Model;

import java.math.BigDecimal;

public class Account extends Model {

    public BigDecimal getBalance() {
        return getBigDecimal("balance");
    }

    void setBalance(BigDecimal balance) {
        setBigDecimal("balance", balance);
    }

    public Boolean isActive() {
        return getBoolean("active");
    }

    void setActive(Boolean active) {
        setBoolean("active", active);
    }
}

package com.github.motyka.moneyapp.transfer;

import org.javalite.activejdbc.Model;

import java.math.BigDecimal;

public class Transfer extends Model {

    public Long getSenderId() {
        return getLong("senderId");
    }

    void setSenderId(Long senderId) {
        setLong("senderId", senderId);
    }

    public Long getRecipientId() {
        return getLong("recipientId");
    }

    void setRecipientId(Long recipientId) {
        setLong("recipientId", recipientId);
    }

    public BigDecimal getAmount() {
        return getBigDecimal("amount");
    }

    void setAmount(BigDecimal amount) {
        setBigDecimal("amount", amount);
    }

    public Boolean getSucceed() {
        return getBoolean("succeed");
    }

    void setSucceed(Boolean succeed) {
        setBoolean("succeed", succeed);
    }

    public String getErrorMessage() {
        return getString("errorMessage");
    }

    void setErrorMessage(String errorMessage) {
        setString("errorMessage", errorMessage);
    }
}

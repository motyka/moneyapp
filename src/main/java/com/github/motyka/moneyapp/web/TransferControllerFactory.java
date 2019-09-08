package com.github.motyka.moneyapp.web;

import com.github.motyka.moneyapp.account.AccountService;
import com.github.motyka.moneyapp.transfer.TransferService;

public class TransferControllerFactory {

    public static TransferController create() {
        return new TransferController(new TransferService(new AccountService()));
    }
}

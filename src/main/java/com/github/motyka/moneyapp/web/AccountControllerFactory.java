package com.github.motyka.moneyapp.web;

import com.github.motyka.moneyapp.account.AccountService;

public class AccountControllerFactory {

    public static AccountController create() {
        AccountService accountService = new AccountService();
        return new AccountController(accountService);
    }
}

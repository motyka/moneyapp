package com.github.motyka.moneyapp.web;

import com.github.motyka.moneyapp.account.Account;
import com.github.motyka.moneyapp.account.AccountService;
import com.github.motyka.moneyapp.exception.OperationNotAllowedException;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.javalite.common.JsonHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

public class AccountController implements CrudHandler {

    private final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void create(@NotNull Context context) {
        Account account = new Account();
        account.fromMap(JsonHelper.toMap(context.body()));
        logger.info("adding " + account);

        account = accountService.create(account);
        context.status(201).result(account.toJson(true));
    }

    @Override
    public void delete(@NotNull Context context, @NotNull String id) {
        logger.info("deleting: " + id);

        accountService.delete(Long.parseLong(id));
        context.status(HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Override
    public void getAll(@NotNull Context context) {
        logger.info("getting all");

        context.result(accountService.findAll().toJson(true));
    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String id) {
        logger.info("getting one: " + id);

        Account account = accountService.findById(Long.parseLong(id));
        context.result(account.toJson(true));
    }

    @Override
    public void update(@NotNull Context context, @NotNull String id) {
        logger.info("updating " + id);

        throw new OperationNotAllowedException("Can't update account");
    }
}

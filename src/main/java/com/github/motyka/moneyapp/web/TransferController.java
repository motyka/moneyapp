package com.github.motyka.moneyapp.web;

import com.github.motyka.moneyapp.exception.OperationNotAllowedException;
import com.github.motyka.moneyapp.transfer.Transfer;
import com.github.motyka.moneyapp.transfer.TransferService;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.javalite.common.JsonHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferController implements CrudHandler {

    private final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Override
    public void create(@NotNull Context context) {
        Transfer transfer = new Transfer();
        transfer.fromMap(JsonHelper.toMap(context.body()));
        logger.info("adding " + transfer);

        transfer = transferService.create(transfer);
        context.status(201).result(transfer.toJson(true));
    }

    @Override
    public void delete(@NotNull Context context, @NotNull String id) {
        logger.info("deleting: " + id);

        throw new OperationNotAllowedException("Can't delete transfer");
    }

    @Override
    public void getAll(@NotNull Context context) {
        logger.info("getting all");

        context.result(transferService.findAll().toJson(true));
    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String id) {
        logger.info("getting one: " + id);

        Transfer transfer = transferService.findById(Long.parseLong(id));
        context.result(transfer.toJson(true));
    }

    @Override
    public void update(@NotNull Context context, @NotNull String id) {
        logger.info("updating " + id);

        throw new OperationNotAllowedException("Can't update transfer");
    }
}

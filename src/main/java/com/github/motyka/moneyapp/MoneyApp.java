package com.github.motyka.moneyapp;

import com.github.motyka.moneyapp.exception.ConstrainViolationException;
import com.github.motyka.moneyapp.exception.OperationFailedException;
import com.github.motyka.moneyapp.exception.OperationNotAllowedException;
import com.github.motyka.moneyapp.web.AccountControllerFactory;
import com.github.motyka.moneyapp.web.ApiPaths;
import com.github.motyka.moneyapp.web.TransferControllerFactory;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Properties;

import static io.javalin.apibuilder.ApiBuilder.crud;
import static io.javalin.apibuilder.ApiBuilder.path;

public class MoneyApp {
    private static final Logger logger = LoggerFactory.getLogger(MoneyApp.class);

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();
        int port = Integer.parseInt(properties.getProperty("http.port"));
        startServer(port);
    }

    static Javalin startServer(int port) {
        Javalin app = Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(getOpenApiOptions()));
        }).start(port);

        app.before(ctx -> Base.open());
        app.after(ctx -> Base.close());

        app.routes(() -> {
            path(ApiPaths.BASE, () -> {
                crud(ApiPaths.ACCOUNTS + ":account-id", AccountControllerFactory.create());
                crud(ApiPaths.TRANSFERS + ":transfer-id", TransferControllerFactory.create());
            });
        });

        app.exception(OperationFailedException.class, (e, ctx) -> {
            logger.info("Operation failed", e);
            ctx.status(HttpURLConnection.HTTP_BAD_REQUEST).result(e.getMessage());
        });

        app.exception(OperationNotAllowedException.class, (e, ctx) -> {
            logger.info("Operation not allowed", e);
            ctx.status(HttpURLConnection.HTTP_BAD_METHOD).result(e.getMessage());
        });

        app.exception(ConstrainViolationException.class, (e, ctx) -> {
            logger.info("Constrain violation", e);
            ctx.status(HttpURLConnection.HTTP_BAD_REQUEST).result(e.getMessage());
        });

        app.exception(ValidationException.class, (e, ctx) -> {
            logger.info("Invalid data", e);
            ctx.status(HttpURLConnection.HTTP_BAD_REQUEST).result("Invalid data");
        });

        app.exception(NumberFormatException.class, (e, ctx) -> {
            logger.info("Incorrect number format", e);
            ctx.status(HttpURLConnection.HTTP_BAD_REQUEST).result("Id should be an integer");
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected error", e);
            ctx.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
        });

        return app;
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = MoneyApp.class.getResourceAsStream("/config.properties");
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }

    private static OpenApiOptions getOpenApiOptions() {
        Info applicationInfo = new Info().version("1.0").description("MoneyApp");
        return new OpenApiOptions(applicationInfo)
                .path("/swagger-docs")
                .swagger(new SwaggerOptions("/swagger").title("MoneyApp Swagger Documentation"));
    }
}

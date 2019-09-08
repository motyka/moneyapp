package com.github.motyka.moneyapp;

import com.github.motyka.moneyapp.account.Account;
import com.github.motyka.moneyapp.transfer.Transfer;
import com.github.motyka.moneyapp.web.ApiPaths;
import io.javalin.Javalin;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.javalite.activejdbc.test.DBSpec;
import org.javalite.common.JsonHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoneyAppTest extends DBSpec {

    private static final int PORT = 9080;
    private static final String baseUrl = String.format("http://localhost:%s/%s", PORT, ApiPaths.BASE);

    private static Javalin server;
    private static CloseableHttpClient client;

    @BeforeAll
    public static void startServer() {
        server = MoneyApp.startServer(PORT);
        client = HttpClients.createDefault();
    }

    @AfterAll
    public static void stopServer() throws IOException {
        client.close();
        server.stop();
    }

    @Test
    @Order(1)
    public void createAccount1() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.ACCOUNTS);
        request.setEntity(new StringEntity("{\"balance\": 123}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            Account account = new Account();
            account.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusLine().getStatusCode());
            assertEquals(account.getBalance(), new BigDecimal(123));
            assertNotNull(account.getId());
        }
    }

    @Test
    @Order(2)
    public void tryToCreateAccountWithId() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.ACCOUNTS);
        request.setEntity(new StringEntity("{\"id\": 5, \"balance\": 123}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    @Order(3)
    public void createAccount2() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.ACCOUNTS);
        request.setEntity(new StringEntity("{\"balance\": 2000}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            Account account = new Account();
            account.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusLine().getStatusCode());
            assertEquals(account.getBalance(), new BigDecimal(2000));
            assertNotNull(account.getId());
        }
    }

    @Test
    @Order(4)
    public void createAccount3() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.ACCOUNTS);
        request.setEntity(new StringEntity("{\"balance\": 4000}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            Account account = new Account();
            account.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusLine().getStatusCode());
            assertEquals(account.getBalance(), new BigDecimal(4000));
            assertNotNull(account.getId());
        }
    }

    @Test
    @Order(5)
    public void getAllAccounts() throws IOException {
        HttpGet request = new HttpGet(baseUrl + ApiPaths.ACCOUNTS);

        try (CloseableHttpResponse response = client.execute(request)) {
            List list = JsonHelper.toList(EntityUtils.toString(response.getEntity(), "UTF-8"));

            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
            assertEquals(list.size(), 3);
        }
    }

    @Test
    @Order(6)
    public void tryToDeleteAccountWithFunds() throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + ApiPaths.ACCOUNTS + "1");

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusLine().getStatusCode());
            assertEquals("Can't delete an account with non 0 balance: 123", EntityUtils.toString(response.getEntity(), "UTF-8"));
        }
    }

    @Test
    @Order(7)
    public void tryToUpdateAccount() throws IOException {
        HttpPatch request = new HttpPatch(baseUrl + ApiPaths.ACCOUNTS + "1");
        request.setEntity(new StringEntity("{\"balance\": 1000}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    @Order(8)
    public void tryToTransferToMuchFunds() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.TRANSFERS);
        request.setEntity(new StringEntity("{\"senderId\": 1, \"recipientId\": 2, \"amount\": 500}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            Transfer transfer = new Transfer();
            transfer.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusLine().getStatusCode());
            assertNotNull(transfer.getId());
            assertEquals(transfer.getErrorMessage(), "Not enough funds");
            assertFalse(transfer.getSucceed());
            assertEquals(transfer.getSenderId(), 1);
            assertEquals(transfer.getRecipientId(), 2);
            assertEquals(transfer.getAmount(), new BigDecimal(500));
        }
    }

    @Test
    @Order(9)
    public void transferFunds() throws IOException {
        HttpPost request = new HttpPost(baseUrl + ApiPaths.TRANSFERS);
        request.setEntity(new StringEntity("{\"senderId\": 1, \"recipientId\": 2, \"amount\": 123}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            Transfer transfer = new Transfer();
            transfer.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusLine().getStatusCode());
            assertNotNull(transfer.getId());
            assertNull(transfer.getErrorMessage());
            assertTrue(transfer.getSucceed());
            assertEquals(transfer.getSenderId(), 1);
            assertEquals(transfer.getRecipientId(), 2);
            assertEquals(transfer.getAmount(), new BigDecimal(123));
        }
    }

    @Test
    @Order(10)
    public void checkAccount1Funds() throws IOException {
        Long id = 1L;
        HttpGet request = new HttpGet(baseUrl + ApiPaths.ACCOUNTS + id);

        try (CloseableHttpResponse response = client.execute(request)) {
            Account account = new Account();
            account.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
            assertEquals(account.getBalance(), BigDecimal.ZERO);
            assertEquals(account.getLongId(), id);
        }
    }

    @Test
    @Order(11)
    public void checkAccount2Funds() throws IOException {
        Long id = 2L;
        HttpGet request = new HttpGet(baseUrl + ApiPaths.ACCOUNTS + id);

        try (CloseableHttpResponse response = client.execute(request)) {
            Account account = new Account();
            account.fromMap(responseBodyToMap(response));

            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
            assertEquals(account.getBalance(), new BigDecimal(2123));
            assertEquals(account.getLongId(), id);
        }
    }

    @Test
    @Order(12)
    public void getAllTransfers() throws IOException {
        HttpGet request = new HttpGet(baseUrl + ApiPaths.TRANSFERS);

        try (CloseableHttpResponse response = client.execute(request)) {
            List list = JsonHelper.toList(EntityUtils.toString(response.getEntity(), "UTF-8"));

            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
            assertEquals(list.size(), 2);
        }
    }

    @Test
    @Order(13)
    public void deleteAccountWithNoFunds() throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + ApiPaths.ACCOUNTS + 1);

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    @Order(14)
    public void getAllAccountsAfterDelete() throws IOException {
        HttpGet request = new HttpGet(baseUrl + ApiPaths.ACCOUNTS);

        try (CloseableHttpResponse response = client.execute(request)) {
            List list = JsonHelper.toList(EntityUtils.toString(response.getEntity(), "UTF-8"));

            assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
            assertEquals(list.size(), 2);
        }
    }

    @Test
    @Order(15)
    public void tryToDeleteTransfer() throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + ApiPaths.TRANSFERS + 1);

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    @Order(16)
    public void tryToUpdateTransfer() throws IOException {
        HttpPatch request = new HttpPatch(baseUrl + ApiPaths.TRANSFERS + 1);
        request.setEntity(new StringEntity("{\"senderId\": 1, \"recipientId\": 2, \"amount\": 20}"));

        try (CloseableHttpResponse response = client.execute(request)) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.getStatusLine().getStatusCode());
        }
    }

    private Map responseBodyToMap(HttpResponse response) throws IOException {
        return JsonHelper.toMap(EntityUtils.toString(response.getEntity(), "UTF-8"));
    }
}

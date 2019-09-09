Simple money transfer app.

**Technologies used:**
* Javalin
* ActiveJdbc
* H2 Database

Spring could not be used in this assigment. Javalin is used to handle REST endpoints.
For ORM I would normally use Hibernate, but because you prefer lighter frameworks/libs I decided to use ActiveJdbc.
I used both Javalin and ActiveJdbs for the first time during this assigment,
so some things may not perfectly adhere to their design principles.
Data is stored in a H2 memory database. Javalin uses an embedded jetty server.

The default port is `9080` configured in `resorces/config.properties`.

To start the application run: `mvn clean process-classes exec:java`

`process-classes` is used because ActiveJdbc needs to modify the byte code.
`org.javalite:activejdbc-instrumentation:2.3:instrument` goal is bound to it in `pom.xml`.
Invoking this goal is needed for ActiveJdbc models to work.

I didn't need to use getters/setters in ActiveJdbc's Model classes.
I could reference fields using provided by ActiveJdbc methods like `getBigDecimal("balance")`. 
But I preferred to have compiler-time checks, rather than to use field names as strings and risk run-time errors.

**API usage**

While the server is running there is a basic Swagger available under http://localhost:9080/swagger

To POST an account only balance is required (eg. `{"balance": 100}`). The account with generated ID will be returned.

To POST a transfer IDs of two created accounts will be needed (eg. `{"senderId": 1, "recipientId": 2, "amount": 500}`).

A RESTful API shouldn't have "actions", so I treat transfers as resources.

**Tests**

To demonstrate that the API works as expected there is `MoneyAppTest.java` test class
which starts the server and tests the REST endpoints using Apache HTTP Client.

To start this test run: `mvn clean test -Dtest=MoneyAppTest`

Default port of the test server is 9080 configured in `MoneyAppTest.java`
in `private static final int PORT = 9080;` field.

In `test/kotlin` directory there are unit tests written in Kotlin.
I would normally write them without the need for a database by mocking calls to the repository class,
but ActiveJdbc doesn't use repository layer and reads entities' metadata from an active database.
Because of those things, it requires a database even for testing. Again, H2 is used there.

All tests can be run with `mvn clean test`.

**Concurrency**

The API will be called in concurrently by multiple services.
The part transferring money between accosts obviously needs proper synchronization.
It could be done in code, using Java concurrency capabilities,
but then the app wouldn't be safe to run on multiple servers,
because this synchronization would only work locally.
Instead, Account entity has a version field (`record_version`)
that is incremented each time the Account is modified.
If multiple threads update the same record at the same time it will be detected
and the thread that was working on stale data will have its changes rollbacked.
From the performance standpoint,
using optimistic locking on the database level should also be faster in most cases,
than locking it in Java each time an Account is modified,
because I assume that the same account will rarely be modified by multiple threads in the exact same moment.

package com.github.motyka.moneyapp.web

import com.github.motyka.moneyapp.account.Account
import com.github.motyka.moneyapp.account.AccountService
import com.github.motyka.moneyapp.exception.ConstrainViolationException
import com.github.motyka.moneyapp.exception.OperationNotAllowedException
import com.github.motyka.moneyapp.exception.ResourceNotFoundException
import io.javalin.http.Context
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.javalite.activejdbc.test.DBSpec
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.net.HttpURLConnection

@ExtendWith(MockKExtension::class)
class AccountControllerTest : DBSpec() {

    private lateinit var accountController: AccountController

    @RelaxedMockK
    private lateinit var accountService: AccountService
    @RelaxedMockK
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        accountController = AccountController(accountService)
    }

    @Test
    fun `add account`() {
        every { context.body() } returns "{\"balance\": 123}"
        every { accountService.create(any()) } returns Account()

        accountController.create(context)

        verify { context.status(HttpURLConnection.HTTP_CREATED) }
    }

    @Test
    fun `add account with ID`() {
        every { context.body() } returns "{\"id\": 1, \"balance\": 123}"
        every { accountService.create(any()) } throws ConstrainViolationException("")

        assertThrows<ConstrainViolationException> { accountController.create(context) }
    }

    @Test
    fun `delete existing account`() {
        accountController.delete(context, "5")

        verify { context.status(HttpURLConnection.HTTP_NO_CONTENT) }
    }

    @Test
    fun `delete not existing account`() {
        val id = 5L
        every { accountService.delete(id) } throws ResourceNotFoundException("")

        assertThrows<ResourceNotFoundException> { accountController.delete(context, id.toString()) }
    }

    @Test
    fun `update account`() {
        assertThrows<OperationNotAllowedException> { accountController.update(context, "5") }
    }
}
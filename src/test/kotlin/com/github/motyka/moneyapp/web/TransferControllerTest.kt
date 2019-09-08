package com.github.motyka.moneyapp.web

import com.github.motyka.moneyapp.account.AccountService
import com.github.motyka.moneyapp.exception.ConstrainViolationException
import com.github.motyka.moneyapp.exception.OperationNotAllowedException
import com.github.motyka.moneyapp.transfer.Transfer
import com.github.motyka.moneyapp.transfer.TransferService
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
class TransferControllerTest : DBSpec() {

    private lateinit var transferController: TransferController

    @RelaxedMockK
    private lateinit var accountService: AccountService
    @RelaxedMockK
    private lateinit var transferService: TransferService
    @RelaxedMockK
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        transferController = TransferController(transferService)
    }

    @Test
    fun `add transfer`() {
        every { context.body() } returns "{\"senderId\": 1, \"recipientId\": 2, \"amount\": 500}"
        every { transferService.create(any()) } returns Transfer()

        transferController.create(context)

        verify { context.status(HttpURLConnection.HTTP_CREATED) }
    }

    @Test
    fun `add transfer with ID`() {
        every { context.body() } returns "{\"id\": 1, \"senderId\": 1, \"recipientId\": 2, \"amount\": 500}"
        every { transferService.create(any()) } throws ConstrainViolationException("")

        assertThrows<ConstrainViolationException> { transferController.create(context) }
    }

    @Test
    fun `delete transfer`() {
        assertThrows<OperationNotAllowedException> { transferController.delete(context, "5") }
    }

    @Test
    fun `update transfer`() {
        assertThrows<OperationNotAllowedException> { transferController.update(context, "5") }
    }
}
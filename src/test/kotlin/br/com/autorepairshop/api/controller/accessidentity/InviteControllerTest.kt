package br.com.autorepairshop.api.controller.authentication

import br.com.autorepairshop.api.dto.authentication.CompleteInviteRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.accessindentity.AuthFixtures
import br.com.autorepairshop.accessindentity.application.dto.CompleteInviteCommand
import br.com.autorepairshop.accessindentity.application.dto.CustomerInviteResponse
import br.com.autorepairshop.accessindentity.application.dto.toResponse
import br.com.autorepairshop.accessindentity.application.usecase.CompleteInviteUseCase
import br.com.autorepairshop.accessindentity.application.usecase.FindCustomerInviteUseCase
import br.com.autorepairshop.accessindentity.application.usecase.IssueCustomerInviteUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@Tag("unit")
class InviteControllerTest {
    private val findInvite = mockk<FindCustomerInviteUseCase>()
    private val completeInvite = mockk<CompleteInviteUseCase>()
    private val issueInvite = mockk<IssueCustomerInviteUseCase>(relaxUnitFun = true)
    private val controller = InviteController(
        findInvite = findInvite,
        completeInvite = completeInvite,
        issueInvite = issueInvite,
    )

    @Test
    fun `find invite delegates to the use case`() {
        val preview = CustomerInviteResponse(
            customerName = "Ana Souza",
            expiresAt = Instant.parse("2026-09-01T00:00:00Z"),
        )
        every { findInvite.execute(input = "token") } returns preview

        assertEquals(
            expected = preview.customerName,
            actual = controller.findInvite(token = "token").body?.customerName,
        )
    }

    @Test
    fun `complete invite maps the request and returns 201`() {
        val created = AuthFixtures.client().toResponse()
        every { completeInvite.execute(input = any()) } returns created

        withHttpRequest(requestUri = "/invite/token") {
            val response = controller.completeInvite(
                token = "token",
                request = CompleteInviteRequest(
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
        }
        verify {
            completeInvite.execute(
                input = CompleteInviteCommand(
                    token = "token",
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
        }
    }

    @Test
    fun `issue invite delegates to the use case`() {
        val customerId = UUID.randomUUID()

        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.issueInvite(customerId = customerId).statusCode,
        )
        verify { issueInvite.execute(input = customerId) }
    }
}

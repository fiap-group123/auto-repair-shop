package br.com.autorepairshop.api.controller.budget

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.api.dto.budget.BudgetNotificationRequest
import br.com.autorepairshop.api.security.NotificationTokenGuard
import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.application.dto.ReceiveBudgetDecisionCommand
import br.com.autorepairshop.budget.application.usecase.ReceiveBudgetDecisionUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class BudgetNotificationControllerTest {
    private val tokens = NotificationTokenGuard(expectedToken = "local-budget-notification-token")
    private val receive = mockk<ReceiveBudgetDecisionUseCase>()
    private val controller = BudgetNotificationController(
        tokens = tokens,
        receive = receive,
    )

    @Test
    fun `delegates a valid notification`() {
        val serviceOrderId = UUID.randomUUID()
        val budget = BudgetFixtures.pricedResponse(serviceOrderId = serviceOrderId)
        every { receive.execute(input = any()) } returns budget

        val response = controller.receive(
            token = "local-budget-notification-token",
            request = BudgetNotificationRequest(
                serviceOrderId = serviceOrderId,
                decision = "APPROVED",
            ),
        )

        assertEquals(expected = HttpStatus.OK, actual = response.statusCode)
        verify {
            receive.execute(
                input = ReceiveBudgetDecisionCommand(
                    serviceOrderId = serviceOrderId,
                    decision = "APPROVED",
                ),
            )
        }
    }

    @Test
    fun `rejects a wrong token before the use case`() {
        assertFailsWith<AuthenticationException.Unauthenticated> {
            controller.receive(
                token = "nope",
                request = BudgetNotificationRequest(
                    serviceOrderId = UUID.randomUUID(),
                    decision = "APPROVED",
                ),
            )
        }
        verify(exactly = 0) { receive.execute(input = any()) }
    }
}

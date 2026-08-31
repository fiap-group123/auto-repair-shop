package br.com.autorepairshop.api.controller.budget

import br.com.autorepairshop.api.dto.budget.RegisterBudgetRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.application.usecase.ApproveBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.DeleteBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.FindBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.RegisterBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.RejectBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.TradeBudgetUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals

@Tag("unit")
class BudgetControllerTest {
    private val registerBudget = mockk<RegisterBudgetUseCase>()
    private val approveBudget = mockk<ApproveBudgetUseCase>()
    private val rejectBudget = mockk<RejectBudgetUseCase>()
    private val tradeBudget = mockk<TradeBudgetUseCase>()
    private val deleteBudget = mockk<DeleteBudgetUseCase>()
    private val findBudget = mockk<FindBudgetUseCase>()
    private val controller = BudgetController(
        registerBudget = registerBudget,
        approveBudget = approveBudget,
        rejectBudget = rejectBudget,
        tradeBudget = tradeBudget,
        deleteBudget = deleteBudget,
        findBudget = findBudget,
    )

    @Test
    fun `register maps the request and returns 201`() {
        val serviceOrderId = UUID.randomUUID()
        val budget = BudgetFixtures.pricedResponse(serviceOrderId = serviceOrderId)
        every { registerBudget.execute(input = serviceOrderId) } returns budget

        withHttpRequest(requestUri = "/budgets") {
            val response = controller.register(
                request = RegisterBudgetRequest(serviceOrderId = serviceOrderId),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = budget.serviceOrderId,
                actual = response.body?.serviceOrderId,
            )
        }
        verify { registerBudget.execute(input = serviceOrderId) }
    }

    @Test
    fun `find approve reject trade and delete delegate to use cases`() {
        val serviceOrderId = UUID.randomUUID()
        val budget = BudgetFixtures.pricedResponse(serviceOrderId = serviceOrderId)
        every { findBudget.execute(input = serviceOrderId) } returns budget
        every { approveBudget.execute(input = serviceOrderId) } returns budget
        every { rejectBudget.execute(input = serviceOrderId) } returns budget
        every { tradeBudget.execute(input = serviceOrderId) } returns budget
        every { deleteBudget.execute(input = serviceOrderId) } returns Unit

        assertEquals(
            expected = budget.id,
            actual = controller.findBudgetByServiceId(id = serviceOrderId).body?.id,
        )
        assertEquals(
            expected = budget.id,
            actual = controller.approveBudget(id = serviceOrderId).body?.id,
        )
        assertEquals(
            expected = budget.id,
            actual = controller.rejectBudget(id = serviceOrderId).body?.id,
        )
        assertEquals(
            expected = budget.id,
            actual = controller.tradeBudget(id = serviceOrderId).body?.id,
        )
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.deleteBudgetByServiceId(id = serviceOrderId).statusCode,
        )
        verify { deleteBudget.execute(input = serviceOrderId) }
    }
}

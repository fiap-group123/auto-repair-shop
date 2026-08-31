package br.com.autorepairshop.api.exception.budget

import br.com.autorepairshop.budget.domain.exception.BudgetException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class BudgetApiExceptionHandlerTest {
    private val handler = BudgetApiExceptionHandler()

    @Test
    fun `maps domain exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleBudget(
                ex = BudgetException.BudgetNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleBudget(
                ex = BudgetException.ServiceOrderNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleBudget(
                ex = BudgetException.BudgetAlreadyExists(message = "exists"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleBudget(
                ex = BudgetException.EmptyBudget(message = "empty"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleBudget(
                ex = BudgetException.InvalidBudgetStatusTransition(message = "invalid"),
            ).status,
        )
    }
}

package br.com.autorepairshop.api.exception.serviceandexecution

import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class ServiceOrderApiExceptionHandlerTest {
    private val handler = ServiceOrderApiExceptionHandler()

    @Test
    fun `maps domain exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleServiceOrder(
                ex = ServiceOrderException.ServiceOrderNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleServiceOrder(
                ex = ServiceOrderException.OpenOrderAlreadyExists(message = "open"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleServiceOrder(
                ex = ServiceOrderException.VehicleNotOwnedByCustomer(message = "owner"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleServiceOrder(
                ex = ServiceOrderException.InvalidStatusTransition(message = "status"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleServiceOrder(
                ex = ServiceOrderException.InvalidDuration(message = "duration"),
            ).status,
        )
    }
}

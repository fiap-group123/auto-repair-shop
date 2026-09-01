package br.com.autorepairshop.api.exception

import br.com.autorepairshop.shared.domain.exception.DomainException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import kotlin.test.assertEquals

@Tag("unit")
class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()

    @Test
    fun `maps leftover domain exceptions to 422`() {
        val problem = handler.handleDomain(ex = object : DomainException(message = "rule") {})

        assertEquals(expected = HttpStatus.UNPROCESSABLE_CONTENT.value(), actual = problem.status)
        assertEquals(expected = "rule", actual = problem.detail)
    }

    @Test
    fun `maps unreadable bodies to 400`() {
        val problem = handler.handleUnreadable(
            ex = HttpMessageNotReadableException("Malformed JSON", MockHttpInputMessage(ByteArray(size = 0))),
        )

        assertEquals(expected = HttpStatus.BAD_REQUEST.value(), actual = problem.status)
        assertEquals(expected = "Malformed JSON", actual = problem.detail)
    }
}

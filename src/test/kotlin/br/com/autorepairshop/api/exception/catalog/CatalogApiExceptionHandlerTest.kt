package br.com.autorepairshop.api.exception.catalog

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class CatalogApiExceptionHandlerTest {
    private val handler = CatalogApiExceptionHandler()

    @Test
    fun `maps domain exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.ServiceNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.ExtraServiceNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.ServiceAlreadyExists(message = "exists"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.InvalidServiceName(message = "name"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.InvalidStatusTransition(message = "invalid"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.InvalidExtraServiceStatusTransition(message = "invalid"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCatalog(
                ex = CatalogException.InvalidDuration(message = "duration"),
            ).status,
        )
    }
}

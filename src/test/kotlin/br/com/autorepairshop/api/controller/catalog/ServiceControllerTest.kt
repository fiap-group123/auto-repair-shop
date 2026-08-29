package br.com.autorepairshop.api.controller.catalog

import br.com.autorepairshop.api.dto.catalog.RegisterServiceRequest
import br.com.autorepairshop.api.dto.catalog.UpdateServiceRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.dto.UpdateServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.application.usecase.FindServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FinishServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.InProgressServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.ListServicesUseCase
import br.com.autorepairshop.catalog.application.usecase.RegisterServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.UpdateServiceUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import kotlin.test.assertEquals

@Tag("unit")
class ServiceControllerTest {
    private val registerService = mockk<RegisterServiceUseCase>()
    private val updateService = mockk<UpdateServiceUseCase>()
    private val inProgressService = mockk<InProgressServiceUseCase>()
    private val finishService = mockk<FinishServiceUseCase>()
    private val findService = mockk<FindServiceUseCase>()
    private val listServices = mockk<ListServicesUseCase>()
    private val controller = ServiceController(
        registerService = registerService,
        updateService = updateService,
        inProgressService = inProgressService,
        finishService = finishService,
        findService = findService,
        listServices = listServices,
    )

    @Test
    fun `register maps the request and returns 201`() {
        val service = CatalogFixtures.activeService().toResponse()
        every { registerService.execute(input = any()) } returns service

        withHttpRequest(requestUri = "/services") {
            val response = controller.register(
                request = RegisterServiceRequest(
                    serviceOrderId = service.serviceOrderId,
                    name = CatalogFixtures.NAME,
                    basePrice = BigDecimal(CatalogFixtures.PRICE),
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = service.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerService.execute(
                input = RegisterServiceCommand(
                    serviceOrderId = service.serviceOrderId,
                    name = CatalogFixtures.NAME,
                    basePrice = BigDecimal(CatalogFixtures.PRICE),
                ),
            )
        }
    }

    @Test
    fun `list find start finish and update delegate to use cases`() {
        val service = CatalogFixtures.activeService().toResponse()
        every { listServices.execute(input = Unit) } returns listOf(element = service)
        every { findService.execute(input = service.id) } returns service
        every { inProgressService.execute(input = service.id) } returns service
        every { finishService.execute(input = service.id) } returns service
        every { updateService.execute(input = any()) } returns service

        assertEquals(
            expected = 1,
            actual = controller.list().body?.size,
        )
        assertEquals(
            expected = service.id,
            actual = controller.findById(id = service.id).body?.id,
        )
        assertEquals(
            expected = service.id,
            actual = controller.start(id = service.id).body?.id,
        )
        assertEquals(
            expected = service.id,
            actual = controller.finish(id = service.id).body?.id,
        )
        assertEquals(
            expected = HttpStatus.OK,
            actual = controller.update(
                id = service.id,
                request = UpdateServiceRequest(name = CatalogFixtures.OTHER_NAME),
            ).statusCode,
        )
        verify {
            updateService.execute(
                input = UpdateServiceCommand(
                    serviceId = service.id,
                    name = CatalogFixtures.OTHER_NAME,
                    basePrice = null,
                ),
            )
        }
    }
}

package br.com.autorepairshop.api.controller.catalog

import br.com.autorepairshop.api.dto.catalog.RegisterExtraServiceRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.RegisterExtraServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.application.usecase.ApproveExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FindExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.ListExtraServicesByServiceOrderIdUseCase
import br.com.autorepairshop.catalog.application.usecase.RegisterExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.RejectExtraServiceUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

@Tag("unit")
class ExtraServiceControllerTest {
    private val registerExtra = mockk<RegisterExtraServiceUseCase>()
    private val findExtra = mockk<FindExtraServiceUseCase>()
    private val listByServiceOrder = mockk<ListExtraServicesByServiceOrderIdUseCase>()
    private val approveExtra = mockk<ApproveExtraServiceUseCase>()
    private val rejectExtra = mockk<RejectExtraServiceUseCase>()
    private val controller = ExtraServiceController(
        registerExtra = registerExtra,
        findExtra = findExtra,
        listByServiceOrder = listByServiceOrder,
        approveExtra = approveExtra,
        rejectExtra = rejectExtra,
    )

    @Test
    fun `register maps the request and returns 201`() {
        val extra = CatalogFixtures.extraService().toResponse()
        every { registerExtra.execute(input = any()) } returns extra

        withHttpRequest(requestUri = "/extra-services") {
            val response = controller.register(
                request = RegisterExtraServiceRequest(
                    serviceOrderId = extra.serviceOrderId,
                    name = CatalogFixtures.OTHER_NAME,
                    basePrice = BigDecimal(CatalogFixtures.PRICE),
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = extra.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerExtra.execute(
                input = RegisterExtraServiceCommand(
                    serviceOrderId = extra.serviceOrderId,
                    name = CatalogFixtures.OTHER_NAME,
                    basePrice = BigDecimal(CatalogFixtures.PRICE),
                ),
            )
        }
    }

    @Test
    fun `list find approve and reject delegate to use cases`() {
        val extra = CatalogFixtures.extraService().toResponse()
        every { findExtra.execute(input = extra.id) } returns extra
        every { listByServiceOrder.execute(input = extra.serviceOrderId) } returns listOf(element = extra)
        every { approveExtra.execute(input = extra.id) } returns extra
        every { rejectExtra.execute(input = extra.id) } returns extra

        assertEquals(
            expected = extra.id,
            actual = controller.findById(id = extra.id).body?.id,
        )
        assertEquals(
            expected = extra.id,
            actual = controller.listByServiceOrderId(serviceOrderId = extra.serviceOrderId).body?.single()?.id,
        )
        assertEquals(
            expected = extra.id,
            actual = controller.approve(id = extra.id).body?.id,
        )
        assertEquals(
            expected = extra.id,
            actual = controller.reject(id = extra.id).body?.id,
        )
        verify { findExtra.execute(input = extra.id) }
        verify { listByServiceOrder.execute(input = extra.serviceOrderId) }
        verify { approveExtra.execute(input = extra.id) }
        verify { rejectExtra.execute(input = extra.id) }
    }

    @Test
    fun `listing by service order delegates to the use case`() {
        val serviceOrderId = UUID.randomUUID()
        every { listByServiceOrder.execute(input = serviceOrderId) } returns emptyList()

        controller.listByServiceOrderId(serviceOrderId = serviceOrderId)

        verify { listByServiceOrder.execute(input = serviceOrderId) }
    }
}

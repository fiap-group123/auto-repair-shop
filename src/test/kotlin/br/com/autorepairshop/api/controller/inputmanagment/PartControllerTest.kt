package br.com.autorepairshop.api.controller.inputmanagment

import br.com.autorepairshop.api.dto.inputmanagment.RegisterPartRequest
import br.com.autorepairshop.api.dto.inputmanagment.UpdatePartRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.application.dto.UpdatePartCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.application.usecase.DeletePartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.FindPartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ListPartsByServiceOrderIdUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterPartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.UpdatePartUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class PartControllerTest {
    private val registerPart = mockk<RegisterPartUseCase>()
    private val findPart = mockk<FindPartUseCase>()
    private val listByServiceOrder = mockk<ListPartsByServiceOrderIdUseCase>()
    private val updatePart = mockk<UpdatePartUseCase>()
    private val deletePart = mockk<DeletePartUseCase>(relaxUnitFun = true)
    private val controller = PartController(
        registerPart = registerPart,
        findPart = findPart,
        listByServiceOrder = listByServiceOrder,
        updatePart = updatePart,
        deletePart = deletePart,
    )

    @Test
    fun `register maps the request and returns 201`() {
        val part = InventoryFixtures.part().toResponse()
        every { registerPart.execute(input = any()) } returns part

        withHttpRequest(requestUri = "/parts") {
            val response = controller.register(
                request = RegisterPartRequest(
                    serviceOrderId = part.serviceOrderId,
                    inventoryId = part.inventoryId,
                    quantity = part.quantity,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = part.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerPart.execute(
                input = RegisterPartCommand(
                    serviceOrderId = part.serviceOrderId,
                    inventoryId = part.inventoryId,
                    quantity = part.quantity,
                ),
            )
        }
    }

    @Test
    fun `list find update and delete delegate to use cases`() {
        val part = InventoryFixtures.part().toResponse()
        every { findPart.execute(input = part.id) } returns part
        every { listByServiceOrder.execute(input = part.serviceOrderId) } returns listOf(element = part)
        every { updatePart.execute(input = any()) } returns part.copy(quantity = 4)

        assertEquals(
            expected = part.id,
            actual = controller.findById(id = part.id).body?.id,
        )
        assertEquals(
            expected = part.id,
            actual = controller.listByServiceOrderId(serviceOrderId = part.serviceOrderId).body?.single()?.id,
        )
        assertEquals(
            expected = 4,
            actual = controller.update(
                id = part.id,
                request = UpdatePartRequest(quantity = 4),
            ).body?.quantity,
        )
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.delete(id = part.id).statusCode,
        )
        verify {
            updatePart.execute(
                input = UpdatePartCommand(
                    partId = part.id,
                    quantity = 4,
                ),
            )
        }
        verify { deletePart.execute(input = part.id) }
    }
}

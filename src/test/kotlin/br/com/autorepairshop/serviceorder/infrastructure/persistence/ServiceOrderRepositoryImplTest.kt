package br.com.autorepairshop.serviceorder.infrastructure.persistence

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class ServiceOrderRepositoryImplTest {
    private val jpa = mockk<ServiceOrderJpaRepository>()
    private val repo = ServiceOrderRepositoryImpl(jpa = jpa)

    @Test
    fun `maps an order through save and queries`() {
        val order = ServiceOrderFixtures.inDiagnosisWithBudget()
        val stored = slot<ServiceOrderEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(order = order)

        every { jpa.findById(order.id.value) } returns Optional.of(stored.captured)
        every { jpa.findAll() } returns listOf(element = stored.captured)
        every { jpa.findAllByCustomerId(customerId = order.customerId) } returns listOf(element = stored.captured)
        every {
            jpa.existsByVehicleIdAndStatusNot(
                vehicleId = order.vehicleId,
                status = ServiceOrderStatusColumn.DELIVERED,
            )
        } returns true

        assertEquals(
            expected = order.id,
            actual = repo.findById(id = order.id)?.id,
        )
        assertEquals(
            expected = order.status.name,
            actual = stored.captured.status.name,
        )
        assertEquals(
            expected = order.id,
            actual = repo.findAll().single().id,
        )
        assertEquals(
            expected = order.id,
            actual = repo.findByCustomerId(customerId = order.customerId).single().id,
        )
        assertTrue(repo.existsOpenByVehicleId(vehicleId = order.vehicleId))
    }

    @Test
    fun `returns null and false when nothing is stored`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every {
            jpa.existsByVehicleIdAndStatusNot(
                vehicleId = any(),
                status = ServiceOrderStatusColumn.DELIVERED,
            )
        } returns false

        assertNull(repo.findById(id = ServiceOrderFixtures.received().id))
        assertFalse(repo.existsOpenByVehicleId(vehicleId = UUID.randomUUID()))
    }
}

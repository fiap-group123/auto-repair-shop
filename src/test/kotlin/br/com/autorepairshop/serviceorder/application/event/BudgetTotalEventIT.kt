package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.TestcontainersConfiguration
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.dto.UpdateServiceCommand
import br.com.autorepairshop.catalog.application.usecase.RegisterServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.UpdateServiceUseCase
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import br.com.autorepairshop.serviceorder.application.usecase.FinishDiagnosisUseCase
import br.com.autorepairshop.serviceorder.application.usecase.StartDiagnosisUseCase
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import kotlin.test.assertEquals

@Tag("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class BudgetTotalEventIT {

    @Autowired
    private lateinit var customers: CustomerRepository

    @Autowired
    private lateinit var vehicles: VehicleRepository

    @Autowired
    private lateinit var orders: ServiceOrderRepository

    @Autowired
    private lateinit var budgets: BudgetRepository

    @Autowired
    private lateinit var registerService: RegisterServiceUseCase

    @Autowired
    private lateinit var updateService: UpdateServiceUseCase

    @Autowired
    private lateinit var startDiagnosis: StartDiagnosisUseCase

    @Autowired
    private lateinit var finishDiagnosis: FinishDiagnosisUseCase

    @Test
    fun `registering a service recalculates the budget total`() {
        val orderId = openOrderInDiagnosis()

        registerService.execute(
            input = RegisterServiceCommand(
                serviceOrderId = orderId.value,
                name = "Troca de oleo",
                basePrice = BigDecimal("120.00"),
            ),
        )
        assertTotal(
            orderId = orderId,
            expected = "120.00",
        )

        registerService.execute(
            input = RegisterServiceCommand(
                serviceOrderId = orderId.value,
                name = "Alinhamento",
                basePrice = BigDecimal("80.50"),
            ),
        )
        assertTotal(
            orderId = orderId,
            expected = "200.50",
        )
    }

    @Test
    fun `changing a price recalculates the budget after approval is sent`() {
        val orderId = openOrderInDiagnosis()
        val service = registerService.execute(
            input = RegisterServiceCommand(
                serviceOrderId = orderId.value,
                name = "Revisao",
                basePrice = BigDecimal("100.00"),
            ),
        )

        updateService.execute(
            input = UpdateServiceCommand(
                serviceId = service.id,
                basePrice = BigDecimal("250.00"),
            ),
        )
        assertTotal(
            orderId = orderId,
            expected = "250.00",
        )

        finishDiagnosis.execute(input = orderId.value)

        updateService.execute(
            input = UpdateServiceCommand(
                serviceId = service.id,
                basePrice = BigDecimal("999.00"),
            ),
        )
        assertTotal(
            orderId = orderId,
            expected = "999.00",
        )
    }

    private fun openOrderInDiagnosis(): ServiceOrderId {
        val customer = owner()
        val vehicle = CustomerFixtures.vehicle(
            owner = customer,
            plate = nextPlate(),
        )
        vehicles.save(vehicle = vehicle)
        val order = ServiceOrder.open(
            customerId = customer.id.value,
            vehicleId = vehicle.id.value,
        )
        orders.save(order = order)
        startDiagnosis.execute(input = order.id.value)
        return order.id
    }

    /** Documents are unique in the schema, so both tests in this class share one owner. */
    private fun owner(): Customer {
        val document = Document.of(raw = CustomerFixtures.VALID_CNPJ)
        customers.findByDocumentId(id = document)?.let { return it }
        val customer = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.VALID_CNPJ)
        customers.save(customer = customer)
        return customer
    }

    private fun assertTotal(
        orderId: ServiceOrderId,
        expected: String,
    ) {
        assertEquals(
            expected = expected,
            actual = budgets.findByServiceOrderId(serviceOrderId = orderId.value)?.total?.amount?.toPlainString(),
        )
    }

    private companion object {
        private var plates = 0

        /** Plates are unique in the schema, so every order in this class needs a fresh one. */
        fun nextPlate(): String = "XYZ%dA11".format(plates++)
    }
}

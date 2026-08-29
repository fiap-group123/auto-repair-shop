package br.com.autorepairshop.api.controller.customer

import br.com.autorepairshop.api.dto.customer.RegisterCustomerRequest
import br.com.autorepairshop.api.dto.customer.UpdateCustomerRequest
import br.com.autorepairshop.api.security.AuthorizationSupport
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.toResponse
import br.com.autorepairshop.customer.application.usecase.customer.DeactivateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerByDocumentUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.ListCustomersUseCase
import br.com.autorepairshop.customer.application.usecase.customer.ReactivateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.RegisterCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.UpdateCustomerUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class CustomerControllerTest {
    private val registerCustomer = mockk<RegisterCustomerUseCase>()
    private val updateCustomer = mockk<UpdateCustomerUseCase>()
    private val deactivateCustomer = mockk<DeactivateCustomerUseCase>()
    private val reactivateCustomer = mockk<ReactivateCustomerUseCase>()
    private val findCustomer = mockk<FindCustomerUseCase>()
    private val findCustomerByDocument = mockk<FindCustomerByDocumentUseCase>()
    private val listCustomers = mockk<ListCustomersUseCase>()
    private val authorization = mockk<AuthorizationSupport>(relaxUnitFun = true)
    private val controller = CustomerController(
        registerCustomer = registerCustomer,
        updateCustomer = updateCustomer,
        deactivateCustomer = deactivateCustomer,
        reactivateCustomer = reactivateCustomer,
        findCustomer = findCustomer,
        findCustomerByDocument = findCustomerByDocument,
        listCustomers = listCustomers,
        authorization = authorization,
    )

    @Test
    fun `register maps the HTTP request to a command`() {
        val customer = CustomerFixtures.activeCustomer().toResponse()
        every { registerCustomer.execute(input = any()) } returns customer

        withHttpRequest(requestUri = "/customers") {
            val response = controller.register(
                request = RegisterCustomerRequest(
                    documentId = CustomerFixtures.VALID_CPF,
                    name = CustomerFixtures.NAME,
                    email = CustomerFixtures.EMAIL,
                    phone = CustomerFixtures.PHONE,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
        }
        verify {
            registerCustomer.execute(
                input = RegisterCustomerCommand(
                    documentId = CustomerFixtures.VALID_CPF,
                    name = CustomerFixtures.NAME,
                    email = CustomerFixtures.EMAIL,
                    phone = CustomerFixtures.PHONE,
                ),
            )
        }
    }

    @Test
    fun `list find update deactivate and reactivate delegate to use cases`() {
        val customer = CustomerFixtures.activeCustomer().toResponse()
        every { listCustomers.execute(input = Unit) } returns listOf(element = customer)
        every { findCustomerByDocument.execute(input = CustomerFixtures.VALID_CPF) } returns customer
        every { findCustomer.execute(input = customer.id) } returns customer
        every { updateCustomer.execute(input = any()) } returns customer
        every { deactivateCustomer.execute(input = customer.id) } returns Unit
        every { reactivateCustomer.execute(input = customer.id) } returns Unit

        assertEquals(
            expected = 1,
            actual = controller.list().body?.size,
        )
        assertEquals(
            expected = customer.id,
            actual = controller.findByDocument(document = CustomerFixtures.VALID_CPF).body?.id,
        )
        assertEquals(
            expected = customer.id,
            actual = controller.findById(id = customer.id).body?.id,
        )
        assertEquals(
            expected = HttpStatus.OK,
            actual = controller.update(
                id = customer.id,
                request = UpdateCustomerRequest(name = "Jane Doe"),
            ).statusCode,
        )
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.deactivate(id = customer.id).statusCode,
        )
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.reactivate(id = customer.id).statusCode,
        )
        verify {
            authorization.requireCanAccessCustomer(customerId = customer.id)
            updateCustomer.execute(
                input = UpdateCustomerCommand(
                    customerId = customer.id,
                    name = "Jane Doe",
                    email = null,
                    phone = null,
                ),
            )
        }
    }
}

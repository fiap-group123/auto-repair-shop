package br.com.autorepairshop.api.exception.customer

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class CustomerApiExceptionHandlerTest {
    private val handler = CustomerApiExceptionHandler()

    @Test
    fun `maps customer exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleCustomer(ex = CustomerException.CustomerNotFound(message = "missing")).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleCustomer(ex = CustomerException.CustomerAlreadyExists(message = "exists")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.CustomerAlreadyActive(message = "active")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.InvalidDocument(message = "doc")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.InvalidPersonName(message = "name")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.InvalidPhoneNumber(message = "phone")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.InvalidEmailAddress(message = "email")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleCustomer(ex = CustomerException.CustomerInactive(message = "inactive")).status,
        )
    }

    @Test
    fun `maps vehicle exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleVehicle(ex = VehicleException.VehicleNotFound(message = "missing")).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleVehicle(ex = VehicleException.VehicleAlreadyExists(message = "exists")).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleVehicle(ex = VehicleException.AlreadyOwnedByCustomer(message = "owned")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleVehicle(ex = VehicleException.InvalidLicensePlate(message = "plate")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleVehicle(ex = VehicleException.InvalidModelYear(message = "year")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleVehicle(ex = VehicleException.InvalidVehicleName(message = "name")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleVehicle(ex = VehicleException.VehicleAlreadyActive(message = "active")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleVehicle(ex = VehicleException.VehicleInactive(message = "inactive")).status,
        )
    }
}

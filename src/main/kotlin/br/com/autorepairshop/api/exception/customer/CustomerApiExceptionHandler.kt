package br.com.autorepairshop.api.exception.customer

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class CustomerApiExceptionHandler {

    @ExceptionHandler(CustomerException::class)
    fun handleCustomer(ex: CustomerException): ProblemDetail = when (ex) {
        is CustomerException.CustomerNotFound ->
            problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is CustomerException.CustomerAlreadyExists ->
            problem(status = HttpStatus.CONFLICT, ex = ex)

        is CustomerException.CustomerAlreadyActive,
        is CustomerException.InvalidDocument,
        is CustomerException.InvalidPersonName,
        is CustomerException.InvalidPhoneNumber,
        is CustomerException.InvalidEmailAddress,
        is CustomerException.CustomerInactive,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }

    @ExceptionHandler(VehicleException::class)
    fun handleVehicle(ex: VehicleException): ProblemDetail = when (ex) {
        is VehicleException.VehicleNotFound ->
            problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is VehicleException.VehicleAlreadyExists,
        is VehicleException.AlreadyOwnedByCustomer,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is VehicleException.InvalidLicensePlate,
        is VehicleException.InvalidModelYear,
        is VehicleException.InvalidVehicleName,
        is VehicleException.VehicleAlreadyActive,
        is VehicleException.VehicleInactive,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

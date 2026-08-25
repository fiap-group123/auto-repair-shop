package br.com.autorepairshop.api

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.shared.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
open class ApiExceptionHandler {

    @ExceptionHandler(CustomerException::class)
    open fun handleCustomer(ex: CustomerException): ProblemDetail = when (ex) {
        is CustomerException.CustomerNotFound -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is CustomerException.CustomerAlreadyExists -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is CustomerException.CustomerAlreadyActive,
        is CustomerException.InvalidDocument,
        is CustomerException.InvalidPersonName,
        is CustomerException.InvalidPhoneNumber,
        is CustomerException.InvalidEmailAddress,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }

    @ExceptionHandler(VehicleException::class)
    open fun handleVehicle(ex: VehicleException): ProblemDetail = when (ex) {
        is VehicleException.VehicleNotFound -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is VehicleException.VehicleAlreadyExists,
        is VehicleException.AlreadyOwnedByCustomer,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is VehicleException.InvalidLicensePlate,
        is VehicleException.InvalidModelYear,
        is VehicleException.InvalidVehicleName,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }

    @ExceptionHandler(DomainException::class)
    open fun handleDomain(ex: DomainException): ProblemDetail =
        problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)

    private fun problem(
        status: HttpStatus,
        ex: DomainException,
    ): ProblemDetail = ProblemDetail.forStatusAndDetail(status, ex.message)
}

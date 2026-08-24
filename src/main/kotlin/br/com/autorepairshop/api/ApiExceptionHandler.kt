package br.com.autorepairshop.api

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.shared.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(CustomerException.CustomerNotFound::class, VehicleException.VehicleNotFound::class)
    fun notFound(ex: DomainException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(
        CustomerException.CustomerAlreadyExists::class,
        VehicleException.VehicleAlreadyExists::class,
        VehicleException.AlreadyOwnedByCustomer::class,
    )
    fun conflict(ex: DomainException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message)

    @ExceptionHandler(DomainException::class)
    fun unprocessable(ex: DomainException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
}

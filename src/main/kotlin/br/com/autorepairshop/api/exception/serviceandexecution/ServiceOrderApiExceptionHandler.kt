package br.com.autorepairshop.api.exception.serviceandexecution

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ServiceOrderApiExceptionHandler {

    @ExceptionHandler(ServiceOrderException::class)
    fun handleServiceOrder(ex: ServiceOrderException): ProblemDetail = when (ex) {
        is ServiceOrderException.ServiceOrderNotFound,
        -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is ServiceOrderException.OpenOrderAlreadyExists,
        is ServiceOrderException.VehicleNotOwnedByCustomer,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is ServiceOrderException.InvalidStatusTransition,
        is ServiceOrderException.InvalidDuration,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

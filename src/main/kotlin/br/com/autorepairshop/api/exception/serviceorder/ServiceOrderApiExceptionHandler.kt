package br.com.autorepairshop.api.exception.serviceorder

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
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
        is ServiceOrderException.ItemNotFound,
        -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is ServiceOrderException.OpenOrderAlreadyExists,
        is ServiceOrderException.VehicleNotOwnedByCustomer,
        is ServiceOrderException.ItemAlreadyAdded,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is ServiceOrderException.InvalidStatusTransition,
        is ServiceOrderException.ItemsLocked,
        is ServiceOrderException.EmptyBudget,
        is ServiceOrderException.InvalidQuantity,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

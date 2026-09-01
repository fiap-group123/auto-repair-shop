package br.com.autorepairshop.api.exception.inputmanagement

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class InventoryApiExceptionHandler {

    @ExceptionHandler(InventoryException::class)
    fun handleInventory(ex: InventoryException): ProblemDetail = when (ex) {
        is InventoryException.InventoryNotFound,
        is InventoryException.PartNotFound,
        -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is InventoryException.InventoryAlreadyExists,
        is InventoryException.PartAlreadyExists,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is InventoryException.InsufficientStock,
        is InventoryException.InventoryInactive,
        is InventoryException.InventoryAlreadyActive,
        is InventoryException.InvalidInventoryName,
        is InventoryException.InvalidQuantity,
        is InventoryException.InvalidStatusTransition,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

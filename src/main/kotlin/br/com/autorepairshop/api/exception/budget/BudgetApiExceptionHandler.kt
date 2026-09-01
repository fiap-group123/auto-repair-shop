package br.com.autorepairshop.api.exception.budget

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.budget.domain.exception.BudgetException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class BudgetApiExceptionHandler {

    @ExceptionHandler(BudgetException::class)
    fun handleBudget(ex: BudgetException): ProblemDetail = when (ex) {
        is BudgetException.BudgetNotFound,
        is BudgetException.ServiceOrderNotFound,
        -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is BudgetException.BudgetAlreadyExists,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is BudgetException.EmptyBudget,
        is BudgetException.InvalidBudgetStatusTransition,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

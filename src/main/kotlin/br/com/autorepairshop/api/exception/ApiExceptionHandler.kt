package br.com.autorepairshop.api.exception

import br.com.autorepairshop.shared.domain.exception.DomainException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

fun problem(
    status: HttpStatus,
    detail: String?,
): ProblemDetail = ProblemDetail.forStatusAndDetail(status, detail)

fun problem(
    status: HttpStatus,
    ex: DomainException,
): ProblemDetail = problem(status = status, detail = ex.message)

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class ApiExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException): ProblemDetail = problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ProblemDetail = problem(
        status = HttpStatus.BAD_REQUEST,
        detail = ex.mostSpecificCause.message ?: "Malformed request body.",
    )
}

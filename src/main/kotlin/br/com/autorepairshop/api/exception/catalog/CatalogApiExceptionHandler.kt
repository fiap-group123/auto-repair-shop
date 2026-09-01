package br.com.autorepairshop.api.exception.catalog

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class CatalogApiExceptionHandler {

    @ExceptionHandler(CatalogException::class)
    fun handleCatalog(ex: CatalogException): ProblemDetail = when (ex) {
        is CatalogException.ServiceNotFound,
        is CatalogException.ExtraServiceNotFound,
        -> problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is CatalogException.ServiceAlreadyExists ->
            problem(status = HttpStatus.CONFLICT, ex = ex)

        is CatalogException.InvalidServiceName,
        is CatalogException.InvalidStatusTransition,
        is CatalogException.InvalidExtraServiceStatusTransition,
        is CatalogException.InvalidDuration,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }
}

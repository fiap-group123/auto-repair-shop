package br.com.autorepairshop.api.exception.authentication

import br.com.autorepairshop.api.exception.problem
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthApiExceptionHandler {

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuth(ex: AuthenticationException): ProblemDetail = when (ex) {
        is AuthenticationException.InvalidCredentials,
        is AuthenticationException.Unauthenticated,
        -> problem(status = HttpStatus.UNAUTHORIZED, ex = ex)

        is AuthenticationException.UserNotFound ->
            problem(status = HttpStatus.NOT_FOUND, ex = ex)

        is AuthenticationException.UserAlreadyExists,
        is AuthenticationException.CustomerAlreadyHasUser,
        -> problem(status = HttpStatus.CONFLICT, ex = ex)

        is AuthenticationException.Forbidden ->
            problem(status = HttpStatus.FORBIDDEN, ex = ex)

        is AuthenticationException.UserInactive,
        is AuthenticationException.InvalidEmail,
        is AuthenticationException.InvalidRole,
        -> problem(status = HttpStatus.UNPROCESSABLE_CONTENT, ex = ex)
    }

    @ExceptionHandler(SpringAuthenticationException::class)
    fun handleSpringAuth(ex: SpringAuthenticationException): ProblemDetail = problem(
        status = HttpStatus.UNAUTHORIZED,
        detail = ex.message ?: "Authentication required.",
    )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ProblemDetail = problem(
        status = HttpStatus.FORBIDDEN,
        detail = ex.message ?: "Access denied.",
    )

    @ExceptionHandler(JwtException::class)
    fun handleJwt(ex: JwtException): ProblemDetail = problem(
        status = HttpStatus.UNAUTHORIZED,
        detail = ex.message ?: "Invalid or expired token.",
    )
}

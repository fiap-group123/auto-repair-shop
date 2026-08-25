package br.com.autorepairshop.api.security

import br.com.autorepairshop.api.exception.problem
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.net.URI

@Component
class SecurityProblemSupport(private val jsonMapper: JsonMapper) :
    AuthenticationEntryPoint,
    AccessDeniedHandler,
    AuthenticationFailureHandler {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        write(
            request = request,
            response = response,
            status = HttpStatus.UNAUTHORIZED,
            detail = authException.message ?: "Authentication required.",
        )
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        write(
            request = request,
            response = response,
            status = HttpStatus.FORBIDDEN,
            detail = accessDeniedException.message ?: "Access denied.",
        )
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        commence(request = request, response = response, authException = exception)
    }

    private fun write(
        request: HttpServletRequest,
        response: HttpServletResponse,
        status: HttpStatus,
        detail: String,
    ) {
        if (response.isCommitted) {
            return
        }
        val problem = problem(status = status, detail = detail)
        problem.instance = URI.create(request.requestURI)
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        jsonMapper.writeValue(response.outputStream, problem)
    }
}

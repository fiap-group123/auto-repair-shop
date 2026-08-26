package br.com.autorepairshop.api.security

import br.com.autorepairshop.authentication.application.usecase.RequireActiveUserUseCase
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

class ActiveUserFilter(
    private val requireActiveUser: RequireActiveUserUseCase,
    private val securityProblemSupport: SecurityProblemSupport,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val jwt = SecurityContextHolder.getContext().authentication?.principal as? Jwt
        if (jwt == null) {
            filterChain.doFilter(request, response)
            return
        }
        val userId = parseUserId(subject = jwt.subject)
        if (userId == null) {
            reject(request = request, response = response, detail = "JWT subject is invalid.")
            return
        }
        try {
            requireActiveUser.execute(input = userId)
        } catch (ex: AuthenticationException) {
            reject(
                request = request,
                response = response,
                detail = ex.message ?: "User is inactive.",
            )
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun parseUserId(subject: String?): UUID? = subject?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    private fun reject(
        request: HttpServletRequest,
        response: HttpServletResponse,
        detail: String,
    ) {
        SecurityContextHolder.clearContext()
        securityProblemSupport.commence(
            request = request,
            response = response,
            authException = DisabledException(detail),
        )
    }
}

package br.com.autorepairshop.api.security

import br.com.autorepairshop.authentication.application.usecase.RequireActiveUserUseCase
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID

@Tag("unit")
class ActiveUserFilterTest {
    private val requireActiveUser = mockk<RequireActiveUserUseCase>()
    private val securityProblemSupport = mockk<SecurityProblemSupport>()
    private val filterChain = mockk<FilterChain>()
    private val filter = ActiveUserFilter(
        requireActiveUser = requireActiveUser,
        securityProblemSupport = securityProblemSupport,
    )

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `skips the active check when there is no jwt`() {
        every { filterChain.doFilter(any(), any()) } just Runs

        filter.doFilter(
            MockHttpServletRequest(),
            MockHttpServletResponse(),
            filterChain,
        )

        verify(exactly = 0) { requireActiveUser.execute(input = any()) }
        verify { filterChain.doFilter(any(), any()) }
    }

    @Test
    fun `rejects a jwt without a valid subject`() {
        authenticate(subject = "not-a-uuid")
        every { securityProblemSupport.commence(any(), any(), any()) } just Runs

        filter.doFilter(
            MockHttpServletRequest(),
            MockHttpServletResponse(),
            filterChain,
        )

        verify(exactly = 0) { filterChain.doFilter(any(), any()) }
        verify { securityProblemSupport.commence(any(), any(), any()) }
    }

    @Test
    fun `rejects an inactive user on every request`() {
        val userId = UUID.randomUUID()
        authenticate(subject = userId.toString())
        every { requireActiveUser.execute(input = userId) } throws AuthenticationException.UserInactive(
            message = "User is inactive.",
        )
        every { securityProblemSupport.commence(any(), any(), any()) } just Runs

        filter.doFilter(
            MockHttpServletRequest(),
            MockHttpServletResponse(),
            filterChain,
        )

        verify(exactly = 0) { filterChain.doFilter(any(), any()) }
        verify { securityProblemSupport.commence(any(), any(), any()) }
    }

    @Test
    fun `lets an active user through`() {
        val userId = UUID.randomUUID()
        authenticate(subject = userId.toString())
        every { requireActiveUser.execute(input = userId) } returns Unit
        every { filterChain.doFilter(any(), any()) } just Runs

        filter.doFilter(
            MockHttpServletRequest(),
            MockHttpServletResponse(),
            filterChain,
        )

        verify { requireActiveUser.execute(input = userId) }
        verify { filterChain.doFilter(any(), any()) }
    }

    private fun authenticate(subject: String?) {
        val jwt = mockk<Jwt>()
        every { jwt.subject } returns subject
        val authentication = mockk<Authentication>()
        every { authentication.principal } returns jwt
        SecurityContextHolder.getContext().authentication = authentication
    }
}

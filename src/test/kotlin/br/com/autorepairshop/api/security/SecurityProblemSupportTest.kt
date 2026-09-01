package br.com.autorepairshop.api.security

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import tools.jackson.databind.json.JsonMapper
import java.io.OutputStream

@Tag("unit")
class SecurityProblemSupportTest {
    private val jsonMapper = mockk<JsonMapper>()
    private val support = SecurityProblemSupport(jsonMapper = jsonMapper)

    @Test
    fun `writes 401 for authentication failures`() {
        every { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) } just Runs

        support.commence(
            request = MockHttpServletRequest().apply { requestURI = "/auth/login" },
            response = MockHttpServletResponse(),
            authException = BadCredentialsException("Authentication required."),
        )

        verify {
            jsonMapper.writeValue(
                any<OutputStream>(),
                match<ProblemDetail> { problem ->
                    problem.status == HttpStatus.UNAUTHORIZED.value() &&
                        problem.detail == "Authentication required."
                },
            )
        }
    }

    @Test
    fun `writes 403 for access denied`() {
        every { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) } just Runs

        support.handle(
            request = MockHttpServletRequest().apply { requestURI = "/customers" },
            response = MockHttpServletResponse(),
            accessDeniedException = AccessDeniedException("Access denied."),
        )

        verify {
            jsonMapper.writeValue(
                any<OutputStream>(),
                match<ProblemDetail> { problem -> problem.status == HttpStatus.FORBIDDEN.value() },
            )
        }
    }

    @Test
    fun `reuses commence on authentication failure`() {
        every { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) } just Runs

        support.onAuthenticationFailure(
            request = MockHttpServletRequest(),
            response = MockHttpServletResponse(),
            exception = BadCredentialsException("nope"),
        )

        verify { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) }
    }

    @Test
    fun `skips writing when the response is already committed`() {
        val response = mockk<jakarta.servlet.http.HttpServletResponse>()
        every { response.isCommitted } returns true

        support.commence(
            request = MockHttpServletRequest(),
            response = response,
            authException = BadCredentialsException("late"),
        )

        verify(exactly = 0) { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) }
    }

    @Test
    fun `uses fallback details when the exception has no message`() {
        every { jsonMapper.writeValue(any<OutputStream>(), any<ProblemDetail>()) } just Runs
        val authException = mockk<org.springframework.security.core.AuthenticationException>()
        every { authException.message } returns null
        val denied = mockk<AccessDeniedException>()
        every { denied.message } returns null

        support.commence(
            request = MockHttpServletRequest(),
            response = MockHttpServletResponse(),
            authException = authException,
        )
        support.handle(
            request = MockHttpServletRequest(),
            response = MockHttpServletResponse(),
            accessDeniedException = denied,
        )

        verify {
            jsonMapper.writeValue(
                any<OutputStream>(),
                match<ProblemDetail> { it.detail == "Authentication required." },
            )
            jsonMapper.writeValue(
                any<OutputStream>(),
                match<ProblemDetail> { it.detail == "Access denied." },
            )
        }
    }
}

package br.com.autorepairshop.api.exception.accessidentity

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.JwtException
import kotlin.test.assertEquals

@Tag("unit")
class AuthApiExceptionHandlerTest {
    private val handler = AuthApiExceptionHandler()

    @Test
    fun `maps domain exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InvalidCredentials(message = "creds")).status,
        )
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleAuth(ex = AuthenticationException.Unauthenticated(message = "auth")).status,
        )
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InvalidRefresh(message = "refresh")).status,
        )
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleAuth(ex = AuthenticationException.RefreshReuse(message = "reuse")).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleAuth(ex = AuthenticationException.UserNotFound(message = "user")).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InviteNotFound(message = "invite")).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleAuth(ex = AuthenticationException.LinkedCustomerNotFound(message = "link")).status,
        )
        assertEquals(
            expected = HttpStatus.GONE.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InviteExpired(message = "expired")).status,
        )
        assertEquals(
            expected = HttpStatus.GONE.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InviteConsumed(message = "used")).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.UserAlreadyExists(message = "exists")).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.CustomerAlreadyHasUser(message = "has")).status,
        )
        assertEquals(
            expected = HttpStatus.FORBIDDEN.value(),
            actual = handler.handleAuth(ex = AuthenticationException.Forbidden(message = "no")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.UserInactive(message = "inactive")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.UserAlreadyActive(message = "active")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InvalidEmail(message = "email")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InvalidPassword(message = "password")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(ex = AuthenticationException.InvalidRole(message = "role")).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleAuth(
                ex = AuthenticationException.LinkedCustomerInactive(message = "inactive"),
            ).status,
        )
    }

    @Test
    fun `maps spring security failures`() {
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleSpringAuth(ex = BadCredentialsException("Authentication required.")).status,
        )
        assertEquals(
            expected = HttpStatus.FORBIDDEN.value(),
            actual = handler.handleAccessDenied(ex = AccessDeniedException("Access denied.")).status,
        )
        assertEquals(
            expected = HttpStatus.UNAUTHORIZED.value(),
            actual = handler.handleJwt(ex = JwtException("Invalid or expired token.")).status,
        )
    }
}

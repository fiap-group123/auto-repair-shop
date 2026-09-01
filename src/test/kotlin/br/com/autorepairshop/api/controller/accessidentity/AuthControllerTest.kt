package br.com.autorepairshop.api.controller.accessidentity

import br.com.autorepairshop.api.dto.accessidentity.LoginRequest
import br.com.autorepairshop.api.dto.accessidentity.RefreshTokenRequest
import br.com.autorepairshop.api.dto.accessidentity.RegisterUserRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.application.dto.LoginCommand
import br.com.autorepairshop.accessidentity.application.dto.RegisterUserCommand
import br.com.autorepairshop.accessidentity.application.dto.TokenResponse
import br.com.autorepairshop.accessidentity.application.dto.toResponse
import br.com.autorepairshop.accessidentity.application.usecase.LoginUseCase
import br.com.autorepairshop.accessidentity.application.usecase.LogoutUseCase
import br.com.autorepairshop.accessidentity.application.usecase.RefreshTokenUseCase
import br.com.autorepairshop.accessidentity.application.usecase.RegisterUserUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class AuthControllerTest {
    private val login = mockk<LoginUseCase>()
    private val registerUser = mockk<RegisterUserUseCase>()
    private val refreshToken = mockk<RefreshTokenUseCase>()
    private val logout = mockk<LogoutUseCase>(relaxUnitFun = true)
    private val controller = AuthController(
        login = login,
        registerUser = registerUser,
        refreshToken = refreshToken,
        logout = logout,
    )

    @Test
    fun `login returns the token from the use case`() {
        every { login.execute(input = any()) } returns TokenResponse(
            accessToken = "jwt",
            refreshToken = "refresh",
            expiresIn = 900,
        )

        val response = controller.login(
            request = LoginRequest(
                email = AuthFixtures.MANAGER_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
            ),
        )

        assertEquals(
            expected = HttpStatus.OK,
            actual = response.statusCode,
        )
        assertEquals(
            expected = "jwt",
            actual = response.body?.accessToken,
        )
        verify {
            login.execute(
                input = LoginCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
        }
    }

    @Test
    fun `register maps the request and returns 201`() {
        val created = AuthFixtures.manager().toResponse()
        every { registerUser.execute(input = any()) } returns created

        withHttpRequest(requestUri = "/auth/users") {
            val response = controller.register(
                request = RegisterUserRequest(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                ),
            )

            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = created.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerUser.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                    customerId = null,
                ),
            )
        }
    }

    @Test
    fun `refresh delegates to the use case`() {
        val tokens = TokenResponse(
            accessToken = "new-jwt",
            refreshToken = "new-refresh",
            expiresIn = 900,
        )
        every { refreshToken.execute(input = any()) } returns tokens

        assertEquals(
            expected = "new-jwt",
            actual = controller.refresh(request = RefreshTokenRequest(refreshToken = "old")).body?.accessToken,
        )
    }

    @Test
    fun `logout returns no content`() {
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.logout(request = RefreshTokenRequest(refreshToken = "refresh")).statusCode,
        )
        verify { logout.execute(input = any()) }
    }
}

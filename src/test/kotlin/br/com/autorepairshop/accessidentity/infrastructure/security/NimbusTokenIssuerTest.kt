package br.com.autorepairshop.accessidentity.infrastructure.security

import br.com.autorepairshop.accessidentity.AuthFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class NimbusTokenIssuerTest {
    private val encoder = mockk<JwtEncoder>()
    private val issuer = NimbusTokenIssuer(encoder = encoder, ttlSeconds = 60)

    @Test
    fun `issues a jwt for staff without customer id`() {
        val parameters = slot<JwtEncoderParameters>()
        val jwt = mockk<Jwt>()
        every { jwt.tokenValue } returns "access-token"
        every { encoder.encode(capture(parameters)) } returns jwt

        val token = issuer.issue(user = AuthFixtures.manager())

        assertEquals(expected = "access-token", actual = token)
        assertEquals(expected = "MANAGER", actual = parameters.captured.claims.claims["role"])
        assertNull(actual = parameters.captured.claims.claims["customerId"])
    }

    @Test
    fun `includes customer id for a client`() {
        val parameters = slot<JwtEncoderParameters>()
        val jwt = mockk<Jwt>()
        every { jwt.tokenValue } returns "client-token"
        every { encoder.encode(capture(parameters)) } returns jwt
        val client = AuthFixtures.client()

        issuer.issue(user = client)

        assertEquals(
            expected = client.customerId.toString(),
            actual = parameters.captured.claims.claims["customerId"],
        )
        assertEquals(expected = "CLIENT", actual = parameters.captured.claims.claims["role"])
    }
}

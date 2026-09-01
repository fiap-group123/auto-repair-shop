package br.com.autorepairshop.accessidentity.infrastructure.security

import br.com.autorepairshop.accessidentity.application.security.TokenIssuer
import br.com.autorepairshop.accessidentity.domain.aggregate.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class NimbusTokenIssuer(
    private val encoder: JwtEncoder,
    @Value("\${app.security.jwt.ttl-seconds}") private val ttlSeconds: Long,
) : TokenIssuer {
    override fun issue(user: User): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .subject(user.id.value.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttlSeconds))
            .claim("role", user.role.name)
            .apply {
                user.customerId?.let { claim("customerId", it.toString()) }
            }
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}

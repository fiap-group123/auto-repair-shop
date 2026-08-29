package br.com.autorepairshop.api.security

import br.com.autorepairshop.authentication.application.usecase.RequireActiveUserUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val granted = JwtGrantedAuthoritiesConverter()
        granted.setAuthoritiesClaimName("role")
        granted.setAuthorityPrefix("ROLE_")
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(granted)
        return converter
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
        securityProblemSupport: SecurityProblemSupport,
        requireActiveUser: RequireActiveUserUseCase,
    ): SecurityFilterChain {
        val activeUserFilter = ActiveUserFilter(
            requireActiveUser = requireActiveUser,
            securityProblemSupport = securityProblemSupport,
        )
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(securityProblemSupport)
                exceptions.accessDeniedHandler(securityProblemSupport)
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
                resourceServer.authenticationEntryPoint(securityProblemSupport)
                resourceServer.accessDeniedHandler(securityProblemSupport)
            }
            .addFilterAfter(activeUserFilter, BearerTokenAuthenticationFilter::class.java)
            .authorizeHttpRequests { requests -> configureAuthorization(requests = requests) }
        return http.build()
    }

    private fun configureAuthorization(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(
            "/error",
            "/.well-known/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
        ).permitAll()
        requests.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
        requests.requestMatchers(HttpMethod.POST, "/auth/users").permitAll()
        requests.requestMatchers(HttpMethod.POST, "/customers").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers/document/**")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.PUT, "/customers/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/customers/**").hasRole("MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/vehicles").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/vehicles/owner/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/vehicles/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.PUT, "/vehicles/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.PATCH, "/vehicles/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/services").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/services/customer/**", "/services/service-order/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/services/average-execution-time")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/services", "/services/**")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/services/*/in-progress", "/services/*/finish")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.PUT, "/services/**").hasRole("MANAGER")
        requests.requestMatchers(HttpMethod.DELETE, "/services/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/services")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/diagnosis/complete")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/diagnosis")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/approve")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/complete")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/deliver")
            .hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders")
            .hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders/customer/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.anyRequest().authenticated()
    }
}

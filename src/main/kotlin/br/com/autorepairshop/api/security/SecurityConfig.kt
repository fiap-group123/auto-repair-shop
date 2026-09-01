package br.com.autorepairshop.api.security

import br.com.autorepairshop.authentication.application.usecase.RequireActiveUserUseCase
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

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
        users: UserRepository,
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
            .authorizeHttpRequests { requests ->
                configureAuthorization(
                    requests = requests,
                    users = users,
                )
            }
        return http.build()
    }

    private fun configureAuthorization(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
        users: UserRepository,
    ) {
        requests.requestMatchers(
            "/error",
            "/.well-known/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
        ).permitAll()
        requests.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
        requests.requestMatchers(HttpMethod.POST, "/auth/users").access(bootstrapOrManager(users = users))
        requests.requestMatchers(HttpMethod.POST, "/invite/customer/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers("/invite/**").permitAll()
        configureCustomerRules(requests = requests)
        configureVehicleRules(requests = requests)
        configureServiceRules(requests = requests)
        configureExtraServiceRules(requests = requests)
        configureOrderRules(requests = requests)
        configureBudgetRules(requests = requests)
        requests.anyRequest().authenticated()
    }

    private fun configureCustomerRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(HttpMethod.POST, "/customers").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers/document/**")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/customers/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.PUT, "/customers/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/customers/**").hasRole("MANAGER")
    }

    private fun configureVehicleRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(HttpMethod.POST, "/vehicles").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/vehicles/owner/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/vehicles/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.PUT, "/vehicles/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.PATCH, "/vehicles/**").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.DELETE, "/vehicles/**").hasRole("MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/vehicles/**").hasRole("MANAGER")
    }

    private fun configureServiceRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
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
    }

    private fun configureExtraServiceRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(HttpMethod.POST, "/extra-services").hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/extra-services/service-order/**", "/extra-services/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/extra-services/*/approve", "/extra-services/*/reject")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MANAGER")
    }

    private fun configureOrderRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/diagnosis/complete")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/diagnosis/finish")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/diagnosis")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/execution")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/complete")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/finish")
            .hasAnyRole("MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders/*/deliver")
            .hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/service-orders").hasAnyRole("RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders/customer/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders")
            .hasAnyRole("RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.GET, "/service-orders/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
    }

    private fun configureBudgetRules(
        requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        requests.requestMatchers(HttpMethod.GET, "/budgets/**")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MECHANIC", "MANAGER")
        requests.requestMatchers(HttpMethod.POST, "/budgets/*/approve", "/budgets/*/reject", "/budgets/*/trade")
            .hasAnyRole("CLIENT", "RECEPTIONIST", "MANAGER")
        requests.requestMatchers(HttpMethod.DELETE, "/budgets/**").hasRole("MANAGER")
    }

    private fun bootstrapOrManager(users: UserRepository): AuthorizationManager<RequestAuthorizationContext> =
        AuthorizationManager { authentication, _ ->
            if (!users.existsAny()) {
                AuthorizationDecision(true)
            } else {
                val manager = (
                    runCatching { authentication.get() }.getOrNull()
                        ?.authorities
                        ?.any { authority -> authority.authority == "ROLE_MANAGER" }
                    ) == true
                AuthorizationDecision(manager)
            }
        }
}

package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.application.security.Actor
import br.com.autorepairshop.accessidentity.application.security.ActorProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextActorProvider : ActorProvider {
    override fun current(): Actor? = SecurityContextHolder.getContext().authentication?.details as? Actor
}

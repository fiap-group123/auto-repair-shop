package br.com.autorepairshop.serviceorder

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderAssembler
import io.mockk.every
import io.mockk.mockk

fun serviceOrderAssembler(): ServiceOrderAssembler {
    val catalog = mockk<ServiceRepository>()
    every { catalog.findByServiceOrderId(serviceOrderId = any()) } returns emptyList()
    every { catalog.findByServiceOrderIds(serviceOrderIds = any()) } returns emptyList()
    return ServiceOrderAssembler(services = catalog)
}

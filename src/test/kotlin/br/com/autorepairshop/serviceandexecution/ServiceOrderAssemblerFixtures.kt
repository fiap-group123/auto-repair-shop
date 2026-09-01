package br.com.autorepairshop.serviceandexecution

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderAssembler
import io.mockk.every
import io.mockk.mockk

fun serviceOrderAssembler(): ServiceOrderAssembler {
    val catalog = mockk<ServiceRepository>()
    val parts = mockk<PartRepository>()
    every { catalog.findByServiceOrderId(serviceOrderId = any()) } returns emptyList()
    every { catalog.findByServiceOrderIds(serviceOrderIds = any()) } returns emptyList()
    every { parts.findByServiceOrderId(serviceOrderId = any()) } returns emptyList()
    every { parts.findByServiceOrderIds(serviceOrderIds = any()) } returns emptyList()
    return ServiceOrderAssembler(
        services = catalog,
        parts = parts,
    )
}

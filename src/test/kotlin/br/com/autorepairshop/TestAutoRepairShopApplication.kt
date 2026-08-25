package br.com.autorepairshop

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<AutoRepairShopApplication>().with(TestcontainersConfiguration::class).run(*args)
}

package br.com.autorepairshop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class AutoRepairShopApplication

fun main(args: Array<String>) {
    runApplication<AutoRepairShopApplication>(args = args)
}

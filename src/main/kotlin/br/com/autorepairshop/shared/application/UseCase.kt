package br.com.autorepairshop.shared.application

interface UseCase<in IN, out OUT> {
    fun execute(input: IN): OUT
}

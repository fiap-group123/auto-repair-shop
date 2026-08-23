package br.com.autorepairshop.shared.domain.valueobject.document

import br.com.autorepairshop.shared.domain.ValueObject
import br.com.autorepairshop.shared.domain.exception.InvalidDocumentException

@JvmInline
value class Document private constructor(val value: String) : ValueObject {
    val type: DocumentType
        get() = if (value.length == 11) DocumentType.CPF else DocumentType.CNPJ

    companion object {
        fun of(input: String): Document {
            val digits = input.filter { it.isDigit() }
            val valid = when (digits.length) {
                DocumentType.CPF.length -> validateCPF(digits)
                DocumentType.CNPJ.length -> validateCNPJ(digits)
                else -> false
            }

            if (!valid) throw InvalidDocumentException("Invalid document: ${DocumentType.valueOf(digits)}")

            return Document(value = digits)
        }

        private fun validateCPF(cpf: String): Boolean {
            if (cpf.all { it == cpf[0] }) return false
            val d = cpf.map { it - '0' }
            return dv(d.take(9), (10 downTo 2).toList()) == d[9] &&
                dv(d.take(10), (11 downTo 2).toList()) == d[10]
        }

        private fun validateCNPJ(cnpj: String): Boolean {
            if (cnpj.all { it == cnpj[0] }) return false
            val d = cnpj.map { it - '0' }
            val weights1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            val weights2 = listOf(6) + weights1
            return dv(d.take(12), weights1) == d[12] &&
                dv(d.take(13), weights2) == d[13]
        }

        private fun dv(base: List<Int>, weights: List<Int>): Int {
            val remainder = base.zip(weights).sumOf { (digit, weight) -> digit * weight } % 11
            return if (remainder < 2) 0 else 11 - remainder
        }
    }
}
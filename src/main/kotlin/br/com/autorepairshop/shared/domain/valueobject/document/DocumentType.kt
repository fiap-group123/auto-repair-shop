package br.com.autorepairshop.shared.domain.valueobject.document

enum class DocumentType(val length: Number) {
    CPF(length = 11),
    CNPJ(length = 14)
}
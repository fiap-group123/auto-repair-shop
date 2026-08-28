package br.com.autorepairshop.customer.domain.valueobject.document

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DocumentIdTest {

    @Test
    fun `accepts masked and digits-only CPF`() {
        val masked = Document.of(raw = CustomerFixtures.VALID_CPF)
        val digits = Document.of(raw = "52998224725")

        assertEquals(
            expected = "52998224725",
            actual = masked.value,
        )
        assertEquals(
            expected = digits,
            actual = masked,
        )
        assertEquals(
            expected = DocumentType.CPF,
            actual = masked.type,
        )
    }

    @Test
    fun `formats and masks CPF`() {
        val document = Document.of(raw = CustomerFixtures.VALID_CPF)

        assertEquals(
            expected = "529.982.247-25",
            actual = document.formatted(),
        )
        assertEquals(
            expected = "***.982.247-**",
            actual = document.masked(),
        )
        assertEquals(
            expected = document.masked(),
            actual = document.toString(),
        )
    }

    @Test
    fun `rejects uniform and wrong-digit CPF`() {
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = CustomerFixtures.INVALID_CPF)
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "529.982.247-26")
        }
    }

    @Test
    fun `accepts numeric CNPJ and formats it`() {
        val document = Document.of(raw = CustomerFixtures.VALID_CNPJ)

        assertEquals(
            expected = DocumentType.CNPJ,
            actual = document.type,
        )
        assertEquals(
            expected = "11222333000181",
            actual = document.value,
        )
        assertEquals(
            expected = "11.222.333/0001-81",
            actual = document.formatted(),
        )
        assertEquals(
            expected = "**.222.333/****-**",
            actual = document.masked(),
        )
    }

    @Test
    fun `rejects CNPJ with uniform root`() {
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "11.111.111/1111-11")
        }
    }

    @Test
    fun `ofOrNull and isValid cover invalid input`() {
        assertNull(Document.ofOrNull(raw = CustomerFixtures.INVALID_CPF))
        assertFalse(Document.isValid(raw = CustomerFixtures.INVALID_CPF))
        assertTrue(Document.isValid(raw = CustomerFixtures.VALID_CPF))
        assertEquals(
            expected = Document.of(raw = CustomerFixtures.VALID_CPF),
            actual = Document.ofOrNull(raw = CustomerFixtures.VALID_CPF),
        )
    }

    @Test
    fun `rejects incomplete letters and wrong check digits`() {
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "12")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "5299822472A")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "529.982.247-15")
        }
        assertTrue(Document.isValid(raw = "123.456.789-09"))
    }

    @Test
    fun `accepts alphanumeric CNPJ and rejects invalid roots`() {
        val document = Document.of(raw = "12.ABC.345/01DE-35")

        assertEquals(
            expected = DocumentType.CNPJ,
            actual = document.type,
        )
        assertEquals(
            expected = "12ABC34501DE35",
            actual = document.value,
        )
        assertEquals(
            expected = "12.ABC.345/01DE-35",
            actual = document.formatted(),
        )
        assertEquals(
            expected = "**.ABC.345/****-**",
            actual = document.masked(),
        )
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "11222333000!81")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "112223330001AB")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "11222333000171")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "11222333000182")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "12ABC34501DÇ35")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "1234567890123")
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            Document.of(raw = "123456789012")
        }
    }
}

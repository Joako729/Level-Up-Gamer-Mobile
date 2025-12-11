package com.example.level_up

import com.example.level_up.utils.Validacion
import org.junit.Assert.*
import org.junit.Test

class ValidacionTest {

    @Test
    fun esAdulto_retornaTrue_siEsMayorOIgualA18() {
        assertTrue(Validacion.esAdulto(18))
        assertTrue(Validacion.esAdulto(25))
    }

    @Test
    fun esAdulto_retornaFalse_siEsMenorDe18() {
        assertFalse(Validacion.esAdulto(17))
        assertFalse(Validacion.esAdulto(0))
    }

    @Test
    fun esCorreoDuoc_funcionaCorrectamente() {
        assertTrue(Validacion.esCorreoDuoc("alumno@duocuc.cl"))
        assertFalse(Validacion.esCorreoDuoc("correo@gmail.com"))
    }

    @Test
    fun esCorreoValido_validaFormatos() {
        assertTrue(Validacion.esCorreoValido("test@example.com"))
        assertFalse(Validacion.esCorreoValido("estoNoEsUnCorreo"))
    }

    @Test
    fun esClaveValida_longitudMinima() {
        assertTrue(Validacion.esClaveValida("123456"))
        assertFalse(Validacion.esClaveValida("12345"))
    }

    @Test
    fun contrasenasCoinciden_funciona() {
        assertTrue(Validacion.contrasenasCoinciden("pass123", "pass123"))
        assertFalse(Validacion.contrasenasCoinciden("pass123", "otraCosa"))
    }

    @Test
    fun esNombreValido_longitud() {
        assertTrue(Validacion.esNombreValido("Jo"))
        assertFalse(Validacion.esNombreValido("J"))
    }

    @Test
    fun generarCodigoReferido_formato() {
        val nombre = "Marcelo"
        val codigo = Validacion.generarCodigoReferido(nombre)
        assertTrue(codigo.startsWith("MAR"))
        assertTrue(codigo.length >= 6)
    }

    @Test
    fun calcularNivel_logicaPuntos() {
        assertEquals(1, Validacion.calcularNivel(0))
        assertEquals(2, Validacion.calcularNivel(500))
        assertEquals(3, Validacion.calcularNivel(2352))
        assertEquals(4, Validacion.calcularNivel(5880))
        assertEquals(5, Validacion.calcularNivel(11760))
    }

    @Test
    fun obtenerPorcentajeDescuento_logicaNivel() {
        assertEquals(5, Validacion.obtenerPorcentajeDescuento(1))
        assertEquals(15, Validacion.obtenerPorcentajeDescuento(5))
        assertEquals(0, Validacion.obtenerPorcentajeDescuento(99))
    }
}
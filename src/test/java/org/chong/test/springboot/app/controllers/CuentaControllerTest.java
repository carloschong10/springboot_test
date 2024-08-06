package org.chong.test.springboot.app.controllers;

import org.chong.test.springboot.app.Datos;
import org.chong.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CuentaService cuentaService;

    @Test
    void detalle() throws Exception {
        //Given
        when(cuentaService.findById(1L)).thenReturn(Datos.crearCuenta1().orElseThrow());

        //When
        mvc.perform(get("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON))
                //Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.persona").value("Carlos"))
                .andExpect(jsonPath("$.saldo").value("1000"));

        verify(cuentaService).findById(1L);
    }
}
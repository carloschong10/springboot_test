package org.chong.test.springboot.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chong.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerRestTemplateTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private ObjectMapper objectMapper;

    @LocalServerPort
    private int puerto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    void testTransferir() {
        //Given
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setBancoId(1L);
        dto.setMonto(new BigDecimal("100"));

        //When
        ResponseEntity<String> response = testRestTemplate
//                .postForEntity("/api/cuentas/transferir", dto, String.class); //String.class es la respuesta que se retorna del map response del metodo pero como un StringResponseEntity<String> response = testRestTemplate
                .postForEntity(crearUri("/api/cuentas/transferir"), dto, String.class); //tambien se podria poner con el localhost pero obteniendo el puerto que utilizar de manera dinámica

        System.out.println("puerto" + puerto);

        String json = response.getBody();

        System.out.println("json" + json);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
//        assert json != null;
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizada con exito"));
        assertTrue(json.contains("{\"cuentaOrigenId\":1,\"cuentaDestinoId\":2,\"monto\":100,\"bancoId\":1}"));
    }

    private String crearUri(String uri) {
        return "http://localhost:" + puerto + uri;
    }
}
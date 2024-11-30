package org.chong.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chong.test.springboot.app.models.Cuenta;
import org.chong.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_rt")
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
    void testTransferir() throws JsonProcessingException {
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

        JsonNode jsonNode = objectMapper.readTree(json); //convirtiendo un json de tipo String a un JsonNode para poder navegar en los atributos

        assertEquals("Transferencia realizada con exito", jsonNode.path("mensaje").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
        assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());


        Map<String, Object> response2 = new HashMap<>();
        response2.put("date", LocalDate.now().toString());
        response2.put("status", HttpStatus.OK);
        response2.put("mensaje", "Transferencia realizada con exito");
        response2.put("transaccion", dto);

        assertEquals(objectMapper.writeValueAsString(response2), json);

    }

    @Test
    @Order(2)
    void testDetalle() {
        ResponseEntity<Cuenta> response = testRestTemplate.getForEntity(crearUri("/api/cuentas/1"), Cuenta.class);
        Cuenta cuenta = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(cuenta);
        assertEquals(1L, cuenta.getId());
        assertEquals("Carlos", cuenta.getPersona());
        assertEquals("900.00", cuenta.getSaldo().toPlainString());
        assertEquals(new Cuenta(1L, "Carlos", new BigDecimal("900.00")), cuenta);
    }

    private String crearUri(String uri) {
        return "http://localhost:" + puerto + uri;
    }

    @Test
    @Order(3)
    void testListar() throws JsonProcessingException {
        ResponseEntity<Cuenta[]> response = testRestTemplate.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(cuentas);
        assertEquals(2, cuentas.size());
        assertEquals(1L, cuentas.get(0).getId());
        assertEquals("Carlos", cuentas.get(0).getPersona());
        assertEquals("900.00", cuentas.get(0).getSaldo().toPlainString());
        assertEquals(2L, cuentas.get(1).getId());
        assertEquals("Noelia", cuentas.get(1).getPersona());
        assertEquals("2100.00", cuentas.get(1).getSaldo().toPlainString());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(cuentas)); //convirtiendo un json de tipo String a un JsonNode para poder navegar en los atributos

        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Carlos", jsonNode.get(0).path("persona").asText());
        assertEquals("900.0", jsonNode.get(0).path("saldo").asText());
        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("Noelia", jsonNode.get(1).path("persona").asText());
        assertEquals("2100.0", jsonNode.get(1).path("saldo").asText());

    }

    @Test
    @Order(4)
    void testGuardar() {
        Cuenta cuenta = new Cuenta(null, "María", new BigDecimal("3800"));
        ResponseEntity<Cuenta> response = testRestTemplate.postForEntity(crearUri("/api/cuentas"), cuenta, Cuenta.class); //el campo cuenta por default se envia como json en el request
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        Cuenta cuentaCreada = response.getBody();
//        assert cuentaCreada != null;
        assertNotNull(cuentaCreada);
        assertEquals(3L, cuentaCreada.getId());
        assertEquals("María", cuentaCreada.getPersona());
        assertEquals("3800", cuentaCreada.getSaldo().toPlainString());
    }

    @Test
    @Order(5)
    void testEliminar() {
        ResponseEntity<Cuenta[]> response = testRestTemplate.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(response.getBody());
        assertNotNull(cuentas);
        assertEquals(3, cuentas.size());
        assertEquals("María", cuentas.get(2).getPersona());

//        testRestTemplate.delete(crearUri("/api/cuentas/3")); //1ra forma de eliminar
//        ResponseEntity<Void> exchange = testRestTemplate.exchange(crearUri("/api/cuentas/3"), HttpMethod.DELETE, null, Void.class);//los parametros de la ruta se pasan como un PathVarible con el mismo nombre del PathVariable del Controller pero tambien de forma directa

        Map<String, Long> pathVariables = Map.of("id", 1L);
        ResponseEntity<Void> exchange = testRestTemplate.exchange(crearUri("/api/cuentas/{id}"), HttpMethod.DELETE, null, Void.class, pathVariables); //para usarlo con PathVariable se usa con Maps
        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
        assertFalse(exchange.hasBody());

        response = testRestTemplate.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
        cuentas = Arrays.asList(response.getBody());
        assertEquals(2, cuentas.size());

        ResponseEntity<Cuenta> response2 = testRestTemplate.getForEntity(crearUri("/api/cuentas/4"), Cuenta.class);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        assertFalse(response2.hasBody());

    }
}
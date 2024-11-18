package org.chong.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chong.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTests {

    ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testTransferir() throws JsonProcessingException {
        //Given
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setBancoId(1L);
        dto.setMonto(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", HttpStatus.OK);
        response.put("mensaje", "Transferencia realizada con exito");
        response.put("transaccion", dto);

        //When
//        webTestClient.post().uri("http://localhost:8081/crud_banco/api/cuentas/transferir") //si estamos haciendo los test para un proyecto diferente es necesario colocar la url del servidor y tenerlo levantado
        webTestClient.post().uri("/api/cuentas/transferir") //cuando estamos haciendo los test en el mismo proyecto, no es necesario colocar toda la url ni levantar el servidor del proyecto, ya que se levanta automaticamente al hacer el test
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()

                //Then: se puede validar de 2 formas con consumiWith y con jsonPath
                .expectStatus().isOk()
//                .expectBody() //si no se pone nada dentro del expectBody por defecto espera un tipo byte[]
                .expectBody(String.class) //pero si dentro del exectBody se colocara (String.class)
                .consumeWith( respuesta -> {
                    try {
//                        JsonNode json = objectMapper.readTree(respuesta.getResponseBody()); //la respuesta de tipo byte[] se transforma en tipo jsonNode que representa un Json
                        String jsonString = respuesta.getResponseBody();
                        JsonNode json = objectMapper.readTree(jsonString); //pero readTree tambien se sobrecarga con dato tipo Srtring
                        assertEquals("Transferencia realizada con exito", json.path("mensaje").asText());
                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
                        assertEquals("100", json.path("transaccion").path("monto").asText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                //pero si el expectBody fuera de tipo String se tendria que comentar el jsonPath ya que solo funciona con tipo byte[]
                /*.jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(is("Transferencia realizada con exito"))
                .jsonPath("$.mensaje").value(is("Transferencia realizada con exito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con exito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con exito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(response));*/
    }
}
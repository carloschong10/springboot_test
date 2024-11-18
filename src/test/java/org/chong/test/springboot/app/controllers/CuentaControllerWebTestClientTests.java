package org.chong.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chong.test.springboot.app.models.Cuenta;
import org.chong.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTests {

    ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    //si se ejecuta primer el test testTransferir() los demas test se caerían ya que están validando el saldo, y en el testTransferir se descuenta
    /*para evitar que el orden de las pruebas de integracion se alteren y tengamos test que modifican datos,
      podriamos darles un orden determinado a los test con la anotacion en la clase: @TestMethodOrder(MethodOrderer.OrderAnnotation.class),
      pero solo en pruebas de inteegracion, para que cuando un metodo que se ejecute antes, no afecte a otro que se ejecute despues*/
    @Test
    @Order(3)
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
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody() //si no se pone nada dentro del expectBody por defecto espera un tipo byte[]
//                .consumeWith( respuesta -> {
//                    try {
//                        JsonNode json = objectMapper.readTree(respuesta.getResponseBody()); //la respuesta de tipo byte[] se transforma en tipo jsonNode que representa un Json
//                        assertEquals("Transferencia realizada con exito", json.path("mensaje").asText());
//                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
//                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
//                        assertEquals("100", json.path("transaccion").path("monto").asText());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
                //el mas usado suele ser jsonPath
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(is("Transferencia realizada con exito"))
                .jsonPath("$.mensaje").value(is("Transferencia realizada con exito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con exito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con exito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(response));

        //Probando con consumeWIth
        /*webTestClient.post().uri("/api/cuentas/transferir") //cuando estamos haciendo los test en el mismo proyecto, no es necesario colocar toda la url ni levantar el servidor del proyecto, ya que se levanta automaticamente al hacer el test
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()

                //Then: se puede validar de 2 formas con consumiWith y con jsonPath
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class) //pero si dentro del exectBody se colocara (String.class)
                .consumeWith( respuesta -> {
                    try {
                        String jsonString = respuesta.getResponseBody();
                        JsonNode json = objectMapper.readTree(jsonString); //pero readTree tambien se sobrecarga con dato tipo Srtring
                        assertEquals("Transferencia realizada con exito", json.path("mensaje").asText());
                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
                        assertEquals("100", json.path("transaccion").path("monto").asText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });*/
    }

    @Test
    @Order(1)
    void testDetalle() throws JsonProcessingException {

        //Given
        Cuenta cuenta = new Cuenta(1L, "Carlos", new BigDecimal("1000"));
        //When
        webTestClient.get().uri("/api/cuentas/1").exchange() //con exchange realizamos el request
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody() //el expect Body sin parametros recibimos el Json de tipo byte[] y podemos utilizar el metodo .jsonPath() y comprobar cada atributo; o bien podemos mapear este json a una clase existente que tengamos siempre y cuando los nombres de atributos del json sean iguales a los nombres de atributos de nuestra clase.
                .jsonPath("$.persona").isEqualTo("Carlos")
                .jsonPath("$.saldo").isEqualTo(1000)
                .json(objectMapper.writeValueAsString(cuenta));
    }

    @Test
    @Order(2)
    void testDetalle2() {

        webTestClient.get().uri("/api/cuentas/2").exchange() //con exchange realizamos el request
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(respuesta -> {
                    Cuenta cuenta = respuesta.getResponseBody();
                    assertNotNull(cuenta);
                    assertEquals("Noelia", cuenta.getPersona());
                    assertEquals("2000.00", cuenta.getSaldo().toPlainString());
                });

    }

    @Test
    @Order(4)
    void testDetalle3() throws JsonProcessingException {

        //Given
        Cuenta cuenta = new Cuenta(1L, "Carlos", new BigDecimal("900"));
        //When
        webTestClient.get().uri("/api/cuentas/1").exchange() //con exchange realizamos el request
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody() //el expect Body sin parametros recibimos el Json de tipo byte[] y podemos utilizar el metodo .jsonPath() y comprobar cada atributo; o bien podemos mapear este json a una clase existente que tengamos siempre y cuando los nombres de atributos del json sean iguales a los nombres de atributos de nuestra clase.
                .jsonPath("$.persona").isEqualTo("Carlos")
                .jsonPath("$.saldo").isEqualTo(900)
                .json(objectMapper.writeValueAsString(cuenta));
    }

    @Test
    @Order(5)
    void testListar() {
        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].persona").isEqualTo("Carlos")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].saldo").isEqualTo(900)
                .jsonPath("$[1].persona").isEqualTo("Noelia")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].saldo").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2)); //para determinar el tamaño de nuestro arreglo

    }

    @Test
    @Order(6)
    void testListar2() {
        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody() //para utilizar le consumeWith debemos indicar acá el tipo de dato el cual deseamos esperar el resultado, por defecto es byte[] y cuando es byte[] utilizamos jsonPath, pero acá quiero obtener el resultado como una Lista de cuentas asi que usaremos .expectBodyList(Cuenta.class)
                .expectBodyList(Cuenta.class)
                .consumeWith(response -> {
                    List<Cuenta> cuentas = response.getResponseBody();

                    assert cuentas != null;
                    assertNotNull(cuentas);
                    assertEquals(2, cuentas.size());
                    assertEquals(1L, cuentas.get(0).getId());
                    assertEquals("Carlos", cuentas.get(0).getPersona());
                    assertEquals("900.00", cuentas.get(0).getSaldo().toPlainString());
                    assertEquals(2L, cuentas.get(1).getId());
                    assertEquals("Noelia", cuentas.get(1).getPersona());
                    assertEquals("2100.00", cuentas.get(1).getSaldo().toPlainString());
                })
                .hasSize(2)
                .value(hasSize(2));
    }

    @Test
    @Order(7)
    void testGuardar() {
        //given
        Cuenta cuenta = new Cuenta(null, "Maria", new BigDecimal("3000"));
        //when
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta) //de forma automatica el bodyValue convierte cuenta en un json y lo envia al backend
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.persona").isEqualTo("Maria")
                .jsonPath("$.persona").value(is("Maria"))
                .jsonPath("$.saldo").isEqualTo(3000);


    }

    @Test
    @Order(8)
    void testGuardar2() {
        //given
        Cuenta cuenta = new Cuenta(null, "Jackie", new BigDecimal("3500"));
        //when
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta) //de forma automatica el bodyValue convierte cuenta en un json y lo envia al backend
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(response -> {
                    Cuenta c = response.getResponseBody();
                    assertNotNull(c);
                    assertEquals(4L, c.getId());
                    assertEquals("Jackie", c.getPersona());
                    assertEquals(new BigDecimal("3500"), c.getSaldo());
                });
    }

    @Test
    @Order(9)
    void testEliminar() {
        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectBodyList(Cuenta.class)
                .hasSize(4);

        webTestClient.delete().uri("/api/cuentas/3").exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(3);

        webTestClient.get().uri("/api/cuentas/3").exchange()
                .expectStatus().is5xxServerError();
    }
}
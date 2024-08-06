package org.chong.test.springboot.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.chong.test.springboot.app.Datos.*;

import org.chong.test.springboot.app.exceptions.DineroInsuficienteException;
import org.chong.test.springboot.app.models.Banco;
import org.chong.test.springboot.app.models.Cuenta;
import org.chong.test.springboot.app.repositories.BancoRepository;
import org.chong.test.springboot.app.repositories.CuentaRepository;
import org.chong.test.springboot.app.services.CuentaService;
import org.chong.test.springboot.app.services.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class SpringbootTestApplicationTests {

    CuentaRepository cuentaRepository;
    BancoRepository bancoRepository;

    CuentaService cuentaService;

    @BeforeEach
    void setUp() {
        cuentaRepository = mock(CuentaRepository.class);
        bancoRepository = mock(BancoRepository.class);
        cuentaService = new CuentaServiceImpl(cuentaRepository, bancoRepository);

        //reiniciamos los saldos que están como static en la clase Datos, para que el orden de los test no altere su funcionalidad, por ejm si les intercambiamos el nombre
        /*
        Datos.CUENTA1.setSaldo(new BigDecimal("1000"));
        Datos.CUENTA2.setSaldo(new BigDecimal("2000"));
        Datos.BANCO.setTotalTransferencias(0);
        */

        //otra forma y la mas recomendada es asignarlos a través de métodos staticos en la clase Datos, asi siempre va a llamar a una nueva instancia cada vez que se llame
        //e importamos Datos de manera static para que ya llamemos a sus métodos staticos sin necesita de poner el nombre de la clase
    }

    @Test
    void contextLoads2() {
        //Given: Dado el contexto
        when(cuentaRepository.findById(1L)).thenReturn(crearCuenta1());
        when(cuentaRepository.findById(2L)).thenReturn(crearCuenta2());
        when(bancoRepository.findById(1L)).thenReturn(crearBanco());

        //When: Cuando invocamos los metodos de prueba del service, entonces realizamos la prueba
        BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
        BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        cuentaService.transferir(1L, 2L, new BigDecimal("100"), 1L);

        saldoOrigen = cuentaService.revisarSaldo(1L);
        saldoDestino = cuentaService.revisarSaldo(2L);

        assertEquals("900", saldoOrigen.toPlainString());
        assertEquals("2100", saldoDestino.toPlainString());

        verify(cuentaRepository, times(3)).findById(1L);
        verify(cuentaRepository, times(3)).findById(2L);
        verify(cuentaRepository, times(2)).update(any(Cuenta.class));

        int total = cuentaService.revisarTotalTransferencias(1L);
        assertEquals(1, total);

//        verify(bancoRepository).findById(1L);
        verify(bancoRepository, times(2)).findById(1L);
        verify(bancoRepository).update(any(Banco.class));

        verify(cuentaRepository, times(6)).findById(anyLong()); //se llama en total 6 veces (3 del ID 1L y 3 del ID 2L)
        verify(bancoRepository, never()).findAll();
    }

    @Test
    void contextLoads() {
        //Given: Dado el contexto
        when(cuentaRepository.findById(1L)).thenReturn(crearCuenta1());
        when(cuentaRepository.findById(2L)).thenReturn(crearCuenta2());
        when(bancoRepository.findById(1L)).thenReturn(crearBanco());

        //When: Cuando invocamos los metodos de prueba del service, entonces realizamos la prueba
        BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
        BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        assertThrows(DineroInsuficienteException.class, () -> {
            cuentaService.transferir(1L, 2L, new BigDecimal("1200"), 1L);
        });

        saldoOrigen = cuentaService.revisarSaldo(1L);
        saldoDestino = cuentaService.revisarSaldo(2L);

        //como se produce la excepcion DineroInsuficienteException no cambian los saldos
        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        int total = cuentaService.revisarTotalTransferencias(1L);
        assertEquals(0, total);

        verify(cuentaRepository, times(3)).findById(1L);
        verify(cuentaRepository, times(2)).findById(2L); //se ejecuta 2 veces porque falla por el DineroInsuficienteException
        verify(cuentaRepository, never()).update(any(Cuenta.class)); //y el update de cuentaRepository nunca se ejecuta

//        verify(bancoRepository).findById(1L);
        verify(bancoRepository, times(1)).findById(1L); //se llega a ejecutar 1 vez que es solo para la cuenta 1 y en debito se cae por la expcecion DineroInsuficienteException
        verify(bancoRepository, never()).update(any(Banco.class)); //y el update de bancoRepository nunca se ejecuta

        verify(cuentaRepository, times(5)).findById(anyLong()); //se llama en total 6 veces (3 del ID 1L y 2 del ID 2L)
        verify(bancoRepository, never()).findAll();
    }

    @Test
    void contextLoad3() {
        when(cuentaRepository.findById(1L)).thenReturn(crearCuenta1());

        Cuenta cuenta1 = cuentaService.findById(1L);
        Cuenta cuenta2 = cuentaService.findById(1L);

        //vamos a verificar que estas 2 instancias son las mismas para este método
        assertSame(cuenta1, cuenta2);
        assertTrue(cuenta1 == cuenta2);

        assertEquals("Carlos", cuenta1.getPersona());
        assertEquals("Carlos", cuenta2.getPersona());

        verify(cuentaRepository, times(2)).findById(1L);
    }
}

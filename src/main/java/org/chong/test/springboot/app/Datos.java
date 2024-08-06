package org.chong.test.springboot.app;

import org.chong.test.springboot.app.models.Banco;
import org.chong.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.Optional;

public class Datos {
    /*
    public static final Cuenta CUENTA1 = new Cuenta(1L, "Carlos", new BigDecimal("1000"));
    public static final Cuenta CUENTA2 = new Cuenta(2L, "Noelia", new BigDecimal("2000"));
    public static final Banco BANCO = new Banco(1L, "Caja Sullana", 0);
    */

    public static Optional<Cuenta> crearCuenta1() {
        return Optional.of(new Cuenta(1L, "Carlos", new BigDecimal("1000")));
    }

    public static Optional<Cuenta> crearCuenta2() {
        return Optional.of(new Cuenta(2L, "Noelia", new BigDecimal("2000")));
    }

    public static Optional<Banco> crearBanco() {
        return Optional.of(new Banco(1L, "Caja Sullana", 0));
    }
}

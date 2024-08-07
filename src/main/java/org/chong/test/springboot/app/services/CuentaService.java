package org.chong.test.springboot.app.services;

import org.chong.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {

    List<Cuenta> findAll();

    Cuenta findById(Long id);

    Cuenta save(Cuenta cuenta);

    int revisarTotalTransferencias(Long bancoId);

    BigDecimal revisarSaldo(Long cuentaId);

    void transferir(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto, Long bancoId);
}

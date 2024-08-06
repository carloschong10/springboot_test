package org.chong.test.springboot.app.services;

import org.chong.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {
    Cuenta findById(Long id);

    int revisarTotalTransferencias(Long bancoId);

    BigDecimal revisarSaldo(Long cuentaId);

    void transferir(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto, Long bancoId);
}

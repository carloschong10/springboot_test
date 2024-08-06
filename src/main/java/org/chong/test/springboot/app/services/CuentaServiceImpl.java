package org.chong.test.springboot.app.services;

import org.chong.test.springboot.app.models.Banco;
import org.chong.test.springboot.app.models.Cuenta;
import org.chong.test.springboot.app.repositories.BancoRepository;
import org.chong.test.springboot.app.repositories.CuentaRepository;

import java.math.BigDecimal;

public class CuentaServiceImpl implements CuentaService {

    private CuentaRepository cuentaRepository;
    private BancoRepository bancoRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository, BancoRepository bancoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.bancoRepository = bancoRepository;
    }

    @Override
    public Cuenta findById(Long id) {
        return cuentaRepository.findById(id);
    }

    @Override
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId);

        return banco.getTotalTransferencias();
    }

    @Override
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId);

        return cuenta.getSaldo();
    }

    @Override
    public void transferir(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto, Long bancoId) {
        Cuenta cuentaOri = cuentaRepository.findById(cuentaOrigen);
        cuentaOri.debito(monto);
        cuentaRepository.update(cuentaOri);

        Cuenta cuentaDest = cuentaRepository.findById(cuentaDestino);
        cuentaDest.credito(monto);
        cuentaRepository.update(cuentaDest);

        Banco banco = bancoRepository.findById(bancoId);
        int totalTransferencias = banco.getTotalTransferencias();
        banco.setTotalTransferencias(++totalTransferencias);
        bancoRepository.update(banco);
    }
}

package org.chong.test.springboot.app.services;

import org.chong.test.springboot.app.models.Banco;
import org.chong.test.springboot.app.models.Cuenta;
import org.chong.test.springboot.app.repositories.BancoRepository;
import org.chong.test.springboot.app.repositories.CuentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CuentaServiceImpl implements CuentaService {

    private CuentaRepository cuentaRepository;
    private BancoRepository bancoRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository, BancoRepository bancoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.bancoRepository = bancoRepository;
    }

    @Override
    public Cuenta findById(Long id) {
        return cuentaRepository.findById(id).orElseThrow();
    }

    @Override
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId).orElseThrow();

        return banco.getTotalTransferencias();
    }

    @Override
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();

        return cuenta.getSaldo();
    }

    @Override
    public void transferir(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto, Long bancoId) {
        Cuenta cuentaOri = cuentaRepository.findById(cuentaOrigen).orElseThrow();
        cuentaOri.debito(monto);
        cuentaRepository.save(cuentaOri);

        Cuenta cuentaDest = cuentaRepository.findById(cuentaDestino).orElseThrow();
        cuentaDest.credito(monto);
        cuentaRepository.save(cuentaDest);

        Banco banco = bancoRepository.findById(bancoId).orElseThrow();
        int totalTransferencias = banco.getTotalTransferencias();
        banco.setTotalTransferencias(++totalTransferencias);
        bancoRepository.save(banco);
    }
}

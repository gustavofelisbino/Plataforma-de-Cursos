package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.dominio.StatusMatricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteCreator</b> de {@link CertificadoCapacitacao}.
 */
public class EmissorCertificadoCapacitacao extends EmissorCertificado {

    private static final int VALIDADE_CERTIFICADO_EM_ANOS = 2;

    @Override
    protected Certificado criarCertificado(String codigo, Matricula matricula, LocalDate dataEmissao) {
        LocalDate dataValidade = dataEmissao.plusYears(VALIDADE_CERTIFICADO_EM_ANOS);
        return new CertificadoCapacitacao(codigo, matricula, dataEmissao, dataValidade);
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.CAPACITACAO;
    }

    @Override
    protected boolean requisitosAtendidos(Matricula matricula) {
        return matricula.getStatus() == StatusMatricula.CONCLUIDA;
    }

    @Override
    public String descricaoRequisito() {
        return "o curso precisa estar 100% concluído";
    }
}

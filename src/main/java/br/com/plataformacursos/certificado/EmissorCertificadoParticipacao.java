package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteCreator</b> de {@link CertificadoParticipacao}.
 */
public class EmissorCertificadoParticipacao extends EmissorCertificado {

    public static final int PARTICIPACAO_MINIMA = 75;

    @Override
    protected Certificado criarCertificado(String codigo, Matricula matricula, LocalDate dataEmissao) {
        return new CertificadoParticipacao(codigo, matricula, dataEmissao);
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.PARTICIPACAO;
    }

    @Override
    protected boolean requisitosAtendidos(Matricula matricula) {
        return matricula.getProgresso() >= PARTICIPACAO_MINIMA;
    }

    @Override
    public String descricaoRequisito() {
        return "é necessário ter no mínimo " + PARTICIPACAO_MINIMA + "% de participação";
    }
}

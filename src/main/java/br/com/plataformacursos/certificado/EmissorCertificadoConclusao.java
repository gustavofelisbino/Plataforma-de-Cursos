package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.dominio.StatusMatricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteCreator</b> de {@link CertificadoConclusao}.
 */
public class EmissorCertificadoConclusao extends EmissorCertificado {

    @Override
    protected Certificado criarCertificado(String codigo, Matricula matricula, LocalDate dataEmissao) {
        return new CertificadoConclusao(codigo, matricula, dataEmissao);
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.CONCLUSAO;
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

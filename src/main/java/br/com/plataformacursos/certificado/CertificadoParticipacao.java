package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteProduct</b>.
 *
 * <p>Certificado de participação: atesta a participação do aluno em
 * workshops, palestras e trilhas livres, sem exigir conclusão integral.</p>
 */
public class CertificadoParticipacao extends Certificado {

    private final int percentualParticipacao;

    public CertificadoParticipacao(String codigo, Matricula matricula, LocalDate dataEmissao) {
        super(codigo, matricula, dataEmissao);
        this.percentualParticipacao = matricula.getProgresso();
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.PARTICIPACAO;
    }

    public int getPercentualParticipacao() {
        return percentualParticipacao;
    }

    @Override
    protected String gerarTexto() {
        return String.format("Certificamos que %s participou do curso \"%s\" (%d horas),%n"
                + "tendo cumprido %d%% das atividades propostas.",
                getNomeAluno(), getTituloCurso(), getCargaHoraria(), percentualParticipacao);
    }
}

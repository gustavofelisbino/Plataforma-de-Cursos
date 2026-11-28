package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteProduct</b>.
 *
 * <p>Certificado de capacitação: atesta que o aluno concluiu o curso, com a data de validade do certificado.</p>
 */
public class CertificadoCapacitacao extends Certificado {

    private final LocalDate dataValidade;

    protected CertificadoCapacitacao(String codigo, Matricula matricula, LocalDate dataEmissao, LocalDate dataValidade) {
        super(codigo, matricula, dataEmissao);
        this.dataValidade = dataValidade;
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.CAPACITACAO;
    }

    @Override
    protected String gerarTexto() {
        return String.format("Certificamos que %s concluiu com êxito o curso \"%s\" (%d horas),%n"
                + "com validade até o dia %s.",
                getNomeAluno(), getTituloCurso(), getCargaHoraria(),
                getDataValidade().format(Certificado.FORMATO_DATA));
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }
}

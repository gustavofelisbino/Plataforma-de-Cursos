package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDate;

/**
 * Factory Method: <b>ConcreteProduct</b>.
 *
 * <p>Certificado de conclusão: atesta que o aluno concluiu 100% do curso.</p>
 */
public class CertificadoConclusao extends Certificado {

    public CertificadoConclusao(String codigo, Matricula matricula, LocalDate dataEmissao) {
        super(codigo, matricula, dataEmissao);
    }

    @Override
    public TipoCertificado getTipo() {
        return TipoCertificado.CONCLUSAO;
    }

    @Override
    protected String gerarTexto() {
        return String.format("Certificamos que %s concluiu com êxito o curso \"%s\",%n"
                + "com carga horária total de %d horas.", getNomeAluno(), getTituloCurso(), getCargaHoraria());
    }
}

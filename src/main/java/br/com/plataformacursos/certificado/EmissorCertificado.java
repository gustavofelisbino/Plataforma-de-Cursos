package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.dominio.StatusMatricula;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Factory Method: <b>Creator</b>.
 *
 * <p>Concentra o fluxo de emissão de um certificado (validar a matrícula,
 * gerar o código, criar o certificado e vinculá-lo à matrícula) e delega às
 * subclasses, por meio do <i>factory method</i> {@link #criarCertificado},
 * a decisão de <b>qual</b> certificado concreto instanciar.</p>
 *
 * <p>Para suportar um novo tipo de certificado basta criar um novo
 * {@link Certificado} concreto e um novo emissor concreto — o restante do
 * sistema não precisa ser alterado.</p>
 */
public abstract class EmissorCertificado {

    /**
     * Operação do Creator que utiliza o factory method.
     */
    public final Certificado emitir(Matricula matricula, LocalDate dataEmissao) {
        Objects.requireNonNull(matricula, "A matrícula é obrigatória.");
        Objects.requireNonNull(dataEmissao, "A data de emissão é obrigatória.");

        if (matricula.getCurso().getTipoCertificado() != getTipo()) {
            throw new IllegalArgumentException("O curso \"" + matricula.getCurso().getTitulo()
                    + "\" emite certificado de " + matricula.getCurso().getTipoCertificado().getDescricao()
                    + ", não de " + getTipo().getDescricao() + ".");
        }
        if (matricula.getStatus() == StatusMatricula.CANCELADA) {
            throw new IllegalStateException("Não é possível emitir certificado para matrícula cancelada.");
        }
        if (!requisitosAtendidos(matricula)) {
            throw new IllegalStateException("Requisito não atendido para certificado de "
                    + getTipo().getDescricao() + ": " + descricaoRequisito()
                    + " (progresso atual: " + matricula.getProgresso() + "%).");
        }

        Certificado certificado = criarCertificado(gerarCodigo(matricula, dataEmissao), matricula, dataEmissao);
        matricula.vincularCertificado(certificado);
        return certificado;
    }

    /** Factory method: cada emissor concreto decide qual certificado criar. */
    protected abstract Certificado criarCertificado(String codigo, Matricula matricula, LocalDate dataEmissao);

    /** Tipo de certificado produzido por este emissor. */
    public abstract TipoCertificado getTipo();

    /** Regra mínima para que a matrícula tenha direito ao certificado. */
    protected abstract boolean requisitosAtendidos(Matricula matricula);

    /** Descrição legível da regra mínima, usada nas mensagens de erro. */
    public abstract String descricaoRequisito();

    private String gerarCodigo(Matricula matricula, LocalDate dataEmissao) {
        return String.format("%s-%d-%05d", getTipo().getPrefixoCodigo(), dataEmissao.getYear(), matricula.getId());
    }
}

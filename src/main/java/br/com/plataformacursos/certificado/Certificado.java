package br.com.plataformacursos.certificado;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Factory Method: <b>Product</b>.
 *
 * <p>Define a interface comum dos certificados. Quem usa um certificado
 * (console, notificações, matrícula) depende apenas desta abstração.</p>
 */
public abstract class Certificado {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String codigo;
    private final String nomeAluno;
    private final String tituloCurso;
    private final int cargaHoraria;
    private final LocalDate dataEmissao;

    protected Certificado(String codigo, Matricula matricula, LocalDate dataEmissao) {
        Objects.requireNonNull(matricula, "A matrícula é obrigatória.");
        this.codigo = Objects.requireNonNull(codigo, "O código é obrigatório.");
        this.nomeAluno = matricula.getAluno().getNome();
        this.tituloCurso = matricula.getCurso().getTitulo();
        this.cargaHoraria = matricula.getCurso().getCargaHoraria();
        this.dataEmissao = Objects.requireNonNull(dataEmissao, "A data de emissão é obrigatória.");
    }

    /** Tipo do certificado concreto. */
    public abstract TipoCertificado getTipo();

    /** Texto principal do certificado, específico de cada tipo. */
    protected abstract String gerarTexto();

    /** Representação do certificado pronta para exibição. */
    public final String formatar() {
        String titulo = "CERTIFICADO DE " + getTipo().getDescricao().toUpperCase();
        String linha = "=".repeat(64);
        return linha + System.lineSeparator()
                + centralizar(titulo, 64) + System.lineSeparator()
                + linha + System.lineSeparator()
                + gerarTexto() + System.lineSeparator()
                + System.lineSeparator()
                + "Emitido em: " + dataEmissao.format(FORMATO_DATA) + System.lineSeparator()
                + "Código de verificação: " + codigo + System.lineSeparator()
                + linha;
    }

    private static String centralizar(String texto, int largura) {
        int espacos = Math.max(0, (largura - texto.length()) / 2);
        return " ".repeat(espacos) + texto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public String getTituloCurso() {
        return tituloCurso;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s - %s", codigo, getTipo().getDescricao(), nomeAluno, tituloCurso);
    }
}

package br.com.plataformacursos.dominio;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.notificacao.EventoMatricula;
import br.com.plataformacursos.notificacao.ObservadorMatricula;
import br.com.plataformacursos.notificacao.SujeitoMatricula;
import br.com.plataformacursos.notificacao.TipoEvento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Vínculo entre um {@link Aluno} e um {@link Curso}.
 *
 * <p>Observer: a matrícula é o <b>ConcreteSubject</b>. Toda mudança relevante
 * de estado (progresso, conclusão, cancelamento, emissão de certificado)
 * é comunicada aos observadores anexados, sem que a matrícula conheça
 * os canais de notificação concretos.</p>
 */
public class Matricula implements SujeitoMatricula {

    private final int id;
    private final Aluno aluno;
    private final Curso curso;
    private final LocalDate dataMatricula;
    private final List<ObservadorMatricula> observadores = new ArrayList<>();

    private int progresso;
    private StatusMatricula status;
    private Certificado certificado;

    public Matricula(int id, Aluno aluno, Curso curso, LocalDate dataMatricula) {
        this.id = id;
        this.aluno = Objects.requireNonNull(aluno, "O aluno é obrigatório.");
        this.curso = Objects.requireNonNull(curso, "O curso é obrigatório.");
        this.dataMatricula = Objects.requireNonNull(dataMatricula, "A data da matrícula é obrigatória.");
        this.progresso = 0;
        this.status = StatusMatricula.ATIVA;
    }

    // ------------------------------------------------------------------
    // Regras de negócio que alteram o estado e disparam notificações
    // ------------------------------------------------------------------

    /**
     * Atualiza o percentual de conclusão do curso. Ao atingir 100% a
     * matrícula é concluída automaticamente.
     */
    public void registrarProgresso(int novoProgresso) {
        if (status != StatusMatricula.ATIVA) {
            throw new IllegalStateException("Só é possível registrar progresso em matrículas ativas (status atual: "
                    + status.getDescricao() + ").");
        }
        if (novoProgresso < 0 || novoProgresso > 100) {
            throw new IllegalArgumentException("O progresso deve estar entre 0 e 100.");
        }
        if (novoProgresso <= progresso) {
            throw new IllegalArgumentException("O novo progresso (" + novoProgresso
                    + "%) deve ser maior que o atual (" + progresso + "%).");
        }

        this.progresso = novoProgresso;
        notificar(new EventoMatricula(TipoEvento.PROGRESSO_ATUALIZADO, this,
                "Progresso atualizado para " + novoProgresso + "%."));

        if (novoProgresso == 100) {
            this.status = StatusMatricula.CONCLUIDA;
            notificar(new EventoMatricula(TipoEvento.CURSO_CONCLUIDO, this,
                    "Parabéns! Você concluiu o curso " + curso.getTitulo() + "."));
        }
    }

    public void cancelar() {
        if (status != StatusMatricula.ATIVA) {
            throw new IllegalStateException("Só é possível cancelar matrículas ativas (status atual: "
                    + status.getDescricao() + ").");
        }
        this.status = StatusMatricula.CANCELADA;
        notificar(new EventoMatricula(TipoEvento.MATRICULA_CANCELADA, this,
                "Sua matrícula no curso " + curso.getTitulo() + " foi cancelada."));
    }

    /**
     * Associa o certificado emitido a esta matrícula.
     */
    public void vincularCertificado(Certificado certificado) {
        Objects.requireNonNull(certificado, "O certificado é obrigatório.");
        if (status == StatusMatricula.CANCELADA) {
            throw new IllegalStateException("Não é possível emitir certificado para matrícula cancelada.");
        }
        if (this.certificado != null) {
            throw new IllegalStateException("Esta matrícula já possui o certificado " + this.certificado.getCodigo() + ".");
        }
        this.certificado = certificado;
        notificar(new EventoMatricula(TipoEvento.CERTIFICADO_EMITIDO, this,
                "Seu certificado de " + certificado.getTipo().getDescricao()
                        + " foi emitido. Código: " + certificado.getCodigo() + "."));
    }

    // ------------------------------------------------------------------
    // Observer: gerenciamento dos observadores
    // ------------------------------------------------------------------

    @Override
    public void anexar(ObservadorMatricula observador) {
        Objects.requireNonNull(observador, "O observador é obrigatório.");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void desanexar(ObservadorMatricula observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificar(EventoMatricula evento) {
        // Copia defensiva: um observador pode se desanexar durante a notificação.
        for (ObservadorMatricula observador : List.copyOf(observadores)) {
            observador.atualizar(evento);
        }
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Curso getCurso() {
        return curso;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    public int getProgresso() {
        return progresso;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public Optional<Certificado> getCertificado() {
        return Optional.ofNullable(certificado);
    }

    public List<ObservadorMatricula> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }

    @Override
    public String toString() {
        return String.format("#%d %s em \"%s\" - %d%% - %s%s",
                id, aluno.getNome(), curso.getTitulo(), progresso, status.getDescricao(),
                certificado == null ? "" : " - certificado " + certificado.getCodigo());
    }
}

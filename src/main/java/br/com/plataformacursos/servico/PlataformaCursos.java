package br.com.plataformacursos.servico;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.certificado.EmissorCertificado;
import br.com.plataformacursos.certificado.TipoCertificado;
import br.com.plataformacursos.dominio.Aluno;
import br.com.plataformacursos.dominio.Curso;
import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.dominio.StatusMatricula;
import br.com.plataformacursos.notificacao.EventoMatricula;
import br.com.plataformacursos.notificacao.ObservadorMatricula;
import br.com.plataformacursos.notificacao.TipoEvento;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Serviço de aplicação que orquestra o fluxo da plataforma: cadastro de
 * alunos e cursos, matrícula, progresso, cancelamento e emissão de certificado.
 *
 * <p>Os dados ficam apenas em memória (o projeto não usa banco de dados).
 * O serviço trabalha somente com as abstrações {@link EmissorCertificado}
 * e {@link ObservadorMatricula}; as implementações concretas são
 * registradas na inicialização da aplicação.</p>
 */
public class PlataformaCursos {

    private final Map<Integer, Aluno> alunos = new LinkedHashMap<>();
    private final Map<Integer, Curso> cursos = new LinkedHashMap<>();
    private final Map<Integer, Matricula> matriculas = new LinkedHashMap<>();
    private final Map<TipoCertificado, EmissorCertificado> emissores = new EnumMap<>(TipoCertificado.class);
    private final List<ObservadorMatricula> observadores = new ArrayList<>();
    private final Clock relogio;

    private int proximoIdAluno = 1;
    private int proximoIdCurso = 1;
    private int proximoIdMatricula = 1;

    public PlataformaCursos() {
        this(Clock.systemDefaultZone());
    }

    public PlataformaCursos(Clock relogio) {
        this.relogio = Objects.requireNonNull(relogio);
    }

    // ------------------------------------------------------------------
    // Configuração
    // ------------------------------------------------------------------

    /** Registra o emissor (ConcreteCreator) responsável por um tipo de certificado. */
    public void registrarEmissor(EmissorCertificado emissor) {
        Objects.requireNonNull(emissor, "O emissor é obrigatório.");
        emissores.put(emissor.getTipo(), emissor);
    }

    /** Registra um canal de notificação que será anexado a todas as novas matrículas. */
    public void registrarObservador(ObservadorMatricula observador) {
        Objects.requireNonNull(observador, "O observador é obrigatório.");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
            matriculas.values().forEach(m -> m.anexar(observador));
        }
    }

    // ------------------------------------------------------------------
    // Cadastros
    // ------------------------------------------------------------------

    public Aluno cadastrarAluno(String nome, String email) {
        boolean emailEmUso = alunos.values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email == null ? "" : email.trim()));
        if (emailEmUso) {
            throw new RegraNegocioException("Já existe um aluno cadastrado com o e-mail " + email + ".");
        }
        Aluno aluno = executar(() -> new Aluno(proximoIdAluno, nome, email));
        alunos.put(aluno.getId(), aluno);
        proximoIdAluno++;
        return aluno;
    }

    public Curso cadastrarCurso(String titulo, int cargaHoraria, TipoCertificado tipoCertificado) {
        if (tipoCertificado != null && !emissores.containsKey(tipoCertificado)) {
            throw new RegraNegocioException("Não há emissor registrado para certificado de "
                    + tipoCertificado.getDescricao() + ".");
        }
        Curso curso = executar(() -> new Curso(proximoIdCurso, titulo, cargaHoraria, tipoCertificado));
        cursos.put(curso.getId(), curso);
        proximoIdCurso++;
        return curso;
    }

    // ------------------------------------------------------------------
    // Fluxo da matrícula
    // ------------------------------------------------------------------

    public Matricula matricular(int idAluno, int idCurso) {
        Aluno aluno = buscarAluno(idAluno);
        Curso curso = buscarCurso(idCurso);

        boolean jaMatriculado = matriculas.values().stream()
                .anyMatch(m -> m.getAluno().equals(aluno)
                        && m.getCurso().equals(curso)
                        && m.getStatus() != StatusMatricula.CANCELADA);
        if (jaMatriculado) {
            throw new RegraNegocioException(aluno.getNome() + " já possui matrícula em \"" + curso.getTitulo() + "\".");
        }

        Matricula matricula = new Matricula(proximoIdMatricula++, aluno, curso, LocalDate.now(relogio));
        observadores.forEach(matricula::anexar);
        matriculas.put(matricula.getId(), matricula);

        matricula.notificar(new EventoMatricula(TipoEvento.MATRICULA_REALIZADA, matricula,
                "Bem-vindo(a) ao curso " + curso.getTitulo() + "!"));
        return matricula;
    }

    public Matricula registrarProgresso(int idMatricula, int percentual) {
        Matricula matricula = buscarMatricula(idMatricula);
        executar(() -> {
            matricula.registrarProgresso(percentual);
            return matricula;
        });
        return matricula;
    }

    public Matricula cancelarMatricula(int idMatricula) {
        Matricula matricula = buscarMatricula(idMatricula);
        executar(() -> {
            matricula.cancelar();
            return matricula;
        });
        return matricula;
    }

    public Certificado emitirCertificado(int idMatricula) {
        Matricula matricula = buscarMatricula(idMatricula);
        TipoCertificado tipo = matricula.getCurso().getTipoCertificado();
        EmissorCertificado emissor = emissores.get(tipo);
        if (emissor == null) {
            throw new RegraNegocioException("Não há emissor registrado para certificado de " + tipo.getDescricao() + ".");
        }
        return executar(() -> emissor.emitir(matricula, LocalDate.now(relogio)));
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public Aluno buscarAluno(int id) {
        Aluno aluno = alunos.get(id);
        if (aluno == null) {
            throw new RegraNegocioException("Aluno #" + id + " não encontrado.");
        }
        return aluno;
    }

    public Curso buscarCurso(int id) {
        Curso curso = cursos.get(id);
        if (curso == null) {
            throw new RegraNegocioException("Curso #" + id + " não encontrado.");
        }
        return curso;
    }

    public Matricula buscarMatricula(int id) {
        Matricula matricula = matriculas.get(id);
        if (matricula == null) {
            throw new RegraNegocioException("Matrícula #" + id + " não encontrada.");
        }
        return matricula;
    }

    public List<Aluno> listarAlunos() {
        return List.copyOf(alunos.values());
    }

    public List<Curso> listarCursos() {
        return List.copyOf(cursos.values());
    }

    public List<Matricula> listarMatriculas() {
        return List.copyOf(matriculas.values());
    }

    public List<Matricula> listarMatriculasDoAluno(int idAluno) {
        Aluno aluno = buscarAluno(idAluno);
        return matriculas.values().stream().filter(m -> m.getAluno().equals(aluno)).toList();
    }

    public List<TipoCertificado> tiposCertificadoDisponiveis() {
        return List.copyOf(emissores.keySet());
    }

    public EmissorCertificado emissorDe(TipoCertificado tipo) {
        return emissores.get(tipo);
    }

    // ------------------------------------------------------------------
    // Utilitário: traduz erros de validação do domínio em erro de negócio
    // ------------------------------------------------------------------

    private static <T> T executar(Supplier<T> operacao) {
        try {
            return operacao.get();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new RegraNegocioException(e.getMessage(), e);
        }
    }
}

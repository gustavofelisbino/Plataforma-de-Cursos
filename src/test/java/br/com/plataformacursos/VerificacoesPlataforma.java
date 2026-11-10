package br.com.plataformacursos;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.certificado.CertificadoConclusao;
import br.com.plataformacursos.certificado.CertificadoParticipacao;
import br.com.plataformacursos.certificado.EmissorCertificadoConclusao;
import br.com.plataformacursos.certificado.EmissorCertificadoParticipacao;
import br.com.plataformacursos.certificado.TipoCertificado;
import br.com.plataformacursos.dominio.Aluno;
import br.com.plataformacursos.dominio.Curso;
import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.dominio.StatusMatricula;
import br.com.plataformacursos.notificacao.EventoMatricula;
import br.com.plataformacursos.notificacao.NotificacaoEmail;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.notificacao.ObservadorMatricula;
import br.com.plataformacursos.notificacao.TipoEvento;
import br.com.plataformacursos.servico.PlataformaCursos;
import br.com.plataformacursos.servico.RegraNegocioException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Verificações automatizadas do fluxo principal, escritas em Java puro para
 * que o projeto não dependa de bibliotecas externas.
 *
 * <p>Execute com {@code ./test.sh} (ou {@code test.bat} no Windows).</p>
 */
public class VerificacoesPlataforma {

    private static final PrintStream SILENCIOSO = new PrintStream(OutputStream.nullOutputStream());
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("UTC"));

    private static int aprovadas = 0;
    private static final List<String> falhas = new ArrayList<>();

    public static void main(String[] args) {
        verificar("matrícula notifica observadores", VerificacoesPlataforma::matriculaNotificaObservadores);
        verificar("conclusão automática ao atingir 100%", VerificacoesPlataforma::conclusaoAutomatica);
        verificar("e-mail ignora atualização de progresso", VerificacoesPlataforma::emailIgnoraProgresso);
        verificar("observador desanexado não recebe eventos", VerificacoesPlataforma::observadorDesanexado);
        verificar("emissor de conclusão cria CertificadoConclusao", VerificacoesPlataforma::emissorConclusao);
        verificar("conclusão exige 100% do curso", VerificacoesPlataforma::conclusaoExigeCursoCompleto);
        verificar("emissor de participação cria CertificadoParticipacao", VerificacoesPlataforma::emissorParticipacao);
        verificar("participação exige mínimo de 75%", VerificacoesPlataforma::participacaoExigeMinimo);
        verificar("certificado não é emitido duas vezes", VerificacoesPlataforma::certificadoUnico);
        verificar("matrícula cancelada não gera certificado", VerificacoesPlataforma::canceladaSemCertificado);
        verificar("emissor rejeita curso de outro tipo", VerificacoesPlataforma::emissorRejeitaOutroTipo);
        verificar("matrícula duplicada é rejeitada", VerificacoesPlataforma::matriculaDuplicada);
        verificar("progresso não pode regredir", VerificacoesPlataforma::progressoNaoRegride);
        verificar("e-mail de aluno duplicado é rejeitado", VerificacoesPlataforma::emailDuplicado);

        System.out.printf("%n%d verificações aprovadas, %d falharam.%n", aprovadas, falhas.size());
        if (!falhas.isEmpty()) {
            falhas.forEach(f -> System.out.println("  FALHA: " + f));
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Observer
    // ------------------------------------------------------------------

    private static void matriculaNotificaObservadores() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        afirmar(ctx.eventos.size() == 1, "esperava 1 evento, recebeu " + ctx.eventos.size());
        afirmar(ctx.eventos.get(0).tipo() == TipoEvento.MATRICULA_REALIZADA, "tipo inesperado");
        afirmar(ctx.email.getEnviados().size() == 1, "e-mail de boas-vindas não enviado");
        afirmar(ctx.notificacaoPlataforma.caixaDeEntrada(ctx.aluno).size() == 1, "caixa de entrada vazia");
        afirmar(m.getObservadores().size() == 3, "observadores não anexados à matrícula");
    }

    private static void conclusaoAutomatica() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 100);
        afirmar(m.getStatus() == StatusMatricula.CONCLUIDA, "matrícula deveria estar concluída");
        afirmar(ctx.tiposRecebidos().equals(List.of(TipoEvento.MATRICULA_REALIZADA,
                TipoEvento.PROGRESSO_ATUALIZADO, TipoEvento.CURSO_CONCLUIDO)), "sequência: " + ctx.tiposRecebidos());
    }

    private static void emailIgnoraProgresso() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 30);
        ctx.plataforma.registrarProgresso(m.getId(), 60);
        afirmar(ctx.email.getEnviados().size() == 1, "e-mail não deveria ser enviado para progresso");
        afirmar(ctx.notificacaoPlataforma.caixaDeEntrada(ctx.aluno).size() == 3, "plataforma deveria registrar tudo");
    }

    private static void observadorDesanexado() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        m.desanexar(ctx.coletor);
        ctx.plataforma.registrarProgresso(m.getId(), 40);
        afirmar(ctx.eventos.size() == 1, "observador desanexado recebeu evento");
    }

    // ------------------------------------------------------------------
    // Factory Method
    // ------------------------------------------------------------------

    private static void emissorConclusao() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 100);
        Certificado c = ctx.plataforma.emitirCertificado(m.getId());
        afirmar(c instanceof CertificadoConclusao, "classe inesperada: " + c.getClass().getSimpleName());
        afirmar(c.getCodigo().equals("CONC-2026-00001"), "código inesperado: " + c.getCodigo());
        afirmar(c.getDataEmissao().equals(LocalDate.of(2026, 11, 10)), "data inesperada");
        afirmar(m.getCertificado().orElseThrow() == c, "certificado não vinculado");
        afirmar(ctx.tiposRecebidos().contains(TipoEvento.CERTIFICADO_EMITIDO), "evento de certificado ausente");
        afirmar(c.formatar().contains("concluiu com êxito"), "texto do certificado inesperado");
    }

    private static void conclusaoExigeCursoCompleto() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 90);
        esperarErro(() -> ctx.plataforma.emitirCertificado(m.getId()));
    }

    private static void emissorParticipacao() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoParticipacao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 75);
        Certificado c = ctx.plataforma.emitirCertificado(m.getId());
        afirmar(c instanceof CertificadoParticipacao, "classe inesperada: " + c.getClass().getSimpleName());
        afirmar(((CertificadoParticipacao) c).getPercentualParticipacao() == 75, "percentual inesperado");
        afirmar(c.getCodigo().startsWith("PART-"), "prefixo inesperado: " + c.getCodigo());
    }

    private static void participacaoExigeMinimo() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoParticipacao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 74);
        esperarErro(() -> ctx.plataforma.emitirCertificado(m.getId()));
        afirmar(m.getCertificado().isEmpty(), "certificado não deveria existir");
    }

    private static void certificadoUnico() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 100);
        ctx.plataforma.emitirCertificado(m.getId());
        esperarErro(() -> ctx.plataforma.emitirCertificado(m.getId()));
    }

    private static void canceladaSemCertificado() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoParticipacao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 80);
        ctx.plataforma.cancelarMatricula(m.getId());
        esperarErro(() -> ctx.plataforma.emitirCertificado(m.getId()));
        afirmar(ctx.tiposRecebidos().contains(TipoEvento.MATRICULA_CANCELADA), "evento de cancelamento ausente");
    }

    private static void emissorRejeitaOutroTipo() {
        Aluno aluno = new Aluno(1, "Teste", "teste@email.com");
        Curso curso = new Curso(1, "Curso", 10, TipoCertificado.PARTICIPACAO);
        Matricula m = new Matricula(1, aluno, curso, LocalDate.of(2026, 1, 1));
        m.registrarProgresso(100);
        try {
            new EmissorCertificadoConclusao().emitir(m, LocalDate.of(2026, 1, 2));
            throw new AssertionError("deveria rejeitar o tipo de certificado");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    // ------------------------------------------------------------------
    // Regras gerais
    // ------------------------------------------------------------------

    private static void matriculaDuplicada() {
        Contexto ctx = new Contexto();
        ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        esperarErro(() -> ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId()));
    }

    private static void progressoNaoRegride() {
        Contexto ctx = new Contexto();
        Matricula m = ctx.plataforma.matricular(ctx.aluno.getId(), ctx.cursoConclusao.getId());
        ctx.plataforma.registrarProgresso(m.getId(), 50);
        esperarErro(() -> ctx.plataforma.registrarProgresso(m.getId(), 40));
        esperarErro(() -> ctx.plataforma.registrarProgresso(m.getId(), 120));
    }

    private static void emailDuplicado() {
        Contexto ctx = new Contexto();
        esperarErro(() -> ctx.plataforma.cadastrarAluno("Outra", "ALUNO@email.com"));
    }

    // ------------------------------------------------------------------
    // Infraestrutura mínima de verificação
    // ------------------------------------------------------------------

    private static final class Contexto {
        final PlataformaCursos plataforma = new PlataformaCursos(RELOGIO);
        final NotificacaoEmail email = new NotificacaoEmail(SILENCIOSO);
        final NotificacaoPlataforma notificacaoPlataforma = new NotificacaoPlataforma(SILENCIOSO);
        final List<EventoMatricula> eventos = new ArrayList<>();
        final ObservadorMatricula coletor = eventos::add;
        final Aluno aluno;
        final Curso cursoConclusao;
        final Curso cursoParticipacao;

        Contexto() {
            plataforma.registrarEmissor(new EmissorCertificadoConclusao());
            plataforma.registrarEmissor(new EmissorCertificadoParticipacao());
            plataforma.registrarObservador(email);
            plataforma.registrarObservador(notificacaoPlataforma);
            plataforma.registrarObservador(coletor);
            aluno = plataforma.cadastrarAluno("Aluno Teste", "aluno@email.com");
            cursoConclusao = plataforma.cadastrarCurso("Curso Completo", 20, TipoCertificado.CONCLUSAO);
            cursoParticipacao = plataforma.cadastrarCurso("Workshop", 4, TipoCertificado.PARTICIPACAO);
        }

        List<TipoEvento> tiposRecebidos() {
            return eventos.stream().map(EventoMatricula::tipo).toList();
        }
    }

    private static void verificar(String nome, Runnable verificacao) {
        try {
            verificacao.run();
            aprovadas++;
            System.out.println("[OK]    " + nome);
        } catch (Throwable e) {
            falhas.add(nome + " -> " + e.getMessage());
            System.out.println("[FALHA] " + nome + " -> " + e.getMessage());
        }
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void esperarErro(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException esperado) {
            return;
        }
        throw new AssertionError("esperava RegraNegocioException");
    }
}

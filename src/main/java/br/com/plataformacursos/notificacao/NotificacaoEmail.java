package br.com.plataformacursos.notificacao;

import br.com.plataformacursos.dominio.Aluno;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Observer: <b>ConcreteObserver</b>.
 *
 * <p>Simula o envio de e-mails ao aluno. Para não gerar excesso de mensagens,
 * o e-mail é enviado apenas nos marcos importantes da matrícula — atualizações
 * de progresso são ignoradas.</p>
 */
public class NotificacaoEmail implements ObservadorMatricula {

    private static final Set<TipoEvento> EVENTOS_RELEVANTES = EnumSet.of(
            TipoEvento.MATRICULA_REALIZADA,
            TipoEvento.CURSO_CONCLUIDO,
            TipoEvento.CERTIFICADO_EMITIDO,
            TipoEvento.MATRICULA_CANCELADA);

    private final PrintStream saida;
    private final List<String> enviados = new ArrayList<>();

    public NotificacaoEmail(PrintStream saida) {
        this.saida = Objects.requireNonNull(saida);
    }

    @Override
    public void atualizar(EventoMatricula evento) {
        if (!EVENTOS_RELEVANTES.contains(evento.tipo())) {
            return;
        }
        Aluno aluno = evento.matricula().getAluno();
        String email = String.format("Para: %s | Assunto: [%s] %s | %s",
                aluno.getEmail(),
                evento.tipo().getTitulo(),
                evento.matricula().getCurso().getTitulo(),
                evento.mensagem());
        enviados.add(email);
        saida.println("  [E-mail] " + email);
    }

    public List<String> getEnviados() {
        return Collections.unmodifiableList(enviados);
    }
}

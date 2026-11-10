package br.com.plataformacursos.notificacao;

import br.com.plataformacursos.dominio.Aluno;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Observer: <b>ConcreteObserver</b>.
 *
 * <p>Notificação interna da plataforma (sininho): registra todos os eventos
 * na caixa de entrada do aluno, inclusive as atualizações de progresso.</p>
 */
public class NotificacaoPlataforma implements ObservadorMatricula {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final PrintStream saida;
    private final Map<Integer, List<String>> caixasDeEntrada = new HashMap<>();

    public NotificacaoPlataforma(PrintStream saida) {
        this.saida = Objects.requireNonNull(saida);
    }

    @Override
    public void atualizar(EventoMatricula evento) {
        Aluno aluno = evento.matricula().getAluno();
        String mensagem = String.format("%s - %s: %s",
                evento.ocorridoEm().format(FORMATO), evento.tipo().getTitulo(), evento.mensagem());
        caixasDeEntrada.computeIfAbsent(aluno.getId(), id -> new ArrayList<>()).add(mensagem);
        saida.println("  [Plataforma] " + aluno.getNome() + " -> " + mensagem);
    }

    public List<String> caixaDeEntrada(Aluno aluno) {
        return List.copyOf(caixasDeEntrada.getOrDefault(aluno.getId(), List.of()));
    }
}

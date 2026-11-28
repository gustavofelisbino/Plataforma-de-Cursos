package br.com.plataformacursos.notificacao;

import br.com.plataformacursos.dominio.Matricula;

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
 * <p>Canal da coordenação do curso: simula o aviso à equipe pedagógica quando
 * um aluno conclui o curso (para providenciar o certificado) ou cancela a
 * matrícula (para registrar a desistência). Os demais eventos não exigem
 * ação da coordenação e são ignorados.</p>
 */
public class NotificacaoCoordenacao implements ObservadorMatricula {

    private static final Set<TipoEvento> EVENTOS_RELEVANTES = EnumSet.of(
            TipoEvento.CURSO_CONCLUIDO,
            TipoEvento.MATRICULA_CANCELADA);

    private final PrintStream saida;
    private final List<String> avisos = new ArrayList<>();

    public NotificacaoCoordenacao(PrintStream saida) {
        this.saida = Objects.requireNonNull(saida);
    }

    @Override
    public void atualizar(EventoMatricula evento) {
        if (!EVENTOS_RELEVANTES.contains(evento.tipo())) {
            return;
        }
        // Sem Matricula.toString(): o aviso não pode variar com a ordem dos eventos
        // (ex.: certificado de participação emitido antes dos 100%).
        Matricula matricula = evento.matricula();
        String aviso = String.format("%s: matrícula #%d (%s em \"%s\")",
                evento.tipo().getTitulo(), matricula.getId(),
                matricula.getAluno().getNome(), matricula.getCurso().getTitulo());
        avisos.add(aviso);
        saida.println("  [Coordenação] " + aviso);
    }

    public List<String> getAvisos() {
        return Collections.unmodifiableList(avisos);
    }
}

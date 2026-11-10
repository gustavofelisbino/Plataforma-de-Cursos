package br.com.plataformacursos.notificacao;

import br.com.plataformacursos.dominio.Matricula;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Dados enviados pelo sujeito aos observadores a cada notificação.
 */
public record EventoMatricula(TipoEvento tipo, Matricula matricula, String mensagem, LocalDateTime ocorridoEm) {

    public EventoMatricula {
        Objects.requireNonNull(tipo, "O tipo do evento é obrigatório.");
        Objects.requireNonNull(matricula, "A matrícula é obrigatória.");
        Objects.requireNonNull(mensagem, "A mensagem é obrigatória.");
        Objects.requireNonNull(ocorridoEm, "A data do evento é obrigatória.");
    }

    public EventoMatricula(TipoEvento tipo, Matricula matricula, String mensagem) {
        this(tipo, matricula, mensagem, LocalDateTime.now());
    }
}

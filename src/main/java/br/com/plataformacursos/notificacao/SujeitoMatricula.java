package br.com.plataformacursos.notificacao;

/**
 * Observer: <b>Subject</b>.
 *
 * <p>Contrato de quem publica eventos de matrícula e mantém a lista de
 * observadores interessados.</p>
 */
public interface SujeitoMatricula {

    void anexar(ObservadorMatricula observador);

    void desanexar(ObservadorMatricula observador);

    void notificar(EventoMatricula evento);
}

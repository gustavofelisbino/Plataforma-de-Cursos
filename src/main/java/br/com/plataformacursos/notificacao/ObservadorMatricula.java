package br.com.plataformacursos.notificacao;

/**
 * Observer: <b>Observer</b>.
 *
 * <p>Interface implementada por todo canal que deseja ser avisado sobre
 * mudanças em uma matrícula.</p>
 */
public interface ObservadorMatricula {

    void atualizar(EventoMatricula evento);
}

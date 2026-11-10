package br.com.plataformacursos.notificacao;

/**
 * Eventos do ciclo de vida de uma matrícula que são publicados aos observadores.
 */
public enum TipoEvento {
    MATRICULA_REALIZADA("Matrícula confirmada"),
    PROGRESSO_ATUALIZADO("Progresso atualizado"),
    CURSO_CONCLUIDO("Curso concluído"),
    CERTIFICADO_EMITIDO("Certificado disponível"),
    MATRICULA_CANCELADA("Matrícula cancelada");

    private final String titulo;

    TipoEvento(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}

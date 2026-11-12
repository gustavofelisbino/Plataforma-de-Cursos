package br.com.plataformacursos.console;

import br.com.plataformacursos.certificado.TipoCertificado;
import br.com.plataformacursos.servico.PlataformaCursos;

/**
 * Carrega dados de exemplo em memória para facilitar o uso da aplicação.
 */
public final class DadosIniciais {

    private DadosIniciais() {
    }

    public static void carregar(PlataformaCursos plataforma) {
        plataforma.cadastrarCurso("Java Orientado a Objetos", 40, TipoCertificado.CONCLUSAO);
        plataforma.cadastrarCurso("Design Patterns na Prática", 60, TipoCertificado.CONCLUSAO);
        plataforma.cadastrarCurso("Workshop de Git e GitHub", 8, TipoCertificado.PARTICIPACAO);

        plataforma.cadastrarAluno("Ana Souza", "ana.souza@email.com");
        plataforma.cadastrarAluno("Bruno Lima", "bruno.lima@email.com");
    }

    /**
     * Matrículas de exemplo usadas no modo web, para que a tela já abra com
     * os três status possíveis: uma concluída com certificado, uma em
     * andamento e uma cancelada. Deve ser chamado depois de {@link #carregar}.
     */
    public static void carregarMatriculasExemplo(PlataformaCursos plataforma) {
        int concluida = plataforma.matricular(1, 1).getId();       // Ana em Java OO
        plataforma.registrarProgresso(concluida, 100);
        plataforma.emitirCertificado(concluida);

        int emAndamento = plataforma.matricular(2, 2).getId();     // Bruno em Design Patterns
        plataforma.registrarProgresso(emAndamento, 40);

        int cancelada = plataforma.matricular(2, 3).getId();       // Bruno no Workshop de Git
        plataforma.cancelarMatricula(cancelada);
    }
}

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
}

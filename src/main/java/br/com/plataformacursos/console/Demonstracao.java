package br.com.plataformacursos.console;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.servico.PlataformaCursos;
import br.com.plataformacursos.servico.RegraNegocioException;

import java.io.PrintStream;

/**
 * Executa, sem interação, o fluxo completo de ponta a ponta da plataforma.
 * Útil para apresentar rapidamente os dois patterns funcionando.
 */
public class Demonstracao {

    private final PlataformaCursos plataforma;
    private final NotificacaoPlataforma notificacaoPlataforma;
    private final PrintStream saida;

    public Demonstracao(PlataformaCursos plataforma, NotificacaoPlataforma notificacaoPlataforma, PrintStream saida) {
        this.plataforma = plataforma;
        this.notificacaoPlataforma = notificacaoPlataforma;
        this.saida = saida;
    }

    public void executar() {
        titulo("Cursos disponíveis");
        plataforma.listarCursos().forEach(c -> saida.println("  " + c));

        titulo("Alunos cadastrados");
        plataforma.listarAlunos().forEach(a -> saida.println("  " + a));

        titulo("1) Ana se matricula em \"Design Patterns na Prática\"");
        Matricula matriculaAna = plataforma.matricular(1, 2);

        titulo("2) Ana avança no curso (observadores são notificados)");
        plataforma.registrarProgresso(matriculaAna.getId(), 50);
        plataforma.registrarProgresso(matriculaAna.getId(), 100);

        titulo("3) Emissão do certificado de conclusão (Factory Method)");
        Certificado certificadoAna = plataforma.emitirCertificado(matriculaAna.getId());
        saida.println(certificadoAna.formatar());

        titulo("4) Bruno se matricula no \"Workshop de Git e GitHub\"");
        Matricula matriculaBruno = plataforma.matricular(2, 3);
        plataforma.registrarProgresso(matriculaBruno.getId(), 60);

        titulo("5) Tentativa de emitir certificado antes do mínimo exigido");
        try {
            plataforma.emitirCertificado(matriculaBruno.getId());
        } catch (RegraNegocioException e) {
            saida.println("  Erro esperado: " + e.getMessage());
        }

        titulo("6) Bruno atinge 80% e recebe certificado de participação (Factory Method)");
        plataforma.registrarProgresso(matriculaBruno.getId(), 80);
        Certificado certificadoBruno = plataforma.emitirCertificado(matriculaBruno.getId());
        saida.println(certificadoBruno.formatar());

        titulo("7) Bruno se matricula em \"Java Orientado a Objetos\" e cancela");
        Matricula matriculaCancelada = plataforma.matricular(2, 1);
        plataforma.cancelarMatricula(matriculaCancelada.getId());

        titulo("Situação final das matrículas");
        plataforma.listarMatriculas().forEach(m -> saida.println("  " + m));

        titulo("Caixa de entrada da Ana na plataforma");
        notificacaoPlataforma.caixaDeEntrada(plataforma.buscarAluno(1)).forEach(m -> saida.println("  - " + m));
    }

    private void titulo(String texto) {
        saida.println();
        saida.println(">>> " + texto);
    }
}

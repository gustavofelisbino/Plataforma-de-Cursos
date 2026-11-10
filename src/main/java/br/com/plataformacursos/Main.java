package br.com.plataformacursos;

import br.com.plataformacursos.certificado.EmissorCertificadoConclusao;
import br.com.plataformacursos.certificado.EmissorCertificadoParticipacao;
import br.com.plataformacursos.console.DadosIniciais;
import br.com.plataformacursos.console.Demonstracao;
import br.com.plataformacursos.console.MenuConsole;
import br.com.plataformacursos.notificacao.NotificacaoEmail;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.servico.PlataformaCursos;

import java.util.Scanner;

/**
 * Ponto de entrada. Monta a aplicação registrando os emissores de
 * certificado (Factory Method) e os canais de notificação (Observer).
 *
 * <p>Uso: sem argumentos abre o menu interativo; com {@code --demo}
 * executa o fluxo completo automaticamente.</p>
 */
public class Main {

    public static void main(String[] args) {
        PlataformaCursos plataforma = new PlataformaCursos();

        // Factory Method: um ConcreteCreator por tipo de certificado.
        plataforma.registrarEmissor(new EmissorCertificadoConclusao());
        plataforma.registrarEmissor(new EmissorCertificadoParticipacao());

        // Observer: canais anexados a todas as matrículas.
        NotificacaoPlataforma notificacaoPlataforma = new NotificacaoPlataforma(System.out);
        plataforma.registrarObservador(new NotificacaoEmail(System.out));
        plataforma.registrarObservador(notificacaoPlataforma);

        DadosIniciais.carregar(plataforma);

        boolean modoDemo = args.length > 0 && "--demo".equals(args[0]);
        if (modoDemo) {
            new Demonstracao(plataforma, notificacaoPlataforma, System.out).executar();
        } else {
            new MenuConsole(plataforma, notificacaoPlataforma, new Scanner(System.in), System.out).iniciar();
        }
    }
}

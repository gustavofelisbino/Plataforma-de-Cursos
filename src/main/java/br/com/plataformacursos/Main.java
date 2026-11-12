package br.com.plataformacursos;

import br.com.plataformacursos.certificado.EmissorCertificadoConclusao;
import br.com.plataformacursos.certificado.EmissorCertificadoParticipacao;
import br.com.plataformacursos.console.DadosIniciais;
import br.com.plataformacursos.console.Demonstracao;
import br.com.plataformacursos.console.MenuConsole;
import br.com.plataformacursos.notificacao.NotificacaoEmail;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.servico.PlataformaCursos;
import br.com.plataformacursos.web.ServidorWeb;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

/**
 * Ponto de entrada. Monta a aplicação registrando os emissores de
 * certificado (Factory Method) e os canais de notificação (Observer).
 *
 * <p>Uso: sem argumentos abre o menu interativo; com {@code --demo}
 * executa o fluxo completo automaticamente; com {@code --web [porta]} sobe o
 * front-end em http://localhost:8080 (ou na porta informada).</p>
 */
public class Main {

    private static final int PORTA_PADRAO = 8080;

    public static void main(String[] args) throws IOException {
        PlataformaCursos plataforma = new PlataformaCursos();

        // Factory Method: um ConcreteCreator por tipo de certificado.
        plataforma.registrarEmissor(new EmissorCertificadoConclusao());
        plataforma.registrarEmissor(new EmissorCertificadoParticipacao());

        // Observer: canais anexados a todas as matrículas.
        NotificacaoPlataforma notificacaoPlataforma = new NotificacaoPlataforma(System.out);
        plataforma.registrarObservador(new NotificacaoEmail(System.out));
        plataforma.registrarObservador(notificacaoPlataforma);

        DadosIniciais.carregar(plataforma);

        String modo = args.length > 0 ? args[0] : "";
        if ("--web".equals(modo)) {
            DadosIniciais.carregarMatriculasExemplo(plataforma);
            iniciarWeb(plataforma, notificacaoPlataforma, args.length > 1 ? args[1] : null);
        } else if ("--demo".equals(modo)) {
            new Demonstracao(plataforma, notificacaoPlataforma, System.out).executar();
        } else {
            new MenuConsole(plataforma, notificacaoPlataforma, new Scanner(System.in), System.out).iniciar();
        }
    }

    private static void iniciarWeb(PlataformaCursos plataforma, NotificacaoPlataforma notificacaoPlataforma,
                                   String portaInformada) throws IOException {
        int porta = PORTA_PADRAO;
        if (portaInformada != null) {
            try {
                porta = Integer.parseInt(portaInformada);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + portaInformada + ". Use, por exemplo: --web 9090");
                System.exit(1);
            }
        }
        try {
            new ServidorWeb(plataforma, notificacaoPlataforma).iniciar(porta);
            System.out.println("Front-end disponível em http://localhost:" + porta + " (Ctrl+C para encerrar)");
        } catch (BindException e) {
            System.err.println("A porta " + porta + " já está em uso. Feche o outro programa ou escolha outra porta,"
                    + " por exemplo: --web 9090");
            System.exit(1);
        }
    }
}

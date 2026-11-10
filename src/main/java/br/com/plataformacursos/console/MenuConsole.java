package br.com.plataformacursos.console;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.certificado.TipoCertificado;
import br.com.plataformacursos.dominio.Aluno;
import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.servico.PlataformaCursos;
import br.com.plataformacursos.servico.RegraNegocioException;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Interface de linha de comando da plataforma.
 */
public class MenuConsole {

    private final PlataformaCursos plataforma;
    private final NotificacaoPlataforma notificacaoPlataforma;
    private final Scanner entrada;
    private final PrintStream saida;

    public MenuConsole(PlataformaCursos plataforma, NotificacaoPlataforma notificacaoPlataforma,
                       Scanner entrada, PrintStream saida) {
        this.plataforma = plataforma;
        this.notificacaoPlataforma = notificacaoPlataforma;
        this.entrada = entrada;
        this.saida = saida;
    }

    public void iniciar() {
        saida.println("=== Plataforma de Cursos ===");
        boolean executando = true;
        while (executando) {
            exibirMenu();
            String opcao = lerTexto("Escolha uma opção: ");
            if (opcao == null) {
                break; // fim da entrada
            }
            try {
                executando = processar(opcao.trim());
            } catch (RegraNegocioException e) {
                saida.println("Erro: " + e.getMessage());
            }
        }
        saida.println("Até logo!");
    }

    private void exibirMenu() {
        saida.println();
        saida.println(" 1. Listar cursos");
        saida.println(" 2. Listar alunos");
        saida.println(" 3. Cadastrar aluno");
        saida.println(" 4. Cadastrar curso");
        saida.println(" 5. Matricular aluno em curso");
        saida.println(" 6. Registrar progresso");
        saida.println(" 7. Emitir certificado");
        saida.println(" 8. Cancelar matrícula");
        saida.println(" 9. Listar matrículas");
        saida.println("10. Ver notificações de um aluno");
        saida.println(" 0. Sair");
    }

    private boolean processar(String opcao) {
        switch (opcao) {
            case "1" -> listar(plataforma.listarCursos());
            case "2" -> listar(plataforma.listarAlunos());
            case "3" -> cadastrarAluno();
            case "4" -> cadastrarCurso();
            case "5" -> matricular();
            case "6" -> registrarProgresso();
            case "7" -> emitirCertificado();
            case "8" -> cancelarMatricula();
            case "9" -> listar(plataforma.listarMatriculas());
            case "10" -> verNotificacoes();
            case "0" -> {
                return false;
            }
            default -> saida.println("Opção inválida.");
        }
        return true;
    }

    private void cadastrarAluno() {
        String nome = lerTexto("Nome: ");
        String email = lerTexto("E-mail: ");
        Aluno aluno = plataforma.cadastrarAluno(nome, email);
        saida.println("Aluno cadastrado: " + aluno);
    }

    private void cadastrarCurso() {
        String titulo = lerTexto("Título: ");
        int cargaHoraria = lerInteiro("Carga horária (horas): ");
        List<TipoCertificado> tipos = plataforma.tiposCertificadoDisponiveis();
        for (int i = 0; i < tipos.size(); i++) {
            TipoCertificado tipo = tipos.get(i);
            saida.printf("  %d. %s (%s)%n", i + 1, tipo.getDescricao(),
                    plataforma.emissorDe(tipo).descricaoRequisito());
        }
        int indice = lerInteiro("Tipo de certificado: ");
        if (indice < 1 || indice > tipos.size()) {
            throw new RegraNegocioException("Tipo de certificado inválido.");
        }
        saida.println("Curso cadastrado: " + plataforma.cadastrarCurso(titulo, cargaHoraria, tipos.get(indice - 1)));
    }

    private void matricular() {
        listar(plataforma.listarAlunos());
        int idAluno = lerInteiro("ID do aluno: ");
        listar(plataforma.listarCursos());
        int idCurso = lerInteiro("ID do curso: ");
        Matricula matricula = plataforma.matricular(idAluno, idCurso);
        saida.println("Matrícula realizada: " + matricula);
    }

    private void registrarProgresso() {
        int idMatricula = selecionarMatricula();
        int percentual = lerInteiro("Novo progresso (0-100): ");
        saida.println("Matrícula atualizada: " + plataforma.registrarProgresso(idMatricula, percentual));
    }

    private void emitirCertificado() {
        int idMatricula = selecionarMatricula();
        Certificado certificado = plataforma.emitirCertificado(idMatricula);
        saida.println(certificado.formatar());
    }

    private void cancelarMatricula() {
        int idMatricula = selecionarMatricula();
        saida.println("Matrícula atualizada: " + plataforma.cancelarMatricula(idMatricula));
    }

    private void verNotificacoes() {
        listar(plataforma.listarAlunos());
        Aluno aluno = plataforma.buscarAluno(lerInteiro("ID do aluno: "));
        List<String> mensagens = notificacaoPlataforma.caixaDeEntrada(aluno);
        if (mensagens.isEmpty()) {
            saida.println("Nenhuma notificação para " + aluno.getNome() + ".");
            return;
        }
        mensagens.forEach(m -> saida.println("  - " + m));
    }

    private int selecionarMatricula() {
        List<Matricula> matriculas = plataforma.listarMatriculas();
        if (matriculas.isEmpty()) {
            throw new RegraNegocioException("Nenhuma matrícula cadastrada.");
        }
        listar(matriculas);
        return lerInteiro("ID da matrícula: ");
    }

    private void listar(List<?> itens) {
        if (itens.isEmpty()) {
            saida.println("  (nenhum registro)");
            return;
        }
        itens.forEach(item -> saida.println("  " + item));
    }

    private String lerTexto(String rotulo) {
        saida.print(rotulo);
        return entrada.hasNextLine() ? entrada.nextLine() : null;
    }

    private int lerInteiro(String rotulo) {
        String valor = lerTexto(rotulo);
        if (valor == null) {
            throw new RegraNegocioException("Entrada encerrada.");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Informe um número inteiro válido.");
        }
    }
}

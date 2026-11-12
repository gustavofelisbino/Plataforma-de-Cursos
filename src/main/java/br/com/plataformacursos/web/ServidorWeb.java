package br.com.plataformacursos.web;

import br.com.plataformacursos.certificado.Certificado;
import br.com.plataformacursos.certificado.TipoCertificado;
import br.com.plataformacursos.dominio.Aluno;
import br.com.plataformacursos.dominio.Curso;
import br.com.plataformacursos.dominio.Matricula;
import br.com.plataformacursos.notificacao.NotificacaoPlataforma;
import br.com.plataformacursos.servico.PlataformaCursos;
import br.com.plataformacursos.servico.RegraNegocioException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Front-end web simples: expõe a {@link PlataformaCursos} como uma API JSON
 * e serve a página {@code web/index.html}. Usa apenas o servidor HTTP do JDK.
 *
 * <p>O servidor atende uma requisição por vez (executor padrão), então o
 * serviço em memória não precisa ser thread-safe.</p>
 */
public class ServidorWeb {

    private final PlataformaCursos plataforma;
    private final NotificacaoPlataforma notificacaoPlataforma;

    public ServidorWeb(PlataformaCursos plataforma, NotificacaoPlataforma notificacaoPlataforma) {
        this.plataforma = plataforma;
        this.notificacaoPlataforma = notificacaoPlataforma;
    }

    /**
     * Sobe o servidor na porta informada (0 escolhe uma porta livre) e o
     * devolve já iniciado, para que quem chamou possa consultar a porta ou pará-lo.
     */
    public HttpServer iniciar(int porta) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", porta), 0);
        servidor.createContext("/api/", this::tratarApi);
        servidor.createContext("/", this::servirPagina);
        servidor.start();
        return servidor;
    }

    // ------------------------------------------------------------------
    // Rotas
    // ------------------------------------------------------------------

    private void tratarApi(HttpExchange troca) throws IOException {
        try {
            String metodo = troca.getRequestMethod();
            String[] partes = troca.getRequestURI().getPath().replaceAll("^/api/|/$", "").split("/");
            Map<String, String> corpo = lerFormulario(troca);
            String json = rotear(metodo, partes, corpo);
            if (json == null) {
                responder(troca, 404, "application/json", erro("Rota não encontrada."));
            } else {
                responder(troca, 200, "application/json", json);
            }
        } catch (RegraNegocioException e) {
            responder(troca, 400, "application/json", erro(e.getMessage()));
        } catch (NumberFormatException e) {
            responder(troca, 400, "application/json", erro("Valor numérico inválido."));
        } catch (RuntimeException e) {
            responder(troca, 500, "application/json", erro("Erro inesperado: " + e.getMessage()));
        }
    }

    private String rotear(String metodo, String[] p, Map<String, String> corpo) {
        boolean get = "GET".equals(metodo);
        boolean post = "POST".equals(metodo);

        switch (p[0]) {
            case "tipos-certificado":
                if (get) {
                    return lista(plataforma.tiposCertificadoDisponiveis().stream()
                            .map(t -> obj("valor", t.name(), "descricao", t.getDescricao())).toList());
                }
                break;
            case "cursos":
                if (get && p.length == 1) {
                    return lista(plataforma.listarCursos().stream().map(ServidorWeb::json).toList());
                }
                if (post && p.length == 1) {
                    return json(plataforma.cadastrarCurso(corpo.get("titulo"),
                            inteiro(corpo, "cargaHoraria"), tipo(corpo.get("tipoCertificado"))));
                }
                break;
            case "alunos":
                if (get && p.length == 1) {
                    return lista(plataforma.listarAlunos().stream().map(ServidorWeb::json).toList());
                }
                if (post && p.length == 1) {
                    return json(plataforma.cadastrarAluno(corpo.get("nome"), corpo.get("email")));
                }
                if (get && p.length == 3 && "notificacoes".equals(p[2])) {
                    Aluno aluno = plataforma.buscarAluno(Integer.parseInt(p[1]));
                    return lista(notificacaoPlataforma.caixaDeEntrada(aluno).stream().map(ServidorWeb::texto).toList());
                }
                break;
            case "matriculas":
                if (get && p.length == 1) {
                    return lista(plataforma.listarMatriculas().stream().map(ServidorWeb::json).toList());
                }
                if (post && p.length == 1) {
                    return json(plataforma.matricular(inteiro(corpo, "idAluno"), inteiro(corpo, "idCurso")));
                }
                if (post && p.length == 3) {
                    int id = Integer.parseInt(p[1]);
                    switch (p[2]) {
                        case "progresso":
                            return json(plataforma.registrarProgresso(id, inteiro(corpo, "percentual")));
                        case "cancelar":
                            return json(plataforma.cancelarMatricula(id));
                        case "certificado":
                            Certificado c = plataforma.emitirCertificado(id);
                            return obj("codigo", c.getCodigo(), "texto", c.formatar());
                        default:
                            break;
                    }
                }
                if (get && p.length == 3 && "certificado".equals(p[2])) {
                    Matricula m = plataforma.buscarMatricula(Integer.parseInt(p[1]));
                    Certificado c = m.getCertificado()
                            .orElseThrow(() -> new RegraNegocioException("Matrícula sem certificado emitido."));
                    return obj("codigo", c.getCodigo(), "texto", c.formatar());
                }
                break;
            default:
                break;
        }
        return null;
    }

    private void servirPagina(HttpExchange troca) throws IOException {
        String caminho = troca.getRequestURI().getPath();
        if (!"/".equals(caminho) && !"/index.html".equals(caminho)) {
            responder(troca, 404, "text/plain; charset=utf-8", "Não encontrado");
            return;
        }
        try (InputStream in = ServidorWeb.class.getResourceAsStream("/web/index.html")) {
            if (in == null) {
                responder(troca, 500, "text/plain; charset=utf-8",
                        "index.html não encontrado no classpath (src/main/resources/web).");
                return;
            }
            responder(troca, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    // ------------------------------------------------------------------
    // Conversão para JSON (manual, para não depender de bibliotecas)
    // ------------------------------------------------------------------

    private static String json(Curso c) {
        return obj("id", c.getId(), "titulo", c.getTitulo(), "cargaHoraria", c.getCargaHoraria(),
                "tipoCertificado", c.getTipoCertificado().name(),
                "tipoCertificadoDescricao", c.getTipoCertificado().getDescricao());
    }

    private static String json(Aluno a) {
        return obj("id", a.getId(), "nome", a.getNome(), "email", a.getEmail());
    }

    private static String json(Matricula m) {
        return obj("id", m.getId(),
                "idAluno", m.getAluno().getId(), "aluno", m.getAluno().getNome(),
                "idCurso", m.getCurso().getId(), "curso", m.getCurso().getTitulo(),
                "dataMatricula", m.getDataMatricula().toString(),
                "progresso", m.getProgresso(),
                "status", m.getStatus().name(), "statusDescricao", m.getStatus().getDescricao(),
                "certificado", m.getCertificado().map(Certificado::getCodigo).orElse(null));
    }

    /** Monta um objeto JSON a partir de pares chave/valor. */
    private static String obj(Object... pares) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < pares.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append(texto((String) pares[i])).append(':').append(valor(pares[i + 1]));
        }
        return sb.append('}').toString();
    }

    private static String valor(Object v) {
        if (v == null) return "null";
        if (v instanceof Number) return v.toString();
        return texto(v.toString());
    }

    private static String lista(List<String> itens) {
        return "[" + String.join(",", itens) + "]";
    }

    private static String texto(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String erro(String mensagem) {
        return obj("erro", mensagem);
    }

    // ------------------------------------------------------------------
    // Utilitários HTTP
    // ------------------------------------------------------------------

    /** Lê o corpo no formato application/x-www-form-urlencoded. */
    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> campos = new HashMap<>();
        if (corpo.isBlank()) return campos;
        for (String par : corpo.split("&")) {
            String[] kv = par.split("=", 2);
            campos.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return campos;
    }

    private static int inteiro(Map<String, String> corpo, String campo) {
        String v = corpo.get(campo);
        if (v == null || v.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " é obrigatório.");
        }
        return Integer.parseInt(v.trim());
    }

    private static TipoCertificado tipo(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : TipoCertificado.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Tipo de certificado inválido: " + valor);
        }
    }

    private static void responder(HttpExchange troca, int status, String tipo, String conteudo) throws IOException {
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type",
                tipo.contains("charset") ? tipo : tipo + "; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = troca.getResponseBody()) {
            out.write(bytes);
        }
    }
}

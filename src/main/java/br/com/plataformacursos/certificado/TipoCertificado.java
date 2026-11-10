package br.com.plataformacursos.certificado;

/**
 * Tipos de certificado suportados pela plataforma. Cada tipo possui um
 * {@link EmissorCertificado} concreto responsável por criá-lo.
 */
public enum TipoCertificado {
    CONCLUSAO("Conclusão", "CONC"),
    PARTICIPACAO("Participação", "PART");

    private final String descricao;
    private final String prefixoCodigo;

    TipoCertificado(String descricao, String prefixoCodigo) {
        this.descricao = descricao;
        this.prefixoCodigo = prefixoCodigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPrefixoCodigo() {
        return prefixoCodigo;
    }
}

package br.com.plataformacursos.dominio;

import br.com.plataformacursos.certificado.TipoCertificado;

import java.util.Objects;

/**
 * Curso oferecido pela plataforma. Cada curso define qual tipo de
 * certificado é emitido para os alunos matriculados.
 */
public class Curso {

    private final int id;
    private final String titulo;
    private final int cargaHoraria;
    private final TipoCertificado tipoCertificado;

    public Curso(int id, String titulo, int cargaHoraria, TipoCertificado tipoCertificado) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do curso é obrigatório.");
        }
        if (cargaHoraria <= 0) {
            throw new IllegalArgumentException("A carga horária deve ser maior que zero.");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.cargaHoraria = cargaHoraria;
        if (tipoCertificado == null) {
            throw new IllegalArgumentException("O tipo de certificado é obrigatório.");
        }
        this.tipoCertificado = tipoCertificado;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public TipoCertificado getTipoCertificado() {
        return tipoCertificado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curso outro)) return false;
        return id == outro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%dh) - certificado de %s",
                id, titulo, cargaHoraria, tipoCertificado.getDescricao());
    }
}

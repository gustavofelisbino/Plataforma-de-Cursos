package br.com.plataformacursos.dominio;

import java.util.Objects;

/**
 * Pessoa cadastrada na plataforma que pode se matricular em cursos.
 */
public class Aluno {

    private final int id;
    private final String nome;
    private final String email;

    public Aluno(int id, String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("E-mail inválido: " + email);
        }
        this.id = id;
        this.nome = nome.trim();
        this.email = email.trim().toLowerCase();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno outro)) return false;
        return id == outro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d %s <%s>", id, nome, email);
    }
}

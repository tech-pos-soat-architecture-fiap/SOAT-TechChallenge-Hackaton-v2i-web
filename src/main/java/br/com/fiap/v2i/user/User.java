package br.com.fiap.v2i.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
//TODO talvez precise adicionar mais atributos aqui
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    protected User() {
    }

    private User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User create(String username, String encodedPassword) {
        return new User(username, encodedPassword);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

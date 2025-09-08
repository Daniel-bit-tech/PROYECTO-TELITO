package com.example.telitodev.entity;

// Prueba con jakarta.persistence (Spring Boot 3.x)
import jakarta.persistence.*;

// Si jakarta no funciona, usa javax.persistence (Spring Boot 2.x)
// import javax.persistence.*;

@Entity
@Table(name = "test_entity")
public class TestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    // Constructor vacío requerido por JPA
    public TestEntity() {}
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}

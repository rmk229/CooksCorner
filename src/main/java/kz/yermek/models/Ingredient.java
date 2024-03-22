package kz.yermek.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String amount;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}

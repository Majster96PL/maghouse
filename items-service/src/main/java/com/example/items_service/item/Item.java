package com.example.items_service.item;

import com.example.user_service.auth.registration.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Builder
@Data
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name required!")
    @Size(max = 50)
    private String name;
    @Size(max = 50)
    @NotBlank(message = "Code required!")
    private String itemCode;
    private int quantity;
    @ManyToOne
    @JoinColumn(name = "firstname", nullable = false)
    private User firstname;

}

package com.petcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "duenos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dueno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El RUT es obligatorio")
	@Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener el formato 12345678-9 (sin puntos y con guion)")
	@Column(nullable = false, unique = true, length = 12)
	private String rut;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
	@Column(nullable = false, length = 100)
	private String nombre;

	@NotBlank(message = "El teléfono es obligatorio")
	@Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
	@Column(nullable = false, length = 20)
	private String telefono;

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email no tiene un formato válido")
	@Column(nullable = false, length = 100)
	private String email;

	@OneToMany(mappedBy = "dueno")
	@JsonIgnore
	private List<Mascota> mascotas = new ArrayList<>();
}

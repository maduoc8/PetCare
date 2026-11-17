package com.petcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mascotas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
	@Column(nullable = false, length = 60)
	private String nombre;

	@NotBlank(message = "La especie es obligatoria")
	@Size(max = 40, message = "La especie no puede superar los 40 caracteres")
	@Column(nullable = false, length = 40)
	private String especie;

	@Size(max = 60, message = "La raza no puede superar los 60 caracteres")
	@Column(length = 60)
	private String raza;

	@PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
	private LocalDate fechaNacimiento;

	@NotNull(message = "Debe indicar el dueño de la mascota: \"dueno\": {\"id\": ...}")
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "dueno_id", nullable = false)
	private Dueno dueno;

	@OneToMany(mappedBy = "mascota")
	@JsonIgnore
	private List<Cita> citas = new ArrayList<>();
}

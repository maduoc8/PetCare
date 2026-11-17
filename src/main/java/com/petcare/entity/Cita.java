package com.petcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "La fecha es obligatoria (formato 2026-11-20T10:30:00)")
	@Column(nullable = false)
	private LocalDateTime fecha;

	@NotBlank(message = "El motivo es obligatorio")
	@Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
	@Column(nullable = false, length = 200)
	private String motivo;

	@Size(max = 500, message = "El diagnóstico no puede superar los 500 caracteres")
	@Column(length = 500)
	private String diagnostico;

	@NotNull(message = "El costo es obligatorio")
	@PositiveOrZero(message = "El costo no puede ser negativo")
	@Column(nullable = false)
	private Integer costo;

	@NotNull(message = "Debe indicar la mascota de la cita: \"mascota\": {\"id\": ...}")
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "mascota_id", nullable = false)
	private Mascota mascota;
}

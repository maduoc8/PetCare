package com.petcare.controller;

import com.petcare.entity.Cita;
import com.petcare.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

	private final CitaService citaService;

	@GetMapping
	public List<Cita> listar() {
		return citaService.listar();
	}

	@GetMapping("/{id}")
	public Cita obtener(@PathVariable Long id) {
		return citaService.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
		Cita creada = citaService.crear(cita);
		return ResponseEntity.created(URI.create("/api/citas/" + creada.getId())).body(creada);
	}

	@PutMapping("/{id}")
	public Cita actualizar(@PathVariable Long id, @Valid @RequestBody Cita cita) {
		return citaService.actualizar(id, cita);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		citaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}

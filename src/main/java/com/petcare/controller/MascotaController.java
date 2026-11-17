package com.petcare.controller;

import com.petcare.entity.Cita;
import com.petcare.entity.Mascota;
import com.petcare.service.CitaService;
import com.petcare.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

	private final MascotaService mascotaService;
	private final CitaService citaService;

	@GetMapping
	public List<Mascota> listar() {
		return mascotaService.listar();
	}

	@GetMapping("/{id}")
	public Mascota obtener(@PathVariable Long id) {
		return mascotaService.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<Mascota> crear(@Valid @RequestBody Mascota mascota) {
		Mascota creada = mascotaService.crear(mascota);
		return ResponseEntity.created(URI.create("/api/mascotas/" + creada.getId())).body(creada);
	}

	@PutMapping("/{id}")
	public Mascota actualizar(@PathVariable Long id, @Valid @RequestBody Mascota mascota) {
		return mascotaService.actualizar(id, mascota);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		mascotaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/citas")
	public List<Cita> citas(@PathVariable Long id) {
		return citaService.listarPorMascota(id);
	}
}

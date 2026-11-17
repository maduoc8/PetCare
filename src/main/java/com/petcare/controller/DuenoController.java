package com.petcare.controller;

import com.petcare.entity.Dueno;
import com.petcare.entity.Mascota;
import com.petcare.service.DuenoService;
import com.petcare.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/duenos")
@RequiredArgsConstructor
public class DuenoController {

	private final DuenoService duenoService;
	private final MascotaService mascotaService;

	@GetMapping
	public List<Dueno> listar() {
		return duenoService.listar();
	}

	@GetMapping("/{id}")
	public Dueno obtener(@PathVariable Long id) {
		return duenoService.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<Dueno> crear(@Valid @RequestBody Dueno dueno) {
		Dueno creado = duenoService.crear(dueno);
		return ResponseEntity.created(URI.create("/api/duenos/" + creado.getId())).body(creado);
	}

	@PutMapping("/{id}")
	public Dueno actualizar(@PathVariable Long id, @Valid @RequestBody Dueno dueno) {
		return duenoService.actualizar(id, dueno);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		duenoService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/mascotas")
	public List<Mascota> mascotas(@PathVariable Long id) {
		return mascotaService.listarPorDueno(id);
	}
}

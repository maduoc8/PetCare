package com.petcare.service;

import com.petcare.entity.Dueno;
import com.petcare.exception.ConflictoException;
import com.petcare.exception.RecursoNoEncontradoException;
import com.petcare.repository.DuenoRepository;
import com.petcare.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DuenoService {

	private final DuenoRepository duenoRepository;
	private final MascotaRepository mascotaRepository;

	@Transactional(readOnly = true)
	public List<Dueno> listar() {
		return duenoRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Dueno buscarPorId(Long id) {
		return duenoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Dueño", id));
	}

	@Transactional
	public Dueno crear(Dueno dueno) {
		dueno.setId(null);
		if (duenoRepository.existsByRut(dueno.getRut())) {
			throw new ConflictoException("Ya existe un dueño registrado con el RUT " + dueno.getRut());
		}
		return duenoRepository.save(dueno);
	}

	@Transactional
	public Dueno actualizar(Long id, Dueno datos) {
		Dueno existente = buscarPorId(id);
		if (duenoRepository.existsByRutAndIdNot(datos.getRut(), id)) {
			throw new ConflictoException("Ya existe otro dueño registrado con el RUT " + datos.getRut());
		}
		existente.setRut(datos.getRut());
		existente.setNombre(datos.getNombre());
		existente.setTelefono(datos.getTelefono());
		existente.setEmail(datos.getEmail());
		return duenoRepository.save(existente);
	}

	@Transactional
	public void eliminar(Long id) {
		Dueno dueno = buscarPorId(id);
		if (mascotaRepository.existsByDuenoId(id)) {
			throw new ConflictoException("No se puede eliminar el dueño con id " + id
					+ " porque tiene mascotas registradas. Elimínelas o asígnelas a otro dueño primero.");
		}
		duenoRepository.delete(dueno);
	}
}

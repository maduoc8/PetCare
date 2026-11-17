package com.petcare.service;

import com.petcare.entity.Dueno;
import com.petcare.entity.Mascota;
import com.petcare.exception.ConflictoException;
import com.petcare.exception.RecursoNoEncontradoException;
import com.petcare.exception.SolicitudInvalidaException;
import com.petcare.repository.CitaRepository;
import com.petcare.repository.DuenoRepository;
import com.petcare.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

	private final MascotaRepository mascotaRepository;
	private final DuenoRepository duenoRepository;
	private final CitaRepository citaRepository;

	@Transactional(readOnly = true)
	public List<Mascota> listar() {
		return mascotaRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Mascota buscarPorId(Long id) {
		return mascotaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Mascota", id));
	}

	@Transactional(readOnly = true)
	public List<Mascota> listarPorDueno(Long duenoId) {
		if (!duenoRepository.existsById(duenoId)) {
			throw new RecursoNoEncontradoException("Dueño", duenoId);
		}
		return mascotaRepository.findByDuenoId(duenoId);
	}

	@Transactional
	public Mascota crear(Mascota mascota) {
		mascota.setId(null);
		mascota.setDueno(duenoExistente(mascota.getDueno()));
		return mascotaRepository.save(mascota);
	}

	@Transactional
	public Mascota actualizar(Long id, Mascota datos) {
		Mascota existente = buscarPorId(id);
		Dueno dueno = duenoExistente(datos.getDueno());
		existente.setNombre(datos.getNombre());
		existente.setEspecie(datos.getEspecie());
		existente.setRaza(datos.getRaza());
		existente.setFechaNacimiento(datos.getFechaNacimiento());
		existente.setDueno(dueno);
		return mascotaRepository.save(existente);
	}

	@Transactional
	public void eliminar(Long id) {
		Mascota mascota = buscarPorId(id);
		if (citaRepository.existsByMascotaId(id)) {
			throw new ConflictoException("No se puede eliminar la mascota con id " + id
					+ " porque tiene citas registradas. Elimine sus citas primero.");
		}
		mascotaRepository.delete(mascota);
	}

	private Dueno duenoExistente(Dueno referencia) {
		if (referencia == null || referencia.getId() == null) {
			throw new SolicitudInvalidaException("Debe indicar el dueño de la mascota: \"dueno\": {\"id\": ...}");
		}
		return duenoRepository.findById(referencia.getId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Dueño", referencia.getId()));
	}
}

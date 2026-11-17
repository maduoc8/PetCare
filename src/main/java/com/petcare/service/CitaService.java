package com.petcare.service;

import com.petcare.entity.Cita;
import com.petcare.entity.Mascota;
import com.petcare.exception.RecursoNoEncontradoException;
import com.petcare.exception.SolicitudInvalidaException;
import com.petcare.repository.CitaRepository;
import com.petcare.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

	private final CitaRepository citaRepository;
	private final MascotaRepository mascotaRepository;

	@Transactional(readOnly = true)
	public List<Cita> listar() {
		return citaRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Cita buscarPorId(Long id) {
		return citaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Cita", id));
	}

	@Transactional(readOnly = true)
	public List<Cita> listarPorMascota(Long mascotaId) {
		if (!mascotaRepository.existsById(mascotaId)) {
			throw new RecursoNoEncontradoException("Mascota", mascotaId);
		}
		return citaRepository.findByMascotaIdOrderByFechaAsc(mascotaId);
	}

	@Transactional
	public Cita crear(Cita cita) {
		cita.setId(null);
		cita.setMascota(mascotaExistente(cita.getMascota()));
		return citaRepository.save(cita);
	}

	@Transactional
	public Cita actualizar(Long id, Cita datos) {
		Cita existente = buscarPorId(id);
		Mascota mascota = mascotaExistente(datos.getMascota());
		existente.setFecha(datos.getFecha());
		existente.setMotivo(datos.getMotivo());
		existente.setDiagnostico(datos.getDiagnostico());
		existente.setCosto(datos.getCosto());
		existente.setMascota(mascota);
		return citaRepository.save(existente);
	}

	@Transactional
	public void eliminar(Long id) {
		citaRepository.delete(buscarPorId(id));
	}

	private Mascota mascotaExistente(Mascota referencia) {
		if (referencia == null || referencia.getId() == null) {
			throw new SolicitudInvalidaException("Debe indicar la mascota de la cita: \"mascota\": {\"id\": ...}");
		}
		return mascotaRepository.findById(referencia.getId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Mascota", referencia.getId()));
	}
}

package com.petcare;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CitaApiTest extends ApiTestBase {

	@Test
	void crearCita_paraMascotaExistente_devuelve201ConLaMascota() throws Exception {
		long mascotaId = crearMascota(crearDueno());

		postJson("/api/citas", citaJson(mascotaId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.fecha").value("2026-11-20T10:30:00"))
				.andExpect(jsonPath("$.motivo").value("Control anual"))
				.andExpect(jsonPath("$.costo").value(25000))
				.andExpect(jsonPath("$.mascota.id").value(mascotaId))
				.andExpect(jsonPath("$.mascota.nombre").value("Firulais"));
	}

	@Test
	void crearCita_paraMascotaInexistente_devuelve404YNoGuardaNada() throws Exception {
		int citasAntes = cantidadEn(get("/api/citas").andReturn());

		postJson("/api/citas", citaJson(999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.mensaje").value(containsString("999999")));

		assertEquals(citasAntes, cantidadEn(get("/api/citas").andReturn()));
	}

	@Test
	void crearCita_sinMascota_devuelve400() throws Exception {
		postJson("/api/citas", """
				{"fecha":"2026-11-20T10:30:00","motivo":"Control anual","costo":25000}
				""")
				.andExpect(status().isBadRequest());
	}

	@Test
	void crearCita_conCostoNegativo_devuelve400() throws Exception {
		long mascotaId = crearMascota(crearDueno());

		postJson("/api/citas", """
				{"fecha":"2026-11-20T10:30:00","motivo":"Control anual","costo":-1,"mascota":{"id":%d}}
				""".formatted(mascotaId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.costo").exists());
	}

	@Test
	void crearCita_sinFechaNiMotivo_devuelve400ConErroresPorCampo() throws Exception {
		long mascotaId = crearMascota(crearDueno());

		postJson("/api/citas", """
				{"motivo":"  ","costo":1000,"mascota":{"id":%d}}
				""".formatted(mascotaId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.fecha").exists())
				.andExpect(jsonPath("$.errores.motivo").exists());
	}

	@Test
	void obtenerCita_porId_incluyeMascotaYDueno() throws Exception {
		long duenoId = crearDueno();
		long mascotaId = crearMascota(duenoId);
		long id = crearCita(mascotaId);

		get("/api/citas/" + id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.mascota.id").value(mascotaId))
				.andExpect(jsonPath("$.mascota.dueno.id").value(duenoId));
	}

	@Test
	void obtenerCita_inexistente_devuelve404() throws Exception {
		get("/api/citas/999999").andExpect(status().isNotFound());
	}

	@Test
	void listarCitas_incluyeLasCreadas() throws Exception {
		long id = crearCita(crearMascota(crearDueno()));

		get("/api/citas")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItem((int) id)));
	}

	@Test
	void actualizarCita_registraDiagnosticoYCosto() throws Exception {
		long mascotaId = crearMascota(crearDueno());
		long id = crearCita(mascotaId);

		putJson("/api/citas/" + id, """
				{"fecha":"2026-11-20T11:00:00","motivo":"Control anual","diagnostico":"Sano, vacunas al dia","costo":30000,"mascota":{"id":%d}}
				""".formatted(mascotaId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.diagnostico").value("Sano, vacunas al dia"))
				.andExpect(jsonPath("$.costo").value(30000));

		get("/api/citas/" + id)
				.andExpect(jsonPath("$.fecha").value("2026-11-20T11:00:00"))
				.andExpect(jsonPath("$.diagnostico").value("Sano, vacunas al dia"));
	}

	@Test
	void actualizarCita_conMascotaInexistente_devuelve404() throws Exception {
		long id = crearCita(crearMascota(crearDueno()));

		putJson("/api/citas/" + id, citaJson(999999)).andExpect(status().isNotFound());
	}

	@Test
	void actualizarCita_inexistente_devuelve404() throws Exception {
		long mascotaId = crearMascota(crearDueno());

		putJson("/api/citas/999999", citaJson(mascotaId)).andExpect(status().isNotFound());
	}

	@Test
	void eliminarCita_devuelve204YDejaDeExistir() throws Exception {
		long mascotaId = crearMascota(crearDueno());
		long id = crearCita(mascotaId);

		delete("/api/citas/" + id).andExpect(status().isNoContent());
		get("/api/citas/" + id).andExpect(status().isNotFound());
		get("/api/mascotas/" + mascotaId + "/citas").andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void eliminarCita_inexistente_devuelve404() throws Exception {
		delete("/api/citas/999999").andExpect(status().isNotFound());
	}
}

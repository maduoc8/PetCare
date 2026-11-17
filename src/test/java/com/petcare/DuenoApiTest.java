package com.petcare;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DuenoApiTest extends ApiTestBase {

	@Test
	void crearDueno_devuelve201ConIdYDatos() throws Exception {
		String rut = nuevoRut();

		postJson("/api/duenos", duenoJson(rut, "Ana Perez"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.rut").value(rut))
				.andExpect(jsonPath("$.nombre").value("Ana Perez"))
				.andExpect(jsonPath("$.telefono").value("+56911112222"))
				.andExpect(jsonPath("$.email").value("cliente@correo.cl"));
	}

	@Test
	void crearDueno_conNombreVacioYEmailInvalido_devuelve400ConErroresPorCampo() throws Exception {
		postJson("/api/duenos", """
				{"rut":"%s","nombre":"","telefono":"+56911112222","email":"no-es-un-email"}
				""".formatted(nuevoRut()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errores.nombre").exists())
				.andExpect(jsonPath("$.errores.email").exists())
				.andExpect(jsonPath("$.errores.rut").doesNotExist());
	}

	@Test
	void crearDueno_conRutMalFormado_devuelve400() throws Exception {
		postJson("/api/duenos", duenoJson("12.345.678", "Ana Perez"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.rut").exists());
	}

	@Test
	void crearDueno_conRutYaRegistrado_devuelve409() throws Exception {
		String rut = nuevoRut();
		postJson("/api/duenos", duenoJson(rut, "Ana Perez")).andExpect(status().isCreated());

		postJson("/api/duenos", duenoJson(rut, "Otra Persona"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.mensaje").value(containsString(rut)));
	}

	@Test
	void crearDueno_conJsonMalFormado_devuelve400() throws Exception {
		postJson("/api/duenos", "{\"rut\": ")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void obtenerDueno_porId_devuelveSusDatos() throws Exception {
		long id = crearDueno();

		get("/api/duenos/" + id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nombre").value("Ana Perez"));
	}

	@Test
	void obtenerDueno_inexistente_devuelve404() throws Exception {
		get("/api/duenos/999999")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.mensaje").value(containsString("999999")));
	}

	@Test
	void obtenerDueno_conIdNoNumerico_devuelve400() throws Exception {
		get("/api/duenos/abc").andExpect(status().isBadRequest());
	}

	@Test
	void listarDuenos_incluyeLosCreados() throws Exception {
		long id1 = crearDueno();
		long id2 = crearDueno();

		get("/api/duenos")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItems((int) id1, (int) id2)));
	}

	@Test
	void actualizarDueno_reemplazaLosDatos() throws Exception {
		long id = crearDueno();
		String rutNuevo = nuevoRut();

		putJson("/api/duenos/" + id, """
				{"rut":"%s","nombre":"Ana Maria Perez","telefono":"+56933334444","email":"ana@correo.cl"}
				""".formatted(rutNuevo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nombre").value("Ana Maria Perez"));

		get("/api/duenos/" + id)
				.andExpect(jsonPath("$.rut").value(rutNuevo))
				.andExpect(jsonPath("$.nombre").value("Ana Maria Perez"))
				.andExpect(jsonPath("$.telefono").value("+56933334444"))
				.andExpect(jsonPath("$.email").value("ana@correo.cl"));
	}

	@Test
	void actualizarDueno_conElMismoRut_estaPermitido() throws Exception {
		String rut = nuevoRut();
		long id = idDe(postJson("/api/duenos", duenoJson(rut, "Ana Perez")).andReturn());

		putJson("/api/duenos/" + id, duenoJson(rut, "Ana P."))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana P."));
	}

	@Test
	void actualizarDueno_conRutDeOtroDueno_devuelve409() throws Exception {
		String rutAjeno = nuevoRut();
		postJson("/api/duenos", duenoJson(rutAjeno, "Otro Dueno")).andExpect(status().isCreated());
		long id = crearDueno();

		putJson("/api/duenos/" + id, duenoJson(rutAjeno, "Ana Perez"))
				.andExpect(status().isConflict());
	}

	@Test
	void actualizarDueno_inexistente_devuelve404() throws Exception {
		putJson("/api/duenos/999999", duenoJson(nuevoRut(), "Nadie"))
				.andExpect(status().isNotFound());
	}

	@Test
	void eliminarDueno_sinMascotas_devuelve204YDejaDeExistir() throws Exception {
		long id = crearDueno();

		delete("/api/duenos/" + id).andExpect(status().isNoContent());
		get("/api/duenos/" + id).andExpect(status().isNotFound());
	}

	@Test
	void eliminarDueno_conMascotas_devuelve409YNoSeElimina() throws Exception {
		long id = crearDueno();
		crearMascota(id);

		delete("/api/duenos/" + id).andExpect(status().isConflict());
		get("/api/duenos/" + id).andExpect(status().isOk());
	}

	@Test
	void eliminarDueno_inexistente_devuelve404() throws Exception {
		delete("/api/duenos/999999").andExpect(status().isNotFound());
	}

	@Test
	void mascotasDeUnDueno_devuelveSoloLasSuyas() throws Exception {
		long duenoA = crearDueno();
		long duenoB = crearDueno();
		long m1 = crearMascota(duenoA);
		long m2 = crearMascota(duenoA);
		long mAjena = crearMascota(duenoB);

		get("/api/duenos/" + duenoA + "/mascotas")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder((int) m1, (int) m2)))
				.andExpect(jsonPath("$[*].id", not(hasItem((int) mAjena))));
	}

	@Test
	void mascotasDeUnDuenoSinMascotas_devuelveListaVacia() throws Exception {
		long id = crearDueno();

		get("/api/duenos/" + id + "/mascotas")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void mascotasDeUnDuenoInexistente_devuelve404() throws Exception {
		get("/api/duenos/999999/mascotas").andExpect(status().isNotFound());
	}
}

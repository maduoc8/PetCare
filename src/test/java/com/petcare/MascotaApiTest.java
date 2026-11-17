package com.petcare;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MascotaApiTest extends ApiTestBase {

	@Test
	void crearMascota_conDuenoExistente_devuelve201ConSuDueno() throws Exception {
		long duenoId = crearDueno();

		postJson("/api/mascotas", mascotaJson("Firulais", duenoId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nombre").value("Firulais"))
				.andExpect(jsonPath("$.especie").value("Perro"))
				.andExpect(jsonPath("$.raza").value("Quiltro"))
				.andExpect(jsonPath("$.fechaNacimiento").value("2021-03-15"))
				.andExpect(jsonPath("$.dueno.id").value(duenoId))
				.andExpect(jsonPath("$.dueno.nombre").value("Ana Perez"));
	}

	@Test
	void crearMascota_conDuenoInexistente_devuelve404() throws Exception {
		postJson("/api/mascotas", mascotaJson("Firulais", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").value(containsString("999999")));
	}

	@Test
	void crearMascota_sinDueno_devuelve400() throws Exception {
		postJson("/api/mascotas", """
				{"nombre":"Firulais","especie":"Perro","raza":"Quiltro","fechaNacimiento":"2021-03-15"}
				""")
				.andExpect(status().isBadRequest());
	}

	@Test
	void crearMascota_conDuenoSinId_devuelve400() throws Exception {
		postJson("/api/mascotas", """
				{"nombre":"Firulais","especie":"Perro","raza":"Quiltro","fechaNacimiento":"2021-03-15","dueno":{}}
				""")
				.andExpect(status().isBadRequest());
	}

	@Test
	void crearMascota_sinNombreNiEspecie_devuelve400ConErroresPorCampo() throws Exception {
		long duenoId = crearDueno();

		postJson("/api/mascotas", """
				{"nombre":"","raza":"Quiltro","fechaNacimiento":"2021-03-15","dueno":{"id":%d}}
				""".formatted(duenoId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.nombre").exists())
				.andExpect(jsonPath("$.errores.especie").exists());
	}

	@Test
	void crearMascota_conFechaDeNacimientoFutura_devuelve400() throws Exception {
		long duenoId = crearDueno();

		postJson("/api/mascotas", """
				{"nombre":"Firulais","especie":"Perro","raza":"Quiltro","fechaNacimiento":"2999-01-01","dueno":{"id":%d}}
				""".formatted(duenoId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.fechaNacimiento").exists());
	}

	@Test
	void obtenerMascota_porId_incluyeSuDueno() throws Exception {
		long duenoId = crearDueno();
		long id = crearMascota(duenoId);

		get("/api/mascotas/" + id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.dueno.id").value(duenoId));
	}

	@Test
	void obtenerMascota_inexistente_devuelve404() throws Exception {
		get("/api/mascotas/999999").andExpect(status().isNotFound());
	}

	@Test
	void listarMascotas_incluyeLasCreadas() throws Exception {
		long duenoId = crearDueno();
		long m1 = crearMascota(duenoId);
		long m2 = crearMascota(duenoId);

		get("/api/mascotas")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].id", hasItems((int) m1, (int) m2)));
	}

	@Test
	void actualizarMascota_puedeCambiarlaDeDueno() throws Exception {
		long duenoA = crearDueno();
		long duenoB = crearDueno();
		long id = crearMascota(duenoA);

		putJson("/api/mascotas/" + id, """
				{"nombre":"Firulais II","especie":"Perro","raza":"Labrador","fechaNacimiento":"2020-01-10","dueno":{"id":%d}}
				""".formatted(duenoB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Firulais II"))
				.andExpect(jsonPath("$.raza").value("Labrador"))
				.andExpect(jsonPath("$.dueno.id").value(duenoB));

		get("/api/duenos/" + duenoA + "/mascotas").andExpect(jsonPath("$.length()").value(0));
		get("/api/duenos/" + duenoB + "/mascotas").andExpect(jsonPath("$[*].id", contains((int) id)));
	}

	@Test
	void actualizarMascota_conDuenoInexistente_devuelve404() throws Exception {
		long id = crearMascota(crearDueno());

		putJson("/api/mascotas/" + id, mascotaJson("Firulais", 999999))
				.andExpect(status().isNotFound());
	}

	@Test
	void actualizarMascota_inexistente_devuelve404() throws Exception {
		putJson("/api/mascotas/999999", mascotaJson("Firulais", crearDueno()))
				.andExpect(status().isNotFound());
	}

	@Test
	void eliminarMascota_sinCitas_devuelve204YDejaDeExistir() throws Exception {
		long id = crearMascota(crearDueno());

		delete("/api/mascotas/" + id).andExpect(status().isNoContent());
		get("/api/mascotas/" + id).andExpect(status().isNotFound());
	}

	@Test
	void eliminarMascota_conCitas_devuelve409YNoSeElimina() throws Exception {
		long id = crearMascota(crearDueno());
		crearCita(id);

		delete("/api/mascotas/" + id).andExpect(status().isConflict());
		get("/api/mascotas/" + id).andExpect(status().isOk());
	}

	@Test
	void eliminarMascota_inexistente_devuelve404() throws Exception {
		delete("/api/mascotas/999999").andExpect(status().isNotFound());
	}

	@Test
	void citasDeUnaMascota_devuelveSoloLasSuyas() throws Exception {
		long duenoId = crearDueno();
		long mascota = crearMascota(duenoId);
		long otraMascota = crearMascota(duenoId);
		long c1 = crearCita(mascota);
		long c2 = crearCita(mascota);
		long citaAjena = crearCita(otraMascota);

		get("/api/mascotas/" + mascota + "/citas")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder((int) c1, (int) c2)))
				.andExpect(jsonPath("$[*].id", not(hasItem((int) citaAjena))));
	}

	@Test
	void citasDeUnaMascotaInexistente_devuelve404() throws Exception {
		get("/api/mascotas/999999/citas").andExpect(status().isNotFound());
	}
}

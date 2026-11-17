package com.petcare;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {

	private static final AtomicInteger SECUENCIA_RUT = new AtomicInteger(10_000_000);

	@Autowired
	protected MockMvc mvc;

	protected String nuevoRut() {
		return SECUENCIA_RUT.incrementAndGet() + "-K";
	}

	protected ResultActions postJson(String url, String json) throws Exception {
		return mvc.perform(post(url).contentType(APPLICATION_JSON).content(json));
	}

	protected ResultActions putJson(String url, String json) throws Exception {
		return mvc.perform(put(url).contentType(APPLICATION_JSON).content(json));
	}

	protected ResultActions get(String url) throws Exception {
		return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url));
	}

	protected ResultActions delete(String url) throws Exception {
		return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url));
	}

	protected static String duenoJson(String rut, String nombre) {
		return """
				{"rut":"%s","nombre":"%s","telefono":"+56911112222","email":"cliente@correo.cl"}
				""".formatted(rut, nombre);
	}

	protected static String mascotaJson(String nombre, long duenoId) {
		return """
				{"nombre":"%s","especie":"Perro","raza":"Quiltro","fechaNacimiento":"2021-03-15","dueno":{"id":%d}}
				""".formatted(nombre, duenoId);
	}

	protected static String citaJson(long mascotaId) {
		return """
				{"fecha":"2026-11-20T10:30:00","motivo":"Control anual","diagnostico":"","costo":25000,"mascota":{"id":%d}}
				""".formatted(mascotaId);
	}

	protected long crearDueno() throws Exception {
		return idDe(postJson("/api/duenos", duenoJson(nuevoRut(), "Ana Perez"))
				.andExpect(status().isCreated()).andReturn());
	}

	protected long crearMascota(long duenoId) throws Exception {
		return idDe(postJson("/api/mascotas", mascotaJson("Firulais", duenoId))
				.andExpect(status().isCreated()).andReturn());
	}

	protected long crearCita(long mascotaId) throws Exception {
		return idDe(postJson("/api/citas", citaJson(mascotaId))
				.andExpect(status().isCreated()).andReturn());
	}

	protected static long idDe(MvcResult resultado) throws Exception {
		Number id = JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
		return id.longValue();
	}

	protected static int cantidadEn(MvcResult resultado) throws Exception {
		return JsonPath.<Integer>read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.length()");
	}
}

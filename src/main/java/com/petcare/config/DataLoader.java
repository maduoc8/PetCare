package com.petcare.config;

import com.petcare.entity.Cita;
import com.petcare.entity.Dueno;
import com.petcare.entity.Mascota;
import com.petcare.repository.CitaRepository;
import com.petcare.repository.DuenoRepository;
import com.petcare.repository.MascotaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Configuration
@Profile("!test")
public class DataLoader {

	@Bean
	CommandLineRunner cargarDatosDeEjemplo(DuenoRepository duenos, MascotaRepository mascotas, CitaRepository citas) {
		return args -> {
			if (duenos.count() > 0) {
				return;
			}
			Dueno camila = duenos.save(new Dueno(null, "12345678-5", "Camila Rojas", "+56912345678", "camila.rojas@correo.cl", new ArrayList<>()));
			Dueno jorge = duenos.save(new Dueno(null, "9876543-3", "Jorge Muñoz", "+56987654321", "jorge.munoz@correo.cl", new ArrayList<>()));

			Mascota luna = mascotas.save(new Mascota(null, "Luna", "Perro", "Golden Retriever", LocalDate.of(2020, 4, 12), camila, new ArrayList<>()));
			Mascota michi = mascotas.save(new Mascota(null, "Michi", "Gato", "Siamés", LocalDate.of(2022, 8, 1), camila, new ArrayList<>()));
			Mascota rocky = mascotas.save(new Mascota(null, "Rocky", "Perro", "Bulldog Francés", LocalDate.of(2019, 11, 23), jorge, new ArrayList<>()));

			citas.save(new Cita(null, LocalDateTime.of(2026, 10, 5, 10, 0), "Vacuna antirrábica", "Vacuna aplicada sin reacciones", 18000, luna));
			citas.save(new Cita(null, LocalDateTime.of(2026, 11, 2, 16, 30), "Control por vómitos", "Gastritis leve, dieta blanda por 5 días", 25000, michi));
			citas.save(new Cita(null, LocalDateTime.of(2026, 11, 25, 9, 0), "Control dermatológico", null, 30000, rocky));
		};
	}
}

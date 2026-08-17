package com.farmacia.sistemaWeb;

import com.farmacia.sistemaWeb.entity.Rol;
import com.farmacia.sistemaWeb.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@org.springframework.scheduling.annotation.EnableScheduling
public class FarmaciaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmaciaProjectApplication.class, args);
	}

	@Bean
	CommandLineRunner cargarRolesIniciales(RolRepository rolRepository) {
		return args -> {
			if (rolRepository.findByNombre(Rol.NombreRol.ADMIN).isEmpty()) {
				Rol rolAdmin = new Rol();
				rolAdmin.setNombre(Rol.NombreRol.ADMIN);
				rolRepository.save(rolAdmin);
			}

			if (rolRepository.findByNombre(Rol.NombreRol.RECEPCIONISTA).isEmpty()) {
				Rol rolRecepcionista = new Rol();
				rolRecepcionista.setNombre(Rol.NombreRol.RECEPCIONISTA);
				rolRepository.save(rolRecepcionista);
			}

			if (rolRepository.findByNombre(Rol.NombreRol.VETERINARIO).isEmpty()) {
				Rol rolVeterinario = new Rol();
				rolVeterinario.setNombre(Rol.NombreRol.VETERINARIO);
				rolRepository.save(rolVeterinario);
			}
		};
	}
}

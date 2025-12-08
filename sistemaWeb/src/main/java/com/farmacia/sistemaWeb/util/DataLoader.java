package com.farmacia.sistemaWeb.util;

import com.farmacia.sistemaWeb.entity.*;
import com.farmacia.sistemaWeb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            // Crear roles
            Rol adminRol = new Rol();
            adminRol.setNombre(Rol.NombreRol.ADMIN);
            rolRepository.save(adminRol);

            Rol vendedorRol = new Rol();
            vendedorRol.setNombre(Rol.NombreRol.VENDEDOR);
            rolRepository.save(vendedorRol);

            // Crear usuario administrador
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombres("Administrador Principal");
            Set<Rol> roles = new HashSet<>();
            roles.add(adminRol);
            admin.setRoles(roles);
            usuarioRepository.save(admin);

            // Crear usuario vendedor
            Usuario vendedor = new Usuario();
            vendedor.setUsername("recep");
            vendedor.setPassword(passwordEncoder.encode("recep123"));
            vendedor.setNombres("Vendedor de Turno");
            Set<Rol> rolesVendedor = new HashSet<>();
            rolesVendedor.add(vendedorRol);
            vendedor.setRoles(rolesVendedor);
            usuarioRepository.save(vendedor);

            Usuario vendedor2 = new Usuario();
            vendedor2.setUsername("vendedor2");
            vendedor2.setPassword(passwordEncoder.encode("vendedor123"));
            vendedor2.setNombres("Vendedor Nocturno");
            Set<Rol> rolesVendedor2 = new HashSet<>();
            rolesVendedor2.add(vendedorRol);
            vendedor2.setRoles(rolesVendedor2);
            usuarioRepository.save(vendedor2);

            // Crear especialidades
            Especialidad cardiologia = new Especialidad();
            cardiologia.setNombre("Cardiología");
            especialidadRepository.save(cardiologia);

            Especialidad dermatologia = new Especialidad();
            dermatologia.setNombre("Dermatología");
            especialidadRepository.save(dermatologia);
            
            Especialidad cirugia = new Especialidad();
            cirugia.setNombre("Cirugía");
            especialidadRepository.save(cirugia);

            Especialidad oncologia = new Especialidad();
            oncologia.setNombre("Oncología");
            especialidadRepository.save(oncologia);

            Especialidad oftalmologia = new Especialidad();
            oftalmologia.setNombre("Oftalmología");
            especialidadRepository.save(oftalmologia);

            // Crear veterinarios
            Veterinario vet1 = new Veterinario();
            vet1.setNombres("Dr. Juan Pérez");
            vet1.setCmp("12345");
            vet1.setEspecialidad(cardiologia);
            veterinarioRepository.save(vet1);

            Veterinario vet2 = new Veterinario();
            vet2.setNombres("Dra. Ana Gómez");
            vet2.setCmp("54321");
            vet2.setEspecialidad(dermatologia);
            veterinarioRepository.save(vet2);

            Veterinario vet3 = new Veterinario();
            vet3.setNombres("Dr. Carlos Ruiz");
            vet3.setCmp("98765");
            vet3.setEspecialidad(cirugia);
            veterinarioRepository.save(vet3);

            Veterinario vet4 = new Veterinario();
            vet4.setNombres("Dra. Lucia Fernandez");
            vet4.setCmp("123456");
            vet4.setEspecialidad(oncologia);
            veterinarioRepository.save(vet4);

            Veterinario vet5 = new Veterinario();
            vet5.setNombres("Dr. Mario Panta");
            vet5.setCmp("1234567");
            vet5.setEspecialidad(oftalmologia);
            veterinarioRepository.save(vet5);

            // Crear clientes
            Cliente cliente1 = new Cliente();
            cliente1.setNombres("Carlos");
            cliente1.setApellidos("Sanchez");
            cliente1.setDni("12345678");
            cliente1.setTelefono("987654321");
            cliente1.setDireccion("Av. Siempre Viva 123");
            clienteRepository.save(cliente1);

            Cliente cliente2 = new Cliente();
            cliente2.setNombres("Maria");
            cliente2.setApellidos("Lopez");
            cliente2.setDni("87654321");
            cliente2.setTelefono("123456789");
            cliente2.setDireccion("Calle Falsa 456");
            clienteRepository.save(cliente2);

            Cliente cliente3 = new Cliente();
            cliente3.setNombres("Pedro");
            cliente3.setApellidos("Gomez");
            cliente3.setDni("11223344");
            cliente3.setTelefono("998877665");
            cliente3.setDireccion("Jr. Los Pinos 789");
            clienteRepository.save(cliente3);

            Cliente cliente4 = new Cliente();
            cliente4.setNombres("Ana");
            cliente4.setApellidos("Martinez");
            cliente4.setDni("44332211");
            cliente4.setTelefono("9876543210");
            cliente4.setDireccion("Av. Las Palmeras 456");
            clienteRepository.save(cliente4);

            // Crear mascotas
            Mascota mascota1 = new Mascota();
            mascota1.setNombre("Fido");
            mascota1.setEspecie("Perro");
            mascota1.setRaza("Labrador");
            mascota1.setEdad(5);
            mascota1.setCliente(cliente1);
            mascotaRepository.save(mascota1);

            Mascota mascota2 = new Mascota();
            mascota2.setNombre("Mishu");
            mascota2.setEspecie("Gato");
            mascota2.setRaza("Siames");
            mascota2.setEdad(2);
            mascota2.setCliente(cliente2);
            mascotaRepository.save(mascota2);
            
            Mascota mascota3 = new Mascota();
            mascota3.setNombre("Max");
            mascota3.setEspecie("Perro");
            mascota3.setRaza("Golden Retriever");
            mascota3.setEdad(3);
            mascota3.setCliente(cliente1);
            mascotaRepository.save(mascota3);

            Mascota mascota4 = new Mascota();
            mascota4.setNombre("Luna");
            mascota4.setEspecie("Gato");
            mascota4.setRaza("Angora");
            mascota4.setEdad(4);
            mascota4.setCliente(cliente3);
            mascotaRepository.save(mascota4);

            Mascota mascota5 = new Mascota();
            mascota5.setNombre("Rocky");
            mascota5.setEspecie("Perro");
            mascota5.setRaza("Bulldog");
            mascota5.setEdad(6);
            mascota5.setCliente(cliente4);
            mascotaRepository.save(mascota5);

            // Crear medicamentos
            Medicamento med1 = new Medicamento();
            med1.setNombre("Antipulgas");
            med1.setDescripcion("Tratamiento contra pulgas y garrapatas");
            med1.setStock(100);
            med1.setPrecio(15.50);
            medicamentoRepository.save(med1);

            Medicamento med2 = new Medicamento();
            med2.setNombre("Desparasitante");
            med2.setDescripcion("Para parásitos internos");
            med2.setStock(50);
            med2.setPrecio(10.00);
            medicamentoRepository.save(med2);

            Medicamento med3 = new Medicamento();
            med3.setNombre("Antibiótico");
            med3.setDescripcion("Para infecciones bacterianas");
            med3.setStock(75);
            med3.setPrecio(25.00);
            medicamentoRepository.save(med3);
            
            Medicamento med4 = new Medicamento();
            med4.setNombre("Analgésico");
            med4.setDescripcion("Para el dolor");
            med4.setStock(120);
            med4.setPrecio(8.50);
            medicamentoRepository.save(med4);

            Medicamento med5 = new Medicamento();
            med5.setNombre("Antiinflamatorio");
            med5.setDescripcion("Para inflamaciones");
            med5.setStock(80);
            med5.setPrecio(12.00);
            medicamentoRepository.save(med5);
        }
    }
}

package com.farmacia.sistemaWeb.config;

import com.farmacia.sistemaWeb.entity.Categoria;
import com.farmacia.sistemaWeb.entity.Producto;
import com.farmacia.sistemaWeb.repository.CategoriaRepository;
import com.farmacia.sistemaWeb.repository.ProductoRepository;
import com.farmacia.sistemaWeb.repository.ProveedorRepository;
import com.farmacia.sistemaWeb.entity.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCategoriasYProductos();
    }

    private void seedCategoriasYProductos() {
        if (categoriaRepository.count() == 0) {
            Categoria catInsumos = new Categoria();
            catInsumos.setNombre("Insumos Médicos");
            catInsumos.setDescripcion("Materiales usados en consultas y cirugías");
            
            Categoria catMedicamentos = new Categoria();
            catMedicamentos.setNombre("Medicamentos");
            catMedicamentos.setDescripcion("Fármacos y tratamientos médicos");

            Categoria catAlimentos = new Categoria();
            catAlimentos.setNombre("Alimentos y Dietas");
            catAlimentos.setDescripcion("Alimento balanceado y dietas prescritas");

            Categoria catAccesorios = new Categoria();
            catAccesorios.setNombre("Accesorios");
            catAccesorios.setDescripcion("Correas, juguetes, collares");

            categoriaRepository.saveAll(Arrays.asList(catInsumos, catMedicamentos, catAlimentos, catAccesorios));
            
            System.out.println("✅ Seeder: Categorías creadas exitosamente.");
        }

        if (proveedorRepository.count() == 0) {
            Proveedor proveedor = new Proveedor();
            proveedor.setRuc("20123456789");
            proveedor.setRazonSocial("Proveedor General");
            proveedor.setTelefono("987654321");
            proveedor.setDireccion("Av. Principal 123");
            proveedorRepository.save(proveedor);
            System.out.println("✅ Seeder: Proveedor General creado.");
        }

        if (productoRepository.count() == 0) {
            Categoria catInsumos = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Insumos Médicos")).findFirst().orElse(null);
            Categoria catMedicamentos = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Medicamentos")).findFirst().orElse(null);
            Categoria catAlimentos = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Alimentos y Dietas")).findFirst().orElse(null);

            if (catInsumos != null && catMedicamentos != null && catAlimentos != null) {
                List<Producto> productos = Arrays.asList(
                        crearProducto("INS-001", "Jeringa 3ml con aguja", "Jeringa desechable estéril", new BigDecimal("0.50"), new BigDecimal("1.00"), 500, 50, Producto.TipoProducto.INSUMO_MEDICO, catInsumos),
                        crearProducto("INS-002", "Gasa estéril 10x10", "Paquete de gasas estériles", new BigDecimal("0.20"), new BigDecimal("0.50"), 1000, 100, Producto.TipoProducto.INSUMO_MEDICO, catInsumos),
                        crearProducto("INS-003", "Guantes de látex (Caja)", "Guantes descartables talla M", new BigDecimal("5.00"), new BigDecimal("8.00"), 50, 10, Producto.TipoProducto.INSUMO_MEDICO, catInsumos),
                        
                        crearProducto("MED-001", "Meloxicam 1mg", "Antiinflamatorio no esteroideo", new BigDecimal("1.50"), new BigDecimal("3.00"), 200, 20, Producto.TipoProducto.VENTA_PUBLICO, catMedicamentos),
                        crearProducto("MED-002", "Amoxicilina + Ácido Clavulánico", "Antibiótico de amplio espectro", new BigDecimal("2.50"), new BigDecimal("5.00"), 150, 15, Producto.TipoProducto.VENTA_PUBLICO, catMedicamentos),
                        crearProducto("MED-003", "Vacuna Antirrábica", "Vacuna anual contra la rabia", new BigDecimal("8.00"), new BigDecimal("15.00"), 100, 10, Producto.TipoProducto.INSUMO_MEDICO, catMedicamentos),
                        
                        crearProducto("ALI-001", "ProPlan Perro Adulto 3kg", "Alimento premium para perro adulto", new BigDecimal("25.00"), new BigDecimal("35.00"), 30, 5, Producto.TipoProducto.VENTA_PUBLICO, catAlimentos),
                        crearProducto("ALI-002", "Royal Canin Gato Esterilizado 1.5kg", "Alimento para gatos esterilizados", new BigDecimal("18.00"), new BigDecimal("26.00"), 40, 5, Producto.TipoProducto.VENTA_PUBLICO, catAlimentos)
                );
                
                productoRepository.saveAll(productos);
                System.out.println("✅ Seeder: Productos e Insumos creados exitosamente.");
            }
        }
    }

    private Producto crearProducto(String codigo, String nombre, String descripcion, BigDecimal precioCompra, BigDecimal precioVenta, int stock, int stockMinimo, Producto.TipoProducto tipo, Categoria categoria) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioCompra(precioCompra);
        p.setPrecioVenta(precioVenta);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setTipo(tipo);
        p.setCategoria(categoria);
        p.setActivo(true);
        return p;
    }
}

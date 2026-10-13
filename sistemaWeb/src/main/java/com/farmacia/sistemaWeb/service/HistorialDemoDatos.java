package com.farmacia.sistemaWeb.service;

import com.farmacia.sistemaWeb.entity.ClaseProducto;
import com.farmacia.sistemaWeb.entity.UnidadBase;

import java.util.List;

/**
 * Datos fijos del generador de historial de demostración ({@link HistorialDemoService}):
 * catálogo, proveedores, personal, nombres, razas y casos clínicos. Solo datos, sin lógica.
 */
final class HistorialDemoDatos {

    private HistorialDemoDatos() {
    }

    /** Prefijo de los códigos de producto creados por el generador. El primero sirve de marca de "ya generado". */
    static final String PREFIJO = "PZ-";
    static final String CODIGO_MARCA = "PZ-MED-001";

    // ─────────────────────────── Catálogo ───────────────────────────

    /** Presentación: nombre, contenido en unidades base, precio de venta y si se vende / se compra. */
    record Pres(String nombre, int factor, String precio, boolean vendible, boolean comprable) {
    }

    /**
     * Producto del catálogo de demostración.
     * @param compraIdx   presentación con la que se compra (índice en pres)
     * @param costoCompra costo de esa presentación
     * @param objetivo    cantidad (en presentaciones de compra) a la que se repone
     * @param proveedor   índice en PROVEEDORES
     * @param vencMin     meses mínimos de vencimiento de los lotes (0 = sin vencimiento)
     * @param vencMax     meses máximos
     * @param popularidad peso en las ventas de mostrador (0 = no se vende en caja)
     * @param reponer     false = no se repone en el mes (termina bajo el stock mínimo)
     */
    record Prod(String codigo, String nombre, ClaseProducto clase, UnidadBase unidad, String categoria, String descripcion,
                int stockMinimo, List<Pres> pres, int compraIdx, String costoCompra, int objetivo, int proveedor,
                int vencMin, int vencMax, int popularidad, boolean reponer) {
    }

    static final String CAT_MED = "Medicamentos";
    static final String CAT_VAC = "Vacunas y Biológicos";
    static final String CAT_INS = "Insumos Médicos";
    static final String CAT_ALI = "Alimentos y Dietas";
    static final String CAT_ACC = "Accesorios";
    static final String CAT_HIG = "Higiene y Estética";
    static final String CAT_SER = "Servicios";

    static final String[][] CATEGORIAS = {
            {CAT_MED, "Fármacos y tratamientos médicos"},
            {CAT_VAC, "Vacunas y productos biológicos (cadena de frío)"},
            {CAT_INS, "Materiales usados en consultas y cirugías"},
            {CAT_ALI, "Alimento balanceado y dietas prescritas"},
            {CAT_ACC, "Correas, juguetes, collares"},
            {CAT_HIG, "Shampoo, arena, cepillos e insumos de baño y corte"},
            {CAT_SER, "Servicios clínicos y de estética"},
    };

    private static Pres p(String nombre, int factor, String precio) {
        return new Pres(nombre, factor, precio, true, true);
    }

    private static Pres soloVenta(String nombre, int factor, String precio) {
        return new Pres(nombre, factor, precio, true, false);
    }

    private static Pres soloCompra(String nombre, int factor, String precio) {
        return new Pres(nombre, factor, precio, false, true);
    }

    static final List<Prod> PRODUCTOS = List.of(
            // ── Farmacia: medicamentos ──
            new Prod("PZ-MED-001", "Amoxicilina 250 mg tabletas", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.TABLETA, CAT_MED,
                    "Antibiótico betalactámico de amplio espectro", 60,
                    List.of(p("Tableta", 1, "1.50"), p("Blíster x10", 10, "13.00"), soloCompra("Caja x100", 100, "110.00")),
                    2, "45.00", 3, 0, 14, 24, 5, true),
            new Prod("PZ-MED-002", "Meloxicam 1 mg tabletas", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.TABLETA, CAT_MED,
                    "Antiinflamatorio no esteroideo (AINE)", 50,
                    List.of(p("Tableta", 1, "1.20"), p("Blíster x10", 10, "10.00"), soloCompra("Caja x100", 100, "90.00")),
                    2, "38.00", 3, 0, 14, 24, 4, true),
            new Prod("PZ-MED-003", "Prednisolona 5 mg tabletas", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.TABLETA, CAT_MED,
                    "Corticoide de acción intermedia", 40,
                    List.of(p("Tableta", 1, "0.90"), p("Blíster x10", 10, "8.00"), soloCompra("Caja x100", 100, "70.00")),
                    2, "30.00", 2, 0, 14, 24, 3, true),
            new Prod("PZ-MED-004", "Metronidazol 250 mg tabletas", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.TABLETA, CAT_MED,
                    "Antibacteriano y antiprotozoario", 40,
                    List.of(p("Tableta", 1, "1.00"), p("Blíster x10", 10, "9.00"), soloCompra("Caja x100", 100, "80.00")),
                    2, "32.00", 2, 0, 14, 24, 3, true),
            new Prod("PZ-MED-005", "Enrofloxacino 50 mg tabletas", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.TABLETA, CAT_MED,
                    "Antibiótico fluoroquinolona", 40,
                    List.of(p("Tableta", 1, "1.80"), p("Blíster x10", 10, "16.00"), soloCompra("Caja x100", 100, "150.00")),
                    2, "60.00", 2, 0, 14, 24, 2, true),
            new Prod("PZ-MED-006", "Antiparasitario interno perro (praziquantel/pirantel) tableta", ClaseProducto.MEDICAMENTO_LIBRE, UnidadBase.TABLETA, CAT_MED,
                    "Desparasitante de amplio espectro, 1 tableta por cada 10 kg", 30,
                    List.of(p("Tableta", 1, "8.00"), soloCompra("Caja x50", 50, "350.00")),
                    1, "150.00", 2, 0, 18, 30, 8, true),
            new Prod("PZ-MED-007", "Pipeta antipulgas perro 10-20 kg", ClaseProducto.MEDICAMENTO_LIBRE, UnidadBase.PIPETA, CAT_MED,
                    "Pipeta spot-on contra pulgas y garrapatas, protección 30 días", 6,
                    List.of(p("Pipeta", 1, "38.00"), p("Caja x3", 3, "105.00")),
                    1, "60.00", 12, 0, 18, 30, 7, true),
            new Prod("PZ-MED-008", "Pipeta antipulgas gato hasta 4 kg", ClaseProducto.MEDICAMENTO_LIBRE, UnidadBase.PIPETA, CAT_MED,
                    "Pipeta spot-on para gatos, protección 30 días", 6,
                    List.of(p("Pipeta", 1, "32.00"), p("Caja x3", 3, "90.00")),
                    1, "51.00", 8, 0, 18, 30, 5, false),
            new Prod("PZ-MED-009", "Antipulgas masticable perro 10-25 kg", ClaseProducto.MEDICAMENTO_LIBRE, UnidadBase.TABLETA, CAT_MED,
                    "Tableta masticable contra pulgas y garrapatas, protección 12 semanas", 4,
                    List.of(p("Tableta", 1, "95.00"), soloCompra("Display x10", 10, "950.00")),
                    1, "620.00", 2, 0, 18, 30, 3, true),
            new Prod("PZ-MED-010", "Suplemento vitamínico jarabe 120 ml", ClaseProducto.MEDICAMENTO_LIBRE, UnidadBase.FRASCO, CAT_MED,
                    "Complejo vitamínico con hierro para cachorros y convalecientes", 4,
                    List.of(p("Frasco", 1, "28.00"), soloCompra("Caja x12", 12, "336.00")),
                    1, "168.00", 1, 0, 12, 18, 2, true),
            new Prod("PZ-MED-011", "Ketamina 50 mg/ml frasco 10 ml", ClaseProducto.MEDICAMENTO_CONTROLADO, UnidadBase.ML, CAT_MED,
                    "Anestésico disociativo. Uso exclusivo en clínica (controlado)", 20,
                    List.of(new Pres("ml", 1, "6.00", false, false), new Pres("Frasco 10 ml", 10, "60.00", false, true)),
                    1, "45.00", 6, 0, 18, 24, 0, true),
            new Prod("PZ-MED-012", "Tramadol 50 mg/ml ampolla 2 ml", ClaseProducto.MEDICAMENTO_CONTROLADO, UnidadBase.AMPOLLA, CAT_MED,
                    "Analgésico opioide. Uso exclusivo en clínica (controlado)", 5,
                    List.of(new Pres("Ampolla", 1, "12.00", false, false), new Pres("Caja x10", 10, "120.00", false, true)),
                    1, "50.00", 2, 0, 18, 24, 0, true),
            new Prod("PZ-MED-013", "Meloxicam inyectable 0.5% frasco 20 ml", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.ML, CAT_MED,
                    "AINE inyectable para dolor e inflamación. Uso en consulta", 20,
                    List.of(new Pres("ml", 1, "3.00", false, false), new Pres("Frasco 20 ml", 20, "60.00", false, true)),
                    1, "38.00", 5, 0, 14, 24, 0, true),
            new Prod("PZ-MED-014", "Gotas oftálmicas antibióticas 5 ml", ClaseProducto.MEDICAMENTO_RECETA, UnidadBase.FRASCO, CAT_MED,
                    "Tobramicina colirio para conjuntivitis bacteriana", 3,
                    List.of(p("Frasco", 1, "24.00"), soloCompra("Caja x6", 6, "144.00")),
                    1, "66.00", 2, 0, 12, 18, 2, true),

            // ── Farmacia: vacunas ──
            new Prod("PZ-VAC-001", "Vacuna séxtuple canina", ClaseProducto.VACUNA, UnidadBase.DOSIS, CAT_VAC,
                    "Distemper, hepatitis, adenovirus, parainfluenza, parvovirus y leptospira", 10,
                    List.of(new Pres("Dosis", 1, "75.00", true, false), soloCompra("Caja x10 dosis", 10, "750.00")),
                    1, "220.00", 4, 1, 8, 14, 0, true),
            new Prod("PZ-VAC-002", "Vacuna antirrábica canina y felina", ClaseProducto.VACUNA, UnidadBase.DOSIS, CAT_VAC,
                    "Virus rábico inactivado, refuerzo anual", 10,
                    List.of(new Pres("Dosis", 1, "45.00", true, false), soloCompra("Caja x10 dosis", 10, "450.00")),
                    1, "120.00", 3, 1, 8, 14, 0, true),
            new Prod("PZ-VAC-003", "Vacuna triple felina", ClaseProducto.VACUNA, UnidadBase.DOSIS, CAT_VAC,
                    "Panleucopenia, rinotraqueítis y calicivirus felino", 6,
                    List.of(new Pres("Dosis", 1, "80.00", true, false), soloCompra("Caja x10 dosis", 10, "800.00")),
                    1, "260.00", 2, 1, 8, 14, 0, true),
            new Prod("PZ-VAC-004", "Vacuna KC tos de las perreras", ClaseProducto.VACUNA, UnidadBase.DOSIS, CAT_VAC,
                    "Bordetella bronchiseptica y parainfluenza, aplicación intranasal", 4,
                    List.of(new Pres("Dosis", 1, "70.00", true, false), soloCompra("Caja x10 dosis", 10, "700.00")),
                    1, "250.00", 1, 1, 8, 14, 0, true),

            // ── Farmacia: insumos clínicos ──
            new Prod("PZ-INS-001", "Jeringa 3 ml con aguja 21G", ClaseProducto.INSUMO_CLINICO, UnidadBase.UNIDAD, CAT_INS,
                    "Jeringa descartable estéril", 60,
                    List.of(p("Unidad", 1, "1.00"), soloCompra("Caja x100", 100, "100.00")),
                    1, "25.00", 4, 3, 30, 48, 0, true),
            new Prod("PZ-INS-002", "Gasa estéril 10x10 cm", ClaseProducto.INSUMO_CLINICO, UnidadBase.UNIDAD, CAT_INS,
                    "Sobre de gasa estéril de algodón", 80,
                    List.of(p("Unidad", 1, "0.80"), soloCompra("Paquete x100", 100, "80.00")),
                    1, "30.00", 4, 3, 30, 48, 0, true),
            new Prod("PZ-INS-003", "Guantes de examen nitrilo talla M", ClaseProducto.INSUMO_CLINICO, UnidadBase.UNIDAD, CAT_INS,
                    "Guante descartable sin polvo", 80,
                    List.of(p("Unidad", 1, "0.80"), soloCompra("Caja x100", 100, "80.00")),
                    1, "28.00", 5, 3, 0, 0, 0, true),
            new Prod("PZ-INS-004", "Sutura absorbible 3-0 con aguja", ClaseProducto.INSUMO_CLINICO, UnidadBase.UNIDAD, CAT_INS,
                    "Ácido poliglicólico 75 cm, aguja curva", 6,
                    List.of(p("Unidad", 1, "18.00"), soloCompra("Caja x12", 12, "216.00")),
                    1, "120.00", 2, 3, 24, 36, 0, true),
            new Prod("PZ-INS-005", "Catéter endovenoso 22G", ClaseProducto.INSUMO_CLINICO, UnidadBase.UNIDAD, CAT_INS,
                    "Catéter periférico con válvula", 10,
                    List.of(p("Unidad", 1, "6.00"), soloCompra("Caja x50", 50, "300.00")),
                    1, "90.00", 1, 3, 30, 48, 0, true),
            new Prod("PZ-INS-006", "Suero fisiológico 0.9% 1 L", ClaseProducto.INSUMO_CLINICO, UnidadBase.FRASCO, CAT_INS,
                    "Cloruro de sodio 0.9% para fluidoterapia", 8,
                    List.of(p("Frasco", 1, "12.00"), soloCompra("Caja x12", 12, "144.00")),
                    1, "60.00", 3, 3, 18, 24, 0, true),

            // ── Tienda: alimentos ──
            new Prod("PZ-ALI-001", "Croqueta adulto raza mediana 15 kg", ClaseProducto.ALIMENTO, UnidadBase.GRAMO, CAT_ALI,
                    "Alimento balanceado para perro adulto. Se vende por kilo o en bolsa cerrada", 15000,
                    List.of(new Pres("Gramo", 1, "0.02", false, false), soloVenta("Kilo", 1000, "16.00"), p("Bolsa 15 kg", 15000, "199.00")),
                    2, "135.00", 8, 2, 8, 12, 14, true),
            new Prod("PZ-ALI-002", "Croqueta gato adulto 10 kg", ClaseProducto.ALIMENTO, UnidadBase.GRAMO, CAT_ALI,
                    "Alimento balanceado para gato adulto. Se vende por kilo o en bolsa cerrada", 10000,
                    List.of(new Pres("Gramo", 1, "0.02", false, false), soloVenta("Kilo", 1000, "19.00"), p("Bolsa 10 kg", 10000, "165.00")),
                    2, "112.00", 6, 2, 8, 12, 10, true),
            new Prod("PZ-ALI-003", "Croqueta cachorro raza pequeña 3 kg", ClaseProducto.ALIMENTO, UnidadBase.UNIDAD, CAT_ALI,
                    "Alimento para cachorros de razas pequeñas", 4,
                    List.of(p("Bolsa 3 kg", 1, "72.00")),
                    0, "48.00", 10, 2, 8, 12, 5, true),
            new Prod("PZ-ALI-004", "Alimento húmedo gato pouch 85 g", ClaseProducto.ALIMENTO, UnidadBase.UNIDAD, CAT_ALI,
                    "Trocitos en salsa, sabor pollo", 24,
                    List.of(p("Pouch", 1, "4.50"), p("Caja x24", 24, "99.00")),
                    1, "62.00", 5, 2, 6, 10, 9, true),
            new Prod("PZ-ALI-005", "Snack dental perro mediano x7", ClaseProducto.ALIMENTO, UnidadBase.UNIDAD, CAT_ALI,
                    "Barras dentales, ayudan a reducir el sarro", 6,
                    List.of(p("Paquete x7", 1, "22.00")),
                    0, "13.00", 18, 2, 8, 12, 6, true),
            new Prod("PZ-ALI-006", "Dieta veterinaria renal gato 2 kg", ClaseProducto.ALIMENTO, UnidadBase.UNIDAD, CAT_ALI,
                    "Dieta de prescripción para enfermedad renal crónica felina", 3,
                    List.of(p("Bolsa 2 kg", 1, "125.00")),
                    0, "84.00", 6, 2, 8, 12, 1, true),
            new Prod("PZ-ALI-007", "Galletas premio perro 500 g", ClaseProducto.ALIMENTO, UnidadBase.UNIDAD, CAT_ALI,
                    "Galletas horneadas sabor carne", 6,
                    List.of(p("Bolsa 500 g", 1, "14.00")),
                    0, "8.00", 24, 2, 6, 10, 6, false),

            // ── Tienda: accesorios ──
            new Prod("PZ-ACC-001", "Collar nylon ajustable talla M", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Collar con hebilla de seguridad", 4, List.of(p("Unidad", 1, "18.00")), 0, "7.00", 15, 4, 0, 0, 4, true),
            new Prod("PZ-ACC-002", "Correa retráctil 5 m", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Correa extensible para perros hasta 20 kg", 3, List.of(p("Unidad", 1, "45.00")), 0, "22.00", 11, 4, 0, 0, 3, false),
            new Prod("PZ-ACC-003", "Juguete pelota de caucho", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Pelota resistente con sonido", 6, List.of(p("Unidad", 1, "12.00")), 0, "4.50", 24, 4, 0, 0, 5, true),
            new Prod("PZ-ACC-004", "Rascador para gato 60 cm", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Poste de sisal con base alfombrada", 2, List.of(p("Unidad", 1, "89.00")), 0, "48.00", 4, 4, 0, 0, 1, true),
            new Prod("PZ-ACC-005", "Plato de acero inoxidable 750 ml", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Plato antideslizante", 4, List.of(p("Unidad", 1, "16.00")), 0, "6.50", 12, 4, 0, 0, 3, true),
            new Prod("PZ-ACC-006", "Cama acolchada mediana", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Cama lavable 60 x 45 cm", 2, List.of(p("Unidad", 1, "95.00")), 0, "52.00", 4, 4, 0, 0, 1, true),
            new Prod("PZ-ACC-007", "Collar isabelino talla M", ClaseProducto.ACCESORIO, UnidadBase.UNIDAD, CAT_ACC,
                    "Collar de protección post-operatoria", 4, List.of(p("Unidad", 1, "20.00")), 0, "8.00", 12, 4, 0, 0, 1, true),

            // ── Tienda: higiene ──
            new Prod("PZ-HIG-001", "Shampoo hipoalergénico 250 ml", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Shampoo con avena para piel sensible", 4, List.of(p("Frasco 250 ml", 1, "32.00")), 0, "17.00", 14, 5, 18, 30, 4, true),
            new Prod("PZ-HIG-002", "Arena para gatos aglomerante 10 kg", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Arena bentonita con control de olor", 4, List.of(p("Bolsa 10 kg", 1, "42.00")), 0, "26.00", 16, 5, 0, 0, 6, true),
            new Prod("PZ-HIG-003", "Limpiador ótico 120 ml", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Solución para limpieza de oídos de perros y gatos", 3, List.of(p("Frasco 120 ml", 1, "35.00")), 0, "19.00", 8, 5, 18, 30, 2, true),
            new Prod("PZ-HIG-004", "Toallitas húmedas para mascotas x80", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Toallitas sin alcohol con aloe vera", 4, List.of(p("Paquete x80", 1, "15.00")), 0, "7.00", 14, 5, 0, 0, 3, true),
            new Prod("PZ-HIG-005", "Cepillo cardina mediano", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Cepillo de cerdas metálicas para deslanar", 2, List.of(p("Unidad", 1, "25.00")), 0, "11.00", 6, 5, 0, 0, 2, true),
            new Prod("PZ-HIG-006", "Bolsas sanitarias x120 (8 rollos)", ClaseProducto.HIGIENE, UnidadBase.UNIDAD, CAT_HIG,
                    "Bolsas biodegradables para paseo", 5, List.of(p("Paquete", 1, "12.00")), 0, "5.00", 20, 5, 0, 0, 4, true),

            // ── Servicios (sin stock) ──
            new Prod("PZ-SER-001", "Consulta general", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Consulta de medicina general", 0, List.of(p("Servicio", 1, "50.00")), 0, "0", 0, 0, 0, 0, 0, true),
            new Prod("PZ-SER-002", "Consulta especializada", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Consulta de dermatología o cirugía", 0, List.of(p("Servicio", 1, "80.00")), 0, "0", 0, 0, 0, 0, 0, true),
            new Prod("PZ-SER-003", "Control / reevaluación", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Control posterior a un tratamiento", 0, List.of(p("Servicio", 1, "30.00")), 0, "0", 0, 0, 0, 0, 0, true),
            new Prod("PZ-SER-004", "Baño y corte", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Baño medicado o estético con corte de pelo y uñas", 0, List.of(p("Servicio", 1, "60.00")), 0, "0", 0, 0, 0, 0, 4, true),
            new Prod("PZ-SER-005", "Curación de herida", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Limpieza y curación de heridas", 0, List.of(p("Servicio", 1, "35.00")), 0, "0", 0, 0, 0, 0, 0, true),
            new Prod("PZ-SER-006", "Cirugía de esterilización", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Ovariohisterectomía u orquiectomía con anestesia general", 0, List.of(p("Servicio", 1, "280.00")), 0, "0", 0, 0, 0, 0, 0, true),
            new Prod("PZ-SER-007", "Profilaxis dental", ClaseProducto.SERVICIO, UnidadBase.UNIDAD, CAT_SER,
                    "Limpieza dental con ultrasonido bajo sedación", 0, List.of(p("Servicio", 1, "180.00")), 0, "0", 0, 0, 0, 0, 0, true)
    );

    /** Productos con un lote que vence dentro de los próximos 30 días (código → días hasta el vencimiento). */
    static final String[][] LOTES_POR_VENCER = {{"PZ-MED-012", "18"}, {"PZ-MED-014", "12"}, {"PZ-VAC-004", "26"}};
    /** Producto que recibe un lote que vence durante el mes (y se da de baja al día siguiente). */
    static final String PRODUCTO_LOTE_VENCIDO = "PZ-MED-010";

    // ─────────────────────────── Proveedores ───────────────────────────

    /** Razón social, primeros 10 dígitos del RUC (el dígito verificador se calcula), teléfono y dirección. */
    static final String[][] PROVEEDORES = {
            {"Distribuidora Veterinaria Andina S.A.C.", "2060184735", "014367821", "Av. República de Panamá 3591, San Isidro"},
            {"Biológicos del Pacífico S.A.C.", "2054921876", "014482903", "Calle Los Laureles 245, Santiago de Surco"},
            {"Nutrición Animal Lima E.I.R.L.", "2060392514", "987412365", "Av. Los Próceres 1180, Santiago de Surco"},
            {"Insumos Médicos Santa Rosa S.A.C.", "2051738294", "015213478", "Jr. Huancavelica 512, Cercado de Lima"},
            {"Pet Accesorios Perú S.A.C.", "2060751238", "956321784", "Av. Aviación 2890, San Borja"},
            {"Higiene y Cuidado Animal S.R.L.", "2052896413", "014719256", "Av. Angamos Este 1543, Surquillo"},
    };

    // ─────────────────────────── Personal de demostración ───────────────────────────

    /** Nombres, apellidos, email, DNI y especialidad (solo veterinarios). */
    static final String[][] VETERINARIOS = {
            {"Rosa Elena", "Huamán Quispe", "rosa.huaman@petyzoos.pe", "41736285", "Medicina General", "987635214"},
            {"Jorge Luis", "Paredes Salazar", "jorge.paredes@petyzoos.pe", "43827196", "Cirugía", "986214735"},
            {"Milagros", "Chávez Rojas", "milagros.chavez@petyzoos.pe", "45918327", "Dermatología", "989451276"},
    };
    static final String[] RECEPCIONISTA = {"Karen", "Vásquez Torres", "karen.vasquez@petyzoos.pe"};
    static final String[] ENCARGADO = {"Luis Alberto", "Mendoza Ríos", "luis.mendoza@petyzoos.pe"};
    static final String[] GROOMER = {"Diana", "Flores Ccahua", "diana.flores@petyzoos.pe"};

    // ─────────────────────────── Clientes ───────────────────────────

    static final String[] NOMBRES_M = {"Carlos", "José", "Luis", "Jorge", "Miguel", "Juan Carlos", "Ricardo", "César", "Víctor",
            "Manuel", "Fernando", "Diego", "Renzo", "Alonso", "Martín", "Raúl", "Gustavo", "Edwin", "Christian", "Óscar",
            "Daniel", "Hugo", "Javier", "Percy", "Wilmer"};
    static final String[] NOMBRES_F = {"María", "Rosa", "Carmen", "Ana Lucía", "Lucía", "Patricia", "Milagros", "Gabriela",
            "Karina", "Sofía", "Valeria", "Camila", "Andrea", "Fiorella", "Claudia", "Jessica", "Erika", "Liliana",
            "Silvia", "Mónica", "Pilar", "Daniela", "Rocío", "Diana", "Giuliana"};
    static final String[] APELLIDOS = {"Quispe", "Flores", "Sánchez", "Rodríguez", "García", "Rojas", "Huamán", "Mendoza",
            "Torres", "Ramírez", "Chávez", "Vásquez", "Castillo", "Gutiérrez", "Espinoza", "Díaz", "Mamani", "Ramos", "Reyes",
            "Vargas", "Cruz", "Morales", "Ríos", "Salazar", "Paredes", "Castro", "Romero", "Fernández", "Silva", "Medina",
            "Ortiz", "Aguilar", "Huanca", "Condori", "Palomino", "Valverde", "Zapata", "Alvarado", "Cárdenas", "Benites",
            "Tello", "Ponce", "Delgado", "Navarro", "Campos", "Villanueva", "Cabrera", "Arias", "Soto", "Pacheco"};
    /** Distritos con peso: la clínica está en Surco, la mayoría de clientes vive cerca. */
    static final String[] DISTRITOS = {"Santiago de Surco", "Santiago de Surco", "Santiago de Surco", "San Borja", "San Borja",
            "Surquillo", "Surquillo", "Miraflores", "Miraflores", "Chorrillos", "Chorrillos", "La Molina", "Barranco",
            "San Juan de Miraflores", "San Isidro", "Jesús María", "Lince", "Ate"};
    static final String[] CALLES = {"Av. Benavides", "Jr. Los Pinos", "Calle Las Begonias", "Av. Primavera", "Av. Angamos Este",
            "Jr. Huáscar", "Calle Los Cedros", "Av. Caminos del Inca", "Av. Tomás Marsano", "Jr. Las Orquídeas",
            "Calle Monte Rosa", "Av. Aviación", "Av. La Encalada", "Jr. Batallón Callao", "Av. Higuereta", "Calle Schell",
            "Av. San Luis", "Av. Defensores del Morro", "Jr. Domingo Cueto", "Av. El Polo", "Calle Las Gardenias",
            "Av. Velasco Astete", "Jr. Ayacucho", "Calle Los Geranios"};

    // ─────────────────────────── Mascotas ───────────────────────────

    /** Raza, peso mínimo y máximo (kg) adulto, peso relativo en la población. */
    record RazaDemo(String especie, String raza, double pesoMin, double pesoMax, int peso) {
    }

    static final List<RazaDemo> RAZAS = List.of(
            new RazaDemo("Perro", "Mestizo", 6, 24, 20), new RazaDemo("Perro", "Shih Tzu", 4, 8, 9),
            new RazaDemo("Perro", "Schnauzer", 5, 9, 8), new RazaDemo("Perro", "Poodle", 3, 7, 7),
            new RazaDemo("Perro", "Labrador", 25, 36, 6), new RazaDemo("Perro", "Golden Retriever", 25, 34, 4),
            new RazaDemo("Perro", "Pug", 6, 9, 5), new RazaDemo("Perro", "Bulldog Francés", 9, 13, 5),
            new RazaDemo("Perro", "Chihuahua", 1.5, 3, 6), new RazaDemo("Perro", "Yorkshire Terrier", 2, 3.5, 5),
            new RazaDemo("Perro", "Pastor Alemán", 28, 40, 4), new RazaDemo("Perro", "Cocker Spaniel", 10, 14, 4),
            new RazaDemo("Perro", "Beagle", 9, 14, 4), new RazaDemo("Perro", "Husky Siberiano", 18, 27, 3),
            new RazaDemo("Gato", "Mestizo", 3.2, 5.8, 22), new RazaDemo("Gato", "Siamés", 3, 5, 5),
            new RazaDemo("Gato", "Persa", 3.5, 5.5, 4), new RazaDemo("Gato", "Angora", 3, 5, 3),
            new RazaDemo("Gato", "Bengal", 4, 6.5, 2), new RazaDemo("Gato", "British Shorthair", 4, 7, 2),
            new RazaDemo("Conejo", "Holland Lop", 1.4, 2, 1), new RazaDemo("Conejo", "Cabeza de León", 1.2, 1.7, 1),
            new RazaDemo("Hamster", "Sirio", 0.12, 0.18, 1), new RazaDemo("Hamster", "Ruso", 0.03, 0.05, 1)
    );

    static final String[] NOMBRES_PERRO = {"Max", "Rocky", "Toby", "Bruno", "Firulais", "Chocolate", "Canela", "Luna", "Lola",
            "Kira", "Maya", "Coco", "Bobby", "Princesa", "Zeus", "Thor", "Lucky", "Oso", "Negrita", "Manchas", "Simba",
            "Copito", "Nala", "Dante", "Rex", "Duque", "Mía", "Frida", "Pancho", "Bella", "Benito", "Chispa", "Kiara",
            "Tyson", "Peluchín", "Hachi", "Milo", "Sasha", "Pipo", "Rufo", "Kaiser", "Chester", "Panchita", "Tobías",
            "Lulú", "Boby", "Gordo", "Fiona", "Bongo", "Terry", "Lassie", "Rambo", "Pepa", "Tito", "Cholo", "Morocha",
            "Leia", "Odín", "Brownie", "Puchito"};
    static final String[] NOMBRES_GATO = {"Michi", "Garfield", "Pelusa", "Misha", "Tom", "Salem", "Nieve", "Mimi", "Félix",
            "Simón", "Cleo", "Kitty", "Pantera", "Mostaza", "Tigre", "Oreo", "Minina", "Morgana", "Bigotes", "Nube",
            "Romeo", "Olivia", "Kiwi", "Sombra", "Gaturro", "Lucifer", "Chimuelo", "Luna", "Mishi", "Copo", "Benito",
            "Canelita"};
    static final String[] NOMBRES_OTROS = {"Tambor", "Copito", "Bolita", "Galleta", "Nuez", "Pompón"};

    // ─────────────────────────── Casos clínicos ───────────────────────────

    enum Tipo { GENERAL, DERMA, CIRUGIA, VACUNA, CONTROL }

    /** Medicamento de la receta: nombre, indicación, frecuencia, duración. */
    record Med(String nombre, String indicacion, String frecuencia, String duracion) {
    }

    /**
     * Caso clínico de plantilla.
     * @param especie   "Perro", "Gato", "Otro" o "*" (cualquiera de perro/gato)
     * @param servicio  código del servicio que se cobra (null = solo vacunas)
     * @param insumos   pares código:cantidad de lo que se consume (CONSUMO_INTERNO)
     * @param vacunas   códigos de producto de vacuna a aplicar
     * @param fiebre    true = temperatura elevada
     */
    record Caso(Tipo tipo, String especie, String motivoCita, String motivo, String diagnostico, String tratamiento,
                String observaciones, String estadoIngreso, String estadoSalida, String urgencia, int duracion,
                String servicio, String insumos, String vacunas, List<Med> receta, String indicaciones,
                String sistemas, boolean fiebre, int peso) {
    }

    private static Med m(String nombre, String indicacion, String frecuencia, String duracion) {
        return new Med(nombre, indicacion, frecuencia, duracion);
    }

    static final List<Caso> CASOS = List.of(
            new Caso(Tipo.GENERAL, "Perro", "Vómitos y diarrea", "Vómitos (3 episodios) y heces blandas desde ayer",
                    "Gastroenteritis aguda inespecífica, probable transgresión alimentaria",
                    "Fluidoterapia subcutánea en consulta, ayuno de sólidos por 12 h y luego dieta blanda (pollo sancochado con arroz) por 3 días. Metronidazol oral.",
                    "Mucosas rosadas, deshidratación leve (5%). Abdomen sensible a la palpación, sin masas palpables.",
                    "Decaído, deshidratación leve", "Estable, hidratado", "NORMAL", 30, "PZ-SER-001",
                    "PZ-INS-006:1,PZ-INS-001:2,PZ-INS-003:2", null,
                    List.of(m("Metronidazol 250 mg", "1 tableta por cada 10 kg", "Cada 12 horas", "5 días")),
                    "Dar agua fresca a voluntad. Si vuelve a vomitar más de dos veces, regresar a control.",
                    "[\"digestivo\"]", false, 6),
            new Caso(Tipo.GENERAL, "Perro", "Cojera", "Cojea del miembro posterior derecho después de jugar en el parque",
                    "Esguince de rodilla derecha, sin inestabilidad del ligamento cruzado al examen",
                    "Meloxicam inyectable en consulta. Reposo relativo por 10 días (paseos cortos con correa), compresas frías 2 veces al día.",
                    "Dolor a la flexión de la rodilla derecha, prueba de cajón negativa. Resto del examen sin alteraciones.",
                    "Alerta, apoya poco el miembro", "Estable", "NORMAL", 30, "PZ-SER-001",
                    "PZ-MED-013:1,PZ-INS-001:1", null,
                    List.of(m("Meloxicam 1 mg", "1 tableta por cada 10 kg con comida", "Cada 24 horas", "5 días")),
                    "No subir escaleras ni saltar. Control en 10 días si persiste la cojera.",
                    "[\"musculoesqueletico\"]", false, 5),
            new Caso(Tipo.DERMA, "Perro", "Picazón y lamido de patas", "Prurito intenso, se lame las patas y se rasca las orejas hace 3 semanas",
                    "Dermatitis atópica canina con pioderma superficial secundaria",
                    "Baños con shampoo hipoalergénico 2 veces por semana, antibiótico oral por 14 días y corticoide en dosis decreciente.",
                    "Eritema interdigital y en axilas, pápulas y collaretes epidérmicos en abdomen. Citología: cocos abundantes.",
                    "Alerta, prurito marcado", "Estable", "NORMAL", 40, "PZ-SER-002",
                    "PZ-INS-003:2,PZ-INS-002:2", null,
                    List.of(m("Enrofloxacino 50 mg", "1 tableta por cada 10 kg", "Cada 24 horas", "14 días"),
                            m("Prednisolona 5 mg", "1/2 tableta por cada 5 kg, luego reducir a la mitad", "Cada 24 horas", "7 días")),
                    "Usar shampoo hipoalergénico, dejar actuar 10 minutos. Control dermatológico en 15 días.",
                    "[\"piel\"]", false, 7),
            new Caso(Tipo.DERMA, "*", "Otitis", "Sacude la cabeza y tiene mal olor en el oído izquierdo",
                    "Otitis externa bilateral por Malassezia",
                    "Limpieza ótica en consulta. Limpiador ótico cada 48 horas y gotas óticas con antifúngico por 10 días.",
                    "Conducto auditivo eritematoso con secreción marrón oscura. Citología: levaduras abundantes.",
                    "Alerta, molestia al manipular orejas", "Estable", "NORMAL", 30, "PZ-SER-002",
                    "PZ-INS-002:4,PZ-INS-003:2", null,
                    List.of(m("Limpiador ótico 120 ml", "Llenar el conducto, masajear y retirar el exceso", "Cada 48 horas", "2 semanas"),
                            m("Gotas óticas clotrimazol/gentamicina", "4 gotas en cada oído", "Cada 12 horas", "10 días")),
                    "No mojar las orejas durante el baño. Control en 10 días.",
                    "[\"oidos\"]", false, 6),
            new Caso(Tipo.VACUNA, "Perro", "Vacunación anual", "Vacunación anual y control de salud",
                    "Paciente clínicamente sano",
                    "Se aplica vacuna séxtuple. Se recuerda desparasitación interna cada 3 meses y protección antipulgas mensual.",
                    "Condición corporal 3/5, mucosas rosadas, auscultación normal. Apto para vacunar.",
                    "Alerta, buen estado general", "Estable", "NORMAL", 20, null,
                    "PZ-INS-001:1", "PZ-VAC-001", null, null, "[]", false, 10),
            new Caso(Tipo.VACUNA, "Perro", "Vacuna antirrábica", "Refuerzo de vacuna antirrábica y séxtuple",
                    "Paciente clínicamente sano",
                    "Se aplican vacuna séxtuple y antirrábica. Puede presentar decaimiento leve por 24 horas.",
                    "Examen físico sin alteraciones. Peso estable.",
                    "Alerta", "Estable", "NORMAL", 20, null,
                    "PZ-INS-001:2", "PZ-VAC-001,PZ-VAC-002", null, null, "[]", false, 6),
            new Caso(Tipo.VACUNA, "Perro", "Primera vacuna de cachorro", "Primera consulta de cachorro, inicia plan de vacunación",
                    "Cachorro sano, inicia plan de vacunación",
                    "Se aplica primera dosis de vacuna séxtuple. Desparasitación interna. Refuerzo en 21 días.",
                    "Cachorro activo, buena condición corporal. Se indica no sacar a la calle hasta completar vacunas.",
                    "Activo", "Estable", "NORMAL", 30, "PZ-SER-003",
                    "PZ-INS-001:1,PZ-MED-006:1", "PZ-VAC-001", null,
                    "No bañar por 7 días. Próxima vacuna en 21 días.", "[]", false, 3),
            new Caso(Tipo.VACUNA, "Perro", "Vacuna KC", "Vacuna contra tos de las perreras antes de hospedaje",
                    "Paciente clínicamente sano",
                    "Se aplica vacuna KC intranasal. Puede presentar estornudos leves por 2 a 3 días.",
                    "Examen respiratorio normal.",
                    "Alerta", "Estable", "NORMAL", 20, null,
                    null, "PZ-VAC-004", null, null, "[]", false, 1),
            new Caso(Tipo.VACUNA, "Gato", "Vacunación felina", "Vacuna triple felina anual",
                    "Paciente clínicamente sano",
                    "Se aplica vacuna triple felina. Control anual.",
                    "Gato tranquilo en consulta, mucosas rosadas, auscultación normal.",
                    "Alerta", "Estable", "NORMAL", 20, null,
                    "PZ-INS-001:1", "PZ-VAC-003", null, null, "[]", false, 7),
            new Caso(Tipo.VACUNA, "Gato", "Vacunación felina y antirrábica", "Vacunas anuales (triple felina y antirrábica)",
                    "Paciente clínicamente sano",
                    "Se aplican vacuna triple felina y antirrábica.",
                    "Examen físico sin alteraciones.",
                    "Alerta", "Estable", "NORMAL", 20, null,
                    "PZ-INS-001:2", "PZ-VAC-003,PZ-VAC-002", null, null, "[]", false, 4),
            new Caso(Tipo.CIRUGIA, "Perro", "Esterilización programada", "Esterilización programada (ovariohisterectomía)",
                    "Ovariohisterectomía electiva sin complicaciones",
                    "Cirugía bajo anestesia general (ketamina, tramadol). Collar isabelino por 10 días, analgesia y antibiótico oral. Retiro de puntos en 10 días.",
                    "Ayuno de 12 horas cumplido. Exámenes prequirúrgicos normales. Recuperación anestésica sin complicaciones.",
                    "Alerta, en ayunas", "Recuperado de anestesia, estable", "NORMAL", 90, "PZ-SER-006",
                    "PZ-MED-011:2,PZ-MED-012:1,PZ-INS-004:2,PZ-INS-005:1,PZ-INS-006:1,PZ-INS-001:3,PZ-INS-002:8,PZ-INS-003:6,PZ-ACC-007:1", null,
                    List.of(m("Meloxicam 1 mg", "1 tableta por cada 10 kg con comida", "Cada 24 horas", "4 días"),
                            m("Amoxicilina 250 mg", "1 tableta por cada 10 kg", "Cada 12 horas", "7 días")),
                    "Mantener el collar isabelino. Limpiar la herida con suero fisiológico una vez al día. No bañar por 10 días.",
                    "[]", false, 3),
            new Caso(Tipo.CIRUGIA, "Gato", "Esterilización programada", "Castración / esterilización felina programada",
                    "Gonadectomía electiva sin complicaciones",
                    "Cirugía bajo anestesia general. Analgesia por 3 días. Control en 7 días.",
                    "Ayuno cumplido. Recuperación anestésica tranquila.",
                    "Alerta, en ayunas", "Recuperado de anestesia, estable", "NORMAL", 60, "PZ-SER-006",
                    "PZ-MED-011:1,PZ-INS-004:1,PZ-INS-005:1,PZ-INS-001:2,PZ-INS-002:4,PZ-INS-003:4", null,
                    List.of(m("Meloxicam 1 mg", "1/4 de tableta (gato)", "Cada 24 horas", "3 días")),
                    "Mantener en un ambiente tranquilo y usar arena de papel por 5 días.", "[]", false, 3),
            new Caso(Tipo.CIRUGIA, "*", "Profilaxis dental", "Mal aliento y sarro, profilaxis dental programada",
                    "Enfermedad periodontal grado II; profilaxis dental realizada",
                    "Limpieza dental con ultrasonido y pulido bajo sedación. Extracción de una pieza premolar con movilidad.",
                    "Sarro abundante en premolares y molares superiores, gingivitis moderada.",
                    "Alerta, en ayunas", "Recuperado de sedación, estable", "NORMAL", 60, "PZ-SER-007",
                    "PZ-MED-011:1,PZ-INS-002:6,PZ-INS-003:4,PZ-INS-001:2", null,
                    List.of(m("Amoxicilina 250 mg", "1 tableta por cada 10 kg", "Cada 12 horas", "5 días")),
                    "Dieta blanda por 3 días. Iniciar cepillado dental en 1 semana y snack dental diario.", "[\"boca\"]", false, 2),
            new Caso(Tipo.CONTROL, "*", "Control post-operatorio", "Control post-operatorio y retiro de puntos",
                    "Herida quirúrgica en buena evolución, sin signos de infección",
                    "Retiro de puntos. Alta quirúrgica.",
                    "Herida cicatrizada, sin secreción ni inflamación.",
                    "Alerta", "Estable, alta", "NORMAL", 20, "PZ-SER-003",
                    "PZ-INS-002:2,PZ-INS-003:2", null, null, null, "[]", false, 4),
            new Caso(Tipo.GENERAL, "Perro", "Tos", "Tos seca y arcadas desde hace 5 días, estuvo en guardería",
                    "Traqueobronquitis infecciosa canina (tos de las perreras)",
                    "Antibiótico oral por 7 días y antitusivo. Evitar collar en el cuello, usar pechera.",
                    "Tos productiva al palpar la tráquea. Auscultación pulmonar sin crepitaciones.",
                    "Alerta, tos frecuente", "Estable", "NORMAL", 30, "PZ-SER-001",
                    null, null,
                    List.of(m("Amoxicilina 250 mg", "1 tableta por cada 10 kg", "Cada 12 horas", "7 días"),
                            m("Jarabe antitusivo (dextrometorfano)", "1 ml por cada 5 kg", "Cada 8 horas", "5 días")),
                    "Mantener alejado de otros perros por 10 días.", "[\"respiratorio\"]", true, 4),
            new Caso(Tipo.GENERAL, "Perro", "Decaimiento y no come", "Decaído, no come desde hace 2 días, tuvo garrapatas",
                    "Ehrlichiosis canina (test rápido positivo), trombocitopenia leve",
                    "Doxiciclina 10 mg/kg cada 24 horas por 28 días. Control de hemograma en 2 semanas. Pipeta antipulgas y garrapatas.",
                    "Mucosas pálidas, linfonodos aumentados, petequias en abdomen. Fiebre.",
                    "Decaído, mucosas pálidas", "Estable", "URGENCIA", 40, "PZ-SER-001",
                    "PZ-INS-001:2,PZ-INS-003:2,PZ-MED-007:1", null,
                    List.of(m("Doxiciclina 100 mg", "Según peso: 10 mg/kg", "Cada 24 horas", "28 días"),
                            m("Suplemento vitamínico jarabe", "5 ml", "Cada 24 horas", "30 días")),
                    "Volver de inmediato si aparece sangrado de nariz o encías.", "[\"hematologico\",\"linfatico\"]", true, 3),
            new Caso(Tipo.GENERAL, "Perro", "Herida por mordedura", "Herida en el cuello por mordedura de otro perro en el parque",
                    "Herida punzante en región cervical por mordedura, sin compromiso de estructuras profundas",
                    "Tricotomía, limpieza y desbridamiento de la herida. Antibiótico y analgésico.",
                    "Dos heridas punzantes de 1 cm en región cervical lateral, con edema local.",
                    "Alerta, dolor local", "Estable, herida limpia", "URGENCIA", 40, "PZ-SER-005",
                    "PZ-INS-002:6,PZ-INS-003:2,PZ-INS-001:2,PZ-MED-013:1", null,
                    List.of(m("Amoxicilina 250 mg", "1 tableta por cada 10 kg", "Cada 12 horas", "7 días"),
                            m("Meloxicam 1 mg", "1 tableta por cada 10 kg con comida", "Cada 24 horas", "3 días")),
                    "Limpiar la herida con suero fisiológico 2 veces al día. Control en 3 días.", "[\"piel\"]", false, 3),
            new Caso(Tipo.GENERAL, "Perro", "Desparasitación", "Desparasitación y control de peso",
                    "Paciente sano; se desparasita",
                    "Se administra antiparasitario interno en consulta. Repetir en 3 meses.",
                    "Condición corporal 3/5. Sin alteraciones.",
                    "Alerta", "Estable", "NORMAL", 20, "PZ-SER-003",
                    "PZ-MED-006:1", null, null, null, "[]", false, 5),
            new Caso(Tipo.GENERAL, "Gato", "Orina con frecuencia", "Va muchas veces al arenero y orina poco, a veces con sangre",
                    "Cistitis idiopática felina",
                    "Analgesia, aumentar el consumo de agua (alimento húmedo, fuente de agua) y reducir el estrés en casa.",
                    "Vejiga pequeña y dolorosa a la palpación, no obstruida. Tira reactiva: sangre ++.",
                    "Alerta, molestia abdominal", "Estable", "NORMAL", 30, "PZ-SER-001",
                    "PZ-INS-003:2", null,
                    List.of(m("Meloxicam 1 mg", "1/4 de tableta (gato)", "Cada 24 horas", "4 días")),
                    "Ofrecer alimento húmedo diario. Si deja de orinar, acudir de emergencia.", "[\"urinario\"]", false, 5),
            new Caso(Tipo.GENERAL, "Gato", "Vómitos", "Vómitos con pelo 2 a 3 veces por semana",
                    "Tricobezoar con gastritis leve",
                    "Pasta de malta 2 veces por semana, cepillado diario y alimento con control de bolas de pelo.",
                    "Abdomen blando, no doloroso. Pelaje abundante con nudos.",
                    "Alerta", "Estable", "NORMAL", 30, "PZ-SER-001",
                    null, null,
                    List.of(m("Pasta de malta", "2 cm en la pata o en la boca", "2 veces por semana", "4 semanas")),
                    "Cepillar todos los días.", "[\"digestivo\"]", false, 3),
            new Caso(Tipo.DERMA, "Gato", "Caída de pelo", "Caída de pelo en parches redondeados en cabeza y patas",
                    "Dermatofitosis (Microsporum canis), lámpara de Wood positiva",
                    "Itraconazol oral por 4 semanas, baños con shampoo antifúngico y limpieza del ambiente.",
                    "Lesiones alopécicas circulares con descamación. Fluorescencia positiva con lámpara de Wood.",
                    "Alerta", "Estable", "NORMAL", 40, "PZ-SER-002",
                    "PZ-INS-003:2", null,
                    List.of(m("Itraconazol 10 mg/ml solución", "5 mg/kg", "Cada 24 horas, semana por medio", "6 semanas")),
                    "Es contagioso para personas: usar guantes y lavar mantas con agua caliente.", "[\"piel\"]", false, 3),
            new Caso(Tipo.GENERAL, "Gato", "Ojo rojo y lagrimeo", "Lagrimeo y ojo rojo desde hace 3 días, estornuda",
                    "Conjuntivitis felina, probable herpesvirus felino",
                    "Colirio antibiótico cada 8 horas y limpieza ocular con suero fisiológico.",
                    "Hiperemia conjuntival bilateral, secreción serosa. Fluoresceína negativa (sin úlcera).",
                    "Alerta", "Estable", "NORMAL", 30, "PZ-SER-001",
                    "PZ-INS-002:2", null,
                    List.of(m("Gotas oftálmicas antibióticas 5 ml", "1 gota en cada ojo", "Cada 8 horas", "7 días")),
                    "Limpiar las secreciones antes de aplicar las gotas.", "[\"ojos\",\"respiratorio\"]", true, 3),
            new Caso(Tipo.CONTROL, "*", "Control dermatológico", "Control de tratamiento dermatológico",
                    "Dermatitis en remisión, buena respuesta al tratamiento",
                    "Continuar baños semanales. Se suspende el corticoide.",
                    "Menos eritema y prurito. Pelo en crecimiento en zonas afectadas.",
                    "Alerta", "Estable", "NORMAL", 20, "PZ-SER-003",
                    null, null, null, null, "[\"piel\"]", false, 4),
            new Caso(Tipo.GENERAL, "Otro", "No come", "No come bien y tiene los dientes largos",
                    "Maloclusión dental, sobrecrecimiento de incisivos",
                    "Limado dental en consulta. Dieta rica en heno a voluntad y verduras de hoja.",
                    "Incisivos superiores e inferiores sobrecrecidos. Condición corporal 2/5.",
                    "Alerta, bajo peso", "Estable", "NORMAL", 30, "PZ-SER-001",
                    "PZ-INS-003:2", null, null, "Pesar cada semana. Control en 1 mes.", "[\"boca\"]", false, 2),
            new Caso(Tipo.GENERAL, "Otro", "Control general", "Control general y corte de uñas",
                    "Paciente sano. Recorte de uñas",
                    "Recorte de uñas. Revisión de dieta: reducir semillas con grasa y aumentar alimento balanceado.",
                    "Condición corporal adecuada. Pelaje en buen estado.",
                    "Alerta", "Estable", "NORMAL", 20, "PZ-SER-003",
                    null, null, null, null, "[]", false, 1)
    );
}

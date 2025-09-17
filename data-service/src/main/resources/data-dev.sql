-- Datos de prueba para el perfil de desarrollo
-- Autor: Agustin Benavidez - Legajo: 62344

-- Insertar categorías
INSERT INTO categorias (nombre, descripcion, fecha_creacion, fecha_actualizacion) VALUES 
('Electrónicos', 'Productos electrónicos y tecnológicos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ropa', 'Prendas de vestir y accesorios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hogar', 'Artículos para el hogar y decoración', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Deportes', 'Equipamiento y ropa deportiva', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Libros', 'Libros y material educativo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar productos
INSERT INTO productos (nombre, descripcion, precio, categoria_id, activo, fecha_creacion, fecha_actualizacion) VALUES 
-- Electrónicos
('Smartphone Samsung Galaxy', 'Teléfono inteligente con pantalla AMOLED', 299999.99, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Laptop Dell Inspiron', 'Laptop para uso profesional y personal', 750000.00, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido', 85000.00, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tablet iPad', 'Tablet para trabajo y entretenimiento', 450000.00, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Ropa
('Remera Básica', 'Remera de algodón 100% de alta calidad', 12500.00, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jeans Clásicos', 'Pantalón de mezclilla azul clásico', 18750.00, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Campera de Cuero', 'Campera de cuero genuino para hombre', 95000.00, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Zapatillas Deportivas', 'Zapatillas para correr y ejercicio', 32500.00, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Hogar
('Silla de Oficina', 'Silla ergonómica para oficina', 125000.00, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mesa de Comedor', 'Mesa de madera para 6 personas', 225000.00, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lámpara LED', 'Lámpara de mesa con luz LED regulable', 35000.00, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Almohada Memory Foam', 'Almohada ergonómica de memory foam', 18750.00, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Deportes
('Pelota de Fútbol', 'Pelota oficial FIFA para fútbol', 15625.00, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Raqueta de Tenis', 'Raqueta profesional de tenis', 87500.00, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bicicleta Mountain Bike', 'Bicicleta para montaña con 21 velocidades', 380000.00, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pesas Ajustables', 'Set de pesas ajustables hasta 20kg', 65000.00, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Libros
('El Quijote', 'Clásico de la literatura española', 8750.00, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Programación en Java', 'Manual completo de programación Java', 25000.00, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Historia Argentina', 'Libro de historia argentina contemporánea', 15625.00, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cien Años de Soledad', 'Obra maestra de Gabriel García Márquez', 12500.00, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar inventarios
INSERT INTO inventario (producto_id, cantidad, stock_minimo, fecha_creacion, fecha_ultima_actualizacion, version) VALUES 
-- Electrónicos (stock variado)
(1, 25, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Smartphone
(2, 15, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Laptop
(3, 50, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Auriculares
(4, 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),    -- Tablet

-- Ropa (stock alto)
(5, 100, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- Remera
(6, 75, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Jeans
(7, 30, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Campera
(8, 60, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Zapatillas

-- Hogar (stock medio)
(9, 20, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Silla
(10, 12, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Mesa
(11, 35, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Lámpara
(12, 40, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- Almohada

-- Deportes (algunos con stock bajo)
(13, 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Pelota (stock bajo)
(14, 18, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Raqueta
(15, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Bicicleta (stock crítico)
(16, 25, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Pesas

-- Libros (stock variado)
(17, 45, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- El Quijote
(18, 22, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),  -- Java
(19, 4, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),   -- Historia (stock bajo)
(20, 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);   -- Cien Años (sin stock)

-- ============================================================
-- Tablas y datos de ejemplo del supermercado, sobre la base `labmcp`.
-- Se puede ejecutar varias veces: no duplica tablas ni datos.
-- PowerShell (desde la raíz del repo):
--   Get-Content src/main/resources/supermercado.sql | mysql -u root -p labmcp
-- ============================================================

USE labmcp;

CREATE TABLE IF NOT EXISTS producto (
    id        BIGINT         NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100)   NOT NULL,
    categoria VARCHAR(50)    NOT NULL,
    precio    DECIMAL(10, 2) NOT NULL,
    stock     INT            NOT NULL,
    version   BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_producto_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cliente (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20)  NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cliente_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    cliente_id        BIGINT         NOT NULL,
    estado            VARCHAR(20)    NOT NULL,
    direccion_entrega VARCHAR(255)   NOT NULL,
    fecha             DATETIME(6)    NOT NULL,
    total             DECIMAL(12, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT         NOT NULL,
    producto_id     BIGINT         NOT NULL,
    cantidad        INT            NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INSERT IGNORE se apoya en las claves únicas (nombre, telefono) para no duplicar.
INSERT IGNORE INTO producto (nombre, categoria, precio, stock, version) VALUES
    ('Leche entera 1 L',          'Lácteos',   4500.00, 40, 0),
    ('Queso campesino 500 g',     'Lácteos',  12900.00, 15, 0),
    ('Yogur de fresa 1 L',        'Lácteos',   8200.00, 20, 0),
    ('Pan tajado 500 g',          'Panadería', 6800.00, 25, 0),
    ('Arepas de maíz x5',         'Panadería', 4200.00, 30, 0),
    ('Galletas de avena 300 g',   'Panadería', 5600.00, 18, 0),
    ('Huevos AA x12',             'Despensa', 11500.00, 35, 0),
    ('Arroz blanco 1 kg',         'Despensa',  4900.00, 50, 0),
    ('Aceite de girasol 1 L',     'Despensa', 13800.00, 12, 0),
    ('Manzana roja 1 kg',         'Frutas y verduras', 7900.00, 22, 0),
    ('Banano 1 kg',               'Frutas y verduras', 3200.00, 45, 0),
    ('Tomate chonto 1 kg',        'Frutas y verduras', 4600.00,  5, 0),
    ('Jabón en polvo 1 kg',       'Aseo',     14500.00, 10, 0),
    ('Papel higiénico x4',        'Aseo',      9800.00, 28, 0),
    ('Lavaloza líquido 500 ml',   'Aseo',      7300.00,  8, 0);

INSERT IGNORE INTO cliente (nombre, telefono, direccion) VALUES
    ('Ana Gómez',    '3001234567', 'Calle 10 # 43-20, Medellín'),
    ('Carlos Ruiz',  '3109876543', 'Carrera 70 # 52-15, Medellín');

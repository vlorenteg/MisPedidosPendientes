CREATE DATABASE pedidos_pendientes CHARSET utf8mb4 COLLATE utf8mb4_spanish2_ci;
USE pedidos_pendientes;

CREATE TABLE tiendas (
    idTienda INT AUTO_INCREMENT,
    nombreTienda VARCHAR(45) NOT NULL,
    PRIMARY KEY (idTienda)
);

CREATE TABLE pedidos (
    idPedido INT AUTO_INCREMENT,
    fechaPedido DATE DEFAULT NULL,
    fechaEstimadaPedido DATE DEFAULT NULL,
	descripcionPedido VARCHAR(120) DEFAULT NULL,
    importePedido DECIMAL(6,2) DEFAULT NULL,
    estadoPedido INT DEFAULT NULL,
    idTiendaFK INT DEFAULT NULL,
    PRIMARY KEY (idPedido),
    FOREIGN KEY (idTiendaFK)
			REFERENCES tiendas(idTienda)
);

CREATE USER 'api_pedidos'@'localhost' IDENTIFIED BY 'Studium2023;';

GRANT ALL PRIVILEGES ON pedidos_pendientes.* TO 'api_pedidos'@'localhost';
FLUSH PRIVILEGES;


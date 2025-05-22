# 📘 Documentación para Desarrolladores - Horse Racing Server

Este documento técnico describe la arquitectura, los componentes principales y las consideraciones de desarrollo del servidor multijugador de carreras de caballos implementado en Java. Está dirigido a desarrolladores que deseen comprender, mantener o extender el código fuente.

---

## 🧱 Estructura General del Sistema

El sistema está organizado en **paquetes lógicamente separados por responsabilidad**, siguiendo una arquitectura modular en capas:

com.gerson
├── model → Representación de entidades del dominio (jugador)
├── logic → Coordinación del estado del juego y reglas
├── network → Manejo de conexiones cliente-servidor (`threads`)
└── server → Punto de entrada principal del sistema

---

## 📦 Descripción de Componentes Principales

### `com.gerson.model.Player`

- Entidad que representa a cada jugador conectado.
- Contiene: `id`, `name`, `progress`, `reportedClicks`, `PrintStream`.

### `com.gerson.logic.GameManager`

- Controlador central del estado del juego.
- Patrón **Singleton** para garantizar una única instancia global.
- Coordina: registro, mensajes, avance de jugadores, inicio de carrera, y victoria.

### `com.gerson.network.ClientHandler`

- Hilo por cada cliente (`extends Thread`).
- Registra al jugador, recibe su nombre, escucha mensajes y los delega al `GameManager`.
- Maneja desconexiones.

### `com.gerson.server.Server`

- Clase con el método `main`.
- Inicia el `ServerSocket`, acepta conexiones y lanza `ClientHandler` por cliente.
- Permite detener el servidor desde pruebas automatizadas con `stopServer()`.

---

## 🛠️ Tecnologías y Dependencias

- **Lenguaje:** Java 17
- **Gestor de proyectos:** Apache Maven 3.8.6+
- **Pruebas:** JUnit 5, Mockito
- **Cobertura:** JaCoCo
- **Logging:** `java.util.logging`

---

## 🧪 Pruebas y Cobertura

- Las clases están preparadas para pruebas unitarias y de integración.
- Para generar un reporte de cobertura con JaCoCo:

```bash
mvn clean verify
```

El reporte estará en: `target/site/jacoco/index.html`

## 📂 Organización de Archivos Fuente

src/
├── main/
│   └── java/com/gerson/
│       ├── model/           Entidades del dominio
│       ├── logic/           Lógica del juego
│       ├── network/         Hilos cliente
│       └── server/          Clase Server (main)
├── test/
│   └── java/                Pruebas automatizadas

## 📌 Notas para Desarrolladores
Toda la lógica crítica está encapsulada en `GameManager`, facilitando su extensión.

El uso de `synchronized` asegura concurrencia segura sin frameworks externos.

Para integrar una GUI o cliente más avanzado, basta con conectar sockets al puerto 1818 y seguir el protocolo de texto.
# Horse Racing - Servidor de Juego de Carreras Multijugador

## 🏁 Propósito del Proyecto

Este proyecto implementa un **servidor de carreras de caballos multijugador en tiempo real** utilizando Java, Maven y sockets TCP/IP. Su objetivo es permitir que varios jugadores se conecten a través de una red, seleccionen su nombre, y participen en una carrera competitiva controlando su progreso mediante clics.

El servidor gestiona:

- Registro de jugadores.
- Comunicación en tiempo real con los clientes.
- Control del estado global del juego.
- Inicio de la carrera sincronizado.
- Determinación del ganador.

Está orientado a ser una aplicación recreativa y técnica que refuerza conceptos de **programación concurrente**, **comunicación en red** y **arquitectura en capas**.

---

## ⚙️ Requisitos

- Java 8 o superior
- Maven 3.8.6 o superior
- Terminal o IDE (IntelliJ, VS Code, Eclipse)
- Clientes conectados por socket (implementación no incluida aquí)

---

## 🚀 Cómo ejecutar el servidor

1. **Clona o descarga el repositorio del proyecto**.

2. **Navega al directorio raíz del proyecto** desde una terminal:

   ```bash
   cd horse-racing-vyvsw
   ```

3. **Compila el proyecto**

   ```bash
   javac com/gerson/**/*.java
   ```

4. **Ejecuta el servidor**

   ```bash
   java com.gerson.server.Server
   ```

5. El servidor se iniciará en el puerto 1818 y mostrará mensajes en consola indicando el estado del sistema.

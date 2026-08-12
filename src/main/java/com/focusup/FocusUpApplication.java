package com.focusup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación FocusUp - Módulo de Tareas.
 *
 * La anotación {@code @SpringBootApplication} combina tres anotaciones:
 * <ul>
 *   <li>{@code @Configuration}     → clase de configuración Spring</li>
 *   <li>{@code @EnableAutoConfiguration} → activa la configuración automática</li>
 *   <li>{@code @ComponentScan}     → escanea componentes en el paquete base</li>
 * </ul>
 *
 * Al ejecutar, Spring Boot levanta un servidor Tomcat embebido en el puerto
 * configurado en {@code application.properties} y expone la aplicación
 * en {@code http://localhost:8080/focusup}.
 *
 * Módulo     : Gestión de Tareas — FocusUp
 * Framework  : Spring Boot 3.2.5
 * Evidencia  : GA7-220501096-AA3-EV01
 *
 * @author  Daniel Castiblanco
 * @version 1.0
 */
@SpringBootApplication
public class FocusUpApplication {

    /**
     * Punto de entrada de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SpringApplication.run(FocusUpApplication.class, args);
    }
}

# JAVA-VIRTUAL-THREADS

## ¿Qué son hilos virtuales o "virtual threads"?
Los hilos virtuales son una caracteristica que fue introducida en Java 21, estos son hilos ligeros (comparandolos con los hilos tradicionales) que son gestionados por la JVM (Maquina Virtual de Java), estos fueron diseñados para facilitar la escritura, mantenimiento y depuración de aplicaciones concurrentes de alto rendimiento.

En diferencia a los hilos tradicionales que estan vinculados a hilos del sistema operativo, los hilos virtuales estan gestionados por la JVM. 

## ¿Cómo funciona?
Al momento que un hilo virtual ejecuta una operacion o tarea de bloqueo, como puede ser la lectura de uan red o disco, la JVM lo "desmonta" de su carried thread, el carried thread queda libre rapidamente para "montar" y ejecutar otro hilo virtual que esté listo para ser ejecutado, cuando la operación de bloqueo del primer hilo virtual se completa, la JVM lo "remonta" en un carrier thread disponible para continuar su ejecución.

## Carrier threads vs virtual threads

### Carrier Threads: 
    - Hilos pesados
    - Consumen normalmente 1MB por hilo para pila
    - Funcionan directamente sobre el sistema operativo (cada hilo en Java usa un hilo real del sistema)
    - Cuando un hilo de plataforma bloquea el hilo de SO tambien bloquea y ese recurso computacional queda inutilizado hasta que el bloqueo se resuelva
    - No se pueden usar muchos hilos al mismo tiempo porque consumen mucha memoria y recursos
    - Cuando un hilo esta esperando se pierde rendimiento al estar ocupando un recurso valioso como es el procesador sin hacer nada util

### Virtual Threads
    - Hilos ligeros 
    - Su consumo generalmente esta en Kb
    - Se puede tener miles o millones de hilos virtuales sin problema de rendimiento 
    - Usan muy pocos recursos 
    - Son gestionados por la JVM por lo que no estan vincualdos a hilos reales del SO 
    - Es ideal para programas que requieran hacer muchas tareas de esperas como hablar con servidores, leer archivos, etc
    - Son faciles de usar 

## Escalabilidad 

Ejemplo practico: 
    Digamos que una aplicación como el de un servidor web recibe muchas conexiones al mismo tiempo unas 1000 personas pidiendo datos de una base de datos
        - Con hilos tradicionales Java necesitará crear 1000 hilos del sistema operativo, uno por cada persona que está esperando una respuesta
        - Si un hilo se queda esperando (por ejemplo, a que la base de datos responda), el procesador queda "bloqueado" sin hacer nada útil mientras espera
    
    Como resultado se agotan los recursos del sistema y tu aplicación no escala bien cuando hay muchos usuarios

    ¿Qué pasaria en cambio con los hilos virtuales?

    - Cuando un hilo virtual necesita esperar la JVM lo "pausa" y libera al hilo físico que lo estaba ejecutando
    - Ese hilo físico queda libre para hacer otra tarea mientras tanto
    - Cuando la espera termina, la JVM "reanuda" el hilo virtual y lo pone a correr nuevamente

    Esto nos da a entender que se puede tener miles o millones de hilos virtuales esperando cosas al mismo tiempo, pero solo necesitas unos pocos hilos reales (carrier threads) para moverlos, normalmente uno por núcleo del procesador.

## Creación y gestión Virtual Threads

La forma más común de crear un hilo virtual es usando ***Thread.ofVirtual().***

```java
// Creación de un hilo virtual
Thread virtualThread = Thread.ofVirtual().start(() -> {
    System.out.println("Hello from a virtual thread!");
});

// O usando un Runnable
Runnable task = () -> {
    System.out.println("Another virtual thread doing work.");
};
Thread.ofVirtual().start(task);

```
***Executors.newVirtualThreadPerTaskExecutor()***: Para casos donde necesitas un ***ExecutorService*** que cree un hilo virtual para cada tarea enviada.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        System.out.println("Task 1 on a virtual thread.");
    });
    executor.submit(() -> {
        System.out.println("Task 2 on another virtual thread.");
    });
} // El executor se cierra y espera a que las tareas terminen

```

## Concepto de structured concurrency
### ¿Qué es Structured Concurrency?
La Concurrencia Estructurada es un paradigma que busca traer la misma disciplina y predictibilidad que tenemos en el código secuencial (como if/else, bucles for, try-catch) al código concurrente.

Imagina que cuando llamas a un método en Java, el control del flujo permanece dentro del método hasta que este termina (ya sea que devuelva un valor o lance una excepción). La concurrencia estructurada aplica este mismo principio a las tareas concurrentes:

Una unidad de trabajo que se divide en varias subtareas concurrentes es tratada como una única unidad de trabajo. Su ciclo de vida (inicio, fin, éxito, falla, cancelación) está unido al de su padre.

Piensa en ello como si todas las subtareas concurrentes fueran "hijas" de la tarea principal, y la tarea principal no puede terminar hasta que todas sus hijas hayan terminado (o hayan sido manejadas adecuadamente).


## Ventajas y Desventajas 

| Ventajas                                                      | Desventajas                                                   |
|---------------------------------------------------------------|----------------------------------------------------------------|
| Permiten manejar **millones de hilos simultáneamente**        | No todas las bibliotecas son compatibles aún                  |
| **Muy bajo consumo de memoria** (solo unos pocos KB por hilo) | Debugging y profiling pueden ser más difíciles                |
| Mejoran el rendimiento en tareas de **espera (I/O)**          | Requieren **Java 21 o superior** (versión con soporte completo) |
| No bloquean hilos del sistema operativo                       | Algunos problemas pueden surgir con **hilos nativos o legacy**|
| Facilitan escribir código concurrente sin programación reactiva | La comunidad y herramientas aún están madurando               |
| Se integran fácilmente con código existente                   | Puede haber una curva de aprendizaje para entender su modelo  |
| Evitan el uso excesivo de callbacks y promesas                | **No reemplazan completamente** todos los casos de uso de hilos tradicionales |
| Requieren menos sincronización entre hilos                    | Algunas herramientas de monitoreo aún no están 100% preparadas|
| Escalan mucho mejor en servidores con muchas conexiones       |                                                              |


## Lenguajes con propuestas similares

Python: async/await con asyncio, permite concurrencia usando corutinas. No usa hilos reales, sino un solo hilo con eventos.

Ruby: Fibers y Threads, ruby tiene soporte para Fibers (corutinas ligeras) y algunos runtimes implementan concurrencia ligera.

Erlang: Procesos ligeros (green threads), su modelo de actor usa miles de procesos ligeros gestionados por la VM (BEAM). Muy escalables.


## Opinión: 

Los Virtual Threads de Java me parece que representa un gran avance en el mundo donde hoy vivimos debido a que las aplicaciones que usamos cada dia son cada vez mas concurrentes, desde servidores web hasta microservicios, gracias a la propuesta de los hilos virtuales tenemos una solucion elegante y eficiente al problema de bloqueos y consumo excesivo de recursos. A diferencia de soluciones anteriores como usar framework reactivos o basados en callbacks que pueden llegar a complicar el codigo, los hilos virtuales permiten escribir codigo secuencial y legible, esto reduce bastante la complejidad para los desarrolladores, al igual que mejora la escalabilidad de las aplicaciones, me parece a mi que la propuesta de los Virtual Threads tienen un futuro muy prometedor, si se comienza a usar este paradigma para mejorar la eficiencia y eficacia de los programas se puede decir que estariamos en una nueva era de programacion que sea mas legible, eficiente y sobre todo escalable. 






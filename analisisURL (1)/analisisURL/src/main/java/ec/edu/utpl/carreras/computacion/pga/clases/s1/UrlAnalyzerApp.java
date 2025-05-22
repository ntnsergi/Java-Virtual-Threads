package ec.edu.utpl.carreras.computacion.pga.clases.s1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UrlAnalyzerApp {
    public static void main(String[] args) {
        String inputPath = "C:/Users/Usuario iTC/Documents/4to ciclo/ProgAvanzada/SEMANA 7/taller_investigacion/analisisURL (1)/analisisURL/src/main/resources/data/urls_parcial1.txt";
        String outputPath = "resources/data/resultados.csv";

        List<String> urls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                urls.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Error leyendo archivo: " + e.getMessage());
            return;
        }

        List<UrlAnalyzer> analyzers = new ArrayList<>();

        // Usamos un Executor con virtual threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (String url : urls) {
                UrlAnalyzer analyzer = new UrlAnalyzer(url);
                analyzers.add(analyzer);
                // Enviamos cada tarea al executor
                futures.add(executor.submit(analyzer));
            }

            // Esperar a que todas las tareas terminen
            for (Future<?> f : futures) {
                try {
                    f.get(); // Espera a que cada hilo termine
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error en tarea: " + e.getMessage());
                }
            }

        } // El executor se cierra automáticamente aquí

        // Escribir archivo CSV
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("URL,NumURLsInternas");
            for (UrlAnalyzer analyzer : analyzers) {
                writer.printf("%s,%d%n", analyzer.getUrl(), analyzer.getInternalLinks());
            }
        } catch (IOException e) {
            System.out.println("Error escribiendo archivo: " + e.getMessage());
        }

        System.out.println("Proceso terminado. Revisa el archivo: " + outputPath);
    }
}

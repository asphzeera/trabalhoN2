package org.example;

import org.example.Controller.EditorContext;
import org.example.Controller.PersistenciaService;
import org.example.Models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenciaServiceTest {

    @Test
    @DisplayName("Persistência JSON: Salvar e carregar com fidelidade total")
    void testSalvarECarregarJson(@TempDir Path tempDir) throws IOException {
        PersistenciaService service = new PersistenciaService();
        EditorContext context = new EditorContext();

        Circulo c = new Circulo(new Ponto(50, 60), 25.0);
        c.setCorPreenchimentoHex("#112233");
        c.setCorBordaHex("#445566");
        c.setEspessuraBorda(3.5);
        c.setGroupId("grupo-alfa");

        Quadrado q = new Quadrado(new Ponto(200, 200), 40.0);
        q.setCorPreenchimentoHex("#AABBCC");
        q.setGroupId("grupo-alfa");

        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(10, 10));
        p.adicionarPonto(new Ponto(80, 20));
        p.adicionarPonto(new Ponto(50, 90));

        context.getFormas().add(c);
        context.getFormas().add(q);
        context.getFormas().add(p);
        context.setGridSnappingHabilitado(true);
        context.setGridSize(25.0);

        File arquivo = tempDir.resolve("projeto_teste.json").toFile();

        service.salvarParaArquivo(arquivo, context, 1200, 800);
        assertTrue(arquivo.exists());
        assertTrue(arquivo.length() > 0);

        PersistenciaService.ProjetoDTO dtoCarregado = service.carregarDeArquivo(arquivo);
        assertEquals(1200, dtoCarregado.canvasLargura);
        assertEquals(800, dtoCarregado.canvasAltura);
        assertTrue(dtoCarregado.gridSnapping);
        assertEquals(25.0, dtoCarregado.gridSize);

        List<FormaGeometrica> formasCarregadas = service.converterParaFormas(dtoCarregado);
        assertEquals(3, formasCarregadas.size());

        FormaGeometrica f0 = formasCarregadas.get(0);
        assertTrue(f0 instanceof Circulo);
        Circulo cCarregado = (Circulo) f0;
        assertEquals(50.0, cCarregado.getCentro().getX());
        assertEquals(60.0, cCarregado.getCentro().getY());
        assertEquals(25.0, cCarregado.getRaio());
        assertEquals("#112233", cCarregado.getCorPreenchimentoHex());
        assertEquals("grupo-alfa", cCarregado.getGroupId());

        FormaGeometrica f1 = formasCarregadas.get(1);
        assertTrue(f1 instanceof Quadrado);
        assertEquals("grupo-alfa", f1.getGroupId());

        FormaGeometrica f2 = formasCarregadas.get(2);
        assertTrue(f2 instanceof Poligono);
        assertEquals(3, f2.getQuantidadePontos());
    }

    @Test
    @DisplayName("Persistência JSON: Tratamento de exceção em arquivo corrompido")
    void testArquivoCorrompido(@TempDir Path tempDir) throws IOException {
        PersistenciaService service = new PersistenciaService();
        File arquivoCorrompido = tempDir.resolve("corrompido.json").toFile();

        try (FileWriter fw = new FileWriter(arquivoCorrompido)) {
            fw.write("{ dadosInvalidos: [ este nao eh um json valido ... ");
        }

        assertThrows(Exception.class, () -> service.carregarDeArquivo(arquivoCorrompido));
    }
}

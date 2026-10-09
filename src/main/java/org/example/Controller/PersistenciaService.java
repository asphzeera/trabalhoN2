package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.example.Models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pela serialização e desserialização dos projetos do editor vetorial
 * em formato JSON estruturado, garantindo tolerância a falhas e integridade dos dados.
 */
public class PersistenciaService {

    public static class ProjetoDTO {
        public double canvasLargura;
        public double canvasAltura;
        public boolean gridSnapping;
        public double gridSize;
        public List<FormaDTO> formas;

        public ProjetoDTO() {
            this.formas = new ArrayList<>();
        }
    }

    public static class FormaDTO {
        public String tipo; // "POLIGONO", "CIRCULO", "QUADRADO", "HEXAGONO"
        public String corPreenchimentoHex;
        public String corBordaHex;
        public double espessuraBorda;
        public String groupId;
        public List<PontoDTO> pontos;
        public PontoDTO centro;
        public double raio;

        public FormaDTO() {
            this.pontos = new ArrayList<>();
        }
    }

    public static class PontoDTO {
        public double x;
        public double y;

        public PontoDTO() {}

        public PontoDTO(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private final Gson gson;

    public PersistenciaService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Salva o estado atual do contexto em um arquivo JSON.
     */
    public void salvarParaArquivo(File arquivo, EditorContext context, double canvasLargura, double canvasAltura) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("O arquivo de destino não pode ser nulo.");
        }

        ProjetoDTO projeto = new ProjetoDTO();
        projeto.canvasLargura = canvasLargura;
        projeto.canvasAltura = canvasAltura;
        projeto.gridSnapping = context.isGridSnappingHabilitado();
        projeto.gridSize = context.getGridSize();

        for (FormaGeometrica forma : context.getFormas()) {
            FormaDTO fDto = new FormaDTO();
            fDto.corPreenchimentoHex = forma.getCorPreenchimentoHex();
            fDto.corBordaHex = forma.getCorBordaHex();
            fDto.espessuraBorda = forma.getEspessuraBorda();
            fDto.groupId = forma.getGroupId();

            for (Ponto p : forma.getPontos()) {
                fDto.pontos.add(new PontoDTO(p.getX(), p.getY()));
            }

            if (forma instanceof Circulo) {
                Circulo c = (Circulo) forma;
                fDto.tipo = "CIRCULO";
                fDto.centro = new PontoDTO(c.getCentro().getX(), c.getCentro().getY());
                fDto.raio = c.getRaio();
            } else if (forma instanceof Quadrado) {
                Quadrado q = (Quadrado) forma;
                fDto.tipo = "QUADRADO";
                fDto.centro = new PontoDTO(q.getCentroOriginal().getX(), q.getCentroOriginal().getY());
                fDto.raio = q.getRaioOriginal();
            } else if (forma instanceof Hexagono) {
                Hexagono h = (Hexagono) forma;
                fDto.tipo = "HEXAGONO";
                fDto.centro = new PontoDTO(h.getCentroOriginal().getX(), h.getCentroOriginal().getY());
                fDto.raio = h.getRaioOriginal();
            } else {
                fDto.tipo = "POLIGONO";
            }

            projeto.formas.add(fDto);
        }

        String json = gson.toJson(projeto);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

    /**
     * Carrega e valida um projeto salvo em arquivo JSON.
     */
    public ProjetoDTO carregarDeArquivo(File arquivo) throws IOException, JsonSyntaxException {
        if (arquivo == null || !arquivo.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado.");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha).append("\n");
            }
        }

        ProjetoDTO projeto = gson.fromJson(sb.toString(), ProjetoDTO.class);
        if (projeto == null || projeto.formas == null) {
            throw new IOException("O conteúdo do arquivo JSON está corrompido ou incompleto.");
        }

        return projeto;
    }

    /**
     * Converte os dados carregados de volta para objetos de domínio FormaGeometrica.
     */
    public List<FormaGeometrica> converterParaFormas(ProjetoDTO projeto) {
        List<FormaGeometrica> lista = new ArrayList<>();
        for (FormaDTO fDto : projeto.formas) {
            FormaGeometrica forma;
            if ("CIRCULO".equalsIgnoreCase(fDto.tipo)) {
                Ponto centro = (fDto.centro != null) ? new Ponto(fDto.centro.x, fDto.centro.y) : new Ponto(100, 100);
                double raio = (fDto.raio > 0) ? fDto.raio : 30.0;
                forma = new Circulo(centro, raio);
            } else if ("QUADRADO".equalsIgnoreCase(fDto.tipo)) {
                Ponto centro = (fDto.centro != null) ? new Ponto(fDto.centro.x, fDto.centro.y) : new Ponto(100, 100);
                double raio = (fDto.raio > 0) ? fDto.raio : 30.0;
                forma = new Quadrado(centro, raio);
            } else if ("HEXAGONO".equalsIgnoreCase(fDto.tipo)) {
                Ponto centro = (fDto.centro != null) ? new Ponto(fDto.centro.x, fDto.centro.y) : new Ponto(100, 100);
                double raio = (fDto.raio > 0) ? fDto.raio : 30.0;
                forma = new Hexagono(centro, raio);
            } else {
                Poligono pol = new Poligono();
                if (fDto.pontos != null) {
                    for (PontoDTO pDto : fDto.pontos) {
                        pol.adicionarPonto(new Ponto(pDto.x, pDto.y));
                    }
                }
                forma = pol;
            }

            if (fDto.corPreenchimentoHex != null) forma.setCorPreenchimentoHex(fDto.corPreenchimentoHex);
            if (fDto.corBordaHex != null) forma.setCorBordaHex(fDto.corBordaHex);
            if (fDto.espessuraBorda > 0) forma.setEspessuraBorda(fDto.espessuraBorda);
            forma.setGroupId(fDto.groupId);

            lista.add(forma);
        }
        return lista;
    }
}

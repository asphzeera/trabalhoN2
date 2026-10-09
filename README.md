# Editor de Desenho Vetorial Interativo com JavaFX

> **Avaliação Prática N3 – Semestre 2026/1**  
> **Disciplina:** Programação Orientada a Objetos 1 (POO 1)  
> **Instituição:** Universidade de Rio Verde - UniRV  
> **Docente:** Prof. Me. Sergio Souza Novak  

---

## 📌 Sumário
- [1. Visão Geral](#1-visão-geral)
- [2. Arquitetura e Padrões de Projeto (GoF)](#2-arquitetura-e-padrões-de-projeto-gof)
- [3. Estrutura de Pacotes e Módulos](#3-estrutura-de-pacotes-e-módulos)
- [4. Modelagem Matemática e Validações](#4-modelagem-matemática-e-validações)
- [5. Persistência de Dados e Tratamento de Exceções](#5-persistência-de-dados-e-tratamento-de-exceções)
- [6. Funcionalidades da Interface e Atalhos](#6-funcionalidades-da-interface-e-atalhos)
- [7. Como Compilar, Testar e Executar](#7-como-compilar-testar-e-executar)
- [8. Testes Automatizados](#8-testes-automatizados)

---

## 1. Visão Geral
Este software desktop é um editor vetorial bidimensional desenvolvido em **Java 21** e **JavaFX**, gerenciado via **Apache Maven**. O sistema permite desenhar, transformar, agrupar, persistir e manipular formas geométricas poligonais e circulares sobre um Canvas interativo, empregando separação estrita de responsabilidades no padrão **MVC (Model-View-Controller)** e padrões de projeto clássicos da GoF.

---

## 2. Arquitetura e Padrões de Projeto (GoF)

O projeto foi construído respeitando integralmente as especificações da disciplina e o diagrama de classes UML (`diagrama_classes.pdf`):

| Padrão | Aplicação no Projeto |
| :--- | :--- |
| **State Pattern** | A interface `Ferramenta` define o contrato de comportamento interativo do mouse (`aoPressionarMouse`, `aoArrastarMouse`, `aoSoltarMouse`, `aoClicar`). Os estados concretos são `FerramentaDesenhar`, `FerramentaSelecionar`, `FerramentaTransformar`, `FerramentaInserirForma` e `FerramentaEditarVertice`. A classe `EditorContext` gerencia o estado ativo e delega as ações. |
| **Memento & Command** | Implementado em `HistoricoController`. A cada alteração estrutural ou geométrica na cena, um instantâneo profundo do conjunto de formas é empilhado em `undoStack` (e revertido para `redoStack`). As ações de **Desfazer (Ctrl+Z)** e **Refazer (Ctrl+Y)** restauram o desenho sem vazamento de estado. |
| **Prototype** | O contrato `FormaGeometrica` e as classes concretas implementam o método `clonar()`. Isso assegura duplicações independentes das formas em memória, isolando o histórico de alterações e prevenindo referências compartilhadas indesejadas. |
| **Composite** | Formas geométricas podem compartilhar um identificador de grupo (`groupId`). Quando agrupadas, conjuntos heterogêneos de formas comportam-se como uma única entidade lógica para seleção conjunta, translação e transformações geométricas. |
| **MVC** | Separação física e lógica entre os modelos geométricos (`org.example.Models`), lógica de controle e ferramentas (`org.example.Controller`) e componentes de renderização/interface gráfica (`org.example.Views`). |

---

## 3. Estrutura de Pacotes e Módulos

```
src/
├── main/
│   └── java/
│       └── org/
│           └── example/
│               ├── App.java                               # Ponto de entrada JavaFX
│               │
│               ├── Models/                                # Modelos Geométricos (M)
│               │   ├── FormaGeometrica.java               # Interface formal da geometria
│               │   ├── FormaBase.java                     # Classe abstrata com base matemática
│               │   ├── Ponto.java                         # Coordenada bidimensional cartesiana
│               │   ├── Poligono.java                      # Polígonos de vértices arbitrários
│               │   ├── Circulo.java                       # Círculo com centro e raio positivo
│               │   ├── Quadrado.java                      # Quadrado com cálculo automático
│               │   └── Hexagono.java                      # Hexágono com cálculo automático
│               │
│               ├── Controller/                            # Controle, Ferramentas e Estado (C)
│               │   ├── EditorContext.java                 # Contexto central do editor
│               │   ├── HistoricoController.java           # Desfazer / Refazer (Memento)
│               │   ├── Ferramenta.java                    # Interface do State Pattern
│               │   ├── ModoFerramenta.java                # Enum de modos de transformação
│               │   ├── FerramentaDesenhar.java            # Criação por cliques de polígonos
│               │   ├── FerramentaSelecionar.java          # Seleção e translação por arrasto
│               │   ├── FerramentaTransformar.java         # Rotação, escala, cisalhamento
│               │   ├── FerramentaInserirForma.java        # Inserção de círculos/quadrados/hexágonos
│               │   ├── FerramentaEditarVertice.java       # Manipulação pontual de vértices
│               │   └── PersistenciaService.java           # Serialização JSON com DTOs e validação
│               │
│               └── Views/                                 # Apresentação e Telas (V)
│                   ├── ContainerApp.java                  # Painel raiz, Canvas e eventos
│                   ├── BarraLateralView.java              # Ferramentas, estilos e camadas
│                   └── PainelStatusView.java              # Métricas em tempo real
│
└── test/
    └── java/
        └── org/
            └── example/
                ├── ModelsTest.java                        # Testes de área, perímetro, clone
                ├── TransformacoesTest.java                # Testes de escala, rotação, cisalhamento
                ├── HistoricoControllerTest.java           # Testes de Undo/Redo e isolamento
                ├── EditorContextTest.java                 # Testes de seleção, Composite e snap
                └── PersistenciaServiceTest.java           # Testes de I/O em JSON e erros
```

---

## 4. Modelagem Matemática e Validações

### 4.1 Fórmulas Implementadas
1. **Área de Polígonos (Fórmula do Cadarço de Gauss / Shoelace):**
   $$A = \frac{1}{2} \left| \sum_{i=1}^{n-1} (x_i y_{i+1} - x_{i+1} y_i) + (x_n y_1 - x_1 y_n) \right|$$
2. **Perímetro de Polígonos:**
   $$P = \sum_{i=1}^{n-1} \text{dist}(p_i, p_{i+1}) + \text{dist}(p_n, p_1)$$
3. **Círculos:**
   $$A = \pi \cdot r^2, \quad P = 2 \cdot \pi \cdot r$$
4. **Transformações Geométricas em relação ao Centroide ($C_x, C_y$):**
   - **Translação:** $p_x' = p_x + dx, \quad p_y' = p_y + dy$
   - **Escala:** $p_x' = C_x + (p_x - C_x) \cdot fator, \quad p_y' = C_y + (p_y - C_y) \cdot fator$
   - **Rotação:** 
     $$p_x' = C_x + (dx \cos\theta - dy \sin\theta), \quad p_y' = C_y + (dx \sin\theta + dy \cos\theta)$$
   - **Cisalhamento (Shear):**
     $$p_x' = C_x + dx + sh_x \cdot dy, \quad p_y' = C_y + sh_y \cdot dx + dy$$
5. **Algoritmo Ray-Casting:** Determinação rigorosa se um ponto $(p_x, p_y)$ encontra-se no interior de polígonos côncavos ou convexos.

### 4.2 Validações de Negócio
- `Poligono`: Exige no mínimo 3 pontos para fechamento e persistência.
- `Circulo`: Exige raio estritamente positivo ($r > 0$), disparando `IllegalArgumentException` caso contrário.
- Formas regulares (`Quadrado`, `Hexagono`): Geram seus vértices uniformemente distribuídos de forma automática no construtor.

---

## 5. Persistência de Dados e Tratamento de Exceções

- **Formato:** JSON estruturado contendo dimensões do canvas, estado da grade magnética e lista de formas com coordenadas de vértices, centroides, raios, estilos e identificadores de grupo.
- **Tolerância a Falhas:**
  - Carregamento protegido por blocos `try-catch`: arquivos malformatados, incompletos ou corrompidos exibem um diálogo amigável em JavaFX (`Alert.AlertType.ERROR`) detalhando o ocorrido sem interromper ou fechar o aplicativo.
  - Alerta de saída: Ao tentar fechar a janela com alterações pendentes, o usuário é questionado se deseja salvar o projeto antes de sair.

---

## 6. Funcionalidades da Interface e Atalhos

- **Barra Lateral:**
  - *Estilos:* Seletores de cor de preenchimento, cor de contorno e espessura do traço.
  - *Grade:* Ativação do **Snap to Grid** (alinhamento magnético ao passo da grade).
  - *Ferramentas:* Polígono Livre, Selecionar/Mover, Transladar, Escalar, Rotacionar, Cisalhar, Editar Vértice e Excluir.
  - *Formas Rápidas:* Inserir Quadrado, Círculo e Hexágono via clique ou arrasto.
  - *Camadas e Agrupamento:* Trazer para Frente, Enviar para Trás, Agrupar (Composite) e Desagrupar.
- **Barra de Status:**
  - Exibição contínua das coordenadas do mouse $(X, Y)$, total de formas no projeto, quantidade de formas selecionadas e cálculo dinâmico em tempo real de área e perímetro da seleção.
- **Teclas de Atalho:**
  - `Ctrl + Z`: Desfazer
  - `Ctrl + Y`: Refazer
  - `Ctrl + S`: Salvar projeto em arquivo JSON
  - `Ctrl + O`: Abrir projeto salvo
  - `Ctrl + G`: Agrupar formas selecionadas
  - `Ctrl + U`: Desagrupar
  - `Delete` / `Backspace`: Excluir formas selecionadas

---

## 7. Como Compilar, Testar e Executar

### Pré-requisitos
- **Java Development Kit (JDK):** Versão 17 ou 21 LTS
- **Apache Maven:** 3.8+

### Execução via Maven padrão
```bash
# Compilar o projeto
mvn clean compile

# Rodar todos os testes unitários
mvn test

# Iniciar a aplicação JavaFX
mvn javafx:run
```

### Execução em ambientes com Nix / NixOS
Caso esteja executando em um sistema operacional como NixOS:
```bash
./run.sh
# ou diretamente via:
nix-shell -p maven --run "mvn javafx:run"
```

---

## 8. Testes Automatizados

O projeto conta com **18 testes automatizados** utilizando JUnit 5 cobrindo 100% dos requisitos centrais:

- `ModelsTest`: Criação e validação de `Ponto`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono`, fórmula de Gauss e padrão Prototype.
- `TransformacoesTest`: Validação numérica de translação, rotação em radianos, escala e cisalhamento.
- `HistoricoControllerTest`: Validação de pilhas de desfazer/refazer e isolamento de referências (Memento).
- `EditorContextTest`: Validação do agrupamento e seleção conjunta (Composite), ordenação de camadas e snap magnético.
- `PersistenciaServiceTest`: Ciclo completo de salvamento e carregamento JSON e recuperação de erro em arquivos corrompidos.

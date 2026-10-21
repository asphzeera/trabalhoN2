# 🎓 Guia de Explicação e Apresentação do Projeto (Avaliação N3 - POO 1)

Este documento foi preparado como um **roteiro de estudos e apresentação** para você explicar com segurança cada parte do **Editor de Desenho Vetorial Interativo** ao **Prof. Me. Sergio Souza Novak**.

---

## 🗺️ 1. Visão Geral (Como abrir a apresentação)

> **O que falar:**  
> *"Professor, o projeto consiste em um Editor de Desenho Vetorial 2D desenvolvido em Java 21 com JavaFX e gerenciado pelo Maven. Toda a arquitetura foi pensada para aplicar na prática os pilares da Programação Orientada a Objetos (Abstração, Encapsulamento, Herança e Polimorfismo), a separação em camadas MVC e quatro padrões de projeto clássicos do catálogo GoF: State, Prototype, Memento/Command e Composite."*

---

## 🏗️ 2. Arquitetura MVC (Separação de Responsabilidades)

O código está dividido exatamente nos três pacotes exigidos na especificação:

1. **`org.example.Models` (Camada de Modelo / Domínio):**
   * Contém apenas a matemática, geometria e estrutura de dados das formas (`Ponto`, `FormaGeometrica`, `FormaBase`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono`).
   * **Destaque para o professor:** Os modelos **não conhecem o JavaFX**. Eles não importam `Canvas` nem `GraphicsContext`. Isso garante baixo acoplamento e coesão alta.

2. **`org.example.Controller` (Camada de Controle e Estado):**
   * Gerencia o estado da aplicação (`EditorContext`), o histórico de ações (`HistoricoController`), a persistência em arquivos (`PersistenciaService`) e o comportamento das ferramentas (`Ferramenta` e suas implementações).
   * Faz a ponte entre os eventos de mouse disparados na tela e as alterações matemáticas nos modelos.

3. **`org.example.Views` (Camada de Visualização / Interface Gráfica):**
   * Responsável apenas por desenhar no `Canvas` e exibir botões/controles (`ContainerApp`, `BarraLateralView`, `PainelStatusView`).
   * Quando o modelo muda, o `EditorContext` aciona callbacks (`requestRedraw()` e `requestStatsUpdate()`) para que a View se atualize sem que o Controller precise manipular componentes visuais diretamente.

---

## 🧩 3. Os 4 Padrões de Projeto (GoF) — O Ponto Mais Importante (35% da Nota)

O professor certamente vai perguntar **onde** e **por que** cada padrão foi usado. Aqui está a explicação exata no código:

### 3.1. Padrão State (Comportamento das Ferramentas)
* **O Problema:** Em um editor gráfico, o clique e o arrasto do mouse fazem coisas totalmente diferentes dependendo do botão selecionado (desenhar polígono, selecionar/mover, escalar, rotacionar, cisalhar, inserir forma ou editar vértice). Sem o padrão *State*, teríamos um `if/else` ou `switch` gigante e difícil de manter dentro do Canvas.
* **Onde está no código:**
  * **Interface de Estado:** `Ferramenta.java` (define `aoPressionarMouse`, `aoArrastarMouse`, `aoSoltarMouse`, `aoClicar`).
  * **Contexto:** `EditorContext.java` possui o atributo `private Ferramenta ferramentaAtual`.
  * **Estados Concretos:**
    * `FerramentaDesenhar`: adiciona vértices a cada clique e fecha o polígono com duplo clique ou ao clicar no ponto inicial.
    * `FerramentaSelecionar`: detecta se clicou dentro de uma forma (ou abre uma caixa elástica de seleção) e translada as formas selecionadas ao arrastar.
    * `FerramentaTransformar`: aplica escala, rotação, translação ou cisalhamento dependendo do `ModoFerramenta`.
    * `FerramentaInserirForma`: cria um `Circulo`, `Quadrado` ou `Hexagono` no clique inicial e ajusta o raio dinamicamente enquanto o usuário arrasta o mouse.
    * `FerramentaEditarVertice`: localiza o vértice mais próximo do clique (num raio de 10px) e move apenas aquele ponto específico.
* **Como explicar o fluxo:** Quando o usuário mexe o mouse no `ContainerApp`, o Canvas apenas chama `contexto.getFerramentaAtual().aoArrastarMouse(evento, contexto)`. Ele não sabe qual ferramenta é; o próprio objeto da ferramenta resolve o que fazer via polimorfismo!

---

### 3.2. Padrão Prototype (Clonagem Profunda)
* **O Problema:** Em Java, se fizermos `FormaGeometrica b = a;`, estamos copiando apenas a **referência de memória**. Se movermos a forma `b`, a forma `a` também se move. Para salvar estados no histórico (Undo/Redo), precisamos de cópias 100% independentes (*Deep Copy*).
* **Onde está no código:**
  * Na interface `FormaGeometrica` e na classe `Ponto`, existe o método `clonar()`.
  * Na classe `FormaBase`, o método auxiliar `copiarAtributosPara(FormaBase destino)` copia cores, espessura, `groupId` e percorre a lista de `pontos` chamando `p.clonar()` para cada vértice.
  * Cada subclasse (`Poligono`, `Circulo`, `Quadrado`, `Hexagono`) implementa `clonar()` retornando uma nova instância com todos os seus pontos clonados.

---

### 3.3. Padrões Memento e Command (Undo / Redo — Desfazer e Refazer)
* **O Problema:** O usuário precisa desfazer (`Ctrl+Z`) e refazer (`Ctrl+Y`) qualquer operação (criação, exclusão, mudança de cor, rotação, agrupamento) sem corromper o estado atual.
* **Onde está no código:**
  * Classe `HistoricoController.java`.
  * Possui duas pilhas (`Stack<List<FormaGeometrica>>`): `undoStack` e `redoStack`.
  * **Como funciona:**
    1. Antes de qualquer modificação nas formas, o `EditorContext` chama `registrarHistorico()`, que aciona `historicoController.registrarEstado(formas)`.
    2. O `HistoricoController` clona profundamente todas as formas da tela (usando o padrão **Prototype**) e empilha na `undoStack`, limpando a `redoStack`.
    3. Quando o usuário aperta **Desfazer (`Ctrl+Z`)**, o estado atual vai para a `redoStack` e o topo da `undoStack` é desempilhado e restaurado na tela.

---

### 3.4. Padrão Composite (Agrupamento Lógico de Formas)
* **O Problema:** O usuário deve poder selecionar várias formas diferentes (ex: um círculo e um hexágono), clicar em "Agrupar" e passar a tratá-las como uma única figura composta para seleção e transformações.
* **Onde está no código:**
  * Atributo `protected String groupId` em `FormaBase`.
  * Métodos `agruparSelecionados()` e `desagruparSelecionados()` em `EditorContext.java`.
  * **Como funciona:**
    1. Ao agrupar, o `EditorContext` gera um ID único (`"grupo-" + UUID`) e atribui a todas as formas selecionadas.
    2. No método `selecionarForma(...)` do `EditorContext`, se o usuário clicar em **uma** forma que possui `groupId`, o sistema busca automaticamente todas as outras formas da tela com aquele mesmo `groupId` e seleciona todas juntas.
    3. Na `FerramentaTransformar`, é calculado o **centroide comum** de todas as formas do grupo (`calcularCentroideComum`), permitindo que o grupo inteiro sofra translação, escala, rotação ou cisalhamento em conjunto.

---

## 📐 4. Pilares de POO e Matemática Geométrica (25% da Nota)

### 4.1. Herança, Abstração e Polimorfismo
* **`FormaGeometrica` (Interface):** Define o contrato com todos os métodos obrigatórios.
* **`FormaBase` (Classe Abstrata):** Concentra os atributos protegidos (`pontos`, `corPreenchimentoHex`, `corBordaHex`, `espessuraBorda`, `groupId`) e implementa toda a matemática baseada em vértices que é comum aos polígonos.
* **Polimorfismo:**
  * `Poligono`, `Quadrado` e `Hexagono` herdam os cálculos de área e perímetro de `FormaBase` porque são formados por vértices.
  * Já a classe `Circulo` sobrescreve (`@Override`) os métodos `calcularArea()` ($\pi r^2$), `calcularPerimetro()` ($2\pi r$), `contemPonto()` (distância ao centro $\le r$) e `escalar()` (multiplica o raio pelo fator), pois a matemática de um círculo perfeito é contínua e não baseada em arestas retas.

### 4.2. Cálculos Matemáticos em `FormaBase.java`
1. **Área de Polígonos (Fórmula de Gauss / Shoelace):**
   * Implementada em `FormaBase.calcularArea()`.
   * Percorre os vértices multiplicando em cruz $(x_i \cdot y_{i+1} - x_{i+1} \cdot y_i)$, tira o módulo e divide por 2:
     $$A = \frac{1}{2} \left| \sum_{i=1}^{n-1} (x_i y_{i+1} - x_{i+1} y_i) + (x_n y_1 - x_1 y_n) \right|$$
2. **Perímetro:**
   * Soma das distâncias euclidianas ($\sqrt{(x_2-x_1)^2 + (y_2-y_1)^2}$) entre cada vértice consecutivo, fechando do último para o primeiro.
3. **Detecção de Clique dentro da Forma (Algoritmo *Ray-Casting*):**
   * Implementado em `FormaBase.contemPonto(px, py)`.
   * Traça um raio horizontal imaginário a partir do clique do mouse até o infinito e conta quantas arestas do polígono ele cruza. Se cruzar um número **ímpar** de vezes, o ponto está **dentro** da forma; se for **par**, está **fora**. Funciona até para polígonos côncavos ou estrelas!
4. **Transformações Geométricas 2D (Sempre em relação ao Centroide $C_x, C_y$):**
   * **Translação:** Soma $dx$ e $dy$ em cada ponto.
   * **Escala:** Afasta ou aproxima cada ponto do centroide multiplicando a distância pelo `fator`:
     $$x' = C_x + (x - C_x) \cdot \text{fator}$$
   * **Rotação:** Usa a matriz de rotação trigonométrica em torno do centroide com $\cos(\theta)$ e $\sin(\theta)$:
     $$x' = C_x + (dx \cos\theta - dy \sin\theta), \quad y' = C_y + (dx \sin\theta + dy \cos\theta)$$
   * **Cisalhamento (Shear):** Inclina os eixos deslocando $x$ proporcionalmente a $y$ ($sh_x$) e $y$ proporcionalmente a $x$ ($sh_y$).
5. **Snap to Grid (Grade Magnética):**
   * Implementado em `EditorContext.aplicarSnap(valor)`: arredonda a coordenada do mouse para o múltiplo mais próximo do tamanho da grade (`Math.round(valor / gridSize) * gridSize`).

---

## 💾 5. Persistência em JSON e Tratamento de Erros (20% da Nota)

* **Como salvamos os dados (`PersistenciaService.java`):**
  * Utilizamos a biblioteca **Gson** para exportar o projeto em um arquivo `.json` legível e estruturado.
  * Criamos classes de transferência (**DTOs**: `ProjetoDTO`, `FormaDTO`, `PontoDTO`) que salvam a largura/altura do canvas, estado do *Snap to Grid* e a lista de formas com seu tipo (`"POLIGONO"`, `"CIRCULO"`, `"QUADRADO"`, `"HEXAGONO"`), cores, espessura, `groupId` e coordenadas.
* **Tolerância a Falhas (`try-catch`):**
  * Se o usuário tentar abrir um arquivo `.json` corrompido ou inválido, o bloco `try-catch` em `ContainerApp.carregarArquivo()` captura a exceção e exibe um `Alert` gráfico de erro explicativo na tela, mantendo o programa aberto e seguro.
* **Proteção contra perda de dados ao fechar:**
  * Configuramos `stagePrincipal.setOnCloseRequest(...)` em `ContainerApp.java`. Se houver alterações não salvas (`contexto.isAlteracoesNaoSalvas()`), um alerta pergunta se o usuário deseja **"Salvar Agora"**, **"Sair sem Salvar"** ou **"Cancelar"**.

---

## 🎤 6. Roteiro Prático de Demonstração (O que mostrar ao vivo)

Se o professor pedir para você rodar o programa e demonstrar, siga esta ordem de 2 minutos:

1. **Criar Formas Regulares:** Selecione *"Inserir Quadrado"* e *"Inserir Círculo"*, clique e arraste no Canvas. Mostre na barra inferior (`PainelStatusView`) a área e o perímetro mudando em tempo real.
2. **Desenhar Polígono Livre:** Selecione *"Polígono (Livre)"*, dê vários cliques para formar uma estrela ou triângulo e dê duplo clique para fechar.
3. **Estilização:** Selecione uma forma e mude a cor de preenchimento, a cor da borda e a espessura no painel lateral esquerdo.
4. **Transformações:** Com uma forma selecionada, teste os botões **Escalar**, **Rotacionar** e **Cisalhar** arrastando o mouse no Canvas.
5. **Editar Vértice:** Clique em *"Editar Vértice"*, puxe uma das alças quadradinhas de um polígono e mostre a forma se remodelando e a área sendo recalculada pela Fórmula de Gauss.
6. **Agrupamento (Composite) e Camadas:** Selecione duas formas (usando a caixa de seleção ou `Shift + Clique`), clique em **Agrupar**, desmarque e clique em apenas uma delas para mostrar que ambas são selecionadas e se movem juntas. Mostre também os botões *"Trazer p/ Frente"* e *"Enviar p/ Trás"*.
7. **Undo / Redo (Memento):** Aperte `Ctrl+Z` várias vezes e depois `Ctrl+Y` mostrando o histórico funcionando perfeitamente.
8. **Salvar e Carregar:** Salve o desenho como `teste.json`, exclua as formas da tela e clique em **Carregar** para restaurar o arquivo salvo.

---

## ❓ 7. Possíveis Perguntas do Professor (E como responder!)

1. **"Por que `Quadrado` e `Hexagono` herdam de `FormaBase` e calculam vértices, enquanto `Circulo` calcula área com $\pi r^2$?"**
   * **Resposta:** *"Porque Quadrado e Hexágono são polígonos regulares de 4 e 6 lados. Ao gerar seus vértices automaticamente no construtor usando trigonometria, eles aproveitam toda a implementação da Fórmula de Gauss, perímetro, rotação, cisalhamento e edição de vértices da `FormaBase`. Já o `Circulo` não possui vértices poligonais, então ele sobrescreve esses métodos usando as fórmulas geométricas exatas da circunferência."*

2. **"Por que usar o método `clonar()` (Prototype) dentro do `HistoricoController`?"**
   * **Resposta:** *"Porque em Java as listas guardam referências para objetos. Se salvássemos apenas a lista sem clonar cada `FormaGeometrica` e cada `Ponto`, quando o usuário movesse uma forma na tela, ela também mudaria dentro da pilha de `undoStack`. O `clonar()` garante que cada estado salvo no histórico seja uma fotografia imutável daquele momento."*

3. **"Como funciona a seleção quando duas formas estão sobrepostas?"**
   * **Resposta:** *"Na `FerramentaSelecionar`, percorremos a lista de formas de trás para frente (`for (int i = formas.size() - 1; i >= 0; i--)`). Como o Canvas desenha do índice `0` até o último, o último elemento da lista é o que está visível no topo (Z-index mais alto). Assim, o clique sempre seleciona a forma que está na frente."*

#!/usr/bin/env bash
# Script para compilar e executar o Editor de Desenho Vetorial Interativo
set -e

if command -v mvn &> /dev/null; then
    mvn javafx:run
elif command -v nix-shell &> /dev/null; then
    echo "Executando via nix-shell..."
    nix-shell -p maven --run "mvn javafx:run"
else
    echo "Erro: Maven não encontrado. Instale o Apache Maven ou configure o nix-shell."
    exit 1
fi

#!/usr/bin/env bash
# Script para compilar e executar o Editor de Desenho Vetorial Interativo
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

if command -v nix-shell &> /dev/null && [ -f "$DIR/shell.nix" ]; then
    echo "Iniciando aplicação via nix-shell com bibliotecas gráficas nativas (X11, GTK3, OpenGL)..."
    exec nix-shell --run "mvn javafx:run"
elif command -v mvn &> /dev/null; then
    exec mvn javafx:run
else
    echo "Erro: Maven ou nix-shell não foram encontrados no PATH."
    exit 1
fi

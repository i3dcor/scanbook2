#!/bin/bash
set -e

# 1. Obtener nombre de la rama
CURRENT_BRANCH=$(git.arch branch --show-current | tr -d '\r\n')

if [ "$CURRENT_BRANCH" == "main" ]; then
  echo "❌ Estás en main. Crea una rama primero. p.ej. git checkout -b feature/new-feature"
  exit 1
fi

echo "🚀 Procesando rama: $CURRENT_BRANCH"

# 2. Push
git.arch push -u origin "$CURRENT_BRANCH"

# 3. Crear PR (si ya existe, gh avisa y sigue sin error)
# El "|| true" evita que el script se pare si la PR ya existe y solo quieres hacer merge
gh.arch pr create --fill --base main --head "$CURRENT_BRANCH" || true

sleep 2

# 4. Merge y borrado REMOTO
gh.arch pr merge --merge --delete-branch --auto

# 5. Volver a main y actualizar
git.arch checkout main
git.arch pull

# --- NUEVO PASO ---
# 6. Borrado LOCAL para liberar el nombre
echo "🧹 Limpiando rama local..."
git.arch branch -D "$CURRENT_BRANCH"

echo "🎉 ¡Listo! Rama '$CURRENT_BRANCH' eliminada local y remotamente."
echo "Puedes volver a crearla con 'git checkout -b $CURRENT_BRANCH' cuando quieras."

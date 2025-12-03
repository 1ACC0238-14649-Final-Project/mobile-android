#!/bin/bash

# Script para obtener el Google ID Token desde Logcat
# Uso: ./get_google_token.sh

echo "🔍 Esperando token de Google Sign-In..."
echo "📱 Inicia sesión con Google en la app ahora..."
echo ""

# Filtrar logs de Google Sign-In y buscar el ID Token
adb logcat -c  # Limpiar logs anteriores
adb logcat | grep --line-buffered "ID Token:" | while read line; do
    # Extraer el token de la línea
    token=$(echo "$line" | sed -n 's/.*ID Token: \(.*\)/\1/p')
    if [ ! -z "$token" ]; then
        echo ""
        echo "✅ Token encontrado:"
        echo "$token"
        echo ""
        echo "📋 Token copiado al portapapeles (si tienes pbcopy en macOS)"
        echo "$token" | pbcopy 2>/dev/null || echo "$token" | xclip -selection clipboard 2>/dev/null || echo "No se pudo copiar automáticamente"
        echo ""
        echo "💡 Puedes usar este token en Swagger para probar el endpoint"
        exit 0
    fi
done


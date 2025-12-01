#!/bin/bash

# Script para distribuir la aplicación Android a Firebase App Distribution
# Uso: ./distribute.sh [release_notes]

set -e

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Iniciando distribución a Firebase App Distribution${NC}"

# Verificar que Firebase App ID esté configurado
if [ -z "$FIREBASE_APP_ID" ]; then
    echo -e "${YELLOW}⚠️  FIREBASE_APP_ID no está configurado como variable de entorno${NC}"
    echo -e "${YELLOW}   Por favor, configura tu App ID en app/build.gradle.kts o como variable de entorno${NC}"
    echo -e "${YELLOW}   Ejemplo: export FIREBASE_APP_ID='1:123456789:android:abcdef123456'${NC}"
    read -p "¿Deseas continuar de todas formas? (s/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        exit 1
    fi
fi

# Verificar autenticación de Firebase
if ! firebase projects:list &>/dev/null; then
    echo -e "${YELLOW}⚠️  No estás autenticado con Firebase${NC}"
    echo -e "${YELLOW}   Ejecutando: firebase login${NC}"
    firebase login
fi

# Obtener notas de release
RELEASE_NOTES="${1:-Build generado automáticamente - $(date '+%Y-%m-%d %H:%M:%S')}"

# Exportar notas de release como variable de entorno
export RELEASE_NOTES="$RELEASE_NOTES"

echo -e "${GREEN}📝 Notas de release: $RELEASE_NOTES${NC}"
echo ""

# Limpiar builds anteriores (opcional)
read -p "¿Deseas limpiar builds anteriores? (s/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo -e "${GREEN}🧹 Limpiando proyecto...${NC}"
    ./gradlew clean
fi

# Compilar APK de release
echo -e "${GREEN}🔨 Compilando APK de release...${NC}"
./gradlew assembleRelease

# Verificar que el APK se generó correctamente
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ Error: No se encontró el APK en $APK_PATH${NC}"
    exit 1
fi

APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
echo -e "${GREEN}✅ APK generado exitosamente: $APK_PATH (Tamaño: $APK_SIZE)${NC}"
echo ""

# Distribuir a Firebase App Distribution
echo -e "${GREEN}📤 Distribuyendo a Firebase App Distribution...${NC}"
./gradlew appDistributionUploadRelease

echo ""
echo -e "${GREEN}✅ ¡Distribución completada exitosamente!${NC}"
echo -e "${GREEN}   Los testers recibirán una notificación por email${NC}"
echo -e "${GREEN}   Puedes verificar el estado en Firebase Console${NC}"


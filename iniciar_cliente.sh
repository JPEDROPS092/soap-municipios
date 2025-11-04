#!/bin/bash
# Script para iniciar o cliente de terminal

cd "$(dirname "$0")/cliente"

echo "======================================================================"
echo "  SISTEMA DE CONSULTA DE MUNICÍPIOS E UBS"
echo "======================================================================"
echo ""
echo "Verificando conexão com o servidor SOAP..."
echo ""

# Verifica se o servidor está rodando
if ! curl -s http://localhost:8080/municipios?wsdl > /dev/null 2>&1; then
    echo "✗ ERRO: Servidor SOAP não está rodando!"
    echo ""
    echo "Por favor, inicie o servidor primeiro em outro terminal:"
    echo "  ./iniciar_servidor.sh"
    echo ""
    echo "Ou execute manualmente:"
    echo "  cd servidor"
    echo "  java -jar target/soap-servidor-1.0.0.jar"
    echo ""
    exit 1
fi

echo "✓ Servidor SOAP está ativo!"
echo ""
echo "Iniciando aplicação cliente..."
echo ""
sleep 1

java -jar target/soap-cliente-1.0.0.jar

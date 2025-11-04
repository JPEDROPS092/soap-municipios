#!/bin/bash
# Script para iniciar o servidor SOAP

cd "$(dirname "$0")/servidor"

echo "======================================================================"
echo "  INICIANDO SERVIDOR SOAP - MUNICÍPIOS E UBS"
echo "======================================================================"
echo ""
echo "O servidor SOAP será iniciado na porta 8080"
echo "Mantenha este terminal aberto enquanto usa a aplicação cliente"
echo ""
echo "Para parar o servidor, pressione Ctrl+C"
echo ""
echo "======================================================================"
echo ""

java -jar target/soap-servidor-1.0.0.jar

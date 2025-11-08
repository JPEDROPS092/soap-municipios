"""
Script de teste rápido para verificar a classificação de UBS
"""

import requests
from xml.etree import ElementTree as ET

SOAP_URL = "http://0.0.0.0:8080/ws/municipios"

def testar_classificacao_ubs():
    """Testa a classificação de estabelecimentos"""
    
    # Fazer requisição
    soap_request = '''<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" 
               xmlns:ser="http://service.soap.municipios.com/">
  <soap:Body>
    <ser:listarUBSMunicipio>
      <municipioId>1302603</municipioId>
      <municipioNome>Manaus</municipioNome>
    </ser:listarUBSMunicipio>
  </soap:Body>
</soap:Envelope>'''
    
    headers = {
        'Content-Type': 'text/xml; charset=utf-8',
        'SOAPAction': '""'
    }
    
    response = requests.post(SOAP_URL, data=soap_request, headers=headers)
    root = ET.fromstring(response.text)
    
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    ret = root.find('.//ns2:listarUBSMunicipioResponse/return', ns)
    
    print("=" * 80)
    print("TESTE DE CLASSIFICAÇÃO DE ESTABELECIMENTOS - MANAUS")
    print("=" * 80)
    
    total_ubs = 0
    total_outros = 0
    
    exemplos_ubs = []
    exemplos_outros = []
    
    for item in ret.findall('listaUbs'):
        nome = item.find('nome').text or ''
        nome_upper = nome.upper()
        
        if 'UBS' in nome_upper or 'UNIDADE BASICA' in nome_upper or 'UNIDADE BÁSICA' in nome_upper:
            total_ubs += 1
            if len(exemplos_ubs) < 5:
                exemplos_ubs.append(nome)
        else:
            total_outros += 1
            if len(exemplos_outros) < 5:
                exemplos_outros.append(nome)
    
    total = total_ubs + total_outros
    
    print(f"\n📊 RESUMO:")
    print(f"   Total de Estabelecimentos: {total}")
    print(f"   ├─ UBS: {total_ubs} ({total_ubs/total*100:.1f}%)")
    print(f"   └─ Outros: {total_outros} ({total_outros/total*100:.1f}%)")
    
    print(f"\n✅ EXEMPLOS DE UBS ({len(exemplos_ubs)}):")
    for i, nome in enumerate(exemplos_ubs, 1):
        print(f"   {i}. {nome}")
    
    print(f"\n⚠️  EXEMPLOS DE OUTROS ESTABELECIMENTOS ({len(exemplos_outros)}):")
    for i, nome in enumerate(exemplos_outros, 1):
        print(f"   {i}. {nome}")
    
    print("\n" + "=" * 80)

if __name__ == "__main__":
    try:
        testar_classificacao_ubs()
    except Exception as e:
        print(f"❌ Erro: {e}")
        import traceback
        traceback.print_exc()

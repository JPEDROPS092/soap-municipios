#!/usr/bin/env python3
"""
Teste rápido do parser de UBS
"""

import requests
from xml.etree import ElementTree as ET

SOAP_URL = "http://0.0.0.0:8080/ws/municipios"

def testar_parser():
    """Testa o parser de UBS"""
    
    soap_request = '''<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" 
               xmlns:ser="http://service.soap.municipios.com/">
  <soap:Body>
    <ser:listarUBSMunicipio>
      <municipioId>1300300</municipioId>
      <municipioNome>Autazes</municipioNome>
    </ser:listarUBSMunicipio>
  </soap:Body>
</soap:Envelope>'''
    
    headers = {
        'Content-Type': 'text/xml; charset=utf-8',
        'SOAPAction': '""'
    }
    
    response = requests.post(SOAP_URL, data=soap_request, headers=headers)
    xml = response.text
    
    root = ET.fromstring(xml)
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    
    ret = root.find('.//ns2:listarUBSMunicipioResponse/return', ns)
    
    dados = {
        'totalUbs': int(ret.find('totalUbs').text or 0),
        'totalMedicos': int(ret.find('totalMedicos').text or 0),
        'totalEnfermeiros': int(ret.find('totalEnfermeiros').text or 0),
        'ubs': []
    }
    
    # CORRIGIDO: sem /item
    for item in ret.findall('listaUbs'):
        dados['ubs'].append({
            'nome': item.find('nome').text or '',
            'cnes': item.find('cnes').text or '',
            'endereco': item.find('endereco').text or '',
            'cep': item.find('cep').text or '',
            'latitude': float(item.find('latitude').text or 0),
            'longitude': float(item.find('longitude').text or 0)
        })
    
    print(f"✅ Total de UBS: {dados['totalUbs']}")
    print(f"✅ Total de Médicos: {dados['totalMedicos']}")
    print(f"✅ Total de Enfermeiros: {dados['totalEnfermeiros']}")
    print(f"✅ UBS na lista: {len(dados['ubs'])}")
    
    if dados['ubs']:
        print(f"\n📍 Primeira UBS:")
        print(f"   Nome: {dados['ubs'][0]['nome']}")
        print(f"   CNES: {dados['ubs'][0]['cnes']}")
        print(f"   Endereço: {dados['ubs'][0]['endereco']}")
        print(f"   Coordenadas: {dados['ubs'][0]['latitude']}, {dados['ubs'][0]['longitude']}")
    
    return dados

if __name__ == "__main__":
    testar_parser()

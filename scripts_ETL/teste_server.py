from zeep import Client

WSDL_URL = "http://0.0.0.0:8080/ws/municipios?wsdl"

client = Client(WSDL_URL)

# ---------------------------
# Teste 1: listarMunicipiosPorUF
# ---------------------------
try:
    uf = "AM"
    print("\n=== Teste: listarMunicipiosPorUF ===")
    response = client.service.listarMunicipiosPorUF(uf)
    print(response)
except Exception as e:
    print("Erro:", e)

# ---------------------------
# Teste 2: obterDadosPopulacionais
# ---------------------------
try:
    print("\n=== Teste: obterDadosPopulacionais ===")
    municipio_id = 1302603
    municipio_nome = "Manaus"
    response = client.service.obterDadosPopulacionais(municipio_id, municipio_nome)
    print(response)
except Exception as e:
    print("Erro:", e)

# ---------------------------
# Teste 3: consultarCEP
# ---------------------------
try:
    print("\n=== Teste: consultarCEP ===")
    cep = "69005010"
    response = client.service.consultarCEP(cep)
    print(response)
except Exception as e:
    print("Erro:", e)

# ---------------------------
# Teste 4: listarUBSMunicipio
# ---------------------------
try:
    print("\n=== Teste: listarUBSMunicipio ===")
    municipio_id = 1302603
    municipio_nome = "Manaus"
    response = client.service.listarUBSMunicipio(municipio_id, municipio_nome)
    print(response)
except Exception as e:
    print("Erro:", e)


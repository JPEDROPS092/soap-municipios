
from zeep import Client
from zeep.helpers import serialize_object
import json

WSDL_URL = "http://0.0.0.0:8080/ws/municipios?wsdl"
client = Client(WSDL_URL)

schemas = {}
operations = {}

# Extrair operações do serviço
print("🔍 Extraindo operações do WSDL...")
for service_name, service in client.wsdl.services.items():
    print(f"\n📋 Serviço: {service_name}")
    for port_name, port in service.ports.items():
        print(f"  🔌 Port: {port_name}")
        binding = port.binding
        
        # Iterar sobre as operações do binding
        for op_name in binding._operations.keys():
            operation = binding.get(op_name)
            print(f"    ⚙️  Operação: {op_name}")
            
            try:
                # Capturar informações de input
                input_params = []
                if hasattr(operation, 'input') and operation.input:
                    if hasattr(operation.input.body, 'type'):
                        if hasattr(operation.input.body.type, 'elements'):
                            input_params = [
                                {
                                    'name': elem[0],
                                    'type': str(elem[1].type.name) if hasattr(elem[1], 'type') else 'unknown'
                                }
                                for elem in operation.input.body.type.elements
                            ]
                
                # Capturar informações de output
                output_params = []
                if hasattr(operation, 'output') and operation.output:
                    if hasattr(operation.output.body, 'type'):
                        output_type = str(operation.output.body.type)
                        output_params = [{'type': output_type}]
                
                operations[op_name] = {
                    "input": input_params,
                    "output": output_params
                }
            except Exception as e:
                print(f"      ⚠️  Erro ao processar operação {op_name}: {e}")
                operations[op_name] = {
                    "input": [],
                    "output": [],
                    "error": str(e)
                }

# Extrair tipos complexos
print("\n🔍 Extraindo tipos complexos...")
try:
    for type_name, type_obj in client.wsdl.types.types:
        try:
            schemas[str(type_name)] = {
                'name': str(type_name),
                'type': str(type(type_obj).__name__)
            }
        except Exception as e:
            print(f"  ⚠️  Erro ao processar tipo {type_name}: {e}")
except Exception as e:
    print(f"  ⚠️  Erro ao iterar tipos: {e}")

# Adicionar informação sobre os serviços disponíveis
services_info = {}
for service_name, service in client.wsdl.services.items():
    services_info[service_name] = list(service.ports.keys())

# Salvar tudo em JSON
output = {
    "services": services_info,
    "operations": operations,
    "schemas": schemas
}

with open("wsdl_schema.json", "w", encoding='utf-8') as f:
    json.dump(output, f, indent=4, ensure_ascii=False)

print("\n✅ Schema gerado com sucesso em: wsdl_schema.json")
print(f"📊 Total de operações encontradas: {len(operations)}")
print(f"📋 Total de tipos complexos: {len(schemas)}")

# Exibir resumo das operações
print("\n" + "="*60)
print("RESUMO DAS OPERAÇÕES DISPONÍVEIS")
print("="*60)
for op_name, op_info in operations.items():
    print(f"\n🔧 {op_name}")
    if op_info.get('input'):
        print(f"   📥 Entrada: {op_info['input']}")
    if op_info.get('output'):
        print(f"   📤 Saída: {op_info['output']}")


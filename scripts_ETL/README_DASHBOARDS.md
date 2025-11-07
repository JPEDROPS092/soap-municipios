# 📊 Dashboards do Sistema de Saúde Municipal

Scripts Python para gerar dashboards interativos com dados do serviço SOAP.

## 🚀 Scripts Disponíveis

### 1. `dashboard_completo.py` - Dashboard Completo (RECOMENDADO)

Dashboard integrado que usa **todas as 4 operações SOAP**:
- ✅ `listarMunicipiosPorUF` - Lista municípios por estado
- ✅ `obterDadosPopulacionais` - Dados demográficos
- ✅ `listarUBSMunicipio` - Dados de UBS e profissionais
- ✅ `consultarCEP` - Consulta de endereços

**Visualizações:**
- 👥 Pirâmide populacional (Homens vs Mulheres)
- 📊 Distribuição por faixa etária
- 🏥 Recursos de saúde (UBS, Médicos, Enfermeiros)
- 👨‍⚕️ Distribuição de profissionais
- 🗺️ Mapa interativo com localização das UBS
- 📈 Indicadores de cobertura

**Saídas:**
- Dashboard HTML interativo
- Relatório em texto (.txt)
- Dados em JSON

### 2. `dashboard_ubs.py` - Dashboard Específico de UBS

Foco apenas em dados de UBS de um município específico.

## 📋 Pré-requisitos

### Instalar dependências:

```bash
pip install plotly pandas requests
```

Ou use o arquivo de requirements:

```bash
pip install -r requirements_dashboard.txt
```

## 🎯 Como Usar

### Dashboard Completo:

```bash
python dashboard_completo.py
```

**Passos:**
1. Digite a sigla da UF (ex: `AM`)
2. Aguarde a lista de municípios
3. Digite o código IBGE ou nome do município (ex: `Manaus` ou `1302603`)
4. Aguarde a geração do dashboard
5. O navegador abrirá automaticamente

### Dashboard de UBS:

```bash
python dashboard_ubs.py
```

**Passos:**
1. Digite o código IBGE (ex: `1302603`)
2. Digite o nome do município (ex: `Manaus`)
3. Aguarde a geração
4. O navegador abrirá automaticamente

## 📂 Arquivos Gerados

Os dashboards são salvos na pasta `dashboards/`:

```
dashboards/
├── dashboard_completo_manaus.html      # Dashboard interativo
├── relatorio_manaus.txt                # Relatório textual
├── dados_manaus.json                   # Dados em JSON
└── tabela_ubs_manaus.html             # Tabela de UBS
```

## 📊 Exemplo de Dados Exibidos

### Demografia:
- População total, homens e mulheres
- Distribuição por faixa etária (0-10, 11-20, 21-30, 40+)

### Saúde:
- Total de UBS
- Total de médicos e enfermeiros
- UBS com geolocalização
- Lista completa de UBS com endereços

### Indicadores:
- UBS por 10.000 habitantes
- Médicos por 1.000 habitantes
- Enfermeiros por 1.000 habitantes
- Profissionais por UBS
- Razão médico/enfermeiro

## 🗺️ Mapa Interativo

O dashboard inclui um mapa interativo usando OpenStreetMap:
- Cada marcador representa uma UBS
- Hover mostra: nome, endereço e CNES
- Zoom e pan habilitados

## 🎨 Tecnologias Utilizadas

- **Plotly**: Gráficos interativos
- **Pandas**: Manipulação de dados
- **Requests**: Requisições HTTP/SOAP
- **XML ElementTree**: Parse de XML/SOAP

## ⚙️ Schema das Operações SOAP

Baseado no arquivo `wsdl_schema.json`:

```json
{
    "operations": {
        "listarMunicipiosPorUF": {
            "input": [{"name": "uf", "type": "string"}]
        },
        "obterDadosPopulacionais": {
            "input": [
                {"name": "municipioId", "type": "int"},
                {"name": "municipioNome", "type": "string"}
            ]
        },
        "consultarCEP": {
            "input": [{"name": "cep", "type": "string"}]
        },
        "listarUBSMunicipio": {
            "input": [
                {"name": "municipioId", "type": "int"},
                {"name": "municipioNome", "type": "string"}
            ]
        }
    }
}
```

## 🔧 Troubleshooting

### Erro de conexão:
- Verifique se o servidor SOAP está rodando
- URL padrão: `http://0.0.0.0:8080/ws/municipios`

### Sem dados de UBS:
- Verifique se o banco está populado
- Execute `importar_dados.py` primeiro

### Erro no mapa:
- Algumas UBS podem não ter coordenadas
- O mapa só exibe UBS com latitude/longitude válidas

## 📝 Notas

- Os dashboards são **100% offline** após gerados
- Compatível com todos os navegadores modernos
- Gráficos são interativos (zoom, pan, hover)
- Dados atualizados em tempo real do serviço SOAP

## 🤝 Contribuindo

Para adicionar novos gráficos ou visualizações, edite as funções:
- `criar_dashboard_completo()` - Adicionar subplots
- `gerar_relatorio_texto()` - Adicionar estatísticas

---

**Desenvolvido para o projeto DAD-IFAM** 🎓

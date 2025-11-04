# 🚀 Aplicação SOAP: Consulta de Demografia e UBS

Este é um projeto full-stack que implementa um serviço web **SOAP** para consulta de dados públicos brasileiros. A aplicação permite a um cliente de terminal consultar dados demográficos reais do **Censo 2022** e dados detalhados de **Unidades Básicas de Saúde (UBS)**, incluindo contagem de profissionais, obtidos do **CNES**.

## 📸 Preview da Interface

O cliente de terminal possui uma interface moderna e colorida com:
- 🎨 Cores ANSI para melhor visualização
- ✨ Ícones Unicode para feedback visual
- 📊 Dados formatados e bem organizados
- 🔄 Animações de loading durante buscas
- 🎯 Navegação intuitiva e responsiva

---

## 🏗️ Componentes do Projeto

O projeto é dividido em três componentes principais:

1. **ETL (Python):** Script que processa e agrega dados de múltiplos CSVs (Censo 2022, CNES) e os carrega num banco de dados.
2. **Banco de Dados (MySQL):** A fonte da verdade para todos os dados complexos.
3. **Aplicação Java (Servidor + Cliente):**
   - Um **Servidor SOAP (JAX-WS)** com arquitetura em camadas que consulta o banco MySQL e APIs REST externas.
   - Um **Cliente de Terminal** moderno e interativo que consome exclusivamente o serviço SOAP.

---

## ✨ Funcionalidades

### 📍 Dados de Municípios
* **Listar Municípios por UF:** Consome a API REST pública do IBGE em tempo real.
* Interface com lista numerada e cores alternadas para fácil visualização.

### 👥 Dados Demográficos (Censo 2022)
Consulta o banco MySQL local para obter dados reais de:
* População Total
* Total de Homens e Mulheres
* Distribuição por faixa etária (0-10, 11-20, 21-30, 40+ anos)
* Formatação com separadores de milhar para melhor leitura

### 🏥 Dados de UBS (CNES)
Consulta o banco MySQL local para obter dados reais de:
* Total de UBS no município
* Total de Médicos
* Total de Enfermeiros
* Listagem completa de UBS com:
  - Nome e código CNES
  - Endereço completo (integrado com API ViaCEP)
  - Coordenadas geográficas (Latitude/Longitude)

### 🔍 Recursos Adicionais
* **Consulta CEP:** Integração com API ViaCEP para validação de endereços
* **Feedback Visual:** Animações de loading e mensagens coloridas
* **Tratamento de Erros:** Mensagens claras e orientações para resolução

---

## 🏛️ Arquitetura da Solução

```
┌──────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────┐
│   CSVs (Censo 22, CNES)  │ ───> │ Python (importar_dados) │ ───> │  MySQL (soap_ubs_db)│
│   (Dados Brutos)         │      │   (Processamento/ETL)   │      │ (Fonte da Verdade)  │
└──────────────────────────┘      └─────────────────────────┘      └─────────────────────┘
                                                                             ↑
                                                                             │ (JDBC)
┌──────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────┐
│ Cliente Java (Terminal)  │ <──> │   Servidor SOAP (Java)  │ ───> │ APIs REST Externas  │
│   (Interface Colorida)   │      │ (Arquitetura em Camadas)│      │   (IBGE, ViaCEP)    │
└──────────────────────────┘      └─────────────────────────┘      └─────────────────────┘
```

### 📦 Estrutura do Servidor Java

O servidor segue uma arquitetura em camadas bem definida:

```
servidor/src/main/java/com/municipios/soap/
│
├── 📦 server/          # Inicialização do servidor
│   └── ServidorSOAP.java
│
├── 📦 service/         # Lógica de negócio e Web Services
│   ├── MunicipioWebService.java (interface)
│   └── MunicipioWebServiceImpl.java
│
├── 📦 model/           # Modelos de dados
│   ├── Municipio.java
│   ├── DadosPopulacionais.java
│   ├── DadosUBS.java
│   ├── UBS.java
│   └── Endereco.java
│
└── 📦 database/        # Camada de acesso a dados
    └── DatabaseConnector.java
```

**Benefícios desta arquitetura:**
- ✅ Separação clara de responsabilidades
- ✅ Facilita manutenção e escalabilidade
- ✅ Código organizado e profissional
- ✅ Fácil adicionar novos serviços

---

## 📋 Pré-requisitos

Antes de começar, garanta que você tem as seguintes ferramentas instaladas:

* **Java 11+** (JDK)
* **Apache Maven** 3.6+
* **Servidor MySQL** 8.0+
* **Python 3+**
* **Bibliotecas Python:**
  ```bash
  pip install pandas mysql-connector-python
  ```

---

## ⚙️ Instalação e Configuração

### Passo 1: Configurar o Banco de Dados (MySQL)

1. Conecte-se ao seu servidor MySQL (via Workbench, DBeaver, ou linha de comando).

2. Crie o banco de dados e o usuário:

```sql
CREATE DATABASE soap_ubs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'soap_user'@'localhost' IDENTIFIED BY 'sua_senha_secreta';
GRANT ALL PRIVILEGES ON soap_ubs_db.* TO 'soap_user'@'localhost';
FLUSH PRIVILEGES;
USE soap_ubs_db;
```

> ⚠️ **IMPORTANTE:** Altere `sua_senha_secreta` para uma senha forte.

3. Execute o script abaixo para criar as três tabelas necessárias:

```sql
/* Tabela de Demografia (do Censo 2022) */
CREATE TABLE demografia_municipio (
    ibge_municipio VARCHAR(10) PRIMARY KEY,
    municipio_nome VARCHAR(255),
    populacao_total BIGINT,
    populacao_homens BIGINT,
    populacao_mulheres BIGINT,
    faixa_0_10 BIGINT,
    faixa_11_20 BIGINT,
    faixa_21_30 BIGINT,
    faixa_40_mais BIGINT
);

/* Tabela de Estabelecimentos (do CNES) */
CREATE TABLE ubs_estabelecimentos (
    cnes VARCHAR(15) PRIMARY KEY,
    ibge_municipio VARCHAR(10),
    nome VARCHAR(255),
    logradouro VARCHAR(255),
    bairro VARCHAR(100),
    latitude DECIMAL(12, 9),
    longitude DECIMAL(12, 9),
    cep VARCHAR(9),
    INDEX idx_ibge_municipio (ibge_municipio)
);

/* Tabela de Totais (Pré-calculada pelo Python) */
CREATE TABLE ubs_totais_municipio (
    ibge_municipio VARCHAR(10) PRIMARY KEY,
    total_ubs INT,
    total_medicos INT,
    total_enfermeiros INT
);
```

### Passo 2: Carregar os Dados (Python ETL)

1. Crie uma pasta `scripts_etl/` na raiz do projeto.

2. Mova os seguintes arquivos para dentro dela:
   - `importar_dados.py`
   - `Estabelecimentos.csv`
   - `EstabelecimentoProfissionais.csv`
   - `Agregados_por_municipios_demografia_BR.csv`

3. **⚠️ IMPORTANTE:** Edite o arquivo `importar_dados.py` com suas credenciais:

```python
DB_CONFIG = {
    'user': 'soap_user',
    'password': 'sua_senha_secreta',  # <-- MUDE AQUI
    'host': 'localhost',
    'database': 'soap_ubs_db',
    'raise_on_warnings': True
}
```

4. Execute o script:

```bash
cd scripts_etl/
python importar_dados.py
```

5. Aguarde pela saída de sucesso:
```
✓ Conectado ao MySQL com sucesso!
✓ ... estabelecimentos importados.
✓ ... totais de UBS calculados e importados.
✓ 5570 registros demográficos REAIS (Censo 2022) importados.
🎉 Processo de importação concluído!
```

### Passo 3: Configurar e Compilar o Servidor (Java)

1. **⚠️ IMPORTANTE:** Edite o arquivo de conexão do servidor:
   - **Caminho:** `servidor/src/main/java/com/municipios/soap/database/DatabaseConnector.java`
   - **Altere a senha:**

```java
private static final String DB_PASSWORD = "sua_senha_secreta"; // <-- MUDE AQUI
```

2. Compile o servidor:

```bash
cd servidor/
mvn clean package
```

### Passo 4: Compilar o Cliente (Java)

```bash
cd cliente/
mvn clean package
```

---

## ▶️ Como Executar a Aplicação

### 🖥️ No IntelliJ IDEA (Recomendado)

#### Terminal 1: Servidor
1. Abra a classe `ServidorSOAP.java` em:
   ```
   servidor/src/main/java/com/municipios/soap/server/ServidorSOAP.java
   ```
2. Clique com o botão direito → **Run 'ServidorSOAP.main()'**
3. Aguarde a mensagem: `✓ Servidor SOAP iniciado com sucesso!`

#### Terminal 2: Cliente
1. Abra a classe `ClienteTerminal.java` em:
   ```
   cliente/src/main/java/com/municipios/cliente/ClienteTerminal.java
   ```
2. Clique com o botão direito → **Run 'ClienteTerminal.main()'**
3. Aproveite a interface colorida! 🎨

### 💻 Na Linha de Comando

A aplicação requer dois terminais executados simultaneamente:

#### Terminal 1: Iniciar o Servidor SOAP

```bash
# A partir da raiz do projeto
./iniciar_servidor.sh
```

Aguarde até ver:
```
✓ Servidor SOAP iniciado com sucesso!
URL do serviço: http://localhost:8080/municipios
```

**Deixe este terminal aberto.**

#### Terminal 2: Executar o Cliente

```bash
# A partir da raiz do projeto (novo terminal)
./executar_cliente.sh
```

Você verá o banner do sistema e poderá começar a usar! 🚀

---

## 🎨 Recursos da Interface

### Cores e Símbolos
- 🔵 **Azul/Ciano:** Títulos e destaques principais
- 🟢 **Verde:** Sucesso e confirmações
- 🟡 **Amarelo:** Avisos e informações
- 🔴 **Vermelho:** Erros e alertas
- 🟣 **Magenta:** Seções especiais

### Feedback Visual
- ✓ Confirmações de sucesso
- ✗ Indicadores de erro
- → Navegação intuitiva
- • Listas organizadas
- ⚠ Avisos importantes
- 🔄 Animações de loading

### Formatação de Dados
- Números com separadores de milhar (ex: 1.234.567)
- Cores alternadas em listas longas
- Hierarquia visual clara
- Espaçamento otimizado

---

## 🔧 Resolução de Problemas

### ❌ Servidor SOAP não está rodando

**Sintoma:** Cliente mostra erro de conexão

**Solução:**
1. Inicie o servidor primeiro (`./iniciar_servidor.sh` ou via IntelliJ)
2. Aguarde a mensagem de sucesso
3. Então execute o cliente

### 🔌 Porta 8080 já está em uso

**Sintoma:** `Address already in use`

**Solução:**
```bash
# Encontre o processo usando a porta
lsof -i :8080

# Mate o processo (substitua PID pelo número real)
kill -9 PID
```

### 🗄️ Erro de conexão com MySQL

**Sintoma:** `Cannot invoke "..." because "conn" is null`

**Causa:** Credenciais incorretas ou MySQL não está rodando

**Solução:**
1. Verifique se o MySQL está rodando: `sudo systemctl status mysql`
2. Confirme a senha em `DatabaseConnector.java`
3. Recompile o servidor: `mvn clean package`
4. Reinicie o servidor

### 📊 Dados incorretos ou ausentes

**Sintoma:** Valores zerados ou incorretos

**Solução:**
```sql
-- Limpe as tabelas
TRUNCATE TABLE demografia_municipio;
TRUNCATE TABLE ubs_estabelecimentos;
TRUNCATE TABLE ubs_totais_municipio;
```

Execute novamente o script Python:
```bash
cd scripts_etl/
python importar_dados.py
```

---

## 📁 Estrutura do Projeto

```
soap_municipios_java/
│
├── 📂 servidor/                    # Servidor SOAP
│   ├── src/main/java/com/municipios/soap/
│   │   ├── server/                # Inicialização
│   │   ├── service/               # Web Services
│   │   ├── model/                 # Modelos de dados
│   │   └── database/              # Conexão DB
│   └── pom.xml
│
├── 📂 cliente/                     # Cliente Terminal
│   ├── src/main/java/com/municipios/cliente/
│   │   └── ClienteTerminal.java  # Interface colorida
│   └── pom.xml
│
├── 📂 scripts_etl/                 # Scripts Python
│   ├── importar_dados.py
│   └── *.csv                      # Dados brutos
│
├── 📜 iniciar_servidor.sh          # Script do servidor
├── 📜 executar_cliente.sh          # Script do cliente
├── 📜 .gitignore
└── 📜 README.md
```

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 11** - Linguagem principal
- **JAX-WS** - Framework SOAP
- **Apache Maven** - Gerenciamento de dependências
- **JDBC** - Conexão com banco de dados
- **Apache HttpClient** - Consumo de APIs REST
- **Gson** - Parser JSON

### Banco de Dados
- **MySQL 8.0+** - Banco de dados relacional
- **Python 3** - Scripts ETL
- **Pandas** - Processamento de dados

### APIs Externas
- **IBGE API** - Dados de municípios em tempo real
- **ViaCEP** - Consulta de CEPs

---

## 📊 Volume de Dados

- **5.570 municípios** com dados demográficos do Censo 2022
- **Milhares de estabelecimentos** de saúde do CNES
- **Dados agregados** de profissionais de saúde por município
- **Integração em tempo real** com APIs públicas

---

## 👥 Autor

**Desenvolvido como projeto acadêmico**

- Instituto Federal do Amazonas (IFAM)
- Disciplina: Desenvolvimento de Aplicações Distribuídas

---

## 📝 Licença

Este projeto é de uso educacional e acadêmico.

---

## 🙏 Agradecimentos

- **IBGE** - Pelos dados públicos do Censo 2022
- **DataSUS/CNES** - Pelos dados de estabelecimentos de saúde
- **ViaCEP** - Pela API gratuita de consulta de CEPs

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique a seção de **Resolução de Problemas**
2. Confira se todos os pré-requisitos estão instalados
3. Valide as credenciais do banco de dados
4. Certifique-se de que as portas 8080 e 3306 estão disponíveis

---

**⭐ Se este projeto foi útil, considere deixar uma estrela no repositório!**

# 🚀 Aplicação SOAP: Consulta de Demografia e UBS

Este é um projeto full-stack que implementa um serviço web **SOAP** para consulta de dados públicos brasileiros. A aplicação permite a um cliente de terminal consultar dados demográficos reais do **Censo 2022** e dados detalhados de **Unidades Básicas de Saúde (UBS)**, incluindo contagem de profissionais, obtidos do **CNES**.

O projeto é dividido em três componentes principais:
1.  **ETL (Python):** Um script que processa e agrega dados de múltiplos CSVs (Censo, CNES) e os carrega num banco de dados.
2.  **Banco de Dados (MySQL):** A fonte da verdade para todos os dados complexos.
3.  **Aplicação Java (Servidor + Cliente):**
    * Um **Servidor SOAP (JAX-WS)** que consulta o banco MySQL e APIs REST externas.
    * Um **Cliente de Terminal** que consome *exclusivamente* o serviço SOAP.

## ✨ Funcionalidades

* **Listar Municípios por UF:** Consome a API REST pública do IBGE em tempo real.
* **Consultar Dados Demográficos (Censo 2022):** Consulta o banco MySQL local para obter dados reais de:
    * População Total
    * Total de Homens e Mulheres
    * Distribuição por faixa etária (0-10, 11-20, 21-30, 40+).
* **Consultar Dados de UBS (CNES):** Consulta o banco MySQL local para obter dados reais de:
    * Total de UBS no município.
    * Total de Médicos.
    * Total de Enfermeiros.
    * Listagem completa de UBS (Nome, CNES, Endereço, Coordenadas).
* **Consultar CEP:** O servidor também demonstra a capacidade de consumir a API ViaCEP (usada internamente).

## 🏛️ Arquitetura da Solução

Este projeto utiliza uma arquitetura de três camadas para processamento e entrega dos dados.

```
+--------------------------+      +-------------------------+      +---------------------+
|   CSVs (Censo 22, CNES)  | ---> |   Python (importar_dados.py) | ---> |  MySQL (soap_ubs_db)  |
| (Dados Brutos)           |      |   (Processamento/ETL)   |      | (Fonte da Verdade)  |
+--------------------------+      +-------------------------+      +---------------------+
                                                                           ^
                                                                           | (JDBC)
+--------------------------+      +-------------------------+      +---------------------+
|  Cliente Java (Terminal) | <--> |   Servidor SOAP (Java)  | ---> | APIs REST Externas  |
|  (Consome SOAP)          |      |   (Agregador de Dados)  |      | (IBGE, ViaCEP)      |
+--------------------------+      +-------------------------+      +---------------------+
```

## 📋 Pré-requisitos

Antes de começar, garanta que você tem as seguintes ferramentas instaladas:

* **Java 11+** (JDK)
* **Apache Maven** 3.6+
* **Servidor MySQL** 8.0+
* **Python 3+**
* **Bibliotecas Python:** `pandas` e `mysql-connector-python`
    ```bash
    pip install pandas "mysql-connector-python"
    ```

---

## ⚙️ Instalação e Configuração (4 Passos)

Siga estes passos para configurar o ambiente completo.

### Passo 1: Configurar o Banco de Dados (MySQL)

1.  Conecte-se ao seu servidor MySQL (via Workbench, DBeaver, etc.).
2.  Crie o banco de dados e o usuário. (Altere `sua_senha_secreta` para uma senha forte).

```sql
    CREATE DATABASE soap_ubs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER 'soap_user'@'localhost' IDENTIFIED BY 'sua_senha_secreta';
    GRANT ALL PRIVILEGES ON soap_ubs_db.* TO 'soap_user'@'localhost';
    FLUSH PRIVILEGES;
    USE soap_ubs_db;
```

3.  Execute o script abaixo para criar as três tabelas necessárias:

    ```sql
    /* Tabela de Demografia (do Censo 2022) */
    CREATE TABLE demografia_municipio (
        ibge_municipio VARCHAR(10) PRIMARY KEY, /* ID de 6 dígitos */
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
        ibge_municipio VARCHAR(10), /* ID de 6 dígitos */
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
        ibge_municipio VARCHAR(10) PRIMARY KEY, /* ID de 6 dígitos */
        total_ubs INT,
        total_medicos INT,
        total_enfermeiros INT
    );
    ```

### Passo 2: Carregar os Dados (Python ETL)

1.  Recomendamos criar uma pasta `scripts_etl/` na raiz do projeto (`soap_municipios_java/`).
2.  Mova os seguintes ficheiros para dentro dela:
    * `importar_dados.py` (o script Python que corrigimos)
    * `Estabelecimentos.csv`
    * `EstabelecimentoProfissionais.csv`
    * `Agregados_por_municipios_demografia_BR.csv`
3.  **⚠️ IMPORTANTE:** Abra o `importar_dados.py` e edite as suas credenciais do MySQL no topo do ficheiro:
    ```python
    DB_CONFIG = {
        'user': 'soap_user',
        'password': 'sua_senha_secreta', # <-- MUDE AQUI
        'host': 'localhost',
        'database': 'soap_ubs_db',
        'raise_on_warnings': True
    }
    ```
4.  Execute o script. Isto pode demorar alguns minutos.
    ```bash
    cd scripts_etl/
    python importar_dados.py
    ```
5.  Aguarde pela saída de sucesso:
    ```
    ✓ Conectado ao MySQL com sucesso!
    ...
    ✓ ... estabelecimentos importados.
    ✓ ... totais de UBS calculados e importados.
    ✓ 5570 registros demográficos REAIS (Censo 2022) importados.
    🎉 Processo de importação concluído!
    ```

### Passo 3: Configurar e Compilar o Servidor (Java)

1.  **⚠️ IMPORTANTE:** Abra o ficheiro de conexão do servidor Java e adicione a **mesma senha** que definiu no Passo 1:
    * Caminho: `soap_municipios_java/servidor/src/main/java/com/municipios/soap/DatabaseConnector.java`
    * Edite a linha:
    ```java
    private static final String DB_PASSWORD = "sua_senha_secreta"; // <-- MUDE AQUI
    ```
2.  Compile o servidor usando o Maven. (Certifique-se de que o seu `MunicipioWebServiceImpl.java` está atualizado com a lógica de ID de 6 dígitos).
    ```bash
    cd soap_municipios_java/servidor/
    mvn clean package
    ```

### Passo 4: Compilar o Cliente (Java)

1.  Compile o cliente. Nenhuma configuração é necessária aqui.
    ```bash
    cd soap_municipios_java/cliente/
    mvn clean package
    ```

---

## ▶️ Como Executar a Aplicação

A aplicação requer dois terminais a serem executados em simultâneo.

### Terminal 1: Iniciar o Servidor SOAP

Neste terminal, o servidor ficará rodando e à espera de pedidos.

```bash
# A partir da raiz do projeto (soap_municipios_java/)
./iniciar_servidor.sh
```
**Aguarde até ver esta mensagem:**
```
✓ Servidor SOAP iniciado com sucesso!
URL do serviço: http://localhost:8080/municipios
```
**Deixe este terminal aberto.**

### Terminal 2: Executar o Cliente

Abra um *novo* terminal.

```bash
# A partir da raiz do projeto (soap_municipios_java/)
./executar_cliente.sh
```
**Você verá a aplicação iniciar:**
```
✓ Servidor SOAP está ativo!
Iniciando aplicação cliente...

======================================================================
  SISTEMA DE CONSULTA DE MUNICÍPIOS E UBS
======================================================================

Digite a sigla do Estado (UF) para consultar os municípios.
Exemplos: AM, SP, RJ, MG, BA, etc.

UF: 
```

---

## 🔧 Resolução de Problemas (Troubleshooting)

* **Problema:** O cliente mostra `✓ Servidor SOAP não está rodando!`
    * **Causa:** O Terminal 1 (Servidor) não está em execução ou ainda está a iniciar.
    * **Solução:** Inicie o `./iniciar_servidor.sh` e aguarde a mensagem "Servidor SOAP iniciado com sucesso!" antes de iniciar o cliente.

* **Problema:** O Servidor (Terminal 1) mostra um erro `Address already in use` (porta 8080).
    * **Causa:** Outro programa (ou uma instância antiga deste servidor) está a usar a porta 8080.
    * **Solução:** Encontre e pare o processo. (Ex: `lsof -i :8080` e depois `kill -9 PID`).

* **Problema:** O Cliente (Terminal 2) mostra um erro de `SOAP Fault` e o Servidor (Terminal 1) mostra `Cannot invoke "..." because "conn" is null`.
    * **Causa:** O servidor Java não se conseguiu ligar ao MySQL.
    * **Solução:** Verifique se o `DB_PASSWORD` no ficheiro `DatabaseConnector.java` está 100% correto e se o seu servidor MySQL está em execução. Recompile o servidor (`mvn clean package`) após corrigir.

* **Problema:** Os dados de Médicos/Enfermeiros ou Demografia estão errados.
    * **Causa:** Ocorreu um erro durante o script Python (`importar_dados.py`).
    * **Solução:** Apague os dados das tabelas (`TRUNCATE TABLE ...`), corrija o script Python e execute-o novamente.
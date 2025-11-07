import pandas as pd
import mysql.connector
from mysql.connector import errorcode
import sys

# --- CONFIGURAÇÃO DO BANCO ---
DB_CONFIG = {
    'user': 'Pedro',
    'password': 'admin',
    'host': 'localhost',
    'database': 'soap_ubs_db',
    'raise_on_warnings': True
}

# -----------------------------

# --- DEFINIÇÃO DE PROFISSIONAIS ---
# (Baseado nos códigos CBO mais comuns)
CBO_MEDICOS = [
    '225125', '225142', '225135', '225124', '225130' 
]
CBO_ENFERMEIROS = [
    '223505', '223565'
]
# ----------------------------------

def conectar_bd():
    """Tenta conectar ao banco de dados"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("✓ Conectado ao MySQL com sucesso!")
        return conn
    except mysql.connector.Error as err:
        if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
            print("Erro: Usuário ou senha do MySQL incorretos.")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print(f"Erro: O banco de dados '{DB_CONFIG['database']}' não existe.")
        else:
            print(f"Erro ao conectar ao MySQL: {err}")
        sys.exit(1) # Encerra o script se não puder conectar

def criar_tabelas(conn):
    """Cria as tabelas necessárias se não existirem"""
    cursor = conn.cursor()
    print("... Verificando e criando tabelas necessárias...")
    
    tabelas_criadas = []
    
    try:
        # Tabela de Demografia
        try:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS demografia_municipio (
                    ibge_municipio VARCHAR(10) PRIMARY KEY,
                    municipio_nome VARCHAR(255),
                    populacao_total BIGINT,
                    populacao_homens BIGINT,
                    populacao_mulheres BIGINT,
                    faixa_0_10 BIGINT,
                    faixa_11_20 BIGINT,
                    faixa_21_30 BIGINT,
                    faixa_40_mais BIGINT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """)
            if cursor.rowcount == 0:
                tabelas_criadas.append("demografia_municipio")
        except mysql.connector.Error:
            pass  # Tabela já existe
        
        # Tabela de Estabelecimentos
        try:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS ubs_estabelecimentos (
                    cnes VARCHAR(15) PRIMARY KEY,
                    ibge_municipio VARCHAR(10),
                    nome VARCHAR(255),
                    logradouro VARCHAR(255),
                    bairro VARCHAR(100),
                    latitude DECIMAL(12, 9),
                    longitude DECIMAL(12, 9),
                    cep VARCHAR(9)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """)
            if cursor.rowcount == 0:
                tabelas_criadas.append("ubs_estabelecimentos")
        except mysql.connector.Error:
            pass  # Tabela já existe
        
        # Criar índice se não existir
        try:
            cursor.execute("""
                CREATE INDEX IF NOT EXISTS idx_ibge_municipio 
                ON ubs_estabelecimentos (ibge_municipio);
            """)
        except mysql.connector.Error:
            pass  # Índice já existe ou sintaxe não suportada
        
        # Tabela de Totais
        try:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS ubs_totais_municipio (
                    ibge_municipio VARCHAR(10) PRIMARY KEY,
                    total_ubs INT,
                    total_medicos INT,
                    total_enfermeiros INT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """)
            if cursor.rowcount == 0:
                tabelas_criadas.append("ubs_totais_municipio")
        except mysql.connector.Error:
            pass  # Tabela já existe
        
        conn.commit()
        if tabelas_criadas:
            print(f"✓ Tabelas criadas: {', '.join(tabelas_criadas)}")
        else:
            print("✓ Todas as tabelas já existem.")
    except mysql.connector.Error as err:
        print(f"Erro ao verificar tabelas: {err}")
        conn.rollback()
        sys.exit(1)
    finally:
        cursor.close()

def limpar_tabelas(conn):
    """Limpa os dados antigos antes de importar novos"""
    cursor = conn.cursor()
    print("... Limpando tabelas antigas...")
    try:
        # A ordem importa por causa das chaves estrangeiras (se houver)
        cursor.execute("SET FOREIGN_KEY_CHECKS = 0;") # Desabilita checagem
        cursor.execute("TRUNCATE TABLE ubs_totais_municipio;")
        cursor.execute("TRUNCATE TABLE ubs_estabelecimentos;")
        cursor.execute("TRUNCATE TABLE demografia_municipio;")
        cursor.execute("SET FOREIGN_KEY_CHECKS = 1;") # Reabilita
        conn.commit()
        print("✓ Tabelas limpas.")
    except mysql.connector.Error as err:
        print(f"Erro ao limpar tabelas: {err}")
        conn.rollback()
    finally:
        cursor.close()

def importar_estabelecimentos(conn):
    """Lê Estabelecimentos.csv e insere na tabela ubs_estabelecimentos"""
    print("\nIniciando importação de estabelecimentos...")
    try:
        df = pd.read_csv(
            'Estabelecimentos.csv', 
            sep=';', 
            usecols=['CNES', 'IBGE', 'NOME', 'LOGRADOURO', 'BAIRRO', 'LATITUDE', 'LONGITUDE', 'CEP'],
            dtype={'CNES': str, 'IBGE': str, 'CEP': str}
        )
    except FileNotFoundError:
        print("Erro: 'Estabelecimentos.csv' não encontrado. Verifique o nome do arquivo.")
        return None
    except Exception as e:
        print(f"Erro ao ler Estabelecimentos.csv: {e}")
        return None

    df.rename(columns={
        'CNES': 'cnes',
        'IBGE': 'ibge_municipio',
        'NOME': 'nome',
        'LOGRADOURO': 'logradouro',
        'BAIRRO': 'bairro',
        'LATITUDE': 'latitude',
        'LONGITUDE': 'longitude',
        'CEP': 'cep'
    }, inplace=True)

    df.dropna(subset=['cnes', 'ibge_municipio'], inplace=True)
    df.drop_duplicates(subset=['cnes'], inplace=True)
    df = df.fillna(0) 

    cursor = conn.cursor()
    
    sql = """
        INSERT INTO ubs_estabelecimentos 
        (cnes, ibge_municipio, nome, logradouro, bairro, latitude, longitude, cep)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s) AS new_data
        ON DUPLICATE KEY UPDATE nome=new_data.nome;
    """
    
    total = 0
    try:
        for row in df.itertuples(index=False):
            data = (
                row.cnes,
                row.ibge_municipio,
                row.nome,
                row.logradouro,
                row.bairro,
                round(float(row.latitude), 8) if row.latitude else 0.0,
                round(float(row.longitude), 8) if row.longitude else 0.0,
                str(row.cep).replace('.0', '')
            )
            cursor.execute(sql, data)
            total += 1
        conn.commit()
        print(f"✓ {total} estabelecimentos importados.")
        return df[['cnes', 'ibge_municipio']] 
    
    except mysql.connector.Error as err:
        print(f"Erro de SQL ao inserir estabelecimentos: {err}")
        conn.rollback()
        return None
    finally:
        cursor.close()

def calcular_e_importar_totais(conn, df_estab):
    """Lê Profissionais.csv, agrega os dados e insere em ubs_totais_municipio"""
    if df_estab is None:
        print("X Pela da importação de totais: dados de estabelecimentos não foram carregados.")
        return

    print("\nIniciando cálculo e importação de totais (Médicos/Enfermeiros)...")
    try:
        df_prof = pd.read_csv(
            'EstabelecimentoProfissionais.csv',
            sep=',', 
            usecols=['CO_UNIDADE', 'CO_PROFISSIONAL_SUS', 'CO_CBO'],
            dtype={'CO_UNIDADE': str, 'CO_PROFISSIONAL_SUS': str, 'CO_CBO': str}
        )
    except FileNotFoundError:
        print("Erro: 'EstabelecimentoProfissionais.csv' não encontrado. Verifique o nome do arquivo.")
        return
    except Exception as e:
        print(f"Erro ao ler EstabelecimentoProfissionais.csv: {e}")
        return

    # --- CORREÇÃO IMPORTANTE AQUI ---
    # O 'CO_UNIDADE' (ex: 1100012369958) é um composto (IBGE_6_Dígitos + CNES_7_Dígitos).
    # Precisamos de extrair os últimos 7 dígitos para ser o 'cnes'
    
    # Remove a linha antiga: df_prof.rename(columns={'CO_UNIDADE': 'cnes'}, inplace=True)
    
    # Adiciona a linha nova:
    # Garante que é uma string e extrai os últimos 7 caracteres
    df_prof['cnes'] = df_prof['CO_UNIDADE'].astype(str).str.slice(-7)
    
    # ---------------------------------

    # Agora o 'df_prof['cnes']' (ex: 2369958) vai conseguir unir-se
    # ao 'df_estab['cnes']' (ex: 0023914)
    # Nota: df_estab já tem 'cnes' (7 dígitos) e 'ibge_municipio' (6 dígitos)
    
    df_merged = pd.merge(df_prof, df_estab, on='cnes')
    
    # 1. Calcular total de UBS por município (isto já funcionava)
    df_total_ubs = df_estab.groupby('ibge_municipio')['cnes'].count().reset_index(name='total_ubs')

    # 2. Calcular total de Médicos por município (agora df_merged tem dados)
    df_medicos = df_merged[df_merged['CO_CBO'].isin(CBO_MEDICOS)]
    df_total_medicos = df_medicos.groupby('ibge_municipio')['CO_PROFISSIONAL_SUS'].nunique().reset_index(name='total_medicos')

    # 3. Calcular total de Enfermeiros por município (agora df_merged tem dados)
    df_enf = df_merged[df_merged['CO_CBO'].isin(CBO_ENFERMEIROS)]
    df_total_enf = df_enf.groupby('ibge_municipio')['CO_PROFISSIONAL_SUS'].nunique().reset_index(name='total_enfermeiros')

    # 4. Juntar todos os totais
    df_final_totals = pd.merge(df_total_ubs, df_total_medicos, on='ibge_municipio', how='left')
    df_final_totals = pd.merge(df_final_totals, df_total_enf, on='ibge_municipio', how='left')
    
    # Preenche com 0 municípios que têm UBS mas não têm médicos/enfermeiros
    df_final_totals = df_final_totals.fillna(0) 

    cursor = conn.cursor()
    
    sql = """
        INSERT INTO ubs_totais_municipio 
        (ibge_municipio, total_ubs, total_medicos, total_enfermeiros)
        VALUES (%s, %s, %s, %s) AS new_data
        ON DUPLICATE KEY UPDATE 
            total_ubs=new_data.total_ubs, 
            total_medicos=new_data.total_medicos, 
            total_enfermeiros=new_data.total_enfermeiros;
    """
    
    total = 0
    try:
        for row in df_final_totals.itertuples(index=False):
            data = (
                row.ibge_municipio,
                int(row.total_ubs),
                int(row.total_medicos),
                int(row.total_enfermeiros)
            )
            cursor.execute(sql, data)
            total += 1
        conn.commit()
        print(f"✓ {total} municípios tiveram seus totais de UBS calculados e importados.")
        
    except mysql.connector.Error as err:
        print(f"Erro de SQL ao inserir totais: {err}")
        conn.rollback()
    finally:
        cursor.close()

def importar_demografia_simulada(conn):
    """
    (Nome antigo, mas agora importa dados REAIS do Censo 2022)
    Lê 'Agregados_por_municipios_demografia_BR.csv', processa os dados
    demográficos e insere na tabela 'demografia_municipio' com ID de 6 dígitos.
    """
    print("\nImportando dados demográficos REAIS do Censo 2022...")
    
    # Mapeamento das colunas que precisamos
    colunas_necessarias = {
        'CD_MUN': str,
        'NM_MUN': str,
        'V01006': int, # População Total
        'V01007': int, # Total Homens
        'V01008': int, # Total Mulheres
        
        # Faixas Etárias (vamos somar homens e mulheres)
        'V01018': int, # Homens 0-9 anos
        'V01019': int, # Homens 10-19 anos
        'V01020': int, # Homens 20-29 anos
        
        'V01032': int, # Mulheres 0-9 anos
        'V01033': int, # Mulheres 10-19 anos
        'V01034': int, # Mulheres 20-29 anos
    }

    try:
        df = pd.read_csv(
            'Agregados_por_municipios_demografia_BR.csv', 
            sep=';', 
            usecols=colunas_necessarias.keys(),
            dtype=colunas_necessarias,
            na_values=['...'],
            encoding='latin-1' # <-- ESTA É A CORREÇÃO
        )
    except FileNotFoundError:
        print("Erro: 'Agregados_por_municipios_demografia_BR.csv' não encontrado.")
        return
    except Exception as e:
        print(f"Erro ao ler Agregados_por_municipios_demografia_BR.csv: {e}")
        return

    df.dropna(inplace=True) # Remove linhas com dados nulos

    # --- Processamento dos Dados ---
    
    # 1. Converter CD_MUN de 7 dígitos para 6 (ex: 1100015 -> 110001)
    df['ibge_municipio'] = df['CD_MUN'].astype(str).str[:6]
    
    # 2. Calcular Faixas Etárias (conforme solicitado)
    df['faixa_0_10'] = df['V01018'] + df['V01032'] # 0-9 anos (aprox 0-10)
    df['faixa_11_20'] = df['V01019'] + df['V01033'] # 10-19 anos (aprox 11-20)
    df['faixa_21_30'] = df['V01020'] + df['V01034'] # 20-29 anos (aprox 21-30)
    
    # 3. Calcular "40 ou mais"
    df['faixa_40_mais'] = df['V01006'] - (df['faixa_0_10'] + df['faixa_11_20'] + df['faixa_21_30'])

    # 4. Renomear colunas para o banco
    df_final = df.rename(columns={
        'NM_MUN': 'municipio_nome',
        'V01006': 'populacao_total',
        'V01007': 'populacao_homens',
        'V01008': 'populacao_mulheres'
    })

    # 5. Selecionar apenas as colunas do banco
    colunas_banco = [
        'ibge_municipio', 'municipio_nome', 'populacao_total', 'populacao_homens',
        'populacao_mulheres', 'faixa_0_10', 'faixa_11_20', 'faixa_21_30', 'faixa_40_mais'
    ]
    df_final = df_final[colunas_banco]

    # --- Inserção no Banco ---
    cursor = conn.cursor()
    sql = """
        INSERT INTO demografia_municipio
        (ibge_municipio, municipio_nome, populacao_total, populacao_homens, 
         populacao_mulheres, faixa_0_10, faixa_11_20, faixa_21_30, faixa_40_mais)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s) AS new_data
        ON DUPLICATE KEY UPDATE 
            municipio_nome=new_data.municipio_nome,
            populacao_total=new_data.populacao_total;
    """
    
    try:
        # Converte o DataFrame para uma lista de tuplas para inserção
        dados_para_inserir = [tuple(row) for row in df_final.itertuples(index=False)]
        
        cursor.executemany(sql, dados_para_inserir)
        conn.commit()
        print(f"✓ {cursor.rowcount} registros demográficos REAIS (Censo 2022) importados.")
    except mysql.connector.Error as err:
        print(f"Erro ao inserir dados demográficos reais: {err}")
        conn.rollback()
    finally:
        cursor.close()


if __name__ == "__main__":
    conn = conectar_bd()
    if conn:
        # Cria as tabelas se não existirem
        criar_tabelas(conn)
        
        # Limpa as tabelas
        limpar_tabelas(conn)
        
        # Importa estabelecimentos e guarda o mapeamento cnes->ibge
        df_estab_map = importar_estabelecimentos(conn)
        
        # Usa o mapeamento para calcular e importar os totais
        calcular_e_importar_totais(conn, df_estab_map)
        
        # Importa dados de população (simulados)
        importar_demografia_simulada(conn)
        
        conn.close()
        print("\n🎉 Processo de importação concluído!")
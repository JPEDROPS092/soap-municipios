"""
Dashboard Completo - Sistema de Saúde Municipal
Integra todas as operações SOAP: Municípios, UBS, Demografia e CEP
"""

import requests
from xml.etree import ElementTree as ET
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
from datetime import datetime
import webbrowser
from pathlib import Path
import json

# Configurações
SOAP_URL = "http://0.0.0.0:8080/ws/municipios"

# Lista de UFs brasileiras
UFS_BRASIL = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
]

def fazer_requisicao_soap(operacao, params):
    """Função genérica para fazer requisições SOAP"""
    
    # Montar os parâmetros
    params_xml = '\n'.join([f'      <{k}>{v}</{k}>' for k, v in params.items()])
    
    soap_request = f'''<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" 
               xmlns:ser="http://service.soap.municipios.com/">
  <soap:Body>
    <ser:{operacao}>
{params_xml}
    </ser:{operacao}>
  </soap:Body>
</soap:Envelope>'''
    
    headers = {
        'Content-Type': 'text/xml; charset=utf-8',
        'SOAPAction': '""'
    }
    
    try:
        response = requests.post(SOAP_URL, data=soap_request, headers=headers, timeout=10)
        response.raise_for_status()
        return response.text
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro na requisição SOAP ({operacao}): {e}")
        return None

def listar_municipios(uf):
    """Lista municípios de uma UF"""
    xml = fazer_requisicao_soap('listarMunicipiosPorUF', {'uf': uf})
    if not xml:
        return []
    
    root = ET.fromstring(xml)
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    
    municipios = []
    for item in root.findall('.//return/item', ns):
        municipios.append({
            'id': item.find('id').text,
            'nome': item.find('nome').text,
            'ufNome': item.find('ufNome').text,
            'ufSigla': item.find('ufSigla').text
        })
    
    return municipios

def obter_dados_populacionais(municipio_id, municipio_nome):
    """Obtém dados populacionais de um município"""
    xml = fazer_requisicao_soap('obterDadosPopulacionais', {
        'municipioId': municipio_id,
        'municipioNome': municipio_nome
    })
    
    if not xml:
        return None
    
    root = ET.fromstring(xml)
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    
    ret = root.find('.//ns2:obterDadosPopulacionaisResponse/return', ns)
    if ret is None:
        return None
    
    return {
        'municipioId': ret.find('municipioId').text,
        'municipioNome': ret.find('municipioNome').text,
        'populacaoTotal': int(ret.find('populacaoTotal').text or 0),
        'populacaoHomens': int(ret.find('populacaoHomens').text or 0),
        'populacaoMulheres': int(ret.find('populacaoMulheres').text or 0),
        'faixa0a10': int(ret.find('faixa0a10').text or 0),
        'faixa11a20': int(ret.find('faixa11a20').text or 0),
        'faixa21a30': int(ret.find('faixa21a30').text or 0),
        'faixa40Mais': int(ret.find('faixa40Mais').text or 0)
    }

def listar_ubs_municipio(municipio_id, municipio_nome):
    """Lista UBS de um município"""
    xml = fazer_requisicao_soap('listarUBSMunicipio', {
        'municipioId': municipio_id,
        'municipioNome': municipio_nome
    })
    
    if not xml:
        return None
    
    root = ET.fromstring(xml)
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    
    ret = root.find('.//ns2:listarUBSMunicipioResponse/return', ns)
    if ret is None:
        return None
    
    dados = {
        'totalUbs': int(ret.find('totalUbs').text or 0),
        'totalMedicos': int(ret.find('totalMedicos').text or 0),
        'totalEnfermeiros': int(ret.find('totalEnfermeiros').text or 0),
        'ubs': []
    }
    
    # CORREÇÃO: A estrutura XML retorna <listaUbs> diretamente, sem <item>
    for item in ret.findall('listaUbs'):
        dados['ubs'].append({
            'nome': item.find('nome').text or '',
            'cnes': item.find('cnes').text or '',
            'endereco': item.find('endereco').text or '',
            'cep': item.find('cep').text or '',
            'latitude': float(item.find('latitude').text or 0),
            'longitude': float(item.find('longitude').text or 0)
        })
    
    return dados

def consultar_cep(cep):
    """Consulta um CEP"""
    xml = fazer_requisicao_soap('consultarCEP', {'cep': cep})
    
    if not xml:
        return None
    
    root = ET.fromstring(xml)
    ns = {'ns2': 'http://service.soap.municipios.com/'}
    
    ret = root.find('.//ns2:consultarCEPResponse/return', ns)
    if ret is None:
        return None
    
    return {
        'cep': ret.find('cep').text or '',
        'logradouro': ret.find('logradouro').text or '',
        'bairro': ret.find('bairro').text or '' if ret.find('bairro') is not None else '',
        'localidade': ret.find('localidade').text or '' if ret.find('localidade') is not None else '',
        'uf': ret.find('uf').text or '' if ret.find('uf') is not None else ''
    }

def criar_dashboard_completo(municipio_id, municipio_nome, uf):
    """Cria dashboard completo com todas as informações"""
    
    print(f"\n🔍 Coletando dados de {municipio_nome}...")
    
    # Coletar dados
    dados_pop = obter_dados_populacionais(municipio_id, municipio_nome)
    dados_ubs = listar_ubs_municipio(municipio_id, municipio_nome)
    
    if not dados_pop or not dados_ubs:
        print("❌ Erro ao coletar dados")
        return None
    
    # Criar figura com subplots
    fig = make_subplots(
        rows=3, cols=2,
        subplot_titles=(
            '👥 Pirâmide Populacional',
            '🏥 Recursos de Saúde',
            '📊 Distribuição por Faixa Etária',
            '👨‍⚕️ Profissionais de Saúde',
            '🗺️ Mapa de UBS',
            '📈 Indicadores Principais'
        ),
        specs=[
            [{'type': 'bar'}, {'type': 'bar'}],
            [{'type': 'pie'}, {'type': 'pie'}],
            [{'type': 'scattermapbox', 'colspan': 2}, None]
        ],
        vertical_spacing=0.1,
        horizontal_spacing=0.12,
        row_heights=[0.3, 0.3, 0.4]
    )
    
    # 1. Pirâmide Populacional (Homens vs Mulheres)
    fig.add_trace(
        go.Bar(
            name='Homens',
            y=['População'],
            x=[-dados_pop['populacaoHomens']],
            orientation='h',
            marker_color='#1f77b4',
            text=[f"{dados_pop['populacaoHomens']:,}"],
            textposition='inside'
        ),
        row=1, col=1
    )
    
    fig.add_trace(
        go.Bar(
            name='Mulheres',
            y=['População'],
            x=[dados_pop['populacaoMulheres']],
            orientation='h',
            marker_color='#ff7f0e',
            text=[f"{dados_pop['populacaoMulheres']:,}"],
            textposition='inside'
        ),
        row=1, col=1
    )
    
    # 2. Recursos de Saúde (UBS, Médicos, Enfermeiros)
    fig.add_trace(
        go.Bar(
            x=['UBS', 'Médicos', 'Enfermeiros'],
            y=[dados_ubs['totalUbs'], dados_ubs['totalMedicos'], dados_ubs['totalEnfermeiros']],
            marker_color=['#2ca02c', '#d62728', '#9467bd'],
            text=[dados_ubs['totalUbs'], dados_ubs['totalMedicos'], dados_ubs['totalEnfermeiros']],
            textposition='auto',
            showlegend=False
        ),
        row=1, col=2
    )
    
    # 3. Distribuição por Faixa Etária
    fig.add_trace(
        go.Pie(
            labels=['0-10 anos', '11-20 anos', '21-30 anos', '40+ anos'],
            values=[
                dados_pop['faixa0a10'],
                dados_pop['faixa11a20'],
                dados_pop['faixa21a30'],
                dados_pop['faixa40Mais']
            ],
            marker_colors=['#8dd3c7', '#ffffb3', '#bebada', '#fb8072'],
            hole=0.3,
            textinfo='label+percent',
            showlegend=True
        ),
        row=2, col=1
    )
    
    # 4. Profissionais de Saúde
    total_prof = dados_ubs['totalMedicos'] + dados_ubs['totalEnfermeiros']
    if total_prof > 0:
        fig.add_trace(
            go.Pie(
                labels=['Médicos', 'Enfermeiros'],
                values=[dados_ubs['totalMedicos'], dados_ubs['totalEnfermeiros']],
                marker_colors=['#ff7f0e', '#2ca02c'],
                hole=0.4,
                textinfo='label+value+percent',
                showlegend=True
            ),
            row=2, col=2
        )
    
    # 5. Mapa de UBS
    if dados_ubs['ubs']:
        df_ubs = pd.DataFrame(dados_ubs['ubs'])
        df_ubs = df_ubs[(df_ubs['latitude'] != 0) & (df_ubs['longitude'] != 0)]
        
        if not df_ubs.empty:
            fig.add_trace(
                go.Scattermapbox(
                    lat=df_ubs['latitude'],
                    lon=df_ubs['longitude'],
                    mode='markers',
                    marker=go.scattermapbox.Marker(
                        size=12,
                        color='#e74c3c',
                        opacity=0.8
                    ),
                    text=df_ubs['nome'],
                    hovertemplate='<b>%{text}</b><br>' +
                                  'Endereço: ' + df_ubs['endereco'] + '<br>' +
                                  'CNES: ' + df_ubs['cnes'] + '<br>' +
                                  '<extra></extra>',
                    showlegend=False
                ),
                row=3, col=1
            )
            
            center_lat = df_ubs['latitude'].mean()
            center_lon = df_ubs['longitude'].mean()
            
            fig.update_layout(
                mapbox=dict(
                    style='open-street-map',
                    center=dict(lat=center_lat, lon=center_lon),
                    zoom=11
                )
            )
    
    # Layout geral
    fig.update_layout(
        title_text=f"<b>Dashboard Completo de Saúde - {municipio_nome}/{uf}</b><br>" +
                   f"<sub>População Total: {dados_pop['populacaoTotal']:,} habitantes | " +
                   f"Atualizado em {datetime.now().strftime('%d/%m/%Y %H:%M')}</sub>",
        title_x=0.5,
        title_font_size=22,
        showlegend=True,
        height=1200,
        template='plotly_white',
        barmode='relative'
    )
    
    # Atualizar eixos
    fig.update_xaxes(title_text="População", row=1, col=1)
    fig.update_xaxes(title_text="Categoria", row=1, col=2)
    fig.update_yaxes(title_text="Quantidade", row=1, col=2)
    
    return fig, dados_pop, dados_ubs

def gerar_relatorio_texto(municipio_nome, uf, dados_pop, dados_ubs):
    """Gera relatório em texto"""
    
    razao_ubs_pop = (dados_ubs['totalUbs'] / dados_pop['populacaoTotal'] * 10000) if dados_pop['populacaoTotal'] > 0 else 0
    razao_medico_pop = (dados_ubs['totalMedicos'] / dados_pop['populacaoTotal'] * 1000) if dados_pop['populacaoTotal'] > 0 else 0
    razao_enf_pop = (dados_ubs['totalEnfermeiros'] / dados_pop['populacaoTotal'] * 1000) if dados_pop['populacaoTotal'] > 0 else 0
    
    ubs_com_geo = sum(1 for u in dados_ubs['ubs'] if u['latitude'] != 0 and u['longitude'] != 0)
    
    relatorio = f"""
{'='*80}
                RELATÓRIO COMPLETO DE SAÚDE MUNICIPAL
                      {municipio_nome.upper()} - {uf}
{'='*80}

📊 DADOS DEMOGRÁFICOS
{'─'*80}
    População Total:              {dados_pop['populacaoTotal']:>15,} habitantes
    
    Por Gênero:
      • Homens:                   {dados_pop['populacaoHomens']:>15,} ({dados_pop['populacaoHomens']/dados_pop['populacaoTotal']*100:.1f}%)
      • Mulheres:                 {dados_pop['populacaoMulheres']:>15,} ({dados_pop['populacaoMulheres']/dados_pop['populacaoTotal']*100:.1f}%)
    
    Por Faixa Etária:
      • 0-10 anos:                {dados_pop['faixa0a10']:>15,} ({dados_pop['faixa0a10']/dados_pop['populacaoTotal']*100:.1f}%)
      • 11-20 anos:               {dados_pop['faixa11a20']:>15,} ({dados_pop['faixa11a20']/dados_pop['populacaoTotal']*100:.1f}%)
      • 21-30 anos:               {dados_pop['faixa21a30']:>15,} ({dados_pop['faixa21a30']/dados_pop['populacaoTotal']*100:.1f}%)
      • 40+ anos:                 {dados_pop['faixa40Mais']:>15,} ({dados_pop['faixa40Mais']/dados_pop['populacaoTotal']*100:.1f}%)

🏥 INFRAESTRUTURA DE SAÚDE
{'─'*80}
    Total de UBS:                 {dados_ubs['totalUbs']:>15}
    UBS com Geolocalização:       {ubs_com_geo:>15} ({ubs_com_geo/dados_ubs['totalUbs']*100 if dados_ubs['totalUbs'] > 0 else 0:.1f}%)
    
    Total de Médicos:             {dados_ubs['totalMedicos']:>15}
    Total de Enfermeiros:         {dados_ubs['totalEnfermeiros']:>15}
    Total de Profissionais:       {dados_ubs['totalMedicos'] + dados_ubs['totalEnfermeiros']:>15}

📈 INDICADORES DE COBERTURA
{'─'*80}
    UBS por 10.000 hab:           {razao_ubs_pop:>15.2f}
    Médicos por 1.000 hab:        {razao_medico_pop:>15.2f}
    Enfermeiros por 1.000 hab:    {razao_enf_pop:>15.2f}
    
    Médicos por UBS:              {dados_ubs['totalMedicos']/dados_ubs['totalUbs'] if dados_ubs['totalUbs'] > 0 else 0:>15.2f}
    Enfermeiros por UBS:          {dados_ubs['totalEnfermeiros']/dados_ubs['totalUbs'] if dados_ubs['totalUbs'] > 0 else 0:>15.2f}
    Profissionais por UBS:        {(dados_ubs['totalMedicos']+dados_ubs['totalEnfermeiros'])/dados_ubs['totalUbs'] if dados_ubs['totalUbs'] > 0 else 0:>15.2f}

{'='*80}
Relatório gerado em: {datetime.now().strftime('%d/%m/%Y às %H:%M:%S')}
{'='*80}
    """
    
    return relatorio

def main():
    """Função principal"""
    
    print("=" * 80)
    print(" 🏥 DASHBOARD COMPLETO - SISTEMA DE SAÚDE MUNICIPAL ".center(80))
    print("=" * 80)
    
    # Selecionar UF
    print("\n📍 Selecione a UF:")
    print("   ", " | ".join(UFS_BRASIL))
    uf = input("\n   Digite a sigla da UF (ex: AM): ").strip().upper()
    
    if uf not in UFS_BRASIL:
        print("❌ UF inválida!")
        return
    
    # Listar municípios
    print(f"\n🔍 Buscando municípios de {uf}...")
    municipios = listar_municipios(uf)
    
    if not municipios:
        print("❌ Nenhum município encontrado!")
        return
    
    print(f"\n✅ {len(municipios)} municípios encontrados:")
    for i, m in enumerate(municipios[:10], 1):
        print(f"   {i:2d}. {m['nome']} (ID: {m['id']})")
    
    if len(municipios) > 10:
        print(f"   ... e mais {len(municipios) - 10} municípios")
    
    # Selecionar município
    print("\n📍 Digite o código IBGE ou nome do município:")
    busca = input("   > ").strip()
    
    municipio_selecionado = None
    
    # Tentar por ID
    for m in municipios:
        if m['id'] == busca or m['nome'].lower() == busca.lower():
            municipio_selecionado = m
            break
    
    if not municipio_selecionado:
        print("❌ Município não encontrado!")
        return
    
    print(f"\n✅ Município selecionado: {municipio_selecionado['nome']}")
    
    # Criar dashboard
    resultado = criar_dashboard_completo(
        municipio_selecionado['id'],
        municipio_selecionado['nome'],
        uf
    )
    
    if not resultado:
        return
    
    fig, dados_pop, dados_ubs = resultado
    
    # Exibir relatório
    relatorio = gerar_relatorio_texto(municipio_selecionado['nome'], uf, dados_pop, dados_ubs)
    print(relatorio)
    
    # Salvar arquivos
    output_dir = Path('dashboards')
    output_dir.mkdir(exist_ok=True)
    
    nome_arquivo = municipio_selecionado['nome'].lower().replace(' ', '_')
    
    # Salvar HTML
    dashboard_file = output_dir / f'dashboard_completo_{nome_arquivo}.html'
    print(f"\n💾 Salvando dashboard em: {dashboard_file}")
    fig.write_html(str(dashboard_file))
    
    # Salvar relatório
    relatorio_file = output_dir / f'relatorio_{nome_arquivo}.txt'
    print(f"💾 Salvando relatório em: {relatorio_file}")
    with open(relatorio_file, 'w', encoding='utf-8') as f:
        f.write(relatorio)
    
    # Salvar JSON com dados
    dados_file = output_dir / f'dados_{nome_arquivo}.json'
    print(f"💾 Salvando dados JSON em: {dados_file}")
    with open(dados_file, 'w', encoding='utf-8') as f:
        json.dump({
            'municipio': municipio_selecionado,
            'demografia': dados_pop,
            'saude': dados_ubs
        }, f, indent=2, ensure_ascii=False)
    
    # Abrir no navegador
    print(f"\n🌐 Abrindo dashboard no navegador...")
    webbrowser.open(f'file://{dashboard_file.absolute()}')
    
    print("\n✅ Dashboard gerado com sucesso!")
    print(f"📂 Arquivos salvos em: {output_dir.absolute()}")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  Operação cancelada pelo usuário.")
    except Exception as e:
        print(f"\n❌ Erro inesperado: {e}")
        import traceback
        traceback.print_exc()

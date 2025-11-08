package com.municipios.soap.service;

import com.municipios.soap.model.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


/**
 * Interface do Web Service SOAP para consulta de municípios e dados de saúde
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface MunicipioWebService {

    /**
     * Lista todos os municípios de uma UF
     *
     * @param uf Sigla da UF (ex: AM, SP, RJ)
     * @return Lista de municípios
     */
    @WebMethod
    Municipio[] listarMunicipiosPorUF(@WebParam(name = "uf") String uf);

    /**
     * Obtém dados populacionais de um município
     *
     * @param municipioId   ID do município no IBGE
     * @param municipioNome Nome do município
     * @return Dados populacionais do município
     */
    @WebMethod
    DadosPopulacionais obterDadosPopulacionais(
            @WebParam(name = "municipioId") int municipioId,
            @WebParam(name = "municipioNome") String municipioNome
    );

    /**
     * Consulta endereço pelo CEP
     *
     * @param cep CEP a ser consultado
     * @return Dados do endereço
     */
    @WebMethod
    Endereco consultarCEP(@WebParam(name = "cep") String cep);

    /**
     * Lista UBS de um município
     *
     * @param municipioId   ID do município
     * @param municipioNome Nome do município
     * @return Dados de UBS do município
     */
    @WebMethod
    DadosUBS listarUBSMunicipio(
            @WebParam(name = "municipioId") int municipioId,
            @WebParam(name = "municipioNome") String municipioNome
    );

    /**
     * Lista estabelecimentos separados por tipo (UBS e Outros)
     *
     * @param municipioId   ID do município
     * @param municipioNome Nome do município
     * @return Estabelecimentos separados por tipo
     */
    @WebMethod
    EstabelecimentosPorTipo listarEstabelecimentosPorTipo(
            @WebParam(name = "municipioId") int municipioId,
            @WebParam(name = "municipioNome") String municipioNome
    );
}

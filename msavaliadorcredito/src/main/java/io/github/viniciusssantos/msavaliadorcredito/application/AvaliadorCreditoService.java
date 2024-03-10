package io.github.viniciusssantos.msavaliadorcredito.application;

import feign.FeignException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErrosComunicacaoMicroservicoException;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.*;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.Cartao;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.CartoeesResourceClient;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.viniciusssantos.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import jdk.security.jarsigner.JarSignerException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clienteResourceClient;
    private final CartoeesResourceClient cartoeesResourceClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;


    public SituacaoCliente obterSituacaoCredito(String cpf) throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        try {

            //obterDadosCliente -MSCLIENTES
            //obter cartoes dos clientes MSCARTOES
            ResponseEntity<DadosCliente> dadosClietesResponse = clienteResourceClient.dadosClientes(cpf);
            ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoeesResourceClient.getCartoesByCliente(cpf);

            return SituacaoCliente
                    .builder()
                    .cliente(dadosClietesResponse.getBody())
                    .cartoes(cartoesResponse.getBody())
                    .build();
        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }
            throw new ErrosComunicacaoMicroservicoException(e.getMessage(), status);
        }
    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        try {
            ResponseEntity<DadosCliente> dadosClietesResponse = clienteResourceClient.dadosClientes(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoeesResourceClient.getCartoesRendaAteh(renda);
            List<Cartao> cartoes = cartoesResponse.getBody();

            var listaCartoesAprovados = cartoes.stream().map(cartao -> {
                DadosCliente dadosCliente = dadosClietesResponse.getBody();
                BigDecimal limiteBasico = cartao.getLimiteBasico();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
                var fator = idadeBD.divide(BigDecimal.valueOf(10));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);

                CartaoAprovado aprovado = new CartaoAprovado();
                aprovado.setCartao(cartao.getNome());
                aprovado.setBandeira(cartao.getBandeira());
                aprovado.setLimiteAprovado(limiteAprovado);

                return aprovado;
            }).collect(Collectors.toList());

            return new RetornoAvaliacaoCliente(listaCartoesAprovados);


        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }
            throw new ErrosComunicacaoMicroservicoException(e.getMessage(), status);
        }
    }

    public ProtocoloSolicitacaoCartao solicitarEmissaoDeCartao(DadosSolicitacaoEmissaoCartao dados) {
        try {
            emissaoCartaoPublisher.solicitarCartao(dados);
            var protocol = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocol);
        } catch (Exception e) {
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }

    }
}

package io.github.viniciusssantos.msavaliadorcredito.application;

import feign.FeignException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErrosComunicacaoMicroservicoException;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.DadosCliente;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.Cartao;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.viniciusssantos.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.viniciusssantos.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliadorCreditoServiceTest {

    @Mock
    private ClienteResourceClient clienteResourceClient;
    @Mock
    private CartoesResourceClient cartoesResourceClient;
    @Mock
    private SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    @InjectMocks
    private AvaliadorCreditoService service;

    @Test
    void realizarAvaliacao_calculaLimiteAprovadoProporcionalAIdade() throws Exception {
        var dadosCliente = new DadosCliente();
        dadosCliente.setId(1L);
        dadosCliente.setNome("Fulano");
        dadosCliente.setIdade(40);

        var cartao = new Cartao();
        cartao.setId(1L);
        cartao.setNome("Gold");
        cartao.setBandeira("VISA");
        cartao.setLimiteBasico(BigDecimal.valueOf(1000));

        when(clienteResourceClient.dadosClientes(anyString())).thenReturn(ResponseEntity.ok(dadosCliente));
        when(cartoesResourceClient.getCartoesRendaAteh(anyLong())).thenReturn(ResponseEntity.ok(List.of(cartao)));

        RetornoAvaliacaoCliente retorno = service.realizarAvaliacao("12345678900", 5000L);

        assertThat(retorno.getCartoes()).hasSize(1);
        // fator = idade / 10 = 4; limiteAprovado = fator * limiteBasico = 4 * 1000
        assertThat(retorno.getCartoes().get(0).getLimiteAprovado()).isEqualByComparingTo(BigDecimal.valueOf(4000));
    }

    @Test
    void obterSituacaoCredito_lancaNotFoundQuandoClienteNaoExiste() {
        FeignException notFound = mock(FeignException.class);
        when(notFound.status()).thenReturn(404);
        when(clienteResourceClient.dadosClientes(anyString())).thenThrow(notFound);

        assertThrows(DadosClienteNotFoundException.class, () -> service.obterSituacaoCredito("00000000000"));
    }

    @Test
    void realizarAvaliacao_mapeiaFalhaDeConectividadeParaBadGateway() {
        FeignException connectivityFailure = mock(FeignException.class);
        when(connectivityFailure.status()).thenReturn(-1);
        when(clienteResourceClient.dadosClientes(anyString())).thenThrow(connectivityFailure);

        ErrosComunicacaoMicroservicoException ex = assertThrows(ErrosComunicacaoMicroservicoException.class,
                () -> service.realizarAvaliacao("12345678900", 5000L));

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    }

    @Test
    void solicitarEmissaoDeCartao_encapsulaFalhaDoPublisherComoErroDeNegocio() throws Exception {
        var dados = new DadosSolicitacaoEmissaoCartao();
        dados.setIdCartao(1L);
        dados.setCpf("12345678900");
        dados.setEndereco("Rua A");
        dados.setLimiteLiberado(BigDecimal.valueOf(500));

        doThrow(new RuntimeException("fila indisponivel")).when(emissaoCartaoPublisher).solicitarCartao(dados);

        assertThrows(ErroSolicitacaoCartaoException.class, () -> service.solicitarEmissaoDeCartao(dados));
    }
}

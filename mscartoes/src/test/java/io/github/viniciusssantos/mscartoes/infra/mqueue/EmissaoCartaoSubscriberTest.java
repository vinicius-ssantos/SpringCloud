package io.github.viniciusssantos.mscartoes.infra.mqueue;

import io.github.viniciusssantos.mscartoes.domain.BandeiraCartao;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import io.github.viniciusssantos.mscartoes.domain.ClienteCartao;
import io.github.viniciusssantos.mscartoes.infra.repository.CartaoRespository;
import io.github.viniciusssantos.mscartoes.infra.repository.ClienteCartaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmissaoCartaoSubscriberTest {

    @Mock
    private CartaoRespository cartaoRespository;
    @Mock
    private ClienteCartaoRepository clienteCartaoRepository;

    @InjectMocks
    private EmissaoCartaoSubscriber subscriber;

    @Test
    void receberSolicitacaoEmissao_persisteClienteCartaoParaPayloadValido() {
        Cartao cartao = new Cartao("Gold", BandeiraCartao.VISA, BigDecimal.valueOf(5000), BigDecimal.valueOf(1000));
        when(cartaoRespository.findById(1L)).thenReturn(Optional.of(cartao));

        String payload = "{\"idCartao\":1,\"cpf\":\"12345678900\",\"endereco\":\"Rua A\",\"limiteLiberado\":800}";

        subscriber.receberSolicitacaoEmissao(payload);

        ArgumentCaptor<ClienteCartao> captor = ArgumentCaptor.forClass(ClienteCartao.class);
        verify(clienteCartaoRepository).save(captor.capture());
        ClienteCartao salvo = captor.getValue();
        assertThat(salvo.getCpf()).isEqualTo("12345678900");
        assertThat(salvo.getCartao()).isEqualTo(cartao);
        assertThat(salvo.getLimite()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
    void receberSolicitacaoEmissao_rejeitaSemRequeueQuandoJsonInvalido() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> subscriber.receberSolicitacaoEmissao("{ json invalido"));
    }

    @Test
    void receberSolicitacaoEmissao_rejeitaSemRequeueQuandoCartaoNaoExiste() {
        when(cartaoRespository.findById(99L)).thenReturn(Optional.empty());
        String payload = "{\"idCartao\":99,\"cpf\":\"12345678900\",\"endereco\":\"Rua A\",\"limiteLiberado\":800}";

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> subscriber.receberSolicitacaoEmissao(payload));
    }
}

package io.github.viniciusssantos.mscartoes.infra.mqueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import io.github.viniciusssantos.mscartoes.domain.ClienteCartao;
import io.github.viniciusssantos.mscartoes.domain.DadosSolicitacaoEmissaoCartao;
import io.github.viniciusssantos.mscartoes.infra.repository.CartaoRepository;
import io.github.viniciusssantos.mscartoes.infra.repository.ClienteCartaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmissaoCartaoSubscriber {

    private final CartaoRepository cartaoRepository;
    private final ClienteCartaoRepository clienteCartaoRepository;

    @RabbitListener(queues = "${mq.queues.emissao-cartoes}")
    public void receberSolicitacaoEmissao(@Payload String payload) {
        try {
            var mapper = new ObjectMapper();
            DadosSolicitacaoEmissaoCartao dados =
                    mapper.readValue(payload, DadosSolicitacaoEmissaoCartao.class);

            Cartao cartao = cartaoRepository.findById(dados.getIdCartao())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Cartao nao encontrado para o id " + dados.getIdCartao()));
            ClienteCartao clienteCartao = new ClienteCartao();
            clienteCartao.setCartao(cartao);
            clienteCartao.setCpf(dados.getCpf());
            clienteCartao.setLimite(dados.getLimiteLiberado());
            clienteCartaoRepository.save(clienteCartao);
        } catch (Exception e) {
            log.error("Falha ao processar solicitacao de emissao de cartao: {}", payload, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}

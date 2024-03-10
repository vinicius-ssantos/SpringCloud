package io.github.viniciusssantos.msavaliadorcredito.infra.mqueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.viniciusssantos.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class SolicitacaoEmissaoCartaoPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Queue queueEmissaoCartoes;

    // O construtor injeta o RabbitTemplate e a fila (queue) necessária.

    // O método solicitarCartao é responsável por enviar a solicitação de emissão de cartão para a fila.
    public void solicitarCartao(DadosSolicitacaoEmissaoCartao dados) throws JsonProcessingException {
        // Converte os dados em um formato JSON
        var json = convertIntoJson(dados);
        // Envia a mensagem para a fila especificada (queueEmissaoCartoes)
        rabbitTemplate.convertAndSend(queueEmissaoCartoes.getName(), json);
    }

    // O método convertIntoJson converte o objeto DadosSolicitacaoEmissaoCartao em uma representação JSON.
    private String convertIntoJson(DadosSolicitacaoEmissaoCartao dados) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var json = mapper.writeValueAsString(dados);
        return json;
    }
}

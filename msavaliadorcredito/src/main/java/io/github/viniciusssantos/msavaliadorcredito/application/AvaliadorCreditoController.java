package io.github.viniciusssantos.msavaliadorcredito.application;


import io.github.viniciusssantos.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErrosComunicacaoMicroservicoException;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("avaliacoes-credito")
@RequiredArgsConstructor
@Tag(name = "Avaliação de Crédito", description = "Consulta de situação de crédito e solicitação de emissão de cartão")
public class AvaliadorCreditoController {

    private final AvaliadorCreditoService avaliadorCreditoService;


    @Operation(summary = "Consulta situação de crédito do cliente", description = "Combina dados do cliente (msclientes) e cartões já emitidos (mscartoes) via chamadas síncronas")
    @GetMapping(value = "situacao-cliente", params = "cpf")
    public ResponseEntity<?> consultaSituacaoCliente(@RequestParam("cpf") String cpf)
            throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        SituacaoCliente situacaoCliente = avaliadorCreditoService.obterSituacaoCredito(cpf);
        return ResponseEntity.ok(situacaoCliente);
    }


    @Operation(summary = "Avalia o crédito do cliente", description = "Calcula o limite aprovado por cartão elegível, proporcional à idade do cliente (idade/10 x limite básico do cartão)")
    @PostMapping
    public ResponseEntity<?> realizarAvaliacoes(@Valid @RequestBody DadosAvaliacao dados)
            throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        RetornoAvaliacaoCliente retornoAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dados.getCpf(), dados.getRenda());
        return ResponseEntity.ok(retornoAvaliacaoCliente);
    }

    @Operation(summary = "Solicita a emissão de um cartão", description = "Publica a solicitação na fila assíncrona (RabbitMQ); o limite liberado é recalculado no servidor e não aceito do chamador")
    @PostMapping("solicitacoes-cartao")
    public ResponseEntity<?> solicitarCartao(@Valid @RequestBody DadosSolicitacaoEmissaoCartao dados) {
        ProtocoloSolicitacaoCartao protocoloSolicitacaoCartao = avaliadorCreditoService
                .solicitarEmissaoDeCartao(dados);
        return ResponseEntity.ok(protocoloSolicitacaoCartao);
    }

}

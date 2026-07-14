package io.github.viniciusssantos.msavaliadorcredito.application;


import io.github.viniciusssantos.msavaliadorcredito.application.exception.DadosClienteNotFoundException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErroSolicitacaoCartaoException;
import io.github.viniciusssantos.msavaliadorcredito.application.exception.ErrosComunicacaoMicroservicoException;
import io.github.viniciusssantos.msavaliadorcredito.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("avaliacoes-credito")
@RequiredArgsConstructor
public class AvaliadorCreditoController {

    private final AvaliadorCreditoService avaliadorCreditoService;


    @GetMapping(value = "situacao-cliente", params = "cpf")
    public ResponseEntity<?> consultaSituacaoCliente(@RequestParam("cpf") String cpf)
            throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        SituacaoCliente situacaoCliente = avaliadorCreditoService.obterSituacaoCredito(cpf);
        return ResponseEntity.ok(situacaoCliente);
    }


    @PostMapping
    public ResponseEntity<?> realizarAvaliacoes(@Valid @RequestBody DadosAvaliacao dados)
            throws DadosClienteNotFoundException, ErrosComunicacaoMicroservicoException {
        RetornoAvaliacaoCliente retornoAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dados.getCpf(), dados.getRenda());
        return ResponseEntity.ok(retornoAvaliacaoCliente);
    }

    @PostMapping("solicitacoes-cartao")
    public ResponseEntity<?> solicitarCartao(@Valid @RequestBody DadosSolicitacaoEmissaoCartao dados) {
        ProtocoloSolicitacaoCartao protocoloSolicitacaoCartao = avaliadorCreditoService
                .solicitarEmissaoDeCartao(dados);
        return ResponseEntity.ok(protocoloSolicitacaoCartao);
    }

}

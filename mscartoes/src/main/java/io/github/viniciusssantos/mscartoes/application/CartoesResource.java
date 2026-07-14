package io.github.viniciusssantos.mscartoes.application;


import io.github.viniciusssantos.mscartoes.application.representation.CartaoResponse;
import io.github.viniciusssantos.mscartoes.application.representation.CartoesPorClienteResponse;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import io.github.viniciusssantos.mscartoes.domain.ClienteCartao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("cartoes")
@RequiredArgsConstructor
@Tag(name = "Cartões", description = "Catálogo de cartões e cartões emitidos por cliente")
public class CartoesResource {


    private final CartaoService cartaoService;
    private final ClienteCartaoService clienteCartaoService;


    @Operation(summary = "Cadastra um cartão", description = "Cria um novo cartão no catálogo (nome, bandeira, renda mínima e limite básico)")
    @PostMapping
    public ResponseEntity cadastra(@Valid @RequestBody CartaoSaveRequest request){
        Cartao cartao = request.toModel();
        cartaoService.save(cartao);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Lista cartões por renda", description = "Retorna os cartões do catálogo cuja renda mínima exigida é menor ou igual à informada")
    @GetMapping(params = "renda")
    public ResponseEntity<List<CartaoResponse>> getCartoesRendaAteh(@RequestParam("renda") Long renda){
        List<Cartao> list = cartaoService.getCartoesRendaMenorIgual(renda);
        List<CartaoResponse> resultList = list.stream().map(CartaoResponse::fromModel).collect(Collectors.toList());
        return ResponseEntity.ok(resultList);
    }

    @Operation(summary = "Lista cartões emitidos para um cliente", description = "Retorna os cartões já emitidos (com limite liberado) para o CPF informado")
    @GetMapping(params = "cpf")
    public ResponseEntity<List<CartoesPorClienteResponse>> getCartoesByCliente(@RequestParam("cpf") String cpf){
    List<ClienteCartao> lista  = clienteCartaoService.listCartoesByCpf(cpf);
    List<CartoesPorClienteResponse> resultList =
            lista.stream().map(CartoesPorClienteResponse::fromModel).collect(Collectors.toList());
    return ResponseEntity.ok(resultList);
}

    @Operation(summary = "Busca cartão por id", description = "Retorna os dados de um cartão do catálogo pelo id, ou 404 se não existir")
    @GetMapping("/{id}")
    public ResponseEntity<CartaoResponse> getPorId(@PathVariable("id") Long id){
        return cartaoService.getPorId(id)
                .map(CartaoResponse::fromModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}

package io.github.viniciusssantos.msclientes.application;

import io.github.viniciusssantos.msclientes.application.representation.ClienteSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro e consulta de dados de clientes por CPF")
public class ClienteResource {

    private final ClienteService service;

    @Operation(summary = "Cadastra um cliente", description = "Cria um novo cliente e retorna a URL do recurso criado no header Location")
    @PostMapping
    public ResponseEntity save(@Valid @RequestBody ClienteSaveRequest request) {
        var cliente = request.toModel();
        service.save(cliente);
        URI headerLocation = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .query("cpf={cpf}")
                .buildAndExpand(cliente.getCpf())
                .toUri();
        return ResponseEntity.created(headerLocation).build();
    }

    @Operation(summary = "Busca cliente por CPF", description = "Retorna os dados do cliente ou 404 se o CPF não estiver cadastrado")
    @GetMapping(params = "cpf")
    public ResponseEntity dadosCliente(@RequestParam("cpf")String cpf){
        var cliente = service.getByCpf(cpf);
        if(cliente.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleCpfDuplicado(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe um cliente cadastrado com o CPF informado");
    }
}

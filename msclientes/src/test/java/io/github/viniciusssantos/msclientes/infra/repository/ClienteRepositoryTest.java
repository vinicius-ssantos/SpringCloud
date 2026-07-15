package io.github.viniciusssantos.msclientes.infra.repository;

import io.github.viniciusssantos.msclientes.domain.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void findByCpf_retornaClienteQuandoCpfExiste() {
        repository.save(new Cliente("12345678900", "Fulano", 30));

        var encontrado = repository.findByCpf("12345678900");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Fulano");
    }

    @Test
    void findByCpf_retornaVazioQuandoCpfNaoExiste() {
        var encontrado = repository.findByCpf("00000000000");

        assertThat(encontrado).isEmpty();
    }

    @Test
    void save_rejeitaCpfDuplicado() {
        repository.save(new Cliente("12345678900", "Fulano", 30));
        repository.flush();

        var duplicado = new Cliente("12345678900", "Outro Nome", 40);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.save(duplicado);
            repository.flush();
        });
    }
}

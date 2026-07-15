package io.github.viniciusssantos.mscartoes.infra.repository;

import io.github.viniciusssantos.mscartoes.domain.BandeiraCartao;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartaoRepositoryTest {

    @Autowired
    private CartaoRepository repository;

    @Test
    void findByRendaLessThanEqual_retornaSoCartoesDentroDoLimiteDeRenda() {
        repository.save(new Cartao("Basico", BandeiraCartao.VISA, BigDecimal.valueOf(1000), BigDecimal.valueOf(500)));
        repository.save(new Cartao("Gold", BandeiraCartao.MASTERCARD, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000)));

        var resultado = repository.findByRendaLessThanEqual(BigDecimal.valueOf(1000));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Basico");
    }

    @Test
    void findByRendaLessThanEqual_retornaVazioQuandoNenhumCartaoAtendeARenda() {
        repository.save(new Cartao("Gold", BandeiraCartao.MASTERCARD, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000)));

        var resultado = repository.findByRendaLessThanEqual(BigDecimal.valueOf(100));

        assertThat(resultado).isEmpty();
    }
}

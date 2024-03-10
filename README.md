# Tecnologias, Frameworks e Ferramentas Utilizadas nos Microserviços

Os microserviços do projeto utilizam uma variedade de tecnologias, frameworks e ferramentas para fornecer uma arquitetura escalável e eficiente.

## Tecnologias Principais

- **Java**: Linguagem de programação principal para escrever os microserviços, conhecida por sua orientação a objetos e ampla adoção em aplicações empresariais.

- **Docker**: Plataforma de código aberto utilizada para empacotar aplicações em contêineres, facilitando a implantação e o gerenciamento dos microserviços.

## Frameworks e Ferramentas

- **Spring Boot**: Framework que simplifica o desenvolvimento de aplicações Spring, utilizado para criar e configurar os microserviços do projeto.

- **Spring Cloud**: Conjunto de ferramentas e frameworks da Pivotal que facilitam a construção e o gerenciamento de aplicações baseadas em microserviços, fornecendo soluções para problemas comuns em sistemas distribuídos.

- **Eureka Server (cursoms-eureka)**: Serviço de descoberta de microserviços da Netflix, usado para registrar e descobrir microserviços, facilitando a comunicação entre eles em um ambiente distribuído.

- **RabbitMQ (cursoms-rabbitmq)**: Servidor de mensagens de código aberto utilizado para comunicação assíncrona entre os microserviços, oferecendo uma maneira confiável de trocar mensagens.

- **Keycloak (cursoms-keycloak)**: Servidor de autorização e autenticação de código aberto, utilizado para gerenciar a autenticação e autorização dos microserviços, garantindo a segurança das comunicações.

- **API Gateway (cursoms-gateway)**: Padrão de design utilizado para encapsular a complexidade da interação com vários microserviços em um único ponto de acesso, roteando as solicitações para os microserviços apropriados.

## Ferramentas de Suporte

- **Maven**: Ferramenta de automação de compilação utilizada para gerenciar as dependências do projeto e construir o projeto.

Cada microserviço do projeto tem sua própria funcionalidade específica, como gerenciamento de clientes, operações de cartões, avaliação de crédito, etc. Eles se comunicam entre si utilizando o servidor Eureka para descoberta de serviço e o servidor RabbitMQ para comunicação assíncrona.

Este conjunto de tecnologias e ferramentas forma a base sólida para uma arquitetura de microserviços escalável, modular e eficiente.


# Tópicos Abordados

## Arquitetura de Microserviços
Fundamentos da arquitetura de microserviços, incluindo design, implementação e gerenciamento.

## Service Discovery
Aprender a descobrir microserviços e facilitar a comunicação em um ambiente distribuído.

## API Gateway
Utilização de um API Gateway para roteamento eficiente de solicitações para os microserviços correspondentes.

## Balanceamento de Carga
Distribuição inteligente de solicitações entre os microserviços para otimizar desempenho e disponibilidade.

## Desenvolvimento de Microserviços
Utilização do Spring Boot para desenvolver microserviços de forma eficaz.

## Comunicação Síncrona e Assíncrona de Microserviços
Explorar métodos de comunicação entre microserviços, incluindo comunicação síncrona e assíncrona.

## Serviço/Fila de Mensageria com RabbitMQ
Aprender a usar o RabbitMQ para comunicação assíncrona entre os microserviços.

## Authorization Server com Keycloak
Utilização do Keycloak para gerenciar autenticação e autorização em um ambiente de microserviços.

## Desenvolvimento de Imagens Docker
Criação de imagens Docker personalizadas para encapsular os microserviços.

## Criação de Containers Docker a partir das Imagens Customizadas
Utilização de imagens Docker personalizadas para criar e gerenciar containers Docker para os microserviços.
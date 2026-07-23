# ADR-0001: Spring JDBC em vez de JPA/Hibernate

**Status:** Aceito · **Data:** 2026-07-23

## Contexto

O esqueleto da API precisa de acesso a dados desde o primeiro card. O uso real é simples: carregar `glossary_entry` inteiro para um `Map` em memória no boot, e queries de leitura (exato/lema/fuzzy) sobre poucas tabelas (`glossary_entry`, `translation_log`), sem grafo de relacionamentos, sem writes complexos.

## Decisão

Usar `spring-boot-starter-jdbc` (`JdbcTemplate`) em vez de `spring-boot-starter-data-jpa`/Hibernate.

## Alternativas consideradas

- **JPA/Hibernate:** mapeamento objeto-relacional, lazy loading, cache de sessão — nada disso resolve um problema que existe aqui. Traz complexidade (proxies, N+1, dirty checking) sem benefício, e roda contra o princípio "simplicidade primeiro".

## Trade-offs

- Perde: geração automática de queries via repositórios Spring Data, migrations "auto" a partir de entidades.
- Ganha: menos mágica, queries explícitas e previsíveis, startup mais leve, sem curva de aprendizado de JPA para novos contribuidores.

## Gatilho de revisão

Se o modelo de dados crescer com relacionamentos reais (não é o caso hoje — o glossário é uma tabela quase plana) ou se escrever SQL manual para múltiplas tabelas virar fardo perceptível.

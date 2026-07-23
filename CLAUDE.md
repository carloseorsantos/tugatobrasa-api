# TugaToBrasa API — contexto do projeto

Backend do TugaToBrasa: tradutor open source bidirecional pt-PT ⇌ pt-BR de palavras, gírias e expressões, com alerta de falsos amigos. Este repo contém a API, o glossário (fonte da verdade) e as regras de tradução.

## Princípios inegociáveis

1. **Sem LLM no runtime.** Tradução 100% determinística: glossário curado + regras. Cobertura cresce pela comunidade (issues/PRs no CSV), nunca por IA.
2. **Custo zero.** Nenhuma API paga; self-host em um `docker compose up`.
3. **Dado é código.** Glossário em `glossary/glossario.csv` versionado; CI valida todo PR (spec: `docs/glossario-spec.md`).
4. **Bidirecional e assimétrico.** "propina"→mensalidade (PT→BR) e "propina"→suborno (BR→PT) são pares distintos. Nunca assumir simetria.
5. **Backend stateless.** Estado só em Postgres + cache descartável.
6. **Falso amigo sem alerta = bug crítico**, mesmo com código verde.

## Stack e arquitetura

- Java 21, Spring Boot 3.x, Maven, Postgres (pg_trgm), Flyway, cache Caffeine.
- Pipeline (Chain of Responsibility): `Normalizer → GlossaryResolver (exato → lema → fuzzy) → RuleResolver (por direção) → NotFoundHandler (sugestões + convite de contribuição — nunca erro)`.
- Contrato: `openapi.yml` versionado aqui (springdoc); o front gera tipos TS dele. Breaking change = bump `/v2`. Mudou comportamento da API → atualiza `openapi.yml` no MESMO PR.
- Pacotes por feature: `translation/`, `glossary/`, `feedback/`. Records para DTOs, sealed interfaces para resultados do pipeline, injeção por construtor.
- Erros: Problem Details (RFC 7807). Nunca 500 por input de usuário.

## Testes

- JUnit 5 + Testcontainers com Postgres real (fuzzy pg_trgm não roda em H2).
- Todo resolver nasce com testes; todo `false_friend=true` tem caso de teste nas DUAS direções (tradução + presença do alerta).
- Casos linguísticos versionados em `src/test/resources/linguistic-cases.csv`.

## Convenções

- Conventional Commits em inglês (`feat:`, `fix:`, `test:`…); branches `feat/...`/`fix/...`; PRs pequenos (<400 linhas).
- Decisão estrutural (módulo, dependência, endpoint, schema) → mini-ADR em `docs/adr/NNN-titulo.md`.

## Equipe e fluxo (skills em .claude/skills/)

Papéis: `tugatobrasa-arquiteto` (veto em mudança estrutural), `tugatobrasa-dev-senior`, `tugatobrasa-qa` (veto no "Concluído"), `tugatobrasa-design`, `tugatobrasa-sm` (board), `tugatobrasa-fluxo` (ciclo completo: planning → refinamento → implementação → QA gate → encerramento). Peça "roda o fluxo" para executar um ciclo.

## Links

- Kanban (fonte da verdade do trabalho): https://app.notion.com/p/TugaToBrasa-3a5d2ee1c20d806a8e21c0d87f34f1f7
- Front: https://github.com/carloseorsantos/tugatobrasa
- Design system: pasta `tugatobrasa-design-system` (projeto Claude Design)
- Roadmap: Fase 1 (base + glossário) → 2 (/translate) → 3 (front) → 4 (regras + CI) → 5 (glossário UI + feedback)

## Licenças

Código MIT; `glossary/` CC-BY-SA 4.0.

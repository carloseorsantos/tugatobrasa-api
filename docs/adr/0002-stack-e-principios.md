# ADR-0002: Stack e princípios do projeto

**Status:** Aceito · **Data:** 2026-07-23

## Contexto

O TugaToBrasa nasceu com um conjunto de decisões estruturais tomadas antes da fundação dos repositórios, registradas em prosa no `system-design-tugatobrasa.md` e na skill `tugatobrasa-arquiteto`. Esta ADR formaliza essas decisões no formato padrão do projeto, para que fiquem versionadas, com alternativas e gatilho de revisão explícitos — não apenas descritas.

## Decisões

### 1. Sem LLM no pipeline de tradução

**Decisão:** a tradução é 100% determinística — glossário curado (CSV) + regras. Nenhuma chamada a LLM no caminho de request/response.

**Alternativas consideradas:** LLM como fallback para termos fora do glossário (era o plano original em `plano-tugatobrasa.md`, hoje superado). Rejeitado: custo por request, latência imprevisível, resultado não determinístico/não auditável — o produto vende precisão em falsos amigos, e um LLM pode alucinar exatamente onde a precisão importa mais.

**Trade-off:** cobertura cresce mais devagar (depende de contribuição humana), mas cada entrada é auditável e reproduzível.

**Gatilho de revisão:** nunca, a menos que o princípio de custo zero também seja revisto — os dois andam juntos.

### 2. Custo zero para rodar e contribuir

**Decisão:** nenhuma dependência de serviço pago. Self-host completo com `docker compose up`.

**Alternativas consideradas:** serviços gerenciados (RDS, Elasticsearch Cloud, etc.) — rejeitados porque criam barreira de entrada para contribuidores e vão contra a natureza open source do projeto.

**Trade-off:** operação e scaling ficam por conta de quem hospeda; sem suporte gerenciado.

**Gatilho de revisão:** se o projeto precisar de disponibilidade multi-região ou SLA formal (fora do escopo hoje).

### 3. Dois repositórios independentes

**Decisão:** `tugatobrasa` (frontend) e `tugatobrasa-api` (backend + glossário) como repos separados.

**Alternativas consideradas:** monorepo — rejeitado: ciclos de release e CI independentes, contribuidor de front não precisa clonar Java e vice-versa, issues organizadas por área.

**Trade-off:** contrato entre repos (`openapi.yml`) precisa de disciplina de versionamento — breaking change exige bump explícito.

**Gatilho de revisão:** se o time crescer e coordenar dois repos virar mais atrito do que o monorepo evitaria.

### 4. Dado é código — glossário em CSV versionado

**Decisão:** `glossary/glossario.csv` é a fonte da verdade do produto, versionado em Git, curadoria via PR, CI valida.

**Alternativas consideradas:** CMS/admin UI para curadoria — rejeitado para o MVP: CSV com diff em PR é auditável e não exige infraestrutura extra; admin UI fica para quando o volume de contribuição doer.

**Trade-off:** curadoria não-técnica passa por um intermediário (issue → PR) em vez de formulário direto.

**Gatilho de revisão:** volume de contribuições via issue supera a capacidade de conversão manual em PR.

### 5. Postgres + pg_trgm para busca fuzzy

**Decisão:** Postgres com extensão `pg_trgm` e índices GIN resolve o lookup fuzzy do glossário.

**Alternativas consideradas:** Elasticsearch — rejeitado: a escala do projeto (milhares de entradas, não milhões) não justifica um serviço a mais; `pg_trgm` é suficiente e já mora dentro do banco que o projeto já precisa ter.

**Trade-off:** fuzzy search menos sofisticado que um motor de busca dedicado (sem stemming avançado nativo, sem scoring configurável).

**Gatilho de revisão:** latência de busca fuzzy vira gargalo mensurável, ou necessidade de busca semântica/multilíngue além de PT.

### 6. Contrato publicado via OpenAPI

**Decisão:** `openapi.yml` gerado por springdoc no `tugatobrasa-api`, é a fonte da verdade do contrato; o front gera tipos TS a partir dele.

**Alternativas consideradas:** GraphQL — rejeitado: 3 endpoints não justificam o peso de um schema GraphQL; REST + OpenAPI é suficiente e mais simples de consumir para contribuidores.

**Trade-off:** menos flexibilidade de query do lado do cliente (não é um problema real com 3 endpoints).

**Gatilho de revisão:** número de endpoints crescer a ponto de over-fetching/under-fetching virar dor real no front.

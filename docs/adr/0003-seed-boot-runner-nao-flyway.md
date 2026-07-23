# ADR-0003: Seed do glossário via ApplicationRunner no boot, não migration Flyway de dados

**Status:** Aceito · **Data:** 2026-07-23

## Contexto

`glossary/glossario.csv` é a fonte da verdade do glossário e muda a cada PR de curadoria (dado é código, [[ADR-0002]]). O Postgres precisa refletir o conteúdo atual do CSV para o `GlossaryResolver` consultar.

## Decisão

Um `ApplicationRunner` (`GlossaryCsvSeeder`) roda depois do Flyway no boot da aplicação: lê `glossary/glossario.csv`, valida cada linha e faz `UPSERT` (`ON CONFLICT (term_pt, term_br) DO UPDATE`) no Postgres. Cada boot reconcilia o banco com o estado atual do CSV.

## Alternativas consideradas

- **Migration Flyway versionada por conteúdo** (gerar `V2__seed.sql` com `INSERT`s a partir do CSV): rejeitado. Flyway versiona *schema*, não dado que muda a cada PR — cada termo novo exigiria uma migration nova (ou reescrever uma migration existente, o que o Flyway proíbe por checksum). Não escala com o ritmo de curadoria da comunidade.
- **Seed manual/script separado do boot:** rejeitado — depende de alguém lembrar de rodar; o boot automático garante que o banco nunca fica dessincronizado do CSV que está no Git.

## Trade-offs

- Perde: histórico de "quando cada termo entrou" no schema history do Flyway.
- Ganha: banco sempre reflete o CSV atual sem passo manual; PR de curadoria + deploy é o único fluxo necessário.
- Limitação conhecida: o seeder não remove do banco termos que saíram do CSV (só insere/atualiza) — se um termo for removido do CSV, a linha antiga permanece no Postgres até uma limpeza manual.

## Gatilho de revisão

Se remoção de termos do CSV precisar refletir automaticamente no banco (hoje não há caso de uso — `status=REJECTED` cobre "não usar este termo" sem apagar o histórico).

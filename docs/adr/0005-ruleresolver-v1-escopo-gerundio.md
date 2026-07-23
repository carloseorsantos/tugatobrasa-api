# ADR-0005: RuleResolver v1 — só gerúndio ⇌ "estar a"

**Status:** Aceito · **Data:** 2026-07-23

## Contexto

Fase 4 do roadmap promete "tradução de frases (regras)" desde a fundação do projeto — o pipeline documentado é `Normalizer → GlossaryResolver → RuleResolver → NotFoundHandler`, mas `RuleResolver` nunca foi especificado nem implementado. Dois padrões gramaticais foram citados nas notas de fundação como alvo: gerúndio ⇌ "estar a" (`estou a fazer` ⇌ `estou fazendo`) e próclise ⇌ ênclise (posição de pronome: `me dá` ⇌ `dá-me`).

## Decisão

**v1 cobre só gerúndio ⇌ "estar a".** Próclise/ênclise fica pra um ADR e card separados, depois.

Motivo: próclise/ênclise em português não é uma regra simples de inverter — o pt-PT formal escrito também usa ênclise por padrão em oração afirmativa principal (`dá-me`), e só vai pra próclise com palavras atratoras específicas (negação, alguns advérbios, orações subordinadas). Uma regra ingênua ("PT sempre próclise, BR sempre ênclise") erraria sistematicamente e produziria port​uguês tecnicamente incorreto — pior que não ter a regra (doc de QA: "falso positivo de regra é pior que regra ausente"). Merece seu próprio ciclo de refinamento.

Gerúndio ⇌ "estar a" é mecânico e bem definido: detectar `estar` conjugado + infinitivo com "a" (PT) ou + gerúndio (BR), converter a forma verbal. Risco de erro concentrado só na formação do gerúndio/infinitivo de verbos irregulares — mitigável com uma tabela pequena e fechada.

### Estrutura

- Pacote novo `com.tugatobrasa.api.translation.rules`.
- Interface `PhraseRule` — `Optional<String> apply(String normalizedText, Direction direction)`. Cada regra tenta casar um padrão e devolve a frase transformada, ou `Optional.empty()` se não se aplica.
- `RuleResolver` — orquestrador, mantém `List<PhraseRule>`, tenta cada uma em ordem, primeira que casar vence. v1 tem uma regra só (`GerundEstarARule`), mas a estrutura já é extensível pra próclise/ênclise depois sem reabrir este ADR.
- Entra no pipeline **depois** do `GlossaryResolver` retornar `NotResolved` e **antes** do `NotFoundHandler` — só ativa quando não há match de glossário, e só transforma frases que batem o padrão gramatical; qualquer outra entrada segue pro fluxo NOT_FOUND exatamente como hoje (mudança aditiva, sem risco de regressão no caminho existente).
- `resolvedBy = "RULE"` (o `README.md` já antecipava esse valor), `confidence = 1.0` (transformação determinística, não é busca aproximada), `falseFriend = false` sempre — é transformação gramatical, não decisão lexical, não tem sentido de "falso amigo" aqui.

### Verbos irregulares

Tabela fechada e pequena só com os irregulares mais comuns (ex.: pôr→pondo, ser→sendo, ir→indo, vir→vindo, dizer→dizendo — a formação de gerúndio de irregulares em pt é mais estável que a de outros tempos, a lista real deve ser curta). Verbo regular: sufixo -ar/-er/-ir → -ando/-endo/-indo. Verbo que não bate nem a tabela nem o padrão regular esperado: `PhraseRule` devolve vazio, cai pro `NotFoundHandler` — nunca "chuta" uma forma.

## Alternativas consideradas

- **Cobrir as duas regras (gerúndio + próclise/ênclise) no mesmo v1:** rejeitado — próclise/ênclise tem risco linguístico real de gerar frase gramaticalmente errada; não dá pra tratar com a mesma pressa da regra mecânica de gerúndio.
- **Um `PhraseRule` monolítico em vez de interface + lista:** rejeitado — vai precisar de mais regras depois (próclise/ênclise é a próxima confirmada), a interface já paga o custo de extensão agora por um preço baixo.

## Gatilho de revisão

Quando o card de próclise/ênclise entrar em refinamento — decidir lá se cabe na mesma interface `PhraseRule` (deve caber) ou se expõe alguma limitação do desenho atual.

# ADR-0004: Fallback por lema via Lucene `PortugueseStemmer`

**Status:** Aceito · **Data:** 2026-07-23

## Contexto

`GlossaryResolver` precisa de um segundo passo (depois do match exato) que resolva variações morfológicas — plural, conjugação verbal — de um termo já presente no glossário. Ex.: usuário digita "autocarros" (plural), o glossário tem "autocarro" (singular).

## Decisão

Usar `org.apache.lucene:lucene-analysis-common` (`PortugueseStemmer`, algoritmo RSLP/Orengo) para reduzir termos a um stem, indexado em memória junto dos demais índices do glossário (`GlossaryIndex`).

## Alternativas consideradas

- **Heurística própria** (ex.: cortar "s" final): reimplementar mesmo que parcialmente um algoritmo linguístico é o antipadrão clássico de "não reinventar a roda" — cobre poucos casos (plural regular) e erra em irregulares, plurais em "-ões"/"-ais", conjugação verbal. Rejeitado.
- **Outra lib de stemming PT** (ex.: Snowball standalone): Lucene já é madura, mantida, e o próprio `PortugueseStemmer` é internamente baseado em Snowball — não há ganho real em buscar uma dependência menor por esse motivo isolado.

## Trade-offs

- **Over-stemming conhecido:** o algoritmo é agressivo — pode, em teoria, colapsar palavras não relacionadas no mesmo stem. Aceitável: o pior caso é uma sugestão de tradução errada que o usuário reconhece como estranha (não é falso amigo silencioso — o produto já mostra a tradução, o usuário vê se faz sentido).
- Nova dependência (`lucene-analysis-common`) — módulo específico de análise textual, sem trazer o Lucene inteiro (índice de busca, etc.) que este projeto não usa.

## Gatilho de revisão

Se over-stemming gerar reclamação real de usuário (colisão de termos não relacionados na prática, não só em teoria).

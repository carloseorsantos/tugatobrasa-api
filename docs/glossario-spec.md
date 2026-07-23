# Especificação do `glossario.csv` — TugaToBrasa

> Contrato oficial das contribuições da comunidade. Este documento vai integrar o `CONTRIBUTING.md` do repo `tugatobrasa-api`, e as regras de validação viram o script de CI que roda em todo PR.
> Versão 1.0 · definida pelo QA, esquema validado pelo Arquiteto contra o system design.

## 1. Formato do arquivo

- Local: `glossary/glossario.csv` no repo `tugatobrasa-api`.
- Codificação **UTF-8 sem BOM**, separador **vírgula**, uma entrada por linha, com cabeçalho obrigatório na primeira linha.
- Campos que contêm vírgula devem estar entre aspas duplas (`"..."`); listas usam `|` como separador interno (ex.: `maneiro|massa`).
- Linhas vazias e espaços nas bordas dos campos são inválidos.

## 2. Colunas (ordem fixa — espelha a tabela `glossary_entry`)

| # | Coluna | Obrigatória | Formato | Exemplo |
|---|--------|-------------|---------|---------|
| 1 | `term_pt` | sim | minúsculas, NFC | `autocarro` |
| 2 | `term_br` | sim | minúsculas, NFC | `ônibus` |
| 3 | `alternatives_pt` | não | lista com `\|` | `machimbombo (regional)` |
| 4 | `alternatives_br` | não | lista com `\|` | `busão (informal)` |
| 5 | `register` | sim | `NEUTRO` \| `GIRIA` \| `CALAO` | `NEUTRO` |
| 6 | `false_friend` | sim | `true` \| `false` | `true` |
| 7 | `note_pt` | condicional | texto ≤ 280 chars | `Em Portugal, propina é a mensalidade…` |
| 8 | `note_br` | condicional | texto ≤ 280 chars | `No Brasil, propina significa suborno…` |
| 9 | `example_pt` | condicional | frase completa com o termo pt | `Vou apanhar o autocarro.` |
| 10 | `example_br` | condicional | frase completa com o termo br | `Vou pegar o ônibus.` |
| 11 | `region` | não | texto livre curto | `Porto` |
| 12 | `status` | sim | `CURATED` \| `PENDING` \| `REJECTED` | `CURATED` |

## 3. Regras de validação (aplicadas pelo CI, na ordem)

**Estruturais — erro fatal:**
1. Cabeçalho exatamente igual à ordem/nomes da tabela acima.
2. Toda linha com exatamente 12 campos.
3. Campos obrigatórios não-vazios; enums (`register`, `false_friend`, `status`) com valores válidos.
4. `term_pt` e `term_br` em minúsculas e normalizados NFC (exceção: nomes próprios — validador aceita com flag `--allow-proper-noun` mediante revisão).
5. **Sem duplicata exata do par** (`term_pt`,`term_br`). Múltiplas linhas com o mesmo `term_pt` (ou `term_br`) são permitidas — é assim que a assimetria direcional se representa (ex.: propina→mensalidade e propina→suborno em linhas distintas).

**Semânticas — erro fatal:**
6. `register ∈ {GIRIA, CALAO}` → `example_pt` **e** `example_br` obrigatórios (gíria sem exemplo não ensina ninguém).
7. `false_friend = true` → `note_pt` **e** `note_br` obrigatórias — o alerta precisa existir nas **duas direções**, escrito para o leitor daquele lado (a `note_br` explica ao brasileiro; a `note_pt` ao português).
8. `example_pt` deve conter `term_pt` (ou alternativa de `alternatives_pt`); idem para o lado br — exemplo que não usa o termo é erro.

**Avisos — não quebram o build, pedem atenção no review:**
9. Nota com mais de 280 caracteres (alerta deve ser lido em 5 segundos).
10. Termo com espaços múltiplos, pontuação final ou caixa mista.
11. Entrada `CALAO` sem `note_*` — recomendado avisar sobre o peso do termo.

## 4. Fluxo de contribuição

- **Não-dev:** abre issue com o template `novo-termo.yml` (termo, direção, registro, exemplo, é falso amigo?). Uma Action (`.github/workflows/glossary-issue-to-pr.yml`) transcreve os campos automaticamente pra um PR rascunho com a linha no CSV — só mecaniza a transcrição, não valida nada; o mantenedor completa o lado que falta e confirma contra dicionário antes de aprovar.
- **Dev:** PR direto no CSV. O CI roda a validação; qualquer erro fatal quebra o build com mensagem apontando linha e regra violada.
- Toda entrada nova entra como `PENDING`; vira `CURATED` só após revisão contra dicionários de referência (Priberam/Infopédia para pt-PT; Dicio/Michaelis para pt-BR), com a fonte citada no PR.
- `REJECTED` permanece no arquivo (com nota) para não ser re-proposta — o CSV é também a memória do que já foi avaliado.

## 5. Exemplo válido

```csv
term_pt,term_br,alternatives_pt,alternatives_br,register,false_friend,note_pt,note_br,example_pt,example_br,region,status
autocarro,ônibus,machimbombo (regional),busão (informal),NEUTRO,false,,,Vou apanhar o autocarro.,Vou pegar o ônibus.,,CURATED
fixe,legal,porreiro,maneiro|massa,GIRIA,false,,,Este filme é bué fixe!,Esse filme é muito legal!,,CURATED
propina,mensalidade,,,NEUTRO,true,"Em Portugal, propina é a mensalidade da universidade.","No Brasil, propina significa suborno — nunca diga que pagou propina na faculdade.",Paguei as propinas da universidade.,Paguei a mensalidade da faculdade.,,CURATED
propina,suborno,luvas (gíria),,NEUTRO,true,"Em Portugal, propina é a mensalidade — sentido totalmente diferente.","No Brasil, propina é pagamento ilícito.",O político recebeu luvas.,O político foi acusado de receber propina.,,CURATED
```

## 6. Casos linguísticos de teste

As mesmas regras alimentam a suíte `linguistic-cases.csv` (testes de integração): todo `false_friend=true` gera automaticamente 2 casos de teste (um por direção) verificando tradução + presença do alerta.

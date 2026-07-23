# tugatobrasa-api

API do [TugaToBrasa](https://github.com/carloseorsantos/tugatobrasa) — tradutor open source pt-PT ⇌ pt-BR de palavras, gírias e expressões, com alerta de falsos amigos. Este repo contém a API (Java 21 + Spring Boot), o **glossário** (a fonte da verdade do produto) e as regras de tradução.

**Princípios:** 100% determinístico (sem LLM), custo zero para rodar, dado é código — o glossário é um CSV versionado neste repo e cresce por contribuição da comunidade.

## Arquitetura

```
POST /api/v1/translate
  → Normalizer → GlossaryResolver (exato → lema → fuzzy)
  → RuleResolver (gerúndio ⇌ estar a, pronomes, grafias)
  → NotFoundHandler (sugestões + convite de contribuição)
```

Postgres (pg_trgm para busca fuzzy) + Flyway (migrations e seed do CSV) + cache Caffeine. Contrato publicado em `openapi.yml`.

## Rodando localmente

```bash
docker compose up          # sobe Postgres + API
curl localhost:8080/actuator/health
```

## Usando a API

### `POST /api/v1/translate`

```json
{ "text": "bué fixe", "direction": "PT_TO_BR", "context": null }
```

Resposta: tradução com registro (`NEUTRO|GIRIA|CALAO`), alternativas, exemplo, alerta de falso amigo e origem (`GLOSSARY|RULE`). Termo desconhecido retorna `NOT_FOUND` com sugestões próximas e link de contribuição — nunca erro.

| Método | Rota | Função |
|--------|------|--------|
| `POST` | `/api/v1/translate` | traduz palavra/frase (bidirecional) |
| `GET` | `/api/v1/glossary?q=` | busca no glossário |
| `POST` | `/api/v1/feedback` | feedback de tradução |

Documentação completa: `openapi.yml` (gerado por springdoc).

## O glossário

`glossary/glossario.csv` — cada linha é um par pt-PT ⇌ pt-BR com registro, exemplos e notas de falso amigo **nas duas direções**. Especificação completa e regras de validação: [docs/glossario-spec.md](docs/glossario-spec.md).

**Quer adicionar uma palavra?** Veja o [CONTRIBUTING.md](CONTRIBUTING.md) — dá para contribuir sem saber programar.

## Licenças

- Código: [MIT](LICENSE)
- Glossário (`glossary/`): **CC-BY-SA 4.0** — dado aberto, atribuição obrigatória

---

feito pela comunidade · pt-PT ⇌ pt-BR

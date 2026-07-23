# tugatobrasa-api

API do [TugaToBrasa](https://github.com/carloseorsantos/tugatobrasa) — tradutor open source pt-PT ⇌ pt-BR de palavras, gírias e expressões, com alerta de falsos amigos. Este repo contém a API (Java 21 + Spring Boot), o **glossário** (a fonte da verdade do produto) e as regras de tradução.

**Princípios:** 100% determinístico (sem LLM), custo zero para rodar, dado é código — o glossário é um CSV versionado neste repo e cresce por contribuição da comunidade.

## Arquitetura

```
POST /api/v1/translate
  → Normalizer → GlossaryResolver (exato → lema → fuzzy)
  → NotFoundHandler (sugestões + convite de contribuição)
```

Postgres (pg_trgm para busca fuzzy) + Flyway (migrations e seed do CSV). Contrato publicado em `openapi.yml`.

**Planejado, ainda não implementado** (Fase 4/5): `RuleResolver` (gerúndio ⇌ "estar a", pronomes, grafias), cache Caffeine, `GET /glossary`, `POST /feedback`.

## Rodando localmente

```bash
docker compose up          # sobe Postgres + API
curl localhost:8080/actuator/health
```

## Em produção

API publicada num AWS Lightsail, deploy automático a cada merge em `main` com CI verde. Detalhes: [docs/deploy.md](docs/deploy.md).

## Usando a API

### `POST /api/v1/translate`

```json
{ "text": "autocarro", "direction": "PT_TO_BR" }
```

```json
{
  "input": "autocarro",
  "translations": [{
    "source": "autocarro", "target": "ônibus",
    "alternatives": ["busão (informal)"], "register": "NEUTRO", "falseFriend": false,
    "example": "Vou pegar o ônibus.", "confidence": 1.0, "resolvedBy": "GLOSSARY"
  }],
  "fullTranslation": "ônibus", "warnings": [], "suggestions": [], "contributeUrl": null
}
```

Falso amigo: `falseFriend: true` + `note` preenchida + a mesma nota entra em `warnings`. Termo desconhecido: `translations` vazio, `suggestions` com termos próximos e `contributeUrl` apontando pra abrir issue de novo termo — nunca erro.

Documentação completa: `openapi.yml` (gerado por springdoc, em `/v3/api-docs` com a API rodando).

## O glossário

`glossary/glossario.csv` — cada linha é um par pt-PT ⇌ pt-BR com registro, exemplos e notas de falso amigo **nas duas direções**. Especificação completa e regras de validação: [docs/glossario-spec.md](docs/glossario-spec.md).

**Quer adicionar uma palavra?** Veja o [CONTRIBUTING.md](CONTRIBUTING.md) — dá para contribuir sem saber programar.

## Licenças

- Código: [MIT](LICENSE)
- Glossário (`glossary/`): **CC-BY-SA 4.0** — dado aberto, atribuição obrigatória

---

feito pela comunidade · pt-PT ⇌ pt-BR

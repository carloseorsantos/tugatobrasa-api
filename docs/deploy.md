# Deploy

API roda em produção num AWS Lightsail (Ubuntu 22.04, bundle $7/mês), via
`docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build`.

## O que muda em produção (`docker-compose.prod.yml`)

- Postgres não publica porta no host — só acessível dentro da rede docker
  interna (`api` fala com `db:5432`), nunca da internet.
- `POSTGRES_PASSWORD` vem de um `.env` na instância (nunca commitado),
  gerado com `openssl rand -base64 24`. Ver `.env.prod.example`.
- `restart: unless-stopped` nos dois serviços.

## Firewall Lightsail

Só 22 (SSH) e 8080 (API) abertos. 5432 nunca aberto.

## Deploy automático

PR de `carloseorsantos` com CI verde mergeia sozinho (ver
`.github/workflows/automerge.yml`) e a mesma job SSHa na instância e roda
o comando de build acima — chave SSH dedicada (só deploy, não a chave
admin), guardada em `LIGHTSAIL_SSH_KEY`/`LIGHTSAIL_HOST`/`LIGHTSAIL_USER`
nos secrets do repo.

PR de contribuidor externo, mergeado manualmente por um humano, também
deploya sozinho — `.github/workflows/deploy.yml` dispara em qualquer
merge real (evento de usuário, não do GITHUB_TOKEN) pra `main`.

## Verificação manual

```bash
curl http://<ip>/actuator/health
curl -X POST http://<ip>/api/v1/translate -H 'Content-Type: application/json' \
  -d '{"text":"autocarro","direction":"PT_TO_BR"}'
```

## Fora de escopo (decidido, não fazer sem pedido explícito)

- HTTPS (Caddy + domínio) — sem domínio configurado ainda.
- Backup do Postgres — banco é 100% reconstruível a partir do
  `glossary/glossario.csv` versionado no Git (seeder roda no boot); custo
  do snapshot automático do Lightsail não compensa pro volume de dado.

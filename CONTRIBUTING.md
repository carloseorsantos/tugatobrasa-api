# Contribuindo com o TugaToBrasa

Obrigado pelo interesse! A forma mais valiosa de contribuir é **adicionando palavras ao glossário** — e para isso não precisa saber programar.

## Adicionar uma palavra ou gíria (sem código)

1. [Abra uma issue de novo termo](https://github.com/carloseorsantos/tugatobrasa-api/issues/new/choose) usando o formulário.
2. Preencha: o termo, a tradução, a direção (PT→BR ou BR→PT), o registro (neutro, gíria ou calão) e uma frase de exemplo.
3. **Se a palavra engana** (tipo «propina» ou «rapariga»), marque como falso amigo e explique o risco — esse aviso é a alma do projeto.
4. Um mantenedor valida contra dicionários de referência (Priberam/Infopédia para pt-PT; Dicio/Michaelis para pt-BR) e adiciona ao glossário citando a fonte.

## Adicionar via Pull Request (com código)

1. Edite `glossary/glossario.csv` seguindo a [especificação do glossário](docs/glossario-spec.md) — formato das 12 colunas, regras de validação e exemplos.
2. Regras que o CI vai cobrar (as principais):
   - sem duplicata exata do par (`term_pt`,`term_br`);
   - `register` ∈ `NEUTRO | GIRIA | CALAO`; gíria e calão exigem frase de exemplo dos dois lados;
   - `false_friend=true` exige nota **nas duas direções**, escrita para o leitor de cada lado;
   - entrada nova entra com `status=PENDING`.
3. Cite a fonte (link do dicionário de referência) na descrição do PR.
4. O CI valida o CSV; erro fatal aponta linha e regra violada.
5. Um mantenedor revisa e mescla manualmente — PRs de contribuidores externos nunca mesclam sozinhos, mesmo com o CI verde.

## Contribuir com código

- Java 21 + Spring Boot 3.x (Maven). Estrutura de pacotes por feature (`translation/`, `glossary/`, `feedback/`).
- Commits: [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `test:`…), mensagem em inglês.
- Branches `feat/...` ou `fix/...` a partir de `main`; PRs pequenos, com testes.
- Mudou comportamento da API? Atualize o `openapi.yml` no mesmo PR.
- Testes de integração usam Testcontainers (Postgres real — o fuzzy `pg_trgm` não roda em H2).

## Interface (frontend)

Issues e PRs de UI vivem no repo [`tugatobrasa`](https://github.com/carloseorsantos/tugatobrasa).

## Licenças

Ao contribuir, você concorda que: código entra sob [MIT](LICENSE) e entradas do glossário entram sob **CC-BY-SA 4.0**.

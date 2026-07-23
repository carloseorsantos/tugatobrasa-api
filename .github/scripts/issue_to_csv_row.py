#!/usr/bin/env python3
"""Transcreve os campos do form de issue "Novo termo" pra uma linha do
glossario.csv. Não valida contra dicionário nem decide nada linguístico —
só copia o que o contribuidor já escreveu, no formato certo. Quem confirma
o sentido contra Priberam/Infopédia/Dicio/Michaelis é o mantenedor, no
review do PR gerado (ver docs/glossario-spec.md)."""
import csv
import os
import re
import unicodedata

GLOSSARY_PATH = "glossary/glossario.csv"

REGISTER_MAP = {
    "Neutro (uso comum)": "NEUTRO",
    "Gíria (informal)": "GIRIA",
    "Calão (pesado/vulgar)": "CALAO",
}


def parse_fields(body: str) -> dict[str, str]:
    fields = {}
    for label, value in re.findall(r"### (.+?)\n+(.+?)(?=\n### |\Z)", body, re.S):
        value = value.strip()
        fields[label.strip()] = "" if value == "_No response_" else value
    return fields


def norm(term: str) -> str:
    return unicodedata.normalize("NFC", term.strip().lower())


def split_translation(raw: str) -> tuple[str, str]:
    parts = [p.strip() for p in raw.split(",") if p.strip()]
    primary = norm(parts[0]) if parts else ""
    alternatives = "|".join(parts[1:])
    return primary, alternatives


def main() -> None:
    body = os.environ["ISSUE_BODY"]
    fields = parse_fields(body)

    termo = norm(fields.get("Termo original", ""))
    direcao = fields.get("Direção", "")
    traducao_primary, traducao_alts = split_translation(fields.get("Tradução", ""))
    registro = REGISTER_MAP.get(fields.get("Registro", ""), "")
    exemplo = fields.get("Frase de exemplo", "")
    is_false_friend = fields.get("É falso amigo?", "").startswith("Sim")
    nota = fields.get("Se for falso amigo: qual é o risco?", "")
    regiao = fields.get("Região (opcional)", "")
    fonte = fields.get("Fonte (opcional)", "")

    pt_to_br = direcao.startswith("pt-PT")

    row = {
        "term_pt": termo if pt_to_br else traducao_primary,
        "term_br": traducao_primary if pt_to_br else termo,
        "alternatives_pt": "" if pt_to_br else traducao_alts,
        "alternatives_br": traducao_alts if pt_to_br else "",
        "register": registro,
        "false_friend": "true" if is_false_friend else "false",
        # rascunho: nota do contribuidor entra dos dois lados, mantenedor
        # ajusta cada uma pro leitor daquele lado (spec exige as duas)
        "note_pt": nota,
        "note_br": nota,
        "example_pt": exemplo if pt_to_br else "",
        "example_br": "" if pt_to_br else exemplo,
        "region": regiao,
        "status": "PENDING",
    }

    columns = [
        "term_pt", "term_br", "alternatives_pt", "alternatives_br",
        "register", "false_friend", "note_pt", "note_br",
        "example_pt", "example_br", "region", "status",
    ]

    with open(GLOSSARY_PATH, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([row[c] for c in columns])

    missing_example = "example_br" if pt_to_br else "example_pt"
    checklist = [
        "- [ ] Confirmar termo e tradução contra dicionário de referência"
        f" (fonte do contribuidor: {fonte or 'não informada'})",
        f"- [ ] Preencher `{missing_example}` (obrigatório, o form só coleta exemplo de um lado)",
    ]
    if is_false_friend:
        checklist.append(
            "- [ ] `note_pt`/`note_br` vieram com o mesmo texto do contribuidor —"
            " reescrever cada uma pro leitor daquele lado (spec exige nota nas duas direções)"
        )
    checklist.append("- [ ] Trocar `status` para `CURATED` quando validado")

    issue_number = os.environ["ISSUE_NUMBER"]
    pr_body = (
        f"Rascunho gerado automaticamente a partir da issue #{issue_number}. "
        "Nenhum campo foi validado — só transcrito do form.\n\n"
        + "\n".join(checklist)
        + f"\n\nCloses #{issue_number}"
    )
    with open("pr_body.txt", "w", encoding="utf-8") as f:
        f.write(pr_body)

    ascii_term = unicodedata.normalize("NFKD", termo).encode("ascii", "ignore").decode()
    slug = re.sub(r"[^a-z0-9]+", "-", ascii_term.lower()).strip("-") or "termo"
    branch = f"glossary/issue-{issue_number}-{slug}"
    with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as f:
        f.write(f"branch={branch}\n")
        f.write(f"pr_title=glossary: add \"{termo}\" (from #{issue_number})\n")


if __name__ == "__main__":
    main()

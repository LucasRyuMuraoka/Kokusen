# Uzumaki
Projeto front-end que consome a Dattebayo API e apresenta, de forma interativa, dados sobre o universo Naruto.

# Dattebayo API 🦊🍥

<p align="center">
  <img src="https://upload.wikimedia.org/wikipedia/en/9/94/NarutoCoverTankobon1.jpg" alt="Naruto Banner" width="300"/>
</p>

## 📖 Sobre

**Dattebayo** é sua plataforma definitiva para tudo relacionado ao anime **Naruto**!  
Nossa API oferece uma forma prática e poderosa de acessar informações detalhadas sobre personagens, clãs, kekkei-genkai, bijū, equipes, vilarejos e muito mais.

---

## 📦 O que oferecemos

- **🔍 Character Insights** — Explore um vasto acervo de personagens, desde ícones como **Naruto**, **Sasuke** e **Sakura**, até ninjas menos conhecidos que tiveram papéis cruciais.
- **🏯 Clãs e Kekkei-Genkai** — Descubra os segredos das linhagens sanguíneas e seus poderes únicos.
- **🦊 Tailed Beasts (Bijū)** — Conheça a história, o poder e as lendas sobre as bestas com cauda.
- **👥 Times e Vilarejos** — Explore a dinâmica das equipes e as culturas únicas de cada vila oculta.
- **☁️ Organizações** — Mergulhe nos mistérios da **Akatsuki** e da **Kara**.

---

## 💡 Por que escolher o Dattebayo?

- **📚 Dados abrangentes** — Conteúdo cuidadosamente organizado e fiel ao anime.
- **⚡ Integração fácil** — API simples de usar, mesmo para iniciantes.
- **♻️ Atualizações constantes** — Nosso conteúdo acompanha as novidades do universo Naruto.

---

## 🌍 Base URL

https://dattebayo-api.onrender.com

---

## 🔗 Endpoints Principais

| Método | Endpoint                | Descrição                          |
|--------|-------------------------|-------------------------------------|
| GET    | `/characters`           | Lista todos os personagens         |
| GET    | `/characters/:id`       | Detalhes de um personagem          |
| GET    | `/clans`                | Lista todos os clãs                 |
| GET    | `/clans/:id`            | Detalhes de um clã                  |
| GET    | `/kekkei-genkai`        | Lista kekkei-genkai                 |
| GET    | `/tailed-beasts`        | Lista bijū                          |
| GET    | `/villages`             | Lista vilarejos                     |
| GET    | `/teams`                | Lista equipes ninja                 |

---

## 📌 Exemplo de uso

```bash
# Obter todos os personagens
https://dattebayo-api.onrender.com/characters

# Obter detalhes do Naruto
https://dattebayo-api.onrender.com/characters/1

{
  "id": 1,
  "name": "Naruto Uzumaki",
  "village": "Konohagakure",
  "rank": "Hokage",
  "affiliations": ["Team 7", "Konoha 11"],
  "abilities": ["Rasengan", "Shadow Clone Jutsu", "Sage Mode", "Kurama Chakra Mode"]
}

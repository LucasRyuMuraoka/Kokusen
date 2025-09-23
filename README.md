# Kokusen

API REST desenvolvida em **Java + Quarkus**, simulando o universo de *Jujutsu Kaisen*.  
O projeto implementa entidades principais como **Clans, Characters, Techniques e Domain Expansions**, com suporte a paginação, busca, validações e HATEOAS.

---

## 🌐 Deploy
> 🔗 Link do Render: **https://kokusen-lucasryu.onrender.com/q/swagger-ui/**

---

## 📌 Tecnologias
- Java 17+
- Quarkus
- Hibernate ORM (Panache)
- RESTEasy Reactive
- H2 Database (memória)
- OpenAPI (Swagger)

---

## 🚀 Como rodar

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/jujutsu-kaisen-api.git
cd jujutsu-kaisen-api

# Rodar local
./mvnw quarkus:dev
```

API disponível em:  
```
http://localhost:8080
```

Swagger/OpenAPI disponível em:  
```
http://localhost:8080/q/swagger-ui
```

---

## 📂 Estrutura do Projeto

- **entity/** → Entidades JPA (Clan, Character, Technique, DomainExpansion)  
- **dto/** → Objetos de requisição (Request DTOs)  
- **representation/** → Objetos de resposta (HATEOAS)  
- **resource/** → Endpoints REST (Resources)  
- **dto/SearchResponses/** → Respostas paginadas de list/search  

---

## 📖 Documentação da API

### 🏯 Clans

| Método | Endpoint         | Descrição |
|--------|------------------|-----------|
| GET    | `/clans`         | Lista todos os clãs (com paginação e ordenação) |
| GET    | `/clans/{id}`    | Retorna um clã específico pelo ID |
| POST   | `/clans`         | Cria um novo clã |
| PUT    | `/clans/{id}`    | Atualiza um clã existente |
| DELETE | `/clans/{id}`    | Remove um clã (só se não tiver membros) |
| GET    | `/clans/search`  | Busca clãs por nome (com paginação e ordenação) |
| GET    | `/clans/{id}/members` | Lista os membros de um clã (personagens) |

#### JSON Exemplo - POST Clan
```json
{
  "name": "Zenin",
  "description": "Clã tradicional e conservador."
}
```

#### JSON Exemplo - GET Clan
```json
{
  "id": 1,
  "name": "Zenin",
  "description": "Clã tradicional e conservador.",
  "memberIds": [2, 3, 5]
}
```

---

### 👤 Characters

| Método | Endpoint                   | Descrição |
|--------|----------------------------|-----------|
| GET    | `/characters`              | Lista todos os personagens (com paginação e ordenação) |
| GET    | `/characters/{id}`         | Retorna um personagem específico |
| POST   | `/characters`              | Cria um novo personagem |
| PUT    | `/characters/{id}`         | Atualiza um personagem existente |
| DELETE | `/characters/{id}`         | Remove um personagem (limpa associações antes) |
| GET    | `/characters/search`       | Busca personagens por nome |
| GET    | `/characters/{id}/techniques` | Lista técnicas do personagem |
| GET    | `/characters/{id}/domain-expansion` | Retorna a expansão de domínio do personagem (se tiver) |

#### JSON Exemplo - POST Character
```json
{
  "name": "Gojo Satoru",
  "grade": "Special",
  "clanId": 1
}
```

---

### 🌀 Techniques

| Método | Endpoint              | Descrição |
|--------|-----------------------|-----------|
| GET    | `/techniques`         | Lista todas as técnicas (com paginação e ordenação) |
| GET    | `/techniques/{id}`    | Retorna uma técnica pelo ID |
| POST   | `/techniques`         | Cria uma nova técnica |
| PUT    | `/techniques/{id}`    | Atualiza uma técnica |
| DELETE | `/techniques/{id}`    | Remove uma técnica (limpando associações com personagens) |
| GET    | `/techniques/search`  | Busca técnicas por nome |
| GET    | `/techniques/{id}/users` | Lista personagens que usam a técnica |

#### JSON Exemplo - POST Technique
```json
{
  "name": "Cursed Technique Lapse: Blue",
  "description": "Manipulação do infinito para atrair objetos."
}
```

#### JSON Exemplo - GET Technique
```json
{
  "id": 1,
  "name": "Cursed Technique Lapse: Blue",
  "description": "Manipulação do infinito para atrair objetos.",
  "userIds": [1]
}
```

---

### 🏯 Domain Expansions

| Método | Endpoint                      | Descrição |
|--------|-------------------------------|-----------|
| GET    | `/domain-expansions`          | Lista todas as expansões de domínio (com paginação e ordenação) |
| GET    | `/domain-expansions/{id}`     | Retorna uma expansão específica |
| POST   | `/domain-expansions`          | Cria uma nova expansão |
| PUT    | `/domain-expansions/{id}`     | Atualiza uma expansão existente |
| DELETE | `/domain-expansions/{id}`     | Remove uma expansão (limpa associação com o dono antes) |
| GET    | `/domain-expansions/search`   | Busca expansões por nome |

#### JSON Exemplo - POST DomainExpansion
```json
{
  "name": "Unlimited Void",
  "effect": "Coloca o alvo dentro de um espaço infinito."
}
```

#### JSON Exemplo - GET DomainExpansion
```json
{
  "id": 1,
  "name": "Unlimited Void",
  "effect": "Coloca o alvo dentro de um espaço infinito.",
  "ownerId": 1
}
```

---

## 📝 Observações

- Todos os **endpoints de listagem e busca** suportam paginação e ordenação via query params:
  ```
  ?page=1&size=10&sort=name&direction=asc
  ```
- **Validações**:
  - `name` é obrigatório em todas as entidades.
  - `description/effect` também são obrigatórios (não podem ser `null`).
  - Nomes duplicados são bloqueados (409 Conflict).
- **DELETE**:
  - Clans só podem ser deletados se não tiverem membros.
  - Techniques e Domain Expansions limpam suas associações antes de deletar.
- **HATEOAS**:
  Todas as `Representations` incluem links de navegação.  
  Exemplo:
  ```json
  {
    "id": 1,
    "name": "Zenin",
    "links": [
      { "rel": "self", "href": "/clans/1" },
      { "rel": "update", "href": "/clans/1", "method": "PUT" },
      { "rel": "delete", "href": "/clans/1", "method": "DELETE" },
      { "rel": "members", "href": "/clans/1/members", "method": "GET" }
    ]
  }
  ```

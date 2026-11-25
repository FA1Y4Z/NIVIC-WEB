# NIVIC

> Controle financeiro pessoal, simples e centralizado.

O **NIVIC** é uma aplicação web para organizar receitas, despesas e metas financeiras. Cada usuário tem sua própria conta e enxerga apenas os seus dados.

Projeto escolar desenvolvido por **Nicolas** e **Victor**.

## Funcionalidades

- Cadastro e login com senha protegida (BCrypt) e autenticação via JWT
- Receitas e despesas: criar, listar, editar e excluir
- Metas financeiras com barra de progresso e prazo
- Dashboard com saldo, totais e movimentações recentes
- Busca rápida nos lançamentos
- Páginas de Termos de Uso e Política de Privacidade

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security, JdbcTemplate, Bean Validation, JJWT, Maven |
| Frontend | HTML5, CSS3 e JavaScript (sem frameworks) |
| Banco de dados | MariaDB |

## Estrutura do projeto

```text
NIVIC-WEB/
├── backend/
│   ├── banco.sql                  # Criação do banco e das tabelas
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/nivic/
│       │   ├── NivicApplication.java
│       │   ├── controller/        # Rotas da API
│       │   ├── dto/               # Objetos de entrada e validação
│       │   ├── security/          # JWT, filtro de autenticação e CORS
│       │   └── service/           # Regras de negócio (senha)
│       └── resources/
│           └── application.properties.example
└── frontend/
    ├── index.html
    ├── script.js
    ├── styles.css
    ├── termos.html
    └── privacidade.html
```

## Como executar

### Pré-requisitos

- Java JDK 17 ou superior
- Apache Maven 3.9 ou superior
- MariaDB
- Git
- Uma forma de servir o frontend (ex.: extensão **Live Server** do VS Code)

### 1. Clonar o repositório

```bash
git clone https://github.com/FA1Y4Z/NIVIC-WEB.git
cd NIVIC-WEB
```

### 2. Criar o banco de dados

Crie o banco com suporte a acentos e rode o script de tabelas (ele pode ser executado mais de uma vez sem perder dados):

```bash
mariadb -u root -p -e "CREATE DATABASE IF NOT EXISTS nivic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mariadb -u root -p < backend/banco.sql
```

### 3. Configurar a aplicação

```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
```

O arquivo `application.properties` está no `.gitignore` e **não deve ser enviado ao GitHub**.

Defina as variáveis de ambiente no mesmo terminal em que vai iniciar o backend:

| Variável | Descrição |
|---|---|
| `DB_USERNAME` | Usuário do MariaDB |
| `DB_PASSWORD` | Senha do MariaDB |
| `JWT_SECRET` | Chave de assinatura dos tokens. **Mínimo de 32 caracteres.** Sem ela, a aplicação não inicia. |

Linux / macOS:

```bash
export DB_USERNAME=root
export DB_PASSWORD=sua_senha
export JWT_SECRET=$(openssl rand -base64 48)
```

Windows (PowerShell):

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="troque-por-uma-chave-longa-com-mais-de-32-caracteres"
```

> Com `openssl rand`, a chave muda a cada terminal e os tokens antigos deixam de valer. Para desenvolvimento, isso não é problema.

### 4. Iniciar o backend

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Para conferir, acesse `http://localhost:8080/api`: deve aparecer **NIVIC API está funcionando!**

### 5. Abrir o frontend

Abra a pasta `frontend/` no VS Code, clique com o botão direito em `index.html` e escolha **Open with Live Server**. Ele abre em `http://127.0.0.1:5500`.

> O backend só aceita requisições (CORS) de origens listadas em `backend/src/main/java/com/nivic/security/SecurityConfig.java`. Se você usar outro endereço ou porta, adicione-o à lista.

## API

Todas as rotas `/api/**` exigem o cabeçalho `Authorization: Bearer <token>`, exceto as marcadas como públicas.

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| GET | `/api` | Verifica se a API está no ar | Público |
| POST | `/api/usuarios` | Cadastra um usuário | Público |
| POST | `/api/auth/login` | Faz login e retorna o token | Público |
| GET | `/api/usuarios/me` | Dados do usuário logado | Autenticado |
| GET / POST | `/api/receitas` | Lista / cria receitas | Autenticado |
| GET / PUT / DELETE | `/api/receitas/{id}` | Busca / edita / exclui uma receita | Autenticado |
| GET / POST | `/api/despesas` | Lista / cria despesas | Autenticado |
| GET / PUT / DELETE | `/api/despesas/{id}` | Busca / edita / exclui uma despesa | Autenticado |
| GET / POST | `/api/metas` | Lista / cria metas | Autenticado |
| GET / PUT / DELETE | `/api/metas/{id}` | Busca / edita / exclui uma meta | Autenticado |

### Exemplo de uso

Cadastro:

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","email":"maria@email.com","senha":"minhasenha123"}'
```

Login (retorna o `token`):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@email.com","senha":"minhasenha123"}'
```

Criar uma despesa:

```bash
curl -X POST http://localhost:8080/api/despesas \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Mercado","valor":89.90,"categoria":"Alimentação","data":"2026-11-24"}'
```

### Regras de validação

- **Senha:** de 8 a 72 caracteres
- **Valores:** maiores que zero, com até 2 casas decimais
- **Metas:** `valorObjetivo` maior que zero, `valorAtual` maior ou igual a zero e `dataLimite` obrigatória
- **Token:** expira em 1 hora

## Segurança

- Senhas armazenadas com hash BCrypt, nunca em texto puro
- Consultas SQL parametrizadas (proteção contra SQL Injection)
- Cada registro é filtrado pelo usuário do token: ninguém acessa dados de outra conta
- Conteúdo dinâmico escapado no frontend (proteção contra XSS)
- Credenciais e chaves lidas de variáveis de ambiente

## Problemas comuns

| Problema | O que verificar |
|---|---|
| A aplicação não inicia e reclama de `JWT_SECRET` | A variável precisa existir no mesmo terminal e ter 32 caracteres ou mais |
| Erro de conexão com o MariaDB | Serviço ativo, banco `nivic` criado, usuário/senha corretos e porta `3306` |
| Porta `8080` em uso | Encerre o outro processo ou altere `server.port` no `application.properties` |
| O frontend não conecta à API (erro de CORS ou "não foi possível conectar") | Backend rodando e origem do frontend listada no `SecurityConfig` |
| `mvn` não encontra o `pom.xml` | Execute os comandos dentro da pasta `backend/` |
| Erro 401 após algum tempo | O token expirou; faça login novamente |

## Autores

- **Nicolas**
- **Victor**

## Licença

Projeto desenvolvido para fins educacionais.
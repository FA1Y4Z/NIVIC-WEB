# NIVIC-WEB

Aplicação web de organização e gerenciamento financeiro pessoal, desenvolvida para facilitar o controle de receitas, despesas e metas financeiras.

O NIVIC é um projeto desenvolvido por Nicolas e Victor, com uma arquitetura que separa o frontend, o backend e a camada de persistência de dados.

O projeto utiliza Java e Spring Boot no backend, JavaScript no frontend e MariaDB como sistema de gerenciamento de banco de dados.

## Sobre o projeto

O NIVIC-WEB é a versão web do projeto NIVIC. Seu objetivo é oferecer uma aplicação para organizar informações financeiras e acompanhar a movimentação de dinheiro de maneira simples e centralizada.

A aplicação possui uma estrutura dividida em três partes principais:

- **Frontend:** responsável pela interface e pela interação com o usuário.
- **Backend:** responsável pelas regras de negócio, processamento das requisições e comunicação com o banco de dados.
- **Banco de dados:** responsável pelo armazenamento das informações.

## Tecnologias utilizadas

### Backend
- Java 17
- Spring Boot 3.5.16
- Spring Web
- Maven

### Frontend
- HTML5
- CSS3
- JavaScript

### Banco de dados
- MariaDB
- JDBC

## Funcionalidades

- Gerenciamento de informações financeiras.
- Cadastro e organização de receitas.
- Cadastro e organização de despesas.
- Acompanhamento de movimentações financeiras.
- Organização de metas financeiras.
- Comunicação entre frontend e backend por meio de uma API.

## Requisitos para executar o projeto

Antes de iniciar, certifique-se de que as seguintes ferramentas estão instaladas:

| Ferramenta | Versão |
|---|---|
| Java JDK | 17 ou superior |
| Apache Maven | 3.9 ou superior |
| MariaDB | Versão compatível |
| Git | Versão recente |

Para verificar se o Java está instalado:

```bash
java -version
```

Para verificar o Maven:

```bash
mvn -version
```

Para verificar o Git:

```bash
git --version
```

Caso alguma dessas ferramentas não esteja instalada, será necessário instalá-la antes de continuar.

## Instalação e configuração

### 1. Clonando o repositório

Primeiro, abra um terminal e escolha o diretório em que deseja armazenar o projeto.

Execute:

```bash
git clone https://github.com/FA1Y4Z/NIVIC-WEB.git
```

Entre na pasta do projeto:

```bash
cd NIVIC-WEB
```

O comando `git clone` baixa os arquivos do repositório para o computador, permitindo executar e modificar o projeto localmente.

### 2. Configurando o banco de dados

O NIVIC utiliza o MariaDB para armazenar suas informações.

Inicie o serviço do MariaDB e acesse o terminal do banco:

```bash
mariadb -u root -p
```

Depois de informar a senha, crie o banco de dados:

```sql
CREATE DATABASE nivic
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Selecione o banco:

```sql
USE nivic;
```

**Importante:** a criação do banco não cria automaticamente todas as tabelas necessárias. Caso o projeto disponibilize um script SQL de inicialização, execute esse script para criar a estrutura das tabelas antes de iniciar a aplicação.

Não utilize dados de acesso de produção em um ambiente de desenvolvimento.

### 3. Configurando a conexão com o banco

O backend precisa das informações de acesso ao MariaDB para conseguir estabelecer uma conexão.

Localize o arquivo de configuração de exemplo:

```text
src/main/resources/application.properties.example
```

Copie-o para:

```text
src/main/resources/application.properties
```

No Linux, por exemplo:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Abra o arquivo `application.properties` e configure os dados de conexão.

Exemplo:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nivic
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=none
```

As variáveis `DB_USERNAME` e `DB_PASSWORD` representam o usuário e a senha do MariaDB.

No Linux, elas podem ser configuradas no terminal:

```bash
export DB_USERNAME=root
export DB_PASSWORD=sua_senha
```

No Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="sua_senha"
```

Substitua os valores pelos dados utilizados na instalação local do MariaDB.

**Atenção:** não compartilhe senhas, não envie credenciais para o GitHub e não publique seu arquivo `application.properties` com informações privadas.

### 4. Instalando as dependências

Com o banco configurado, volte ao diretório principal do projeto, onde está localizado o arquivo `pom.xml`.

Execute:

```bash
mvn clean install
```

Esse comando realiza duas operações principais:

- `clean`: remove arquivos gerados em compilações anteriores.
- `install`: compila o projeto, executa as etapas de construção e disponibiliza o pacote no repositório local do Maven.

O Maven também baixa automaticamente as dependências declaradas no `pom.xml`.

É necessário ter conexão com a internet na primeira execução para baixar as dependências que ainda não estão disponíveis localmente.

### 5. Executando o backend

Depois da instalação das dependências, inicie a aplicação com:

```bash
mvn spring-boot:run
```

O Spring Boot iniciará o servidor da aplicação.

Por padrão, o backend utiliza a porta `8080`.

Quando a inicialização for concluída, o terminal deverá indicar que a aplicação está em execução.

Para interromper o servidor, utilize:

```text
CTRL + C
```

### 6. Verificando o funcionamento da API

Com o backend iniciado, abra o navegador e acesse:

```text
http://localhost:8080/api
```

A API deverá retornar:

```text
NIVIC API está funcionando!
```

Essa resposta indica que o servidor está ativo e que a rota de verificação está respondendo.

### 7. Executando o frontend

O frontend é composto por HTML, CSS e JavaScript.

Caso os arquivos estejam configurados dentro do diretório de recursos estáticos do Spring Boot, normalmente em:

```text
src/main/resources/static/
```

eles poderão ser servidos pelo próprio backend.

Nesse caso, acesse:

```text
http://localhost:8080
```

Se o frontend estiver separado do backend, será necessário abrir o arquivo HTML por um servidor local, como a extensão Live Server do Visual Studio Code, e garantir que as requisições JavaScript apontem para o endereço correto da API.

Por exemplo:

```javascript
const API_URL = "http://localhost:8080";
```

A URL utilizada deve corresponder à configuração real do backend.

## Estrutura geral da aplicação

O projeto segue uma organização baseada na separação de responsabilidades.

```text
NIVIC-WEB/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── ...
│       │
│       └── resources/
│           ├── application.properties
│           └── static/
│               └── ...
│
├── pom.xml
├── README.md
└── ...
```

A estrutura acima representa uma organização geral de referência. Os diretórios e arquivos podem variar conforme a implementação atual do projeto.

## Problemas comuns

### Erro de conexão com o MariaDB

Verifique:

- Se o serviço do MariaDB está em execução.
- Se o banco `nivic` foi criado.
- Se o usuário e a senha estão corretos.
- Se a porta utilizada é a `3306`.
- Se as variáveis `DB_USERNAME` e `DB_PASSWORD` foram configuradas no mesmo terminal em que o backend será iniciado.

### Erro de porta em uso

Se a porta `8080` já estiver sendo utilizada por outro processo, o Spring Boot não conseguirá iniciar normalmente.

Verifique qual processo está utilizando a porta e encerre-o ou configure outra porta para a aplicação.

### Erro ao executar o Maven

Verifique se o Java e o Maven estão instalados corretamente:

```bash
java -version
mvn -version
```

Também confira se o terminal está aberto no diretório que contém o arquivo `pom.xml`.

### Erro 404 ao acessar uma rota

Um erro 404 significa que o servidor recebeu a requisição, mas não encontrou uma rota correspondente ao endereço solicitado.

Verifique se a URL está correta e se o endpoint está implementado no backend.

## Desenvolvimento e colaboração

O projeto é desenvolvido em colaboração entre Nicolas e Victor.

As alterações são versionadas utilizando Git e armazenadas no GitHub.

Para atualizar sua cópia local:

```bash
git pull origin main
```

Para enviar alterações:

```bash
git add .
git commit -m "Descrição das alterações"
git push origin main
```

Antes de enviar alterações, confira os arquivos modificados e evite adicionar arquivos de configuração que contenham senhas ou outras informações privadas.

## Desenvolvedores

- **Nicolas** — Desenvolvimento.
- **Victor** — Desenvolvimento.

## Repositório

[FA1Y4Z/NIVIC-WEB](https://github.com/FA1Y4Z/NIVIC-WEB)

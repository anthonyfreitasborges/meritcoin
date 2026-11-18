# MeritCoin

Aplicação React + Spring Boot para reconhecimento acadêmico. A imagem Docker serve a interface e a API na mesma origem. O banco de produção é PostgreSQL e os emails reais são enviados pela API HTTP da Brevo para o endereço cadastrado do aluno e da empresa.

## Desenvolvimento local

Requisitos: Java 17+, Node 20+, Docker. Execute `docker compose up -d` na raiz. Em `backend`, execute `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`; defina `JAVA_HOME`). Em `frontend`, execute `npm ci` e `npm run dev`. Abra `http://localhost:5173`. O Vite encaminha `/api` para a API local. Por padrão, o MailHog recebe as mensagens locais em `http://localhost:8025`.

Antes de cadastrar pessoas, entre como administrador em `admin@meritcoin.local` com `admin123` **somente no ambiente local** e crie instituição e curso. Em produção, defina `ADMIN_EMAIL` e uma senha forte em `ADMIN_PASSWORD`.

Cada conta nova exige senha de pelo menos 8 caracteres. O login usa email e senha. Contas antigas criadas pela versão anterior não possuem senha e exigem migração de senhas; em uma instalação nova, comece com banco vazio. Recadastro com o mesmo email em um banco antigo é bloqueado pela unicidade do cadastro.

## Publicação gratuita: Render + Neon + Brevo

1. Envie esta pasta para um repositório GitHub. Não envie arquivos `.env` nem credenciais.
2. Crie um projeto no [Neon](https://neon.com/) no plano Free e anote host, nome do banco, usuário e senha na tela **Connect**. Use conexão direta. O banco do Render Free expira em 30 dias; por isso esta aplicação usa Neon.
3. Crie conta na [Brevo](https://www.brevo.com/), cadastre e verifique um remetente em **Senders**, ative emails transacionais e crie uma chave API (não a chave SMTP). O remetente verificado será `MAIL_FROM`. Os destinatários serão sempre os emails que os usuários cadastrarem. O plano gratuito oferece 300 envios por dia.
4. No [Render](https://render.com/), crie **New > Web Service**, conecte o repositório, escolha **Docker** e o plano **Free**. Use `Dockerfile` na raiz. Não crie um banco no Render.
5. Em **Environment**, configure as variáveis abaixo. A URL JDBC separa host/banco de usuário/senha; não cole a URL `postgresql://usuario:senha@...` diretamente. Mantenha `?sslmode=require`.

   ```text
   DATABASE_URL=jdbc:postgresql://HOST_DO_NEON:5432/NOME_DO_BANCO?sslmode=require
   DATABASE_USER=USUARIO_DO_NEON
   DATABASE_PASSWORD=SENHA_DO_NEON
   ADMIN_EMAIL=seu-email@example.com
   ADMIN_PASSWORD=uma-senha-forte-e-unica
   BREVO_API_KEY=sua-chave-api
   MAIL_FROM=remetente-verificado@example.com
   ```

6. Publique o serviço. Abra a URL `https://...onrender.com`. O Dockerfile compila o frontend e o backend e o Spring serve ambos. A URL da API será a mesma origem com prefixo `/api`; não é preciso configurar CORS ou endereço de API no navegador.
7. Entre como administrador com `ADMIN_EMAIL`/`ADMIN_PASSWORD`, crie instituição e curso. Cadastre aluno, professor e empresa com emails reais. Inicie um semestre, transfira moedas, resgate uma vantagem e confira as caixas de entrada do aluno e da empresa. Se a Brevo rejeitar o remetente, confira a verificação do sender e a chave API.

O serviço gratuito do Render hiberna após 15 minutos sem tráfego; a primeira visita pode levar cerca de um minuto. O plano Free tem limites mensais. O banco Neon Free também tem cotas. O envio real não pode ser testado sem a sua chave Brevo e um remetente verificado.

## Funcionalidades e API

- Instituições e cursos: criar, listar, editar, excluir. A exclusão é impedida se houver vínculos.
- Matrículas: criar, listar, remover.
- Vantagens: criar, listar, editar, desativar (remoção lógica para preservar histórico de resgates).
- Semestres: iniciar, finalizar e listar.
- Movimentações e resgates são registros financeiros imutáveis; não há edição ou exclusão direta para preservar o extrato.
- Conta: cadastro, consulta, edição de nome/email/endereço/senha e desativação lógica. Professor só transfere de sua própria carteira para aluno de seu curso; aluno só resgata para si; empresa só altera suas vantagens; administrador gerencia catálogo e semestres.

## Limitações atuais

Esta é uma aplicação de demonstração. O esquema usa `ddl-auto=update`; antes de operar com dados importantes, introduza migrations e backups. A entrega por email depende da aceitação da Brevo; uma resposta de sucesso da API de email significa aceite do provedor, não prova de entrega na caixa de entrada. O login guarda a sessão no navegador por até sete dias. Não armazene informações pessoais reais sem revisar privacidade e proteção de dados.

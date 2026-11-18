# MeritCoin — guia completo de uso

O MeritCoin é um sistema de reconhecimento acadêmico. Professores recebem moedas virtuais e podem enviá-las a alunos de seus cursos como reconhecimento. Alunos acompanham o saldo e trocam moedas por vantagens publicadas por empresas parceiras. O administrador organiza instituições, cursos e semestres.

> **Comece aqui:** se esta é a primeira utilização do site, siga a seção [Primeiro uso: ordem correta](#primeiro-uso-ordem-correta). Cada pessoa deve criar sua própria conta no perfil apropriado. As moedas **MC** são pontos virtuais do sistema, não dinheiro.

## Acessar o site

1. Abra o endereço [público do MeritCoin](https://meritcoins.onrender.com/) fornecido pelo responsável pelo projeto 
2. Aguarde a tela com as opções **Entrar** e **Criar conta**. 
3. Se você já tem conta, use **Entrar**, informe o **email e a senha cadastrados** e clique em **Entrar na plataforma**.
4. Se ainda não tem conta, use **Criar conta** e siga a seção do seu perfil abaixo.

O endereço de email precisa ser verdadeiro e acessível: ele recebe avisos de moedas e cupons. O sistema não pede CPF ou CNPJ na tela de login; esses documentos são informados no cadastro. Se aparecer **Credenciais inválidas**, confira email e senha. Atualmente não há botão de recuperação de senha na tela de login.

## Primeiro uso: ordem correta

Para que todas as opções de cadastro e reconhecimento funcionem, siga esta sequência:

1. **Administrador:** entra com as credenciais configuradas pelo responsável técnico e cadastra pelo menos uma instituição e um curso.
2. **Professor:** cria sua conta, escolhe a instituição e o curso que leciona.
3. **Aluno:** cria sua conta, escolhe a mesma instituição e se matricula no curso do professor. Pode selecionar vários cursos dessa instituição.
4. **Empresa parceira:** cria sua conta e publica pelo menos uma vantagem.
5. **Administrador:** inicia um semestre. Nesse momento, cada professor **já cadastrado** recebe 1.000 MC.
6. **Professor:** envia moedas e uma mensagem ao aluno matriculado em seu curso.
7. **Aluno:** consulta saldo e extrato, resgata uma vantagem se tiver moedas suficientes e recebe o cupom por email.
8. **Empresa:** recebe por email o código do mesmo resgate para conferência presencial.

Se o professor for cadastrado **depois** do início do semestre, ele não recebe o crédito daquele início automaticamente. O próximo início de semestre creditará os professores existentes naquele momento.

## Perfis e o que cada um faz

- **Administrador:** cadastra, edita e remove instituições e cursos; inicia e finaliza semestres; consulta o histórico de semestres.
- **Professor:** consulta saldo e extrato, vê alunos do curso que leciona e envia moedas com uma mensagem.
- **Aluno:** consulta saldo, extrato e cursos; adiciona ou remove matrículas; vê vantagens disponíveis e resgata cupons.
- **Empresa parceira:** cadastra, edita e remove suas vantagens; recebe por email os códigos dos resgates.

O perfil **Administrador** não é criado pela opção **Criar conta**. As credenciais administrativas são configuradas pelo responsável técnico na hospedagem.

## Administrador: preparar instituições e cursos

### Entrar como administrador

gmail:admin@gmail.com   
senha:teste123

### Cadastrar uma instituição

1. No menu à esquerda, clique em **Instituições e cursos**.
2. No primeiro formulário, preencha **Nome da instituição** (por exemplo, `Faculdade Exemplo`) e **Código** (por exemplo, `FEX`).
3. Clique em **Cadastrar instituição**.
4. Confirme que ela aparece na seção **Instituições cadastradas**.

O código deve ser único. Se o sistema indicar que uma instituição já existe com o mesmo código, escolha outro.

### Cadastrar um curso

1. Ainda em **Instituições e cursos**, vá ao formulário de curso.
2. Preencha **Nome do curso** (por exemplo, `Engenharia de Software`).
3. Selecione a **Instituição** à qual o curso pertence.
4. Clique em **Cadastrar curso** e confirme a inclusão em **Cursos cadastrados**.

Crie os cursos **antes** de orientar alunos e professores a se cadastrarem. O aluno só pode selecionar cursos da instituição escolhida; o professor escolhe um curso de atuação.

### Editar ou remover cadastros acadêmicos

- Na lista **Instituições cadastradas**, use **Editar** para trocar nome e código, ou **Remover** para excluir a instituição.
- Na lista **Cursos cadastrados**, use **Editar** para trocar o nome do curso, ou **Remover** para excluí-lo.
- A edição abre caixas de diálogo do navegador. Confirme cada campo; **Cancelar** interrompe a alteração.
- Não é possível remover uma instituição que possua cursos ou usuários vinculados. Não é possível remover um curso que possua professor ou matrículas vinculadas. O sistema mostra uma mensagem explicando o impedimento.

### Iniciar e finalizar um semestre

1. Clique em **Semestres** no menu.
2. Em **Identificação do semestre**, escreva algo fácil de reconhecer, como `2026/2`.
3. Clique em **Iniciar semestre**. Cada professor já cadastrado recebe **1.000 MC**.
4. Consulte o semestre ativo e o **Histórico de semestres** na mesma página.
5. Ao encerrar o período, clique em **Finalizar semestre**.

Só pode existir **um semestre ativo** por vez. É preciso finalizar o atual antes de iniciar outro. Os saldos não utilizados **se acumulam**; finalizar um semestre não apaga moedas nem bloqueia os envios de reconhecimento. Um novo crédito ocorre somente quando outro semestre é iniciado.

## Criar uma conta de aluno

1. Na página inicial do site, clique em **Criar conta** e escolha **Aluno**.
2. Preencha **Nome**, **E-mail** e **Senha**. A senha precisa ter pelo menos **8 caracteres**.
3. Preencha **CPF**, **RG** e **Endereço**.
4. Escolha uma **Instituição**. Os cursos dessa instituição serão exibidos abaixo.
5. Marque **um ou mais cursos** em **Cursos**.
6. Clique em **Criar minha conta**. O cadastro já cria as matrículas escolhidas e abre o painel do aluno.

Se a lista de instituições ou cursos estiver vazia, o administrador precisa cadastrá-los primeiro. Se aparecer **E-mail já cadastrado** ou **CPF já cadastrado**, confira se você já possui uma conta; o sistema não permite repetir esses dados.

### Ver e alterar matrículas

1. Entre na conta de aluno e clique em **Meus cursos**.
2. Veja os cursos em que você está matriculado.
3. Para adicionar outro curso da **mesma instituição**, escolha-o em **Adicionar curso** e clique em **Matricular-se**.
4. Para sair de um curso, clique em **Remover** ao lado dele e confirme a pergunta do navegador.

Um curso já matriculado não aparece na lista de opções para nova matrícula. Ao remover uma matrícula, o aluno deixa de aparecer para o professor daquele curso. A remoção não apaga movimentações antigas do extrato.

### Consultar saldo e extrato

O saldo em **MC** aparece no topo do painel. Em **Visão geral**, veja as atividades recentes. Em **Meu extrato**, veja todas as movimentações: crédito recebido, reconhecimento recebido e resgate. Valores com sinal **+** entram na carteira; valores com sinal **−** saem dela. O extrato registra data, motivo e quantidade.

### Resgatar uma vantagem

1. Clique em **Vantagens**.
2. Compare o **custo em MC** da vantagem com o seu saldo.
3. Clique em **Resgatar** na vantagem desejada e confirme na janela do navegador.
4. Após a confirmação, a página mostra um **código de cupom**. Guarde-o e confira também o email cadastrado, inclusive a pasta de spam.
5. Apresente o código à empresa parceira. A empresa recebe o mesmo código por email para conferência.

Se o saldo não for suficiente, o botão informa **Saldo insuficiente**. O custo é debitado do saldo quando o resgate é concluído. O extrato mostra a despesa, mas a interface atual **não possui uma tela para consultar novamente todos os códigos de cupons**; mantenha o email do cupom.

## Criar uma conta de professor e reconhecer alunos

### Cadastro

1. Na página inicial, clique em **Criar conta** e escolha **Professor**.
2. Preencha **Nome**, **E-mail**, **Senha** de pelo menos 8 caracteres e **CPF**.
3. Escolha a **Instituição** e o **Curso** que leciona.
4. Clique em **Criar minha conta**.

O professor fica vinculado a **um curso**. Para que receba as 1.000 MC do semestre, cadastre-o **antes** de o administrador iniciar o semestre.

### Enviar moedas a um aluno

1. Entre na conta do professor. Confira o saldo no topo do painel e os alunos mostrados na **Visão geral**.
2. No menu, clique em **Reconhecer aluno**.
3. Em **Aluno**, selecione um estudante matriculado no curso que você leciona.
4. Digite a **Quantidade de moedas** e uma **Mensagem de reconhecimento**.
5. Clique em **Enviar moedas de reconhecimento**.
6. Confira a mensagem de sucesso, o saldo atualizado e a saída em **Meu extrato**. O aluno verá a entrada em seu extrato e receberá um email.

Só é possível enviar uma quantidade positiva até o saldo disponível. Se não houver aluno no seletor, verifique se ele se matriculou no **mesmo curso** do professor. Se o professor estiver sem saldo, peça ao administrador que confira a etapa de início do semestre. Se houver erro de envio de email, confira saldo e extrato antes de tentar novamente.

## Criar uma conta de empresa e publicar vantagens

### Cadastro

1. Na página inicial, clique em **Criar conta** e escolha **Empresa**.
2. Preencha **Nome**, **E-mail**, **Senha** de pelo menos 8 caracteres e **CNPJ**.
3. Clique em **Criar minha conta**. Empresa não escolhe instituição nem curso.

Use um email que a empresa acompanhe: nele chegarão os códigos de resgate para conferência.

### Publicar uma vantagem

1. Entre na conta da empresa e clique em **Minhas vantagens**.
2. Em **Cadastrar vantagem**, preencha **Nome da vantagem**, **Custo em moedas** (número maior que zero) e **Descrição**.
3. **URL da foto do produto** é opcional. Se utilizada, deve ser um link público de imagem que comece com `https://` ou `http://`.
4. Clique em **Publicar vantagem**. Ela aparecerá em **Vantagens publicadas** e ficará disponível aos alunos.

O campo de foto recebe um **link**, não um arquivo enviado do computador. Se a imagem não abrir, confira se o endereço é público e está correto.

### Editar, remover e conferir um resgate

- Nas vantagens publicadas, clique em **Editar** para alterar nome, descrição, custo e URL da foto. O navegador perguntará os campos um por vez.
- Clique em **Remover** para desativar a vantagem. Ela deixa de aparecer aos alunos, mas os resgates anteriores continuam registrados. A interface atual não oferece botão para reativá-la; para voltar a oferecer o benefício, publique uma nova vantagem.
- Quando um aluno resgatar uma vantagem, a empresa receberá um email com o **código de conferência**. Compare-o com o código apresentado pelo aluno. Atualmente não há uma tela no painel da empresa para listar resgates ou marcar cupons como utilizados.

## Editar perfil, sair e desativar conta

Essas opções aparecem na parte inferior do menu lateral para **aluno, professor e empresa**:

- **Editar perfil:** o navegador perguntará nome, email e, para aluno, endereço. Depois perguntará uma nova senha. Deixe o campo da nova senha **vazio** para manter a senha atual. Confirme os diálogos para salvar. Se alterar o email, as próximas notificações serão enviadas para o novo endereço.
- **Sair da conta:** encerra o acesso neste navegador. Para entrar novamente, use email e senha.
- **Desativar conta:** após confirmação, bloqueia novos acessos. Os dados de histórico permanecem para preservar o extrato. **A interface não oferece reativação**, e o mesmo email/CPF/CNPJ permanece cadastrado; não use essa opção apenas para sair.

O administrador possui **Sair da conta**, mas não usa o formulário comum de edição de perfil.

## Exemplo completo para uma primeira demonstração

1. O administrador entra e cadastra `Faculdade Exemplo` com o código `FEX`.
2. Cadastra o curso `Engenharia de Software` nessa instituição.
3. Uma pessoa cria uma conta de **Professor** associada a esse curso.
4. Outra pessoa cria uma conta de **Aluno** na mesma instituição e seleciona o mesmo curso.
5. Uma empresa cria uma conta de **Empresa** e publica uma vantagem chamada `Livro`, com custo de `50 MC`.
6. O administrador entra novamente e inicia o semestre `2026/2`. O professor passa a ter `1.000 MC`.
7. O professor abre **Reconhecer aluno** e envia `100 MC` ao aluno com uma mensagem.
8. O aluno confirma que seu saldo passou a `100 MC`, abre **Vantagens** e resgata o livro por `50 MC`.
9. O aluno confirma saldo de `50 MC` e consulta seu extrato. Ele e a empresa conferem os emails com o mesmo código do cupom.

Para percorrer perfis diferentes no **mesmo navegador**, use **Sair da conta** antes de entrar com a conta seguinte. Use endereços de email reais aos quais você tenha acesso durante o teste de notificações.

## Dúvidas e problemas comuns

**A página está demorando para abrir.** No Render Free, o serviço pode hibernar quando fica sem visitas. Aguarde o primeiro carregamento e tente atualizar a página.

**Não aparecem instituições ou cursos no cadastro.** O administrador precisa criá-los em **Instituições e cursos**. Se surgir um aviso de erro no catálogo, use **Tentar novamente**.

**Não consigo criar uma conta.** Verifique os campos obrigatórios do perfil, a senha de pelo menos 8 caracteres e se email, CPF ou CNPJ já foram usados. Aluno precisa de RG, endereço e pelo menos um curso; professor precisa de um curso.

**Esqueci a senha.** A versão atual não tem recuperação de senha pela tela inicial. Se ainda estiver conectado em outro navegador, use **Editar perfil** para trocá-la. Caso contrário, procure o responsável técnico pelo sistema.

**Sou professor e não vejo alunos.** O aluno deve estar matriculado no curso escolhido no cadastro do professor. O professor vê apenas alunos desse curso.

**Sou professor e meu saldo está zerado.** O administrador deve iniciar um semestre **depois** de o professor ser cadastrado. O crédito não é retroativo.

**O botão de resgate está desativado.** O aluno precisa ter pelo menos a quantidade de MC indicada no custo da vantagem.

**O email não chegou.** Confira o email cadastrado em **Editar perfil**, a pasta de spam e as configurações do remetente na Brevo. O envio pelo provedor pode atrasar ou ser recusado; se a operação mostrar erro, confira também saldo e extrato antes de repetir.

**Preciso encontrar um cupom antigo.** A interface mostra o código no momento do resgate e o envia por email, mas ainda não lista cupons antigos em uma tela. Procure o email do cupom; o extrato mostra que o resgate ocorreu.

o.

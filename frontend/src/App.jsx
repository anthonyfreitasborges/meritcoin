import React, { useEffect, useMemo, useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, Award, BookOpen, Building2, Coins, Gift, GraduationCap, LogOut, Plus, Users, Wallet } from 'lucide-react';

const API = '/api';
async function request(path, options = {}) {
  const token = JSON.parse(localStorage.getItem('merit-user') || 'null')?.token;
  const response = await fetch(API + path, { headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, ...options });
  const data = await response.json().catch(() => ({}));
  if (response.status === 401 && token && path !== '/auth/login') { localStorage.removeItem('merit-user'); window.location.reload(); }
  if (!response.ok) throw new Error(data.erro || 'Não foi possível concluir a operação.');
  return data;
}
const blankRegister = { nome: '', email: '', senha: '', cpf: '', rg: '', endereco: '', cnpj: '', instituicaoId: '', cursoId: '', cursoIds: [] };
const money = value => Number(value || 0).toLocaleString('pt-BR');

export default function App() {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('merit-user') || 'null'));
  const [email, setEmail] = useState(''); const [senha, setSenha] = useState('');
  const [tab, setTab] = useState('inicio'); const [saldo, setSaldo] = useState(0); const [extrato, setExtrato] = useState([]);
  const [vantagens, setVantagens] = useState([]); const [alunos, setAlunos] = useState([]); const [matriculas, setMatriculas] = useState([]); const [cursosDisponiveis, setCursosDisponiveis] = useState([]); const [instituicoes, setInstituicoes] = useState([]); const [cursos, setCursos] = useState([]); const [semestres, setSemestres] = useState([]);
  const [msg, setMsg] = useState(''); const [form, setForm] = useState({}); const [register, setRegister] = useState(blankRegister); const [registerRole, setRegisterRole] = useState('ALUNO');
  const [loading, setLoading] = useState(false);
  const [catalogError, setCatalogError] = useState('');
  const coursesForInstitution = useMemo(() => cursos.filter(c => String(c.instituicaoId) === String(register.instituicaoId)), [cursos, register.instituicaoId]);
  const setField = (key, value) => setForm(previous => ({ ...previous, [key]: value }));

  const loadAcademic = async () => {
    const [i, c] = await Promise.all([request('/instituicoes'), request('/cursos')]); setInstituicoes(i); setCursos(c); setCatalogError('');
  };
  const refresh = async () => {
    if (!user) return;
    try {
      const [v] = await Promise.all([request('/vantagens')]); setVantagens(v);
      if (user.role !== 'ADMIN') {
        const [s, e] = await Promise.all([request(`/usuarios/${user.id}/saldo`), request(`/usuarios/${user.id}/extrato`)]); setSaldo(s.saldo); setExtrato(e);
      }
      if (user.role === 'PROFESSOR') setAlunos(await request(`/professores/${user.id}/alunos`));
      if (user.role === 'ALUNO') {
        let institutionId = user.instituicaoId;
        if (!institutionId) {
          const profile = await request(`/usuarios/${user.id}`);
          institutionId = profile.instituicaoId;
          const updated = { ...profile, token: user.token }; setUser(updated); localStorage.setItem('merit-user', JSON.stringify(updated));
        }
        const [enrollments, options] = await Promise.all([
          request(`/usuarios/${user.id}/matriculas`),
          institutionId ? request(`/cursos?instituicaoId=${institutionId}`) : Promise.resolve([])
        ]);
        setMatriculas(enrollments); setCursosDisponiveis(options);
      }
      if (user.role === 'ADMIN') setSemestres(await request('/admin/semestres'));
    } catch (error) { setMsg(error.message); }
  };
  useEffect(() => { loadAcademic().catch(() => {}); }, []);
  useEffect(() => { if (!user && tab === 'cadastro') loadAcademic().catch(error => setCatalogError(error.message)); }, [tab, user]);
  useEffect(() => { refresh(); }, [user]);

  const login = async event => {
    event.preventDefault(); setLoading(true);
    try { const account = await request('/auth/login', { method: 'POST', body: JSON.stringify({ email, senha }) }); setUser(account); localStorage.setItem('merit-user', JSON.stringify(account)); setMsg(`Bem-vindo(a), ${account.nome.split(' ')[0]}!`); }
    catch (error) { setMsg(error.message); } finally { setLoading(false); }
  };
  const logout = () => { request('/auth/logout', { method: 'POST' }).catch(() => {}); localStorage.removeItem('merit-user'); setUser(null); setTab('inicio'); setMsg(''); };
  const submit = async (path, body, after = refresh) => {
    setLoading(true);
    try { const data = await request(path, { method: 'POST', body: JSON.stringify(body) }); setMsg(data.mensagem || 'Operação realizada com sucesso.'); setForm({}); await after?.(); }
    catch (error) { setMsg(error.message); } finally { setLoading(false); }
  };
  const change = async (path, method, body, after = refresh) => {
    try { await request(path, { method, ...(body ? { body: JSON.stringify(body) } : {}) }); setMsg('Alteração salva.'); await after?.(); }
    catch (error) { setMsg(error.message); }
  };
  const editInstitution = i => {
    const nome = window.prompt('Nome da instituição', i.nome); if (nome === null) return;
    const codigo = window.prompt('Código', i.codigo); if (codigo === null) return;
    change(`/instituicoes/${i.id}`, 'PUT', { nome, codigo }, async () => { await loadAcademic(); await refresh(); });
  };
  const editCourse = c => {
    const nome = window.prompt('Nome do curso', c.nome); if (nome === null) return;
    change(`/cursos/${c.id}`, 'PUT', { nome, instituicaoId: c.instituicaoId }, async () => { await loadAcademic(); await refresh(); });
  };
  const editAdvantage = v => {
    const nome = window.prompt('Nome da vantagem', v.nome); if (nome === null) return;
    const descricao = window.prompt('Descrição', v.descricao); if (descricao === null) return;
    const custo = window.prompt('Custo em moedas', String(v.custoMoedas)); if (custo === null) return;
    const fotoUrl = window.prompt('URL da foto', v.fotoUrl || ''); if (fotoUrl === null) return;
    change(`/vantagens/${v.id}`, 'PUT', { nome, descricao, custoMoedas: Number(custo), fotoUrl, empresaId: user.id });
  };
  const editProfile = async () => {
    const nome = window.prompt('Seu nome', user.nome); if (nome === null) return;
    const nextEmail = window.prompt('Seu email', user.email); if (nextEmail === null) return;
    const endereco = user.role === 'ALUNO' ? window.prompt('Seu endereço', user.endereco || '') : '';
    if (endereco === null) return;
    const nextPassword = window.prompt('Nova senha (deixe vazia para manter a atual)', ''); if (nextPassword === null) return;
    try {
      const profile = await request(`/usuarios/${user.id}`, { method: 'PUT', body: JSON.stringify({ nome, email: nextEmail, endereco, senha: nextPassword }) });
      const updated = { ...profile, token: user.token }; setUser(updated); localStorage.setItem('merit-user', JSON.stringify(updated)); setMsg('Perfil atualizado.');
    } catch (error) { setMsg(error.message); }
  };
  const closeAccount = async () => {
    if (!window.confirm('Desativar sua conta? Você perderá o acesso.')) return;
    try { await request(`/usuarios/${user.id}`, { method: 'DELETE' }); logout(); }
    catch (error) { setMsg(error.message); }
  };
  const createAccount = async event => {
    event.preventDefault();
    const payload = { nome: register.nome, email: register.email, senha: register.senha, role: registerRole,
      ...(registerRole === 'EMPRESA' ? { cnpj: register.cnpj } : { cpf: register.cpf, instituicaoId: Number(register.instituicaoId), ...(registerRole === 'ALUNO' ? { cursoIds: register.cursoIds.map(Number) } : { cursoId: Number(register.cursoId) }) }),
      ...(registerRole === 'ALUNO' ? { rg: register.rg, endereco: register.endereco } : {}) };
    try { const account = await request('/usuarios', { method: 'POST', body: JSON.stringify(payload) }); setUser(account); setTab('inicio'); localStorage.setItem('merit-user', JSON.stringify(account)); setMsg('Cadastro concluído. Bem-vindo(a) ao MeritCoin!'); }
    catch (error) { setMsg(error.message); }
  };
  const redeem = async advantage => {
    if (!window.confirm(`Resgatar “${advantage.nome}” por ${advantage.custoMoedas} moedas?`)) return;
    try { const result = await request(`/resgates/${advantage.id}?alunoId=${user.id}`, { method: 'POST' }); setMsg(`Cupom ${result.codigo} gerado. Enviamos o código para você e para a empresa.`); await refresh(); }
    catch (error) { setMsg(error.message); }
  };

  if (!user) return <main className="auth-page"><section className="auth-aside"><div className="brand brand-light"><span className="brand-mark"><Award size={22}/></span> merit<span>coin</span></div><div className="auth-message"><p className="eyebrow">RECONHECIMENTO QUE TRANSFORMA</p><h1>Seu esforço merece ser celebrado.</h1><p>Receba moedas pelo seu mérito, acompanhe suas conquistas e troque por benefícios exclusivos.</p><div className="auth-stat"><Coins size={20}/><span>Aprender, conquistar e aproveitar.</span></div></div><small className="auth-foot">Uma comunidade que valoriza cada conquista.</small></section>
    <section className="auth-main"><div className="auth-box"><div className="auth-mobile-brand"><div className="brand"><span className="brand-mark"><Award size={20}/></span> merit<span>coin</span></div></div>
      <div className="auth-tabs"><button className={tab === 'inicio' ? 'active' : ''} onClick={() => { setTab('inicio'); setMsg(''); }}>Entrar</button><button className={tab === 'cadastro' ? 'active' : ''} onClick={() => { setTab('cadastro'); setMsg(''); }}>Criar conta</button></div>
      {tab === 'inicio' ? <><p className="eyebrow">BEM-VINDO DE VOLTA</p><h2>Acesse sua conta</h2><p className="muted">Entre para acompanhar seu saldo e suas conquistas.</p><form className="form-stack" onSubmit={login}><label>E-mail<input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="voce@email.com" required/></label><label>Senha<input type="password" value={senha} onChange={e => setSenha(e.target.value)} placeholder="Sua senha" required/></label><button className="primary-button" disabled={loading}>{loading ? 'Aguarde…' : 'Entrar na plataforma'} <span>→</span></button></form></> : <><p className="eyebrow">FAÇA PARTE</p><h2>Crie sua conta</h2><p className="muted">Escolha seu perfil e preencha seus dados para começar.</p><div className="role-picker">{[['ALUNO','Aluno'],['PROFESSOR','Professor'],['EMPRESA','Empresa']].map(([role, label]) => <button type="button" key={role} onClick={() => { setRegisterRole(role); setRegister(blankRegister); }} className={registerRole === role ? 'selected' : ''}>{label}</button>)}</div><form className="form-stack" onSubmit={createAccount}><label>Nome<input value={register.nome} onChange={e => setRegister({...register,nome:e.target.value})} required/></label><label>E-mail<input type="email" value={register.email} onChange={e => setRegister({...register,email:e.target.value})} required/></label><label>Senha<input type="password" minLength="8" value={register.senha} onChange={e => setRegister({...register,senha:e.target.value})} required/></label>{registerRole === 'EMPRESA' ? <label>CNPJ<input value={register.cnpj} onChange={e => setRegister({...register,cnpj:e.target.value})} required/></label> : <><label>CPF<input value={register.cpf} onChange={e => setRegister({...register,cpf:e.target.value})} required/></label>{registerRole === 'ALUNO' && <><label>RG<input value={register.rg} onChange={e => setRegister({...register,rg:e.target.value})} required/></label><label>Endereço<input value={register.endereco} onChange={e => setRegister({...register,endereco:e.target.value})} required/></label></>}<label>Instituição<select required value={register.instituicaoId} onChange={e => setRegister({...register,instituicaoId:e.target.value,cursoId:'',cursoIds:[]})}><option value="">Selecione a instituição</option>{instituicoes.map(i => <option key={i.id} value={i.id}>{i.nome}</option>)}</select></label>{catalogError && <p className="catalog-error">{catalogError} <button type="button" onClick={() => loadAcademic().catch(error => setCatalogError(error.message))}>Tentar novamente</button></p>}{registerRole === 'ALUNO' ? <fieldset className="course-picker" disabled={!register.instituicaoId}><legend>Cursos <span>Selecione um ou mais</span></legend>{coursesForInstitution.length ? coursesForInstitution.map(c => <label key={c.id}><input type="checkbox" checked={register.cursoIds.includes(String(c.id)) || register.cursoIds.includes(c.id)} onChange={e => setRegister({...register,cursoIds:e.target.checked ? [...register.cursoIds,c.id] : register.cursoIds.filter(id => String(id)!==String(c.id))})}/>{c.nome}</label>) : <small>{register.instituicaoId ? 'Nenhum curso cadastrado nesta instituição.' : 'Selecione primeiro uma instituição.'}</small>}</fieldset> : <label>Curso<select required value={register.cursoId} onChange={e => setRegister({...register,cursoId:e.target.value})} disabled={!register.instituicaoId}><option value="">Selecione o curso</option>{coursesForInstitution.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}</select></label>}</>}<button className="primary-button" disabled={loading || (registerRole === 'ALUNO' && register.cursoIds.length === 0)}>{loading ? 'Criando conta…' : 'Criar minha conta'} <span>→</span></button></form></>}
      {msg && <div className="notice auth-notice">{msg}<button onClick={() => setMsg('')}>×</button></div>}
    </div></section></main>;

  const nav = user.role === 'ALUNO' ? [['inicio','Visão geral',Wallet],['cursos','Meus cursos',BookOpen],['vantagens','Vantagens',Gift],['extrato','Meu extrato',ArrowDownLeft]] : user.role === 'PROFESSOR' ? [['inicio','Visão geral',Wallet],['reconhecer','Reconhecer aluno',Award],['extrato','Meu extrato',ArrowDownLeft]] : user.role === 'EMPRESA' ? [['inicio','Visão geral',Wallet],['vantagens','Minhas vantagens',Gift]] : [['inicio','Visão geral',Wallet],['academico','Instituições e cursos',BookOpen],['semestres','Semestres',GraduationCap]];
  const title = nav.find(item => item[0] === tab)?.[1] || 'Visão geral';
  return <div className="app-shell"><aside className="sidebar"><div className="brand"><span className="brand-mark"><Award size={20}/></span> merit<span>coin</span></div><div className="side-label">MENU PRINCIPAL</div><nav>{nav.map(([key,label,Icon])=><button key={key} className={tab===key?'nav-active':''} onClick={()=>{setTab(key);setMsg('');}}><Icon size={18}/>{label}</button>)}</nav><div className="sidebar-bottom"><div className="profile"><div className="avatar">{user.nome?.[0]}</div><div><b>{user.nome}</b><small>{({ALUNO:'Aluno',PROFESSOR:'Professor',EMPRESA:'Empresa parceira',ADMIN:'Administrador'})[user.role]}</small></div></div>{user.role!=='ADMIN'&&<><button className="logout" onClick={editProfile}>Editar perfil</button><button className="logout" onClick={closeAccount}>Desativar conta</button></>}<button className="logout" onClick={logout}><LogOut size={17}/> Sair da conta</button></div></aside>
    <main className="workspace"><header className="topbar"><div><p className="eyebrow">MERITCOIN / {title.toUpperCase()}</p><h1>Olá, {user.nome?.split(' ')[0]} <span>✦</span></h1><p className="muted">Acompanhe suas atividades e conquistas.</p></div>{user.role!=='ADMIN'&&<div className="balance-card"><span className="balance-icon"><Coins size={20}/></span><div><small>Saldo disponível</small><strong>{money(saldo)} <em>MC</em></strong></div></div>}</header>
    {msg&&<div className="notice">{msg}<button onClick={()=>setMsg('')}>×</button></div>}
    {tab==='inicio'&&<><section className="welcome-banner"><div><p className="eyebrow">{user.role==='ALUNO'?'SUA JORNADA DE CONQUISTAS':user.role==='PROFESSOR'?'VALORIZE O MÉRITO':'PAINEL MERITCOIN'}</p><h2>{user.role==='ALUNO'?'Cada conquista merece uma recompensa.':user.role==='PROFESSOR'?'Incentive quem está se dedicando.':user.role==='EMPRESA'?'Conecte sua marca a novas conquistas.':'Gerencie a comunidade MeritCoin.'}</h2><p>{user.role==='ALUNO'?'Explore vantagens disponíveis e acompanhe suas moedas.':user.role==='PROFESSOR'?'Envie moedas e uma mensagem para reconhecer o esforço dos seus alunos.':user.role==='EMPRESA'?'Publique vantagens e acompanhe as trocas realizadas.':'Cadastre instituições e cursos e gerencie o calendário acadêmico.'}</p></div><div className="banner-art"><div className="coin coin-one">✦</div><div className="coin coin-two">MC</div><div className="coin coin-three">✦</div></div></section>
      <div className="stat-grid">{user.role==='ADMIN'?<><Stat icon={Building2} label="Instituições cadastradas" value={instituicoes.length}/><Stat icon={BookOpen} label="Cursos cadastrados" value={cursos.length}/><Stat icon={GraduationCap} label="Semestres registrados" value={semestres.length}/></>:<><Stat icon={Coins} label="Saldo de moedas" value={`${money(saldo)} MC`}/><Stat icon={user.role==='ALUNO'?Gift:user.role==='PROFESSOR'?Users:Gift} label={user.role==='ALUNO'?'Vantagens disponíveis':user.role==='PROFESSOR'?'Alunos no seu curso':'Vantagens publicadas'} value={user.role==='ALUNO'?vantagens.length:user.role==='PROFESSOR'?alunos.length:vantagens.filter(v=>v.empresaId===user.id).length}/><Stat icon={ArrowUpRight} label="Movimentações" value={extrato.length}/></>}</div>
      {user.role==='ALUNO'&&<section className="panel"><div className="section-heading"><div><h2>Vantagens em destaque</h2><p className="muted">Benefícios disponíveis para você resgatar.</p></div><button className="text-button" onClick={()=>setTab('vantagens')}>Ver todas <span>→</span></button></div><AdvantageCards list={vantagens.slice(0,3)} saldo={saldo} redeem={redeem}/></section>}
      {user.role==='PROFESSOR'&&<section className="panel"><div className="section-heading"><div><h2>Alunos do seu curso</h2><p className="muted">Somente estudantes matriculados no curso vinculado ao seu perfil.</p></div><button onClick={()=>setTab('reconhecer')}>Enviar reconhecimento <span>→</span></button></div><PeopleList alunos={alunos}/></section>}
      {user.role==='EMPRESA'&&<section className="panel"><div className="section-heading"><div><h2>Suas vantagens</h2><p className="muted">Benefícios que você publicou na plataforma.</p></div><button onClick={()=>setTab('vantagens')}>Cadastrar vantagem <Plus size={16}/></button></div><AdvantageCards list={vantagens.filter(v=>v.empresaId===user.id).slice(0,3)} saldo={saldo} redeem={redeem} company edit={editAdvantage} remove={v=>{if(window.confirm(`Remover ${v.nome}?`)) change(`/vantagens/${v.id}`, 'DELETE');}}/></section>}
      {user.role==='ADMIN'&&<section className="panel"><div className="section-heading"><div><h2>Semestre atual</h2><p className="muted">Controle os créditos de moedas distribuídos aos professores.</p></div><button onClick={()=>setTab('semestres')}>Gerenciar semestres →</button></div><SemesterStatus semesters={semestres}/></section>}
      {user.role!=='ADMIN'&&<RecentMovements extrato={extrato.slice(0,5)}/>}</>}
    {tab==='extrato'&&<RecentMovements extrato={extrato} full/>}
    {tab==='cursos'&&user.role==='ALUNO'&&<section className="panel"><div className="section-heading"><div><h2>Meus cursos</h2><p className="muted">Você pode estar matriculado em vários cursos da mesma instituição.</p></div><span className="balance-pill"><BookOpen size={15}/>{matriculas.length} {matriculas.length===1?'curso':'cursos'}</span></div><div className="enrollment-list">{matriculas.map(m=><div className="enrollment-row" key={m.id}><div className="stat-icon"><BookOpen size={17}/></div><div><b>{m.curso}</b><small>{m.instituicao}</small></div><button onClick={()=>{if(window.confirm(`Sair do curso ${m.curso}?`)) change(`/matriculas/${m.id}`, 'DELETE');}}>Remover</button></div>)}</div><form className="inline-form" onSubmit={e=>{e.preventDefault();submit('/matriculas',{alunoId:user.id,cursoId:Number(form.novoCursoId)},async()=>{setField('novoCursoId','');await refresh();});}}><label>Adicionar curso<select value={form.novoCursoId||''} onChange={e=>setField('novoCursoId',e.target.value)} required><option value="">Selecione outro curso</option>{cursosDisponiveis.filter(c=>!matriculas.some(m=>m.cursoId===c.id)).map(c=><option key={c.id} value={c.id}>{c.nome}</option>)}</select></label><button disabled={!cursosDisponiveis.some(c=>!matriculas.some(m=>m.cursoId===c.id))}>Matricular-se <Plus size={16}/></button></form>{cursosDisponiveis.length===0&&<p className="muted">Não há cursos cadastrados para sua instituição.</p>}</section>}
    {tab==='vantagens'&&user.role==='ALUNO'&&<section className="panel"><div className="section-heading"><div><h2>Vantagens disponíveis</h2><p className="muted">Escolha um benefício para trocar por moedas.</p></div><span className="balance-pill"><Coins size={16}/>{money(saldo)} MC</span></div><AdvantageCards list={vantagens} saldo={saldo} redeem={redeem}/></section>}
    {tab==='vantagens'&&user.role==='EMPRESA'&&<><section className="panel"><h2>Cadastrar vantagem</h2><p className="muted">Compartilhe uma oferta com os alunos da comunidade.</p><form className="form-grid" onSubmit={e=>{e.preventDefault();submit('/vantagens',{...form,custoMoedas:Number(form.custoMoedas),empresaId:user.id});}}><label>Nome da vantagem<input value={form.nome||''} onChange={e=>setField('nome',e.target.value)} required/></label><label>Custo em moedas<input type="number" min="1" value={form.custoMoedas||''} onChange={e=>setField('custoMoedas',e.target.value)} required/></label><label className="wide">URL da foto do produto<input type="url" value={form.fotoUrl||''} onChange={e=>setField('fotoUrl',e.target.value)} placeholder="https://..."/></label><label className="wide">Descrição<textarea value={form.descricao||''} onChange={e=>setField('descricao',e.target.value)} required/></label><button className="primary-button">Publicar vantagem <Plus size={16}/></button></form></section><section className="panel"><h2>Vantagens publicadas</h2><AdvantageCards list={vantagens.filter(v=>v.empresaId===user.id)} saldo={0} company edit={editAdvantage} remove={v=>{if(window.confirm(`Remover ${v.nome}?`)) change(`/vantagens/${v.id}`, 'DELETE');}}/></section></>}
    {tab==='reconhecer'&&user.role==='PROFESSOR'&&<section className="panel narrow-panel"><h2>Enviar reconhecimento</h2><p className="muted">Escolha um aluno matriculado no seu curso e registre o motivo do reconhecimento.</p><form className="form-stack" onSubmit={e=>{e.preventDefault();submit('/professores/enviar',{professorId:user.id,alunoId:Number(form.alunoId),quantidade:Number(form.quantidade),mensagem:form.mensagem});}}><label>Aluno<select required value={form.alunoId||''} onChange={e=>setField('alunoId',e.target.value)}><option value="">Selecione um aluno</option>{alunos.map(a=><option key={a.id} value={a.id}>{a.nome}</option>)}</select></label><label>Quantidade de moedas<input type="number" min="1" max={saldo} value={form.quantidade||''} onChange={e=>setField('quantidade',e.target.value)} required/></label><label>Mensagem de reconhecimento<textarea value={form.mensagem||''} onChange={e=>setField('mensagem',e.target.value)} placeholder="Conte ao aluno o que você está reconhecendo…" required/></label><p className="form-hint">Seu saldo atual: <b>{money(saldo)} MC</b></p><button className="primary-button" disabled={!alunos.length||loading}>{alunos.length?'Enviar moedas de reconhecimento':'Nenhum aluno matriculado neste curso'}</button></form></section>}
    {tab==='academico'&&user.role==='ADMIN'&&<section className="panel"><h2>Instituições e cursos</h2><p className="muted">Os alunos e professores selecionam estes cadastros ao criar uma conta.</p><form className="inline-form" onSubmit={e=>{e.preventDefault();submit('/instituicoes',{nome:form.instituicao,codigo:form.codigo},async()=>{await loadAcademic();await refresh();});}}><label>Nome da instituição<input value={form.instituicao||''} onChange={e=>setField('instituicao',e.target.value)} required/></label><label>Código<input value={form.codigo||''} onChange={e=>setField('codigo',e.target.value)} required/></label><button>Cadastrar instituição <Plus size={16}/></button></form><form className="inline-form" onSubmit={e=>{e.preventDefault();submit('/cursos',{nome:form.curso,instituicaoId:Number(form.instituicaoId)},async()=>{await loadAcademic();await refresh();});}}><label>Nome do curso<input value={form.curso||''} onChange={e=>setField('curso',e.target.value)} required/></label><label>Instituição<select value={form.instituicaoId||''} onChange={e=>setField('instituicaoId',e.target.value)} required><option value="">Selecione</option>{instituicoes.map(i=><option key={i.id} value={i.id}>{i.nome}</option>)}</select></label><button disabled={!instituicoes.length}>Cadastrar curso <Plus size={16}/></button></form><div className="academic-list"><h3>Instituições cadastradas</h3>{instituicoes.map(i=><div className="academic-row" key={i.id}><Building2 size={17}/><b>{i.nome}</b><span>{cursos.filter(c=>c.instituicaoId===i.id).length} cursos</span><button onClick={()=>editInstitution(i)}>Editar</button><button onClick={()=>{if(window.confirm(`Remover ${i.nome}?`)) change(`/instituicoes/${i.id}`, 'DELETE', null, async()=>{await loadAcademic(); await refresh();});}}>Remover</button></div>)}</div><div className="academic-list"><h3>Cursos cadastrados</h3>{cursos.map(c=><div className="academic-row" key={c.id}><BookOpen size={17}/><b>{c.nome}</b><span>{c.instituicao}</span><button onClick={()=>editCourse(c)}>Editar</button><button onClick={()=>{if(window.confirm(`Remover ${c.nome}?`)) change(`/cursos/${c.id}`, 'DELETE', null, async()=>{await loadAcademic(); await refresh();});}}>Remover</button></div>)}</div></section>}
    {tab==='semestres'&&user.role==='ADMIN'&&<><section className="panel"><h2>Gestão de semestres</h2><p className="muted">Iniciar um semestre credita 1.000 moedas a cada professor. O saldo não utilizado é acumulado.</p><form className="inline-form" onSubmit={e=>{e.preventDefault();submit('/admin/semestres/iniciar',{descricao:form.descricao},refresh);}}><label>Identificação do semestre<input placeholder="Ex.: 2026/2" value={form.descricao||''} onChange={e=>setField('descricao',e.target.value)} required/></label><button disabled={semestres.some(s=>s.ativo)}>Iniciar semestre <Plus size={16}/></button><button type="button" className="secondary-button" disabled={!semestres.some(s=>s.ativo)} onClick={()=>submit('/admin/semestres/finalizar',{},refresh)}>Finalizar semestre</button></form></section><section className="panel"><h2>Histórico de semestres</h2><SemesterTable semesters={semestres}/></section></>}
    </main></div>;
}

function Stat({icon:Icon,label,value}){return <article className="stat-card"><div className="stat-icon"><Icon size={19}/></div><div><small>{label}</small><strong>{value}</strong></div></article>}
function AdvantageCards({list,saldo,redeem,company=false,edit,remove}){if(!list.length)return <div className="empty-state"><Gift size={25}/><b>Nenhuma vantagem por aqui ainda</b><p className="muted">Quando houver novas ofertas, elas aparecerão nesta área.</p></div>;return <div className="advantage-grid">{list.map(v=><article className="advantage-card" key={v.id}>{v.fotoUrl?<img src={v.fotoUrl} alt={v.nome} onError={e=>{e.currentTarget.style.display='none';}}/>:<div className="advantage-placeholder"><Gift size={28}/></div>}<div className="advantage-content"><small>{v.empresa}</small><h3>{v.nome}</h3><p>{v.descricao}</p><div className="advantage-footer"><b><Coins size={15}/>{money(v.custoMoedas)} MC</b>{company&&edit&&<><button onClick={()=>edit(v)}>Editar</button><button onClick={()=>remove(v)}>Remover</button></>}{!company&&<button disabled={saldo<v.custoMoedas} onClick={()=>redeem(v)}>{saldo<v.custoMoedas?'Saldo insuficiente':'Resgatar'}</button>}</div></div></article>)}</div>}
function PeopleList({alunos}){if(!alunos.length)return <div className="empty-state"><Users size={24}/><b>Você ainda não tem alunos matriculados</b><p className="muted">Alunos cadastrados no seu curso aparecerão aqui.</p></div>;return <div className="people-list">{alunos.slice(0,5).map(a=><div className="person-row" key={a.id}><div className="avatar">{a.nome[0]}</div><div><b>{a.nome}</b><small>{a.curso}</small></div></div>)}</div>}
function RecentMovements({extrato,full=false}){return <section className="panel"><div className="section-heading"><div><h2>{full?'Extrato de movimentações':'Atividade recente'}</h2><p className="muted">Histórico de moedas recebidas e utilizadas.</p></div></div>{!extrato.length?<div className="empty-state"><Wallet size={25}/><b>Nenhuma movimentação registrada</b><p className="muted">Suas movimentações aparecerão aqui.</p></div>:<div className="table-scroll"><table><thead><tr><th>Movimentação</th><th>Data</th><th>Detalhes</th><th>Moedas</th></tr></thead><tbody>{extrato.map(m=><tr key={m.id}><td><span className={`movement-icon ${m.quantidade>0?'incoming':'outgoing'}`}>{m.quantidade>0?<ArrowDownLeft size={16}/>:<ArrowUpRight size={16}/>}</span>{m.tipo==='CREDITO_SEMESTRE'?'Crédito semestral':m.tipo==='ENVIO'?(m.quantidade>0?'Reconhecimento recebido':'Reconhecimento enviado'):'Resgate de vantagem'}</td><td>{new Date(m.data).toLocaleString('pt-BR')}</td><td>{m.motivo||'—'}</td><td className={m.quantidade>0?'positive':'negative'}>{m.quantidade>0?'+':''}{money(m.quantidade)} MC</td></tr>)}</tbody></table></div>}</section>}
function SemesterStatus({semesters}){const active=semesters.find(s=>s.ativo);return active?<div className="semester-active"><span className="status-dot"/><div><b>{active.descricao} está ativo</b><small>Iniciado em {new Date(active.inicio).toLocaleDateString('pt-BR')}</small></div></div>:<div className="empty-inline"><GraduationCap size={20}/> Nenhum semestre ativo no momento.</div>}
function SemesterTable({semesters}){if(!semesters.length)return <div className="empty-state"><GraduationCap size={24}/><b>Nenhum semestre registrado</b><p className="muted">Inicie um semestre para creditar moedas aos professores.</p></div>;return <div className="table-scroll"><table><thead><tr><th>Semestre</th><th>Início</th><th>Fim</th><th>Status</th></tr></thead><tbody>{[...semesters].reverse().map(s=><tr key={s.id}><td><b>{s.descricao}</b></td><td>{s.inicio?new Date(s.inicio).toLocaleDateString('pt-BR'):'—'}</td><td>{s.fim?new Date(s.fim).toLocaleDateString('pt-BR'):'—'}</td><td><span className={s.ativo?'status-active':'status-closed'}>{s.ativo?'Ativo':'Finalizado'}</span></td></tr>)}</tbody></table></div>}






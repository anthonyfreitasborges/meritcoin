package br.com.meritcoin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
class ApiController {
    private final InstitutionRepository institutions;
    private final CourseRepository courses;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;
    private final SemesterRepository semesters;
    private final AdvantageRepository advantages;
    private final MovementRepository movements;
    private final RedemptionRepository redemptions;
    private final MailService mail;
    private final AuthService auth;
    @Value("${app.admin.email}") String adminEmail;
    @Value("${app.admin.password}") String adminPassword;

    ApiController(InstitutionRepository i, CourseRepository c, UserRepository u, EnrollmentRepository e,
                  SemesterRepository s, AdvantageRepository a, MovementRepository m, RedemptionRepository r, MailService mail, AuthService auth) {
        this.institutions=i; this.courses=c; this.users=u; this.enrollments=e; this.semesters=s;
        this.advantages=a; this.movements=m; this.redemptions=r; this.mail=mail; this.auth=auth;
    }

    record LoginRequest(@Email @NotBlank String email, @NotBlank String senha) {}
    record RegisterRequest(@NotBlank String nome,@Email @NotBlank String email,String cpf,String rg,String endereco,
                           String cnpj,Role role,Long instituicaoId,Long cursoId,List<Long> cursoIds,@NotBlank String senha) {}
    record TransferRequest(@NotNull Long professorId,@NotNull Long alunoId,@NotNull @Min(1) Integer quantidade,@NotBlank String mensagem) {}
    record AdvantageRequest(@NotBlank String nome,@NotBlank String descricao,String fotoUrl,@NotNull @Min(1) Integer custoMoedas,@NotNull Long empresaId) {}
    record SemesterRequest(@NotBlank String descricao) {}
    record EnrollRequest(@NotNull Long alunoId,@NotNull Long cursoId) {}
    record ProfileRequest(@NotBlank String nome,@Email @NotBlank String email,String endereco,String senha) {}

    @PostMapping("/auth/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        if(req.email().equals(adminEmail) && Objects.equals(req.senha(),adminPassword))
            return ResponseEntity.ok(Map.of("id",0,"nome","Administrador","email",adminEmail,"role","ADMIN","token",auth.issue(null)));
        Optional<UserAccount> found=users.findByEmail(req.email()).filter(u -> u.ativo && auth.matches(req.senha(),u.senhaHash));
        return found.<ResponseEntity<?>>map(u -> { Map<String,Object> dto=userDto(u); dto.put("token",auth.issue(u)); return ResponseEntity.ok(dto); })
            .orElseGet(() -> ResponseEntity.status(401).body(Map.of("erro","Credenciais inválidas.")));
    }

    @PostMapping("/auth/logout") void logout(HttpServletRequest request) { auth.logout(request); }

    @PostMapping("/usuarios")
    @Transactional
    Map<String,Object> register(@Valid @RequestBody RegisterRequest r) {
        if(r.senha().length()<8) throw new IllegalArgumentException("A senha precisa ter ao menos 8 caracteres.");
        if(users.findByEmail(r.email()).isPresent()) throw new IllegalArgumentException("E-mail já cadastrado.");
        if(r.role()==Role.ADMIN) throw new IllegalArgumentException("Administrador não pode ser cadastrado por esta rota.");
        if(r.role()==null) throw new IllegalArgumentException("Informe o perfil do usuário.");
        if((r.role()==Role.ALUNO || r.role()==Role.PROFESSOR) && (r.cpf()==null || r.cpf().isBlank() || r.instituicaoId()==null)) throw new IllegalArgumentException("Aluno e professor precisam de CPF e instituição.");
        if(r.role()==Role.PROFESSOR && r.cursoId()==null) throw new IllegalArgumentException("Professor precisa informar o curso em que leciona.");
        if(r.role()==Role.ALUNO && (r.cursoIds()==null || r.cursoIds().isEmpty()) && r.cursoId()==null) throw new IllegalArgumentException("Aluno precisa selecionar ao menos um curso.");
        if(r.role()==Role.ALUNO && (r.rg()==null || r.rg().isBlank() || r.endereco()==null || r.endereco().isBlank())) throw new IllegalArgumentException("Aluno precisa informar RG e endereço.");
        if(r.role()==Role.EMPRESA && (r.cnpj()==null || r.cnpj().isBlank())) throw new IllegalArgumentException("Empresa precisa informar CNPJ.");
        if(r.role()==Role.EMPRESA && (r.instituicaoId()!=null || r.cursoId()!=null)) throw new IllegalArgumentException("Empresa não deve informar instituição ou curso.");
        if(r.role()!=Role.EMPRESA && (r.cnpj()!=null || r.cpf()==null || r.cpf().isBlank())) throw new IllegalArgumentException("Informe somente o CPF para este perfil.");
        if(r.cpf()!=null && users.existsByCpf(r.cpf())) throw new IllegalArgumentException("CPF já cadastrado.");
        if(r.cnpj()!=null && users.existsByCnpj(r.cnpj())) throw new IllegalArgumentException("CNPJ já cadastrado.");
        UserAccount u=new UserAccount(); u.nome=r.nome(); u.email=r.email(); u.role=r.role(); u.senhaHash=auth.hash(r.senha());
        u.cpf=r.cpf(); u.rg=r.rg(); u.endereco=r.endereco(); u.cnpj=r.cnpj();
        if(r.instituicaoId()!=null) u.instituicao=institutions.findById(r.instituicaoId()).orElseThrow();
        List<Long> courseIds = r.role()==Role.ALUNO && r.cursoIds()!=null && !r.cursoIds().isEmpty()
            ? r.cursoIds().stream().distinct().toList()
            : r.cursoId()==null ? List.of() : List.of(r.cursoId());
        List<Course> selectedCourses=new ArrayList<>();
        for(Long courseId:courseIds) {
            Course course=courses.findById(courseId).orElseThrow();
            if(u.instituicao==null || !course.instituicao.id.equals(u.instituicao.id)) throw new IllegalArgumentException("Todos os cursos devem pertencer à instituição selecionada.");
            selectedCourses.add(course);
        }
        if(r.role()==Role.PROFESSOR && !selectedCourses.isEmpty()) u.curso=selectedCourses.get(0);
        u=users.save(u);
        if(u.role==Role.ALUNO) for(Course course:selectedCourses) { Enrollment enrollment=new Enrollment(); enrollment.aluno=u; enrollment.curso=course; enrollments.save(enrollment); }
        Map<String,Object> dto=userDto(u); dto.put("token",auth.issue(u)); return dto;
    }
    @GetMapping("/instituicoes") List<Map<String,Object>> institutions(){ return institutions.findAll().stream().map(i->Map.<String,Object>of("id",i.id,"nome",i.nome,"codigo",i.codigo)).toList(); }
    @PostMapping("/instituicoes") Map<String,Object> addInstitution(@RequestBody Map<String,String> b,HttpServletRequest request){
        auth.require(request,Role.ADMIN);
        if(b.get("nome")==null || b.get("nome").isBlank() || b.get("codigo")==null || b.get("codigo").isBlank()) throw new IllegalArgumentException("Informe nome e código da instituição.");
        Institution i=institutions.save(new Institution(b.get("nome"),b.get("codigo"))); return Map.of("id",i.id,"nome",i.nome,"codigo",i.codigo);
    }
    @GetMapping("/cursos") List<Map<String,Object>> courses(@RequestParam(required=false) Long instituicaoId){ return (instituicaoId==null?courses.findAll():courses.findByInstituicaoId(instituicaoId)).stream().map(c->Map.<String,Object>of("id",c.id,"nome",c.nome,"instituicaoId",c.instituicao.id,"instituicao",c.instituicao.nome)).toList(); }
    @PostMapping("/cursos") Map<String,Object> addCourse(@RequestBody Map<String,Object> b,HttpServletRequest request){
        auth.require(request,Role.ADMIN);
        if(b.get("nome")==null || b.get("instituicaoId")==null) throw new IllegalArgumentException("Informe nome e instituição do curso.");
        Course c=new Course(); c.nome=b.get("nome").toString(); c.instituicao=institutions.findById(Long.valueOf(b.get("instituicaoId").toString())).orElseThrow(); c=courses.save(c); return Map.of("id",c.id,"nome",c.nome,"instituicaoId",c.instituicao.id,"instituicao",c.instituicao.nome);
    }

    @PutMapping("/instituicoes/{id}") Map<String,Object> updateInstitution(@PathVariable Long id,@RequestBody Map<String,String> b,HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        Institution i=institutions.findById(id).orElseThrow();
        if(b.get("nome")==null || b.get("nome").isBlank() || b.get("codigo")==null || b.get("codigo").isBlank()) throw new IllegalArgumentException("Informe nome e código.");
        i.nome=b.get("nome").trim(); i.codigo=b.get("codigo").trim(); institutions.save(i);
        return Map.of("id",i.id,"nome",i.nome,"codigo",i.codigo);
    }
    @DeleteMapping("/instituicoes/{id}") void deleteInstitution(@PathVariable Long id,HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        if(!courses.findByInstituicaoId(id).isEmpty()) throw new IllegalArgumentException("Remova os cursos da instituição primeiro.");
        if(users.findAll().stream().anyMatch(u->u.instituicao!=null && u.instituicao.id.equals(id))) throw new IllegalArgumentException("Instituição possui usuários vinculados.");
        institutions.deleteById(id);
    }
    @PutMapping("/cursos/{id}") Map<String,Object> updateCourse(@PathVariable Long id,@RequestBody Map<String,Object> b,HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        Course c=courses.findById(id).orElseThrow();
        if(b.get("nome")==null || b.get("nome").toString().isBlank() || b.get("instituicaoId")==null) throw new IllegalArgumentException("Informe nome e instituição.");
        Institution institution=institutions.findById(Long.valueOf(b.get("instituicaoId").toString())).orElseThrow();
        if(!c.instituicao.id.equals(institution.id) && (!enrollments.findByCursoId(id).isEmpty() || users.findAll().stream().anyMatch(u->u.curso!=null && u.curso.id.equals(id)))) throw new IllegalArgumentException("Curso vinculado a usuários não pode mudar de instituição.");
        c.nome=b.get("nome").toString().trim(); c.instituicao=institution; courses.save(c);
        return Map.of("id",c.id,"nome",c.nome,"instituicaoId",institution.id,"instituicao",institution.nome);
    }
    @DeleteMapping("/cursos/{id}") void deleteCourse(@PathVariable Long id,HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        if(!enrollments.findByCursoId(id).isEmpty() || users.findAll().stream().anyMatch(u->u.curso!=null && u.curso.id.equals(id))) throw new IllegalArgumentException("Curso possui usuários vinculados.");
        courses.deleteById(id);
    }

    @GetMapping("/professores/{id}/alunos") List<Map<String,Object>> students(@PathVariable Long id,HttpServletRequest request) {
        auth.requireSelf(request,id);
        UserAccount p=users.findById(id).orElseThrow(); if(p.role!=Role.PROFESSOR || p.curso==null) throw new IllegalArgumentException("Professor sem curso vinculado.");
        return enrollments.findByCursoId(p.curso.id).stream().map(e->userDto(e.aluno)).toList();
    }

    @PostMapping("/matriculas")
    @Transactional
    Map<String,Object> enroll(@Valid @RequestBody EnrollRequest r,HttpServletRequest request) {
        auth.requireSelf(request,r.alunoId());
        UserAccount a=users.findById(r.alunoId()).orElseThrow(); Course c=courses.findById(r.cursoId()).orElseThrow();
        if(a.role!=Role.ALUNO) throw new IllegalArgumentException("O usuário informado não é aluno.");
        if(!a.instituicao.id.equals(c.instituicao.id)) throw new IllegalArgumentException("Aluno e curso devem pertencer à mesma instituição.");
        if(enrollments.existsByAlunoIdAndCursoId(a.id,c.id)) throw new IllegalArgumentException("Aluno já matriculado.");
        Enrollment e=new Enrollment(); e.aluno=a; e.curso=c; e=enrollments.save(e);
        return Map.of("id",e.id,"alunoId",a.id,"aluno",a.nome,"cursoId",c.id,"curso",c.nome);
    }

    @DeleteMapping("/matriculas/{id}") void removeEnrollment(@PathVariable Long id,HttpServletRequest request) {
        Enrollment e=enrollments.findById(id).orElseThrow(); auth.requireSelf(request,e.aluno.id); enrollments.delete(e);
    }

    @GetMapping("/usuarios/{id}") Map<String,Object> user(@PathVariable Long id,HttpServletRequest request){ auth.requireSelf(request,id); return userDto(users.findById(id).orElseThrow()); }
    @PutMapping("/usuarios/{id}") Map<String,Object> updateUser(@PathVariable Long id,@Valid @RequestBody ProfileRequest r,HttpServletRequest request) {
        auth.requireSelf(request,id);
        UserAccount u=users.findById(id).orElseThrow();
        if(users.findByEmail(r.email()).filter(other->!other.id.equals(id)).isPresent()) throw new IllegalArgumentException("E-mail já cadastrado.");
        u.nome=r.nome().trim(); u.email=r.email().trim();
        if(u.role==Role.ALUNO) u.endereco=r.endereco();
        if(r.senha()!=null && !r.senha().isBlank()) {
            if(r.senha().length()<8) throw new IllegalArgumentException("A senha precisa ter ao menos 8 caracteres.");
            u.senhaHash=auth.hash(r.senha());
        }
        users.save(u); return userDto(u);
    }
    @DeleteMapping("/usuarios/{id}") @Transactional void deactivateUser(@PathVariable Long id,HttpServletRequest request) {
        auth.requireSelf(request,id);
        UserAccount u=users.findById(id).orElseThrow(); u.ativo=false; users.save(u); auth.logout(request);
    }
    @GetMapping("/usuarios/{id}/matriculas") List<Map<String,Object>> studentEnrollments(@PathVariable Long id,HttpServletRequest request){
        auth.requireSelf(request,id);
        UserAccount student=users.findById(id).orElseThrow();
        if(student.role!=Role.ALUNO) throw new IllegalArgumentException("Somente alunos possuem matrículas.");
        return enrollments.findByAlunoId(student.id).stream().map(e->Map.<String,Object>of(
            "id",e.id,"cursoId",e.curso.id,"curso",e.curso.nome,"instituicao",e.curso.instituicao.nome)).toList();
    }
    @GetMapping("/usuarios/{id}/saldo") Map<String,Object> balance(@PathVariable Long id,HttpServletRequest request){
        auth.requireSelf(request,id);
        int saldo=movements.findByCarteiraIdOrderByDataDesc(id).stream().mapToInt(m -> signed(m,id)).sum();
        return Map.of("saldo",saldo);
    }
    @GetMapping("/usuarios/{id}/extrato") List<Map<String,Object>> statement(@PathVariable Long id,HttpServletRequest request){
        auth.requireSelf(request,id);
        return movements.findByCarteiraIdOrderByDataDesc(id).stream().map(m -> Map.<String,Object>of(
            "id",m.id,"tipo",m.tipo,"quantidade",signed(m,id),"data",m.data,"motivo",m.motivo==null?"":m.motivo,
            "origem",m.origem==null?"Sistema":m.origem.nome,"destino",m.destino==null?"":m.destino.nome)).toList();
    }
    private int signed(Movement m,Long walletId) {
        if(m.tipo==MovementType.CREDITO_SEMESTRE) return m.quantidade;
        if(m.tipo==MovementType.ENVIO) return m.destino!=null && m.destino.id.equals(walletId)?m.quantidade:-m.quantidade;
        return -m.quantidade;
    }

    @PostMapping("/professores/enviar")
    @Transactional
    Map<String,Object> transfer(@Valid @RequestBody TransferRequest r,HttpServletRequest request) {
        auth.require(request,Role.PROFESSOR); auth.requireSelf(request,r.professorId());
        UserAccount p=users.findLockedById(r.professorId()).orElseThrow(), a=users.findById(r.alunoId()).orElseThrow();
        if(p.role!=Role.PROFESSOR || a.role!=Role.ALUNO) throw new IllegalArgumentException("Perfis inválidos.");
        if(p.curso==null || !enrollments.existsByAlunoIdAndCursoId(a.id,p.curso.id)) throw new IllegalArgumentException("Aluno não está matriculado no curso lecionado pelo professor.");
        int saldo=movements.findByCarteiraIdOrderByDataDesc(p.id).stream().mapToInt(m->signed(m,p.id)).sum();
        if(saldo<r.quantidade()) throw new IllegalArgumentException("Saldo insuficiente.");
        Movement debit=new Movement(); debit.carteira=p; debit.origem=p; debit.destino=a; debit.tipo=MovementType.ENVIO; debit.quantidade=r.quantidade(); debit.motivo=r.mensagem();
        movements.save(debit);
        Movement credit=new Movement(); credit.carteira=a; credit.origem=p; credit.destino=a; credit.tipo=MovementType.ENVIO; credit.quantidade=r.quantidade(); credit.motivo=r.mensagem();
        movements.save(credit);
        mail.send(a.email,"Você recebeu moedas de reconhecimento","Olá "+a.nome+"! Você recebeu "+r.quantidade()+" moedas de "+p.nome+".\nMensagem: "+r.mensagem());
        return Map.of("mensagem","Moedas enviadas com sucesso.");
    }

    @PostMapping("/vantagens")
    Map<String,Object> createAdvantage(@Valid @RequestBody AdvantageRequest r,HttpServletRequest request) {
        auth.require(request,Role.EMPRESA); auth.requireSelf(request,r.empresaId());
        UserAccount empresa=users.findById(r.empresaId()).orElseThrow();
        if(empresa.role!=Role.EMPRESA) throw new IllegalArgumentException("Somente empresas podem cadastrar vantagens.");
        Advantage a=new Advantage(); a.nome=r.nome(); a.descricao=r.descricao(); a.fotoUrl=r.fotoUrl(); a.custoMoedas=r.custoMoedas(); a.empresa=empresa; a=advantages.save(a); return Map.of("id",a.id,"nome",a.nome,"descricao",a.descricao,"custoMoedas",a.custoMoedas,"empresa",empresa.nome);
    }
    @PutMapping("/vantagens/{id}") Map<String,Object> updateAdvantage(@PathVariable Long id,@Valid @RequestBody AdvantageRequest r,HttpServletRequest request) {
        auth.require(request,Role.EMPRESA);
        Advantage a=advantages.findById(id).orElseThrow(); auth.requireSelf(request,a.empresa.id);
        a.nome=r.nome(); a.descricao=r.descricao(); a.fotoUrl=r.fotoUrl(); a.custoMoedas=r.custoMoedas(); advantages.save(a);
        return Map.of("id",a.id,"nome",a.nome,"descricao",a.descricao,"custoMoedas",a.custoMoedas);
    }
    @DeleteMapping("/vantagens/{id}") void deactivateAdvantage(@PathVariable Long id,HttpServletRequest request) {
        auth.require(request,Role.EMPRESA);
        Advantage a=advantages.findById(id).orElseThrow(); auth.requireSelf(request,a.empresa.id);
        a.ativa=false; advantages.save(a);
    }
    @GetMapping("/vantagens") List<Map<String,Object>> listAdvantages(){
        return advantages.findByAtivaTrue().stream().map(a->Map.<String,Object>of("id",a.id,"nome",a.nome,"descricao",a.descricao,
            "fotoUrl",a.fotoUrl==null?"":a.fotoUrl,"custoMoedas",a.custoMoedas,"empresa",a.empresa.nome,"empresaId",a.empresa.id)).toList();
    }
    @PostMapping("/resgates/{vantagemId}")
    @Transactional
    Map<String,Object> redeem(@PathVariable Long vantagemId,@RequestParam Long alunoId,HttpServletRequest request) {
        auth.require(request,Role.ALUNO); auth.requireSelf(request,alunoId);
        UserAccount aluno=users.findLockedById(alunoId).orElseThrow(); Advantage v=advantages.findById(vantagemId).orElseThrow();
        if(aluno.role!=Role.ALUNO || !v.ativa) throw new IllegalArgumentException("Resgate indisponível.");
        int saldo=movements.findByCarteiraIdOrderByDataDesc(aluno.id).stream().mapToInt(m->signed(m,aluno.id)).sum();
        if(saldo<v.custoMoedas) throw new IllegalArgumentException("Saldo insuficiente.");
        Redemption r=new Redemption(); r.aluno=aluno; r.vantagem=v; r.custo=v.custoMoedas; redemptions.save(r);
        Movement m=new Movement(); m.carteira=aluno; m.origem=aluno; m.tipo=MovementType.RESGATE; m.quantidade=v.custoMoedas; m.motivo="Resgate: "+v.nome; m.resgate=r; movements.save(m);
        mail.send(v.empresa.email,"Novo resgate de vantagem","A vantagem "+v.nome+" foi resgatada. Código para conferência: "+r.codigo);
        mail.send(aluno.email,"Seu cupom MeritCoin","Vantagem: "+v.nome+"\nCódigo: "+r.codigo+"\nApresente este código presencialmente.");
        return Map.of("codigo",r.codigo,"vantagem",v.nome,"custo",r.custo,"mensagem","Resgate realizado. O cupom foi enviado por e-mail.");
    }
    @GetMapping("/resgates/aluno/{id}") List<Map<String,Object>> redemptions(@PathVariable Long id,HttpServletRequest request){ auth.requireSelf(request,id); return redemptions.findByAlunoIdOrderByDataDesc(id).stream().map(r->Map.<String,Object>of("id",r.id,"codigo",r.codigo,"vantagem",r.vantagem.nome,"custo",r.custo,"data",r.data,"status",r.status)).toList(); }

    @PostMapping("/admin/semestres/iniciar")
    @Transactional
    Map<String,Object> startSemester(@Valid @RequestBody SemesterRequest r,HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        if(semesters.findFirstByAtivoTrue().isPresent()) throw new IllegalArgumentException("Já existe um semestre ativo.");
        Semester s=new Semester(); s.descricao=r.descricao(); s.inicio=java.time.LocalDateTime.now(); s.ativo=true; semesters.save(s);
        for(UserAccount p:users.findByRole(Role.PROFESSOR)) {
            Movement m=new Movement(); m.carteira=p; m.tipo=MovementType.CREDITO_SEMESTRE; m.quantidade=1000; m.motivo="Crédito inicial do semestre "+s.descricao; m.semestre=s; movements.save(m);
        }
        return Map.of("mensagem","Semestre iniciado. Professores receberam 1.000 moedas.","semestre",s.descricao);
    }
    @PostMapping("/admin/semestres/finalizar")
    @Transactional
    Map<String,String> finishSemester(HttpServletRequest request) {
        auth.require(request,Role.ADMIN);
        Semester s=semesters.findFirstByAtivoTrue().orElseThrow(()->new IllegalArgumentException("Não há semestre ativo."));
        s.ativo=false; s.fim=java.time.LocalDateTime.now(); semesters.save(s);
        return Map.of("mensagem","Semestre finalizado.");
    }
    @GetMapping("/admin/semestres") List<Map<String,Object>> allSemesters(HttpServletRequest request){ auth.require(request,Role.ADMIN); return semesters.findAll().stream().map(s->Map.<String,Object>of("id",s.id,"descricao",s.descricao,"inicio",s.inicio==null?"":s.inicio,"fim",s.fim==null?"":s.fim,"ativo",s.ativo)).toList(); }

    private Map<String,Object> userDto(UserAccount u) {
        Map<String,Object> d=new LinkedHashMap<>(); d.put("id",u.id); d.put("nome",u.nome); d.put("email",u.email); d.put("role",u.role);
        d.put("instituicao",u.instituicao==null?"":u.instituicao.nome); d.put("instituicaoId",u.instituicao==null?null:u.instituicao.id);
        d.put("curso",u.curso==null?"":u.curso.nome); d.put("cursoId",u.curso==null?null:u.curso.id); d.put("endereco",u.endereco==null?"":u.endereco);
        List<String> studentCourses=u.role==Role.ALUNO?enrollments.findByAlunoId(u.id).stream().map(e->e.curso.nome).distinct().toList():List.of();
        if(studentCourses.isEmpty() && u.role==Role.ALUNO && u.curso!=null) studentCourses=List.of(u.curso.nome);
        d.put("cursos",studentCourses); return d;
    }
    @ExceptionHandler({IllegalArgumentException.class,NoSuchElementException.class})
    ResponseEntity<?> badRequest(Exception e){ return ResponseEntity.badRequest().body(Map.of("erro",e.getMessage()==null?"Dados inválidos.":e.getMessage())); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> serviceUnavailable(Exception e){ return ResponseEntity.status(503).body(Map.of("erro",e.getMessage()==null?"Serviço temporariamente indisponível.":e.getMessage())); }
}

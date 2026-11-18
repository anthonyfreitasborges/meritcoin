package br.com.meritcoin;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

enum Role { ALUNO, PROFESSOR, EMPRESA, ADMIN }
enum MovementType { CREDITO_SEMESTRE, ENVIO, RESGATE }
enum RedemptionStatus { GERADO, UTILIZADO, CANCELADO }

@Entity
@Table(name="instituicoes")
class Institution {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String nome;
    @Column(nullable=false, unique=true) public String codigo;
    protected Institution() {}
    Institution(String nome, String codigo) { this.nome=nome; this.codigo=codigo; }
}
@Entity
@Table(name="cursos")
class Course {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String nome;
    @ManyToOne(optional=false) public Institution instituicao;
    protected Course() {}
}
@Entity
@Table(name="usuarios", uniqueConstraints={@UniqueConstraint(columnNames="cpf"),@UniqueConstraint(columnNames="cnpj")})
class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String nome;
    @Column(nullable=false, unique=true) public String email;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role;
    public String cpf;
    public String rg;
    public String endereco;
    public String cnpj;
    public String senhaAdminHash;
    public String senhaHash;
    @Column(nullable=false) public boolean ativo=true;
    @ManyToOne public Institution instituicao;
    @ManyToOne public Course curso;
    protected UserAccount() {}
}
@Entity
@Table(name="matriculas", uniqueConstraints=@UniqueConstraint(columnNames={"aluno_id","curso_id"}))
class Enrollment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public UserAccount aluno;
    @ManyToOne(optional=false) public Course curso;
    protected Enrollment() {}
}
@Entity
@Table(name="semestres")
class Semester {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String descricao;
    public LocalDateTime inicio;
    public LocalDateTime fim;
    @Column(nullable=false) public boolean ativo=false;
    protected Semester() {}
}
@Entity
@Table(name="vantagens")
class Advantage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String nome;
    @Column(nullable=false, length=2000) public String descricao;
    public String fotoUrl;
    @Column(nullable=false) public Integer custoMoedas;
    @Column(nullable=false) public boolean ativa=true;
    @ManyToOne(optional=false) public UserAccount empresa;
    protected Advantage() {}
}
@Entity
@Table(name="movimentacoes")
class Movement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public UserAccount carteira;
    @ManyToOne public UserAccount origem;
    @ManyToOne public UserAccount destino;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public MovementType tipo;
    @Column(nullable=false) public Integer quantidade;
    @Column(nullable=false) public LocalDateTime data=LocalDateTime.now();
    @Column(length=1000) public String motivo;
    @ManyToOne public Semester semestre;
    @ManyToOne public Redemption resgate;
    protected Movement() {}
}
@Entity
@Table(name="resgates")
class Redemption {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public String codigo=UUID.randomUUID().toString().replace("-", "").substring(0,12).toUpperCase();
    @ManyToOne(optional=false) public UserAccount aluno;
    @ManyToOne(optional=false) public Advantage vantagem;
    @Column(nullable=false) public Integer custo;
    @Column(nullable=false) public LocalDateTime data=LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(nullable=false) public RedemptionStatus status=RedemptionStatus.GERADO;
    protected Redemption() {}
}

@Entity
@Table(name="sessoes")
class LoginSession {
    @Id public String token;
    @ManyToOne public UserAccount usuario;
    @Column(nullable=false) public LocalDateTime expiraEm;
    protected LoginSession() {}
}

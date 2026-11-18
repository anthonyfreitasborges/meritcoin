package br.com.meritcoin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

interface InstitutionRepository extends JpaRepository<Institution,Long> {}
interface CourseRepository extends JpaRepository<Course,Long> { List<Course> findByInstituicaoId(Long id); }
interface UserRepository extends JpaRepository<UserAccount,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findLockedById(Long id);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByEmailAndCpf(String email,String cpf);
    Optional<UserAccount> findByEmailAndCnpj(String email,String cnpj);
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
    List<UserAccount> findByRole(Role role);
}
interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
    boolean existsByAlunoIdAndCursoId(Long aluno,Long curso);
    List<Enrollment> findByAlunoId(Long alunoId);
    List<Enrollment> findByCursoId(Long cursoId);
}
interface SemesterRepository extends JpaRepository<Semester,Long> { Optional<Semester> findFirstByAtivoTrue(); }
interface AdvantageRepository extends JpaRepository<Advantage,Long> { List<Advantage> findByAtivaTrue(); List<Advantage> findByEmpresaId(Long id); }
interface MovementRepository extends JpaRepository<Movement,Long> { List<Movement> findByCarteiraIdOrderByDataDesc(Long id); }
interface RedemptionRepository extends JpaRepository<Redemption,Long> { List<Redemption> findByAlunoIdOrderByDataDesc(Long id); }
interface LoginSessionRepository extends JpaRepository<LoginSession,String> { void deleteByUsuarioId(Long id); }

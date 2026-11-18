package br.com.meritcoin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties={
    "spring.datasource.url=jdbc:h2:mem:meritcoin;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "app.admin.email=admin@example.com",
    "app.admin.password=test-secret"
})
class ApiFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean MailService mail;

    private JsonNode post(String path,String body,String token) throws Exception {
        var builder=org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(body);
        if(token!=null) builder.header("Authorization","Bearer "+token);
        return json.readTree(mvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    @Test void registrationPermissionsAndCrud() throws Exception {
        String admin=post("/api/auth/login","{\"email\":\"admin@example.com\",\"senha\":\"test-secret\"}",null).get("token").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/instituicoes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Faculdade\",\"codigo\":\"F1\"}"))
            .andExpect(status().isUnauthorized());
        long institution=post("/api/instituicoes","{\"nome\":\"Faculdade\",\"codigo\":\"F1\"}",admin).get("id").asLong();
        long course=post("/api/cursos","{\"nome\":\"Engenharia\",\"instituicaoId\":"+institution+"}",admin).get("id").asLong();
        String student="{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"senha\":\"password123\",\"cpf\":\"111\",\"rg\":\"22\",\"endereco\":\"Rua A\",\"role\":\"ALUNO\",\"instituicaoId\":"+institution+",\"cursoId\":"+course+"}";
        JsonNode account=post("/api/usuarios",student,null);
        String studentToken=account.get("token").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/semestres/iniciar").header("Authorization","Bearer "+studentToken).contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"2026/2\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios/"+account.get("id").asLong()+"/matriculas").header("Authorization","Bearer "+studentToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].curso").value("Engenharia"));
        mvc.perform(put("/api/cursos/"+course).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Computação\",\"instituicaoId\":"+institution+"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Computação"));
        mvc.perform(delete("/api/cursos/"+course).header("Authorization","Bearer "+admin))
            .andExpect(status().isBadRequest());
    }
    @Test void transferAndRedemptionUseOwners() throws Exception {
        String admin=post("/api/auth/login","{\"email\":\"admin@example.com\",\"senha\":\"test-secret\"}",null).get("token").asText();
        long institution=post("/api/instituicoes","{\"nome\":\"Faculdade 2\",\"codigo\":\"F2\"}",admin).get("id").asLong();
        long course=post("/api/cursos","{\"nome\":\"Direito\",\"instituicaoId\":"+institution+"}",admin).get("id").asLong();
        JsonNode student=post("/api/usuarios","{\"nome\":\"Bia\",\"email\":\"bia@example.com\",\"senha\":\"password123\",\"cpf\":\"222\",\"rg\":\"33\",\"endereco\":\"Rua B\",\"role\":\"ALUNO\",\"instituicaoId\":"+institution+",\"cursoId\":"+course+"}",null);
        JsonNode teacher=post("/api/usuarios","{\"nome\":\"Prof\",\"email\":\"prof@example.com\",\"senha\":\"password123\",\"cpf\":\"333\",\"role\":\"PROFESSOR\",\"instituicaoId\":"+institution+",\"cursoId\":"+course+"}",null);
        JsonNode company=post("/api/usuarios","{\"nome\":\"Loja\",\"email\":\"loja@example.com\",\"senha\":\"password123\",\"cnpj\":\"444\",\"role\":\"EMPRESA\"}",null);
        post("/api/admin/semestres/iniciar","{\"descricao\":\"2026/2\"}",admin);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/professores/enviar")
            .header("Authorization","Bearer "+student.get("token").asText()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"professorId\":"+teacher.get("id").asLong()+",\"alunoId\":"+student.get("id").asLong()+",\"quantidade\":100,\"mensagem\":\"Parabéns\"}"))
            .andExpect(status().isForbidden());
        post("/api/professores/enviar","{\"professorId\":"+teacher.get("id").asLong()+",\"alunoId\":"+student.get("id").asLong()+",\"quantidade\":100,\"mensagem\":\"Parabéns\"}",teacher.get("token").asText());
        JsonNode advantage=post("/api/vantagens","{\"nome\":\"Livro\",\"descricao\":\"Livro novo\",\"custoMoedas\":50,\"empresaId\":"+company.get("id").asLong()+"}",company.get("token").asText());
        long advantageId=advantage.get("id").asLong();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/resgates/"+advantageId).param("alunoId",student.get("id").asText()).header("Authorization","Bearer "+teacher.get("token").asText()))
            .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/resgates/"+advantageId).param("alunoId",student.get("id").asText()).header("Authorization","Bearer "+student.get("token").asText()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.codigo").isNotEmpty());
        mvc.perform(get("/api/usuarios/"+student.get("id").asLong()+"/saldo").header("Authorization","Bearer "+student.get("token").asText()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.saldo").value(50));
        verify(mail,org.mockito.Mockito.atLeastOnce()).send(eq("bia@example.com"),anyString(),anyString());
        verify(mail).send(eq("loja@example.com"),anyString(),anyString());
    }
}

package io.reflectoring.coderadar.rest.query;

import com.fasterxml.jackson.core.type.TypeReference;
import io.reflectoring.coderadar.projectadministration.port.driver.project.create.CreateProjectCommand;
import io.reflectoring.coderadar.query.port.driver.GetCommitResponse;
import io.reflectoring.coderadar.rest.ControllerTestTemplate;
import io.reflectoring.coderadar.rest.ErrorMessageResponse;
import io.reflectoring.coderadar.rest.IdResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URL;
import java.util.List;

import static io.reflectoring.coderadar.rest.JsonHelper.fromJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetCommitsInProjectControllerTest extends ControllerTestTemplate {

    private Long projectId;

    @BeforeEach
    void setUp() throws Exception {
        URL testRepoURL =  this.getClass().getClassLoader().getResource("test-repository");
        CreateProjectCommand command1 =
                new CreateProjectCommand(
                        "test-project", "username", "password", testRepoURL.toString(), false, null, null);
        MvcResult result = mvc().perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(toJson(command1))).andReturn();

        projectId = fromJson(result.getResponse().getContentAsString(), IdResponse.class).getId();
    }

    @Test
    void returnsAllCommitsInProject() throws Exception {
        MvcResult result = mvc().perform(get("/projects/" + projectId + "/commits").contentType(MediaType.APPLICATION_JSON)).andReturn();

        List<GetCommitResponse> commits = fromJson(new TypeReference<List<GetCommitResponse>>() {},
                result.getResponse().getContentAsString());

        Assertions.assertEquals(13, commits.size());
        Assertions.assertEquals("add Finding.java", commits.get(commits.size() - 1).getComment());
        Assertions.assertEquals("testCommit", commits.get(0).getComment());
    }

    @Test
    void returnsErrorWhenProjectWithIdDoesNotExist() throws Exception {
        MvcResult result = mvc().perform(get("/projects/1234/commits").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorMessageResponse response = fromJson(result.getResponse().getContentAsString(), ErrorMessageResponse.class);

        Assertions.assertEquals("Project with id 1234 not found.", response.getErrorMessage());
    }
}

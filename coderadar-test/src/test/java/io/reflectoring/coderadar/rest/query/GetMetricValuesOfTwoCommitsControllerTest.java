package io.reflectoring.coderadar.rest.query;

import io.reflectoring.coderadar.analyzer.port.driver.StartAnalyzingCommand;
import io.reflectoring.coderadar.projectadministration.domain.InclusionType;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.create.CreateAnalyzerConfigurationCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.filepattern.create.CreateFilePatternCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.project.create.CreateProjectCommand;
import io.reflectoring.coderadar.query.domain.DeltaTree;
import io.reflectoring.coderadar.query.domain.MetricValueForCommit;
import io.reflectoring.coderadar.query.domain.MetricsTreeNodeType;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForTwoCommitsCommand;
import io.reflectoring.coderadar.rest.ControllerTestTemplate;
import io.reflectoring.coderadar.rest.ErrorMessageResponse;
import io.reflectoring.coderadar.rest.IdResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URL;
import java.util.*;

import static io.reflectoring.coderadar.rest.JsonHelper.fromJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetMetricValuesOfTwoCommitsControllerTest extends ControllerTestTemplate {

    private Long projectId;

    @BeforeEach
    void setUp() throws Exception {
        URL testRepoURL =  this.getClass().getClassLoader().getResource("test-repository");
        CreateProjectCommand command1 =
                new CreateProjectCommand(
                        "test-project", "username", "password", Objects.requireNonNull(testRepoURL).toString(), false, null, null);
        MvcResult result = mvc().perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(toJson(command1))).andReturn();

        projectId = fromJson(result.getResponse().getContentAsString(), IdResponse.class).getId();

        CreateFilePatternCommand command2 =
                new CreateFilePatternCommand("**/*.java", InclusionType.INCLUDE);
        mvc()
                .perform(
                        post("/projects/" + projectId + "/filePatterns")
                                .content(toJson(command2))
                                .contentType(MediaType.APPLICATION_JSON));

        CreateAnalyzerConfigurationCommand command3 = new CreateAnalyzerConfigurationCommand("io.reflectoring.coderadar.analyzer.loc.LocAnalyzerPlugin", true);
        mvc().perform(post("/projects/" + projectId + "/analyzers").content(toJson(command3)).contentType(MediaType.APPLICATION_JSON));

        StartAnalyzingCommand command4 = new StartAnalyzingCommand(new Date(), true);
        mvc().perform(post("/projects/" + projectId + "/analyze").content(toJson(command4)).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void returnsDeltaTreeForFirstAndLatestCommit() throws Exception {
        GetMetricsForTwoCommitsCommand command = new GetMetricsForTwoCommitsCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit1("fd68136dd6489504e829b11f2fce1fe97c9f5c0c");
        command.setCommit2("d3272b3793bc4b2bc36a1a3a7c8293fcf8fe27df");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/deltaTree")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command))).andReturn();

        DeltaTree deltaTree = fromJson(result.getResponse().getContentAsString(), DeltaTree.class);

        Assertions.assertEquals("root", deltaTree.getName());
        Assertions.assertEquals(MetricsTreeNodeType.MODULE, deltaTree.getType());

        List<MetricValueForCommit> commit1Metrics = deltaTree.getCommit1Metrics();
        List<MetricValueForCommit> commit2Metrics = deltaTree.getCommit2Metrics();

        commit1Metrics.sort(Comparator.comparing(MetricValueForCommit::getMetricName));
        commit2Metrics.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(0L, commit1Metrics.get(0).getValue().longValue());
        Assertions.assertEquals(8L, commit1Metrics.get(1).getValue().longValue());
        Assertions.assertEquals(12L, commit1Metrics.get(2).getValue().longValue());
        Assertions.assertEquals(10L, commit1Metrics.get(3).getValue().longValue());

        Assertions.assertEquals(0L, commit2Metrics.get(0).getValue().longValue());
        Assertions.assertEquals(8L, commit2Metrics.get(1).getValue().longValue());
        Assertions.assertEquals(18L, commit2Metrics.get(2).getValue().longValue());
        Assertions.assertEquals(15L, commit2Metrics.get(3).getValue().longValue());

        DeltaTree firstChild = deltaTree.getChildren().get(0);  // Finding.java
        Assertions.assertEquals("Finding.java", firstChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, firstChild.getType());
        Assertions.assertEquals(4, firstChild.getCommit1Metrics().size());
        Assertions.assertTrue(firstChild.getCommit2Metrics().isEmpty());
        Assertions.assertTrue(firstChild.getChanges().isDeleted());

        DeltaTree secondChild = deltaTree.getChildren().get(1); // GetMetricsForCommitCommand.java
        Assertions.assertEquals("GetMetricsForCommitCommand.java", secondChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, secondChild.getType());
        Assertions.assertTrue(secondChild.getCommit1Metrics().isEmpty());
        Assertions.assertEquals(4, secondChild.getCommit2Metrics().size());
        Assertions.assertTrue(secondChild.getChanges().isAdded());

        DeltaTree thirdChild = deltaTree.getChildren().get(2);  // testModule1/NewRandomFile.java
        Assertions.assertEquals("testModule1/NewRandomFile.java", thirdChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, thirdChild.getType());
        Assertions.assertTrue(thirdChild.getCommit1Metrics().isEmpty());
        Assertions.assertEquals(4, thirdChild.getCommit2Metrics().size());
        Assertions.assertTrue(thirdChild.getChanges().isAdded());
    }

    @Test
    void returnsTreeWhenCommit1IsAfterCommit2() throws Exception {
        GetMetricsForTwoCommitsCommand command = new GetMetricsForTwoCommitsCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit1("d3272b3793bc4b2bc36a1a3a7c8293fcf8fe27df");
        command.setCommit2("fd68136dd6489504e829b11f2fce1fe97c9f5c0c");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/deltaTree")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command))).andReturn();

        DeltaTree deltaTree = fromJson(result.getResponse().getContentAsString(), DeltaTree.class);

        Assertions.assertEquals("root", deltaTree.getName());
        Assertions.assertEquals(MetricsTreeNodeType.MODULE, deltaTree.getType());

        List<MetricValueForCommit> commit1Metrics = deltaTree.getCommit1Metrics();
        List<MetricValueForCommit> commit2Metrics = deltaTree.getCommit2Metrics();

        commit1Metrics.sort(Comparator.comparing(MetricValueForCommit::getMetricName));
        commit2Metrics.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(0L, commit1Metrics.get(0).getValue().longValue());
        Assertions.assertEquals(8L, commit1Metrics.get(1).getValue().longValue());
        Assertions.assertEquals(12L, commit1Metrics.get(2).getValue().longValue());
        Assertions.assertEquals(10L, commit1Metrics.get(3).getValue().longValue());

        Assertions.assertEquals(0L, commit2Metrics.get(0).getValue().longValue());
        Assertions.assertEquals(8L, commit2Metrics.get(1).getValue().longValue());
        Assertions.assertEquals(18L, commit2Metrics.get(2).getValue().longValue());
        Assertions.assertEquals(15L, commit2Metrics.get(3).getValue().longValue());

        DeltaTree firstChild = deltaTree.getChildren().get(0);  // Finding.java
        Assertions.assertEquals("Finding.java", firstChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, firstChild.getType());
        Assertions.assertEquals(4, firstChild.getCommit1Metrics().size());
        Assertions.assertTrue(firstChild.getCommit2Metrics().isEmpty());
        Assertions.assertTrue(firstChild.getChanges().isDeleted());

        DeltaTree secondChild = deltaTree.getChildren().get(1); // GetMetricsForCommitCommand.java
        Assertions.assertEquals("GetMetricsForCommitCommand.java", secondChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, secondChild.getType());
        Assertions.assertTrue(secondChild.getCommit1Metrics().isEmpty());
        Assertions.assertEquals(4, secondChild.getCommit2Metrics().size());
        Assertions.assertTrue(secondChild.getChanges().isAdded());

        DeltaTree thirdChild = deltaTree.getChildren().get(2);  // testModule1/NewRandomFile.java
        Assertions.assertEquals("testModule1/NewRandomFile.java", thirdChild.getName());
        Assertions.assertEquals(MetricsTreeNodeType.FILE, thirdChild.getType());
        Assertions.assertTrue(thirdChild.getCommit1Metrics().isEmpty());
        Assertions.assertEquals(4, thirdChild.getCommit2Metrics().size());
        Assertions.assertTrue(thirdChild.getChanges().isAdded());
    }

    @Test
    void returnsErrorWhenProjectWithIdDoesNotExist() throws Exception {
        GetMetricsForTwoCommitsCommand command = new GetMetricsForTwoCommitsCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit1("fd68136dd6489504e829b11f2fce1fe97c9f5c0c");
        command.setCommit2("d3272b3793bc4b2bc36a1a3a7c8293fcf8fe27df");

        MvcResult result = mvc().perform(get("/projects/1234/metricvalues/deltaTree")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorMessageResponse response = fromJson(result.getResponse().getContentAsString(), ErrorMessageResponse.class);

        Assertions.assertEquals("Project with id 1234 not found.", response.getErrorMessage());
    }
}

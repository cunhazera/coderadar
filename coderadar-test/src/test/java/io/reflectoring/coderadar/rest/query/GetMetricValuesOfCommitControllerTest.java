package io.reflectoring.coderadar.rest.query;

import com.fasterxml.jackson.core.type.TypeReference;
import io.reflectoring.coderadar.analyzer.port.driver.StartAnalyzingCommand;
import io.reflectoring.coderadar.projectadministration.domain.InclusionType;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.create.CreateAnalyzerConfigurationCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.filepattern.create.CreateFilePatternCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.project.create.CreateProjectCommand;
import io.reflectoring.coderadar.query.domain.MetricValueForCommit;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForCommitCommand;
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


class GetMetricValuesOfCommitControllerTest extends ControllerTestTemplate {

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
    void checkMetricsAreCalculatedCorrectlyForCommit() throws Exception {
        GetMetricsForCommitCommand command = new GetMetricsForCommitCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit("d3272b3793bc4b2bc36a1a3a7c8293fcf8fe27df");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/perCommit")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andReturn();

        List<MetricValueForCommit> metricValuesForCommit = fromJson(new TypeReference<List<MetricValueForCommit>>() {},
                result.getResponse().getContentAsString());

        metricValuesForCommit.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(metricValuesForCommit.size(), 4);
        Assertions.assertEquals(0L, metricValuesForCommit.get(0).getValue().longValue());
        Assertions.assertEquals(8L, metricValuesForCommit.get(1).getValue().longValue());
        Assertions.assertEquals(18L, metricValuesForCommit.get(2).getValue().longValue());
        Assertions.assertEquals(15L, metricValuesForCommit.get(3).getValue().longValue());
    }

    @Test
    void checkMetricsAreCalculatedCorrectlyForFirstCommit() throws Exception {
        GetMetricsForCommitCommand command = new GetMetricsForCommitCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit("fd68136dd6489504e829b11f2fce1fe97c9f5c0c");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/perCommit")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andReturn();

        List<MetricValueForCommit> metricValuesForCommit = fromJson(new TypeReference<List<MetricValueForCommit>>() {},
                result.getResponse().getContentAsString());

        metricValuesForCommit.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(metricValuesForCommit.size(), 4);
        Assertions.assertEquals(0L, metricValuesForCommit.get(0).getValue().longValue());
        Assertions.assertEquals(8L, metricValuesForCommit.get(1).getValue().longValue());
        Assertions.assertEquals(12L, metricValuesForCommit.get(2).getValue().longValue());
        Assertions.assertEquals(10L, metricValuesForCommit.get(3).getValue().longValue());
    }

    @Test
    void checkMetricsAreCalculatedCorrectlyForSecondCommit() throws Exception {
        GetMetricsForCommitCommand command = new GetMetricsForCommitCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit("2c37909c99f4518302484c646db16ce1b22b0762");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/perCommit")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andReturn();

        List<MetricValueForCommit> metricValuesForCommit = fromJson(new TypeReference<List<MetricValueForCommit>>() {},
                result.getResponse().getContentAsString());

        metricValuesForCommit.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(metricValuesForCommit.size(), 4);
        Assertions.assertEquals(0L, metricValuesForCommit.get(0).getValue().longValue());
        Assertions.assertEquals(15L, metricValuesForCommit.get(1).getValue().longValue());
        Assertions.assertEquals(27L, metricValuesForCommit.get(2).getValue().longValue());
        Assertions.assertEquals(21L, metricValuesForCommit.get(3).getValue().longValue());
    }

    @Test
    void checkMetricsAreCalculatedCorrectlyForThirdCommit() throws Exception {
        GetMetricsForCommitCommand command = new GetMetricsForCommitCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit("93e1d2a50811e99dd69742ccab2ae3bcaa542243");

        MvcResult result = mvc().perform(get("/projects/" + projectId + "/metricvalues/perCommit")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andReturn();

        List<MetricValueForCommit> metricValuesForCommit = fromJson(new TypeReference<List<MetricValueForCommit>>() {},
                result.getResponse().getContentAsString());

        metricValuesForCommit.sort(Comparator.comparing(MetricValueForCommit::getMetricName));

        Assertions.assertEquals(metricValuesForCommit.size(), 4);
        Assertions.assertEquals(0L, metricValuesForCommit.get(0).getValue().longValue());
        Assertions.assertEquals(15L, metricValuesForCommit.get(1).getValue().longValue());
        Assertions.assertEquals(27L, metricValuesForCommit.get(2).getValue().longValue());
        Assertions.assertEquals(21L, metricValuesForCommit.get(3).getValue().longValue());
    }

    @Test
    void returnsErrorWhenProjectWithIdDoesNotExist() throws Exception {
        GetMetricsForCommitCommand command = new GetMetricsForCommitCommand();
        command.setMetrics(Arrays.asList("coderadar:size:loc:java", "coderadar:size:sloc:java", "coderadar:size:cloc:java", "coderadar:size:eloc:java"));
        command.setCommit("93e1d2a50811e99dd69742ccab2ae3bcaa542243");

        MvcResult result = mvc().perform(get("/projects/1234/metricvalues/perCommit")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(command)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorMessageResponse response = fromJson(result.getResponse().getContentAsString(), ErrorMessageResponse.class);

        Assertions.assertEquals("Project with id 1234 not found.", response.getErrorMessage());
    }
}

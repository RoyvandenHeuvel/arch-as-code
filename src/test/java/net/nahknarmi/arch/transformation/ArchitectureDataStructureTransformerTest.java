package net.nahknarmi.arch.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.DecisionStatus;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import net.nahknarmi.arch.adapter.in.ArchitectureDataStructureReader;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.ImportantTechnicalDecision;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.view.C4ViewContainer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static net.nahknarmi.arch.TestHelper.TEST_PRODUCT_DOCUMENTATION_ROOT_PATH;
import static net.nahknarmi.arch.TestHelper.TEST_SPACES_MANIFEST_PATH;
import static net.nahknarmi.arch.domain.c4.C4Location.INTERNAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ArchitectureDataStructureTransformerTest {
    private static final String PRODUCT_NAME = "TestSpaces";
    private static final String PRODUCT_DESCRIPTION = "TestSpaces is a tool!";

    @Test
    public void should_transform_architecture_yaml_to_structurizr_workspace() throws IOException {
        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        File manifestFile = new File(getClass().getResource(TEST_SPACES_MANIFEST_PATH).getPath());

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader().load(manifestFile);

        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        assertNotNull(workspace);
        assertThat(workspace.getName(), equalTo(PRODUCT_NAME));
        assertThat(workspace.getDescription(), equalTo(PRODUCT_DESCRIPTION));
        assertThat(workspace.getDocumentation().getSections().size(), equalTo(2));
        Model model = workspace.getModel();
        assertThat(model.getPeople().size(), equalTo(4));
        assertThat(model.getSoftwareSystems().size(), equalTo(5));
        Container container = (Container) model.getElement("12"); // "c4://DevSpaces/DevSpaces Web Application"
        assertThat(container, notNullValue());
        assertThat(container.getUrl(), equalTo("https://devspaces.io"));
        Component component = (Component) model.getElement("38"); // "c4://DevSpaces/DevSpaces API/Sign In Controller"
        assertThat(component, notNullValue());
        assertThat(component.getUrl(), equalTo("https://devspaces.io/sign-in"));
    }

    @Test
    public void should_tranform_accept_decision_status() {
        checkStatus(DecisionStatus.Accepted);
    }

    @Test
    public void should_tranform_superseded_decision_status() {
        checkStatus(DecisionStatus.Superseded);
    }

    @Test
    public void should_tranform_deprecated_decision_status() {
        checkStatus(DecisionStatus.Deprecated);
    }

    @Test
    public void should_tranform_rejected_decision_status() {
        checkStatus(DecisionStatus.Rejected);
    }

    @Test
    public void should_tranform_proposed_decision_status() {
        checkStatus(DecisionStatus.Proposed);
    }

    @Test
    public void should_tranform_decision_status_to_default() {
        checkStatus(DecisionStatus.Proposed, "Something invalid");
    }

    private void checkStatus(DecisionStatus decisionStatus) {
        checkStatus(decisionStatus, decisionStatus.name());
    }

    private void checkStatus(DecisionStatus decisionStatus, String statusString) {
        ArchitectureDataStructure dataStructure =
                ArchitectureDataStructure.builder()
                        .name(PRODUCT_NAME)
                        .businessUnit("DevFactory")
                        .description(PRODUCT_DESCRIPTION)
                        .model(buildModel())
                        .views(buildView())
                        .decisions(ImmutableList.of(new ImportantTechnicalDecision("1", new Date(), "title", statusString, "content")))
                        .build();

        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        ArrayList<Decision> decisions = new ArrayList<>(workspace.getDocumentation().getDecisions());
        DecisionStatus result = decisions.get(0).getStatus();

        assertThat(result, equalTo(decisionStatus));
    }

    private C4Model buildModel() {

        return new C4Model(
                ImmutableSet.of(
                        C4Person.builder()
                                .id("1")
                                .alias("@person")
                                .name("person")
                                .description("Foo")
                                .relationships(emptyList()).tags(emptySet()).build()
                ),
                ImmutableSet.of(
                        C4SoftwareSystem.builder()
                                .id("2")
                                .alias("c4://sys")
                                .name("sys")
                                .description("sys")
                                .location(INTERNAL)
                                .tags(emptySet())
                                .relationships(emptyList()).build()
                ),
                emptySet(),
                emptySet()
        );
    }

    private C4ViewContainer buildView() {
        return new C4ViewContainer(
                emptyList(),
                emptyList(),
                emptyList()
        );
    }
}

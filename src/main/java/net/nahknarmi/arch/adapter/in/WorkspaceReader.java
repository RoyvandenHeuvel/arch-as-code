package net.nahknarmi.arch.adapter.in;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.StaticView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static net.nahknarmi.arch.domain.c4.C4Action.*;
import static net.nahknarmi.arch.domain.c4.C4Path.buildPath;

public class WorkspaceReader {

    public ArchitectureDataStructure load(File workspaceFile) throws Exception {
        Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
        ArchitectureDataStructure architectureDataStructure = build(workspace);
        Model model = workspace.getModel();

        C4Model c4Model = new C4Model();
        people(model).forEach(c4Model::addPerson);
        softwareSystems(model).forEach(c4Model::addSoftwareSystem);
        containers(model, c4Model).forEach(c4Model::addContainer);
        components(model, c4Model).forEach(c4Model::addComponent);
        architectureDataStructure.setModel(c4Model);

        C4ViewContainer views = new C4ViewContainer();
        ViewSet workspaceViews = workspace.getViews();
        views.setSystemViews(systemViews(workspaceViews));
        views.setContainerViews(containerViews(workspaceViews));
        views.setComponentViews(componentViews(workspaceViews));
        architectureDataStructure.setViews(views);
        return architectureDataStructure;
    }

    private List<C4SystemView> systemViews(ViewSet views) {
        return views
                .getSystemContextViews()
                .stream()
                .map(systemContextView -> {
                    SoftwareSystem softwareSystem = systemContextView.getSoftwareSystem();
                    C4SystemView c4SystemView = new C4SystemView(softwareSystem.getId(), null);
                    mapCommonViewAttributes(systemContextView, c4SystemView);

                    return c4SystemView;
                })
                .collect(toList());
    }

    private List<C4ContainerView> containerViews(ViewSet views) {
        return views
                .getContainerViews()
                .stream()
                .map(containerView -> {
                    SoftwareSystem softwareSystem = containerView.getSoftwareSystem();
                    C4ContainerView view = new C4ContainerView(softwareSystem.getId(), null);
                    mapCommonViewAttributes(containerView, view);

                    return view;
                })
                .collect(toList());
    }

    private List<C4ComponentView> componentViews(ViewSet views) {
        return views
                .getComponentViews()
                .stream()
                .map(componentView -> {
                    Container container = componentView.getContainer();
                    C4ComponentView view = new C4ComponentView(container.getId(), null);
                    mapCommonViewAttributes(componentView, view);

                    return view;
                })
                .collect(toList());
    }

    private void mapCommonViewAttributes(StaticView view, C4View c4View) {
        c4View.setKey(view.getKey());
        c4View.setName(view.getName());
        c4View.setDescription(view.getDescription());
        c4View.setKey(view.getKey());
        List<C4ViewReference> elements = view.getElements().stream()
                .map(e -> new C4ViewReference(e.getId(), null))
                .collect(toList());

        c4View.setReferences(elements);
    }

    private Set<C4Component> components(Model model, C4Model c4Model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(sys -> sys.getContainers()
                        .stream()
                        .flatMap(cont -> cont.getComponents()
                                .stream()
                                .map(co -> {
                                    C4Path c4Path = buildPath(co);

                                    Set<C4Tag> tags = convertTags(co.getTags());
                                    List<C4Relationship> relationships = mapRelationships(model, co, co.getRelationships());
                                    return C4Component.builder()
                                            .id(co.getId())
                                            .containerId(cont.getId())
                                            .path(c4Path)
                                            .technology(co.toString())
                                            .description(co.getDescription())
                                            .tags(tags)
                                            .name(co.getName())
                                            .relationships(relationships)
                                            .url(co.getUrl())
                                            .build();
                                })

                        )

                ).collect(toSet());
    }

    private Set<C4Container> containers(Model model, C4Model c4Model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(sys -> sys.getContainers().stream().map(c -> {
                    List<C4Relationship> relationships = mapRelationships(model, c, c.getRelationships());
                    Set<C4Tag> tags = convertTags(c.getTags());

                    C4Path path = buildPath(c);
                    return C4Container.builder()
                            .id(c.getId())
                            .systemId(sys.getId())
                            .path(path)
                            .technology(c.getTechnology())
                            .description(c.getDescription())
                            .tags(tags)
                            .name(c.getName())
                            .relationships(relationships)
                            .url(c.getUrl())
                            .build();
                }))
                .collect(toSet());
    }

    private Set<C4SoftwareSystem> softwareSystems(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .map(sys -> {
                    List<C4Relationship> relationships = mapRelationships(model, sys, sys.getRelationships());
                    Set<C4Tag> tags = convertTags(sys.getTags());
                    C4Path path = buildPath(sys);

                    return C4SoftwareSystem.builder()
                            .id(sys.getId())
                            .path(path)
                            .description(sys.getDescription())
                            .location(convertLocation(sys.getLocation()))
                            .tags(tags)
                            .name(sys.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }

    private Set<C4Person> people(Model model) {
        return model
                .getPeople()
                .stream()
                .map(p -> {
                    List<C4Relationship> relationships = mapRelationships(model, p, p.getRelationships());
                    Set<C4Tag> tags = convertTags(p.getTags());
                    C4Path path = buildPath(p);

                    return C4Person.builder()
                            .id(p.getId())
                            .path(path)
                            .description(p.getDescription())
                            .location(convertLocation(p.getLocation()))
                            .tags(tags)
                            .name(p.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }

    private ArchitectureDataStructure build(Workspace workspace) {
        ArchitectureDataStructure result = new ArchitectureDataStructure();
        result.setName(workspace.getName());

        Enterprise enterprise = workspace.getModel().getEnterprise();
        if (enterprise != null) {
            result.setBusinessUnit(enterprise.getName());
        }

        result.setDescription(workspace.getDescription());
        return result;
    }

    private C4Location convertLocation(Location location) {
        return C4Location.valueOf(location.name().toUpperCase());
    }

    private Set<C4Tag> convertTags(String tags) {
        return Arrays.stream(tags.split(",")).map(C4Tag::new).collect(toSet());
    }

    private List<C4Relationship> mapRelationships(Model model, Element fromElement, Set<Relationship> relationships) {
        return relationships
                .stream()
                .map(r -> {
                    String destinationId = r.getDestination().getId();
                    Element element = model.getElement(destinationId);
                    C4Action action = convertAction(fromElement, element);
                    return new C4Relationship(r.getId(), null, action, null, destinationId, r.getDescription(), r.getTechnology());
                }).collect(toList());
    }

    private C4Action convertAction(Element source, Element destination) {
        C4Action action;
        if (source instanceof Person && destination instanceof Person) {
            action = INTERACTS_WITH;
        } else if (destination instanceof Person) {
            action = DELIVERS;
        } else {
            action = USES;
        }
        return action;
    }

}

package net.nahknarmi.arch.generator;

import com.structurizr.model.Element;
import com.structurizr.model.IdGenerator;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public class PathIdGenerator implements IdGenerator {
    @NonNull
    C4Model dataStructureModel;

    @Override
    public String generateId(Element element) {
        C4Path path = buildPath(element);
        String pathString = path.getPath();
        return pathString;
    }

    @Override
    public String generateId(Relationship relationship) {
        String relationshipString = relationship.toString();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hashInBytes = md.digest(relationshipString.getBytes(StandardCharsets.UTF_8));

        // bytes to hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        String id = sb.toString();

        return id;
    }

    @Override
    public void found(String id) {
    }

    private C4Path buildPath_old(Element element) {
        if (element.getParent() == null) {
            String prefix = "c4://";

            if (element instanceof Person) {
                prefix = "@";
            }

            String path = prefix + element.getName().replaceAll("/", "-");
            return new C4Path(path);
        }

        String c4Path = buildPath(element.getParent()).getPath();
        String fullPath = c4Path + "/" + element.getName().replaceAll("/", "-");
        return new C4Path(fullPath);
    }

    private C4Path buildPath(Element element) {
        String className = element.getClass().toString();

        C4Path path;
        switch (className) {
            case "class com.structurizr.model.Person":
                C4Person c4Person = dataStructureModel.getPeople().stream()
                        .filter(p -> p.getName().equals(element.getName()))
                        .findFirst()
                        .get();
                path = c4Person.getPath();
                break;

            case "class com.structurizr.model.SoftwareSystem":
                C4SoftwareSystem c4SoftwareSystem = dataStructureModel.getSystems().stream()
                        .filter(s -> s.getName().equals(element.getName()))
                        .findFirst()
                        .get();
                path = c4SoftwareSystem.getPath();
                break;

            case "class com.structurizr.model.Container":
                C4Container c4Container = dataStructureModel.getContainers().stream()
                        .filter(cont ->
                                cont.getName().equals(element.getName()) &&
                                        element.getParent().getName().equals(cont.getPath().getSystemName())
                        )
                        .findFirst()
                        .get();
                path = c4Container.getPath();
                break;
            case "class com.structurizr.model.Component":
                C4Component c4Component = dataStructureModel.getComponents().stream()
                        .filter(comp ->
                                comp.getName().equals(element.getName()) &&
                                        element.getParent().getName().equals(comp.getPath().getContainerName().get()) &&
                                        element.getParent().getParent().getName().equals(comp.getPath().getSystemName())
                        )
                        .findFirst()
                        .get();
                path = c4Component.getPath();
                break;
            default:
                throw new IllegalStateException("Unsupported element type: " + className);
        }
        return path;
    }
}

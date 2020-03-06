package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Tags;
import com.structurizr.view.Shape;
import com.structurizr.view.Styles;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;

public class StyleViewEnhancer implements WorkspaceEnhancer {

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ViewSet viewSet = workspace.getViews();

        Styles styles = viewSet.getConfiguration().getStyles();
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
        styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person);
        styles.addElementStyle(Tags.CONTAINER).background("#73f275").color("#ffffff");
        styles.addElementStyle("Database").background("#73f275").color("#ffffff").shape(Shape.Cylinder);
        styles.addElementStyle("Folder").background("#73f275").color("#ffffff").shape(Shape.Folder);
        styles.addElementStyle("App").background("#73f275").color("#ffffff").shape(Shape.MobileDevicePortrait);
        styles.addElementStyle("Website").background("#73f275").color("#ffffff").shape(Shape.WebBrowser);
        styles.addElementStyle("Extern").background("#ff8080").color("#ffffff").shape(Shape.WebBrowser);
    }
}

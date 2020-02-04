package net.nahknarmi.arch.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.structurizr.Workspace;
import com.structurizr.util.WorkspaceUtils;
import net.nahknarmi.arch.adapter.WorkspaceConverter;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "import", description = "Imports existing struturizr workspace")
public class ImportCommand implements Callable<Integer> {
    private static final Log logger = LogFactory.getLog(ImportCommand.class);

    @CommandLine.Parameters(index = "0", paramLabel = "EXPORTED_WORKSPACE", description = "Exported structurizr workspace location.", defaultValue = "./")
    private File exportedWorkspacePath;

    // Only for testing purposes
    public ImportCommand(File exportedWorkspacePath) {
        this.exportedWorkspacePath = exportedWorkspacePath;
    }

    public ImportCommand() {
    }

    @Override
    public Integer call() throws Exception {
        Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(exportedWorkspacePath);
        ArchitectureDataStructure dataStructure = new WorkspaceConverter().convert(workspace);

        File tempFile = File.createTempFile("arch-as-code", ".yml");
        new ObjectMapper(new YAMLFactory()).writeValue(tempFile, dataStructure);
        logger.info(String.format("Architecture data structure written to - %s", tempFile.getAbsolutePath()));

        return new PublishCommand(tempFile.getParentFile(), tempFile.getName()).call();
    }
}

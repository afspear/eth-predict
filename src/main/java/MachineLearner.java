import org.encog.Encog;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;
import org.encog.util.simple.TrainingSetUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MachineLearner {
    public static void main(String[] args) {
        if(!Files.exists(Paths.get("allData.csv")))
            return;

        File sourceFile = new File("allData.csv");
        File sourceFileNormal = new File("allDataNormal.csv");

        EncogAnalyst analyst = new EncogAnalyst();
        AnalystWizard wizard = new AnalystWizard(analyst);
        wizard.wizard(sourceFile, true, AnalystFileFormat.DECPNT_COMMA);



        final AnalystNormalizeCSV norm = new AnalystNormalizeCSV();
        norm.analyze(sourceFile, true, CSVFormat.ENGLISH, analyst);
        norm.setProduceOutputHeaders(true);
        norm.normalize(sourceFileNormal);

        MLDataSet trainingSet = TrainingSetUtil.loadCSVTOMemory(CSVFormat.ENGLISH, "allDataNormal.csv", true, 11, 1);
        BasicNetwork network = EncogUtility.simpleFeedForward(11, 3, 2,1, true);

        System.out.println("Training Network...");
        EncogUtility.trainToError(network, trainingSet, 0.10);

        System.out.println();
        System.out.println("Evaluating Network...");
        EncogUtility.evaluate(network, trainingSet);
        Encog.getInstance().shutdown();
    }
}

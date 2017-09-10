
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MachineLearner {
    public static void main(String[] args) throws  Exception {
        new MachineLearner().runNetwork();
    }

    private void runNetwork() throws Exception{
        //First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
        int numLinesToSkip = 1;
        char delimiter = ',';
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        recordReader.initialize(new FileSplit(new File("dataWithIndicators.csv")));

        //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
        int labelIndex = 0;     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
        int batchSize = 1300;    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)

        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, 0, 0, true);
        DataNormalization normalization = new NormalizerStandardize();
        normalization.fitLabel(true);
        normalization.fit(iterator);
        iterator.setPreProcessor(normalization);

        final int numInputs = iterator.inputColumns();
        int outputNum = 1;
        int iterations = 900;
        long seed = 6;

        int numOutputs = 1;
        final int numHiddenNodes = numInputs;
        double learningRate = 0.01;

        Logger.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
          .seed(seed)
          .iterations(iterations)
          .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT)
          .learningRate(learningRate)
          .weightInit(WeightInit.XAVIER)
          .updater(Updater.NESTEROVS)     //To configure: .updater(new Nesterovs(0.9))
          .list()
          .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
            .activation(Activation.TANH).build())
          .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
            .activation(Activation.TANH).build())
          .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
            .activation(Activation.IDENTITY)
            .nIn(numHiddenNodes).nOut(numOutputs).build())
          .pretrain(false).backprop(true).build();

        List<Double> resultsFromRun = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            resultsFromRun.add(runModel(conf, iterator, normalization));
            iterator.reset();
        }
        System.out.println(resultsFromRun);
        resultsFromRun.stream().mapToDouble(value -> value).average().ifPresent(value -> System.out.println(value));

    }
    private Double runModel(MultiLayerConfiguration conf, DataSetIterator iterator, DataNormalization normalization) {


        //run the model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));


        for (int i = 0; i < 30; i++) {

            if (!iterator.hasNext())
                continue;


            DataSet allData = iterator.next();
            allData.shuffle();
            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.80);  //Use 65% of data for training

            DataSet trainingData = testAndTrain.getTrain();
            DataSet testData = testAndTrain.getTest();


            model.fit(trainingData);

            //evaluate the model on the test set
            RegressionEvaluation eval = new RegressionEvaluation();
            INDArray output = model.output(testData.getFeatures());
            //System.out.println(testData.getFeatures());
            //System.out.println(testData.getLabels());
            //System.out.println(output);
            eval.eval(testData.getLabels(), output);
            Logger.info(eval.stats());

        }

        final INDArray input = Nd4j.create(new double[]{315.83941666999999142717570066452d,324.27787045999997417311533354223d,315.00999999999999090505298227072d,323.45978999999999814463080838323d,320.91588681999998774093304139873d,1.0236069303797468295716164822254d,302.71831034625000000914951669984d,319.78533600350000085654755821452d,310.68440970393228173482500155415d,320.21852677165183371103415747089d,-4.0523709112417257322810211602265d,9.4618578680203039406534038521929d,43.232503647104144582724730031948d,-45.711740400275473706802231010008d,16.947576691631095404088531079855d,20.440754118821267582006839802489d});

        normalization.transform(input);
        INDArray out = model.output(input, false);
        normalization.revertLabels(out);

        System.out.println("input=" + input);
        System.out.println("output=" + out);

        return out.getDouble(0);


    }

}





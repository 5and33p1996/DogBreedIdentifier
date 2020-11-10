package com.sandeept.doge;

import android.app.Activity;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Classifier {

    private Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private Interpreter.Options tfliteOptions;

    private List<String> labels;

    private TensorImage inputImageBuffer;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;

    private int imgHeight;
    private int imgWidth;

    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 1.0f;
    private static final float IMAGE_MEAN = 0f;
    private static final float IMAGE_STD = 255f;

    Classifier() {

        tfliteModel = null;
        tflite = null;
        tfliteOptions = new Interpreter.Options();
    }

    void Initialize(Activity activity) throws IOException{

        tfliteModel = FileUtil.loadMappedFile(activity, "model.tflite");
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        labels = FileUtil.loadLabels(activity, "labels.txt");

        int[] inputShape = tflite.getInputTensor(0).shape();
        imgHeight = inputShape[1];
        imgWidth = inputShape[2];
        DataType inputDataType = tflite.getInputTensor(0).dataType();

        int[] outputShape = tflite.getOutputTensor(0).shape();
        DataType outputDataType = tflite.getOutputTensor(0).dataType();

        inputImageBuffer = new TensorImage(inputDataType);

        outputProbabilityBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        probabilityProcessor = new TensorProcessor.Builder().add(getPostProcessNormalizeOp()).build();
    }

    HashMap<String, Float> predict(Bitmap bitmap){

        inputImageBuffer = loadImage(bitmap);

        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());

        Map<String, Float> labeledProbability = new TensorLabel(labels,
                outputProbabilityBuffer).getMapWithFloatValue();

        List<Map.Entry<String, Float>> list = new LinkedList<>(labeledProbability.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> stringFloatEntry, Map.Entry<String, Float> t1) {
                return (t1.getValue()).compareTo(stringFloatEntry.getValue());
            }
        });

        HashMap<String, Float> sortedList = new LinkedHashMap<>();

        for(Map.Entry<String, Float> a : list){
            sortedList.put(a.getKey(), a.getValue());
        }

        return sortedList;
    }

    private TensorImage loadImage(Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        //.add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imgHeight, imgWidth, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        //.add(getPreProcessNormalizeOp())
                        .build();

        return imageProcessor.process(inputImageBuffer);
    }

    private TensorOperator getPreProcessNormalizeOp(){

        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    private TensorOperator getPostProcessNormalizeOp(){

        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    void Finalize(){

        if(tflite != null){

            tflite.close();
            tflite = null;
        }

        tfliteModel = null;
    }
}

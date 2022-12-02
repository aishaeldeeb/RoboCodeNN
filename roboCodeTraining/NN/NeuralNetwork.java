package roboCodeTraining.NN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NeuralNetwork {

    Matrix weights_ih, weights_ho, bias_h, bias_o,weightChange_ih,weightChange_ho;
    double l_rate = 0.001;

    double momentumTerm = 0.9;

    public NeuralNetwork(int i, int h, int o, double mTerm) {
        weights_ih = new Matrix(h, i);
        weights_ho = new Matrix(o, h,0);
        weightChange_ih= new Matrix(h, i,0);
        this.momentumTerm = mTerm;
        weightChange_ho=new Matrix(o, h);
        bias_h = new Matrix(h, 1, true);
        bias_o = new Matrix(o, 1, true);
    }

    public List<Double> predict(double[] X) {
        Matrix input = Matrix.fromArray(X);
        Matrix hidden = Matrix.multiply(weights_ih, input);
        hidden.add(bias_h);
        hidden.customSigmoid();
//        if (Main.isBipolar) {
//            hidden.customSigmoid();
//        } else {
//            hidden.sigmoid();
//        }


        Matrix output = Matrix.multiply(weights_ho, hidden);
        output.add(bias_o);
        output.customSigmoid();
//        if (Main.isBipolar) {
//            output.customSigmoid();
//        } else {
//            output.sigmoid();
//        }


        return output.toArray();
    }

    public double calculateTotalError(List<Double> actualoutputs, double[] expectedoutputs) {
        double error = 0.0;
        for (int i = 0; i < expectedoutputs.length; i++) {
            error += Math.pow(expectedoutputs[i] - actualoutputs.get(i), 2.0);
        }
        error = error * 0.5;
        return error;
    }

    public void fit(double[][] X, double[] Y) {
        int epoch = 1;
        ArrayList<double[]> errorVsEpoch = new ArrayList<>();
        double totalError = -0.001;
        List<Double> actualoutputs = new ArrayList<>();
        List<Double> expectedoutputs;
//        expectedoutputs = Arrays.asList(-1.0,1.0,1.0,-1.0);
//        if (Main.isBipolar) {
//            expectedoutputs = Arrays.asList(-1.0,1.0,1.0,-1.0);
//        } else {
//            expectedoutputs = Arrays.asList(0.0, 1.0, 1.0, 0.0);
//        }

        do {
            for (int i = 0; i < X.length; i++) {
                this.train(X[i], new double[]{Y[i]});
                List<Double> x = predict(X[i]);
                actualoutputs.add(x.get(0));

            }
//            double[] actualoutputsArr = new double[actualoutputs.size()];
//            actualoutputsArr = actualoutputs.toArray();
            totalError = calculateTotalError(actualoutputs, Y);
            System.out.println("Total Error at Epoch no: " + epoch + "= " + totalError);
            errorVsEpoch.add(new double[]{totalError, epoch});

            epoch++;
            actualoutputs.clear();
        } while (totalError > 0.05);


        try {
            PlotErrorVsEpoch(errorVsEpoch);
        } catch(IOException e){
            System.out.println("IOExpection occured");
        }
    }

    public void train(double[] X, double[] Y) {
        Matrix input = Matrix.fromArray(X);
        Matrix hidden = Matrix.multiply(weights_ih, input);
        hidden.add(bias_h);
        hidden.customSigmoid();
//        if (Main.isBipolar) {
//            hidden.customSigmoid();
//        }else {
//            hidden.sigmoid();
//        }


        Matrix output = Matrix.multiply(weights_ho, hidden);
        output.add(bias_o);
        output.customSigmoid();
//        if (Main.isBipolar) {
//            output.customSigmoid();
//        }else{
//            output.sigmoid();
//        }


        Matrix target = Matrix.fromArray(Y);

        Matrix error = Matrix.subtract(target, output);
        Matrix gradient;
        gradient = output.dCustomsigmoid();
//        if (Main.isBipolar) {
//            gradient = output.dCustomsigmoid();
//        }else{
//            gradient = output.dsigmoid();
//        }

        gradient.multiply(error);
        gradient.multiply(l_rate);

        Matrix hidden_T = Matrix.transpose(hidden);
        Matrix who_delta = Matrix.multiply(gradient, hidden_T);
        weightChange_ho.multiply(momentumTerm);
        weights_ho.add(who_delta);
        weights_ho.add(weightChange_ho);

        weightChange_ho.add(who_delta);

        bias_o.add(gradient);

        Matrix who_T = Matrix.transpose(weights_ho);
        Matrix hidden_errors = Matrix.multiply(who_T, error);
        Matrix h_gradient;
        h_gradient = hidden.dCustomsigmoid();
//        if (Main.isBipolar) {
//            h_gradient = hidden.dCustomsigmoid();
//        }else{
//            h_gradient = hidden.dsigmoid();
//        }

        h_gradient.multiply(hidden_errors);
        h_gradient.multiply(l_rate);

        Matrix i_T = Matrix.transpose(input);
        Matrix wih_delta = Matrix.multiply(h_gradient, i_T);
        weightChange_ih.multiply(momentumTerm);
        weights_ih.add(wih_delta);
        weights_ih.add(weightChange_ih);

        weightChange_ih.add(wih_delta);
        weights_ih.add(wih_delta);
        bias_h.add(h_gradient);

    }
    public void PlotErrorVsEpoch(ArrayList<double[]> errorVsEpoch) throws IOException {
        //convert arrayList to 2 arrays
        int size = errorVsEpoch.size();
        double  [] errors = new double[size];
        double  [] epochs = new double[size];

        for (int i = 0 ; i < errorVsEpoch.size(); i++){
            errors[i] = errorVsEpoch.get(i)[0];
            epochs[i] = errorVsEpoch.get(i)[1];

        }

        // configuring plot options
        Plot plot = Plot.plot(Plot.plotOpts().
                        title("Error vs. Epoch").
                        legend(Plot.LegendFormat.BOTTOM)).
                xAxis("Epoch", Plot.axisOpts().
                        range(0, 200)).
                yAxis("Error", Plot.axisOpts().
                        range(0, 3)).
                series("Data", Plot.data().
                                xy(epochs, errors),
                        Plot.seriesOpts().
                                marker(Plot.Marker.CIRCLE));

        plot.save("sample_data", "png");


    }

}
package com.example.demo2;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class Filter extends Application {
    public static final double LEFT_VALUE = 0;
    public static double RIGHT_VALUE = 1;
    public static final Font FONT = new Font(20);
    private static final double highFrequency = 200;
    private  static final double T0 = 1 / (2*highFrequency);

    private XYChart.Series<Number,Number> filterSeries;
    private XYChart.Series<Number,Number> series;
    private XYChart.Series<Number,Number> seriesACH;

    private TextField amplitudeTextField;
    private TextField tauTextField;
    private TextField frequencyTextField;

    private double tau;
    private double amplitude;

    @Override
    public void start(Stage primaryStage)  {
       createGui(primaryStage);
    }

    private void createGui(Stage stage) {
        stage.setTitle("ФНЧ 1-го порядка");
        AnchorPane pane = new AnchorPane();
        Label amplitudeLabel = new Label("Амплитуда");
        amplitudeLabel.setFont(FONT);
        amplitudeLabel.setLayoutX(10);
        amplitudeLabel.setLayoutY(900);
        pane.getChildren().add(amplitudeLabel);
        amplitudeTextField = new TextField();
        amplitudeTextField.setText("1");
        amplitudeTextField.setFont(FONT);
        amplitudeTextField.setLayoutX(130);
        amplitudeTextField.setLayoutY(900);
        pane.getChildren().add(amplitudeTextField);

        Label tayLabel = new Label("Постоянная времени");
        tayLabel.setFont(FONT);
        tayLabel.setLayoutX(390);
        tayLabel.setLayoutY(900);
        pane.getChildren().add(tayLabel);

        tauTextField = new TextField();
        tauTextField.setFont(FONT);
        tauTextField.setText("1");
        tauTextField.setLayoutX(600);
        tauTextField.setLayoutY(900);
        pane.getChildren().add(tauTextField);

        Label frequencyLabel = new Label("Частота: ");
        frequencyLabel.setFont(FONT);
        frequencyLabel.setLayoutX(860);
        frequencyLabel.setLayoutY(900);
        pane.getChildren().add(frequencyLabel);

        frequencyTextField = new TextField();
        frequencyTextField.setFont(FONT);
        frequencyTextField.setText("1");
        frequencyTextField.setLayoutX(950);
        frequencyTextField.setLayoutY(900);
        pane.getChildren().add(frequencyTextField);

        Button redrawButton = new Button("Нарисовать выходной сигнал");
        redrawButton.setFont(FONT);
        redrawButton.setLayoutX(1300);
        redrawButton.setLayoutY(900);
        redrawButton.setOnMouseClicked(event -> redraw());
        pane.getChildren().add(redrawButton);

        LineChart<Number, Number> chart = createChart();
        chart.setLayoutX(50);
        chart.setLayoutY(10);
        pane.getChildren().add(chart);

        Button ACHButton = new Button("АЧХ");
        ACHButton.setFont(FONT);
        ACHButton.setLayoutX(1650);
        ACHButton.setLayoutY(900);
        ACHButton.setOnMouseClicked(mouseEvent -> calculateACH() );
        pane.getChildren().add(ACHButton);

        Button FCHButton = new Button("ФЧХ");
        FCHButton.setFont(FONT);
        FCHButton.setLayoutX(1750);
        FCHButton.setLayoutY(900);
        FCHButton.setOnMouseClicked(mouseEvent -> calculateACH() );
        pane.getChildren().add(FCHButton);

        stage.setScene(new Scene(pane));
        stage.setMaximized(true);
        stage.show();
    }

    private void redraw() {
        try {
            amplitude = Double.parseDouble(amplitudeTextField.getText().replace(",", "."));
            tau = Double.parseDouble(tauTextField.getText().replace(",", "."));
            double frequency = Double.parseDouble(frequencyTextField.getText().replace(",", "."));
            RIGHT_VALUE = 50 * tau;
            List<XYChart.Data<Number, Number>> filterDataSeries = new ArrayList<>();
            List<XYChart.Data<Number, Number>> dataSeries = new ArrayList<>();
            double lastFilterY = 1;
            for (double x = LEFT_VALUE; x < RIGHT_VALUE; x = x + T0) {
                double curY = sinus(x,amplitude,frequency);
                dataSeries.add(new XYChart.Data<>(x, curY));
                double filterY = filter(curY, lastFilterY);
                lastFilterY = filterY;
                filterDataSeries.add(new XYChart.Data<>(x, filterY));
            }
            filterSeries.getData().clear();
            series.getData().clear();
            seriesACH.getData().clear();
            filterSeries.getData().addAll(filterDataSeries);
            series.getData().addAll(dataSeries);
        }
        catch (NumberFormatException e)
        {
            System.out.println(e.getCause().toString());
        }

        }


    private void calculateACH()
    {
        filterSeries.getData().clear();
        series.getData().clear();
        seriesACH.getData().clear();
        tau = Double.parseDouble(tauTextField.getText().replace(",","."));
        for (double frequency = 0.001; frequency < 2000; frequency *= 10)
        {
            RIGHT_VALUE = Math.max(100*tau, 5 * (2 * Math.PI/frequency));
            ObservableList<XYChart.Data<Number,Number>> filterDataSeries = FXCollections.observableArrayList();
            double lastFilterY = 1;
            for (double x = LEFT_VALUE;x<RIGHT_VALUE;x=x+T0)
            {
                double curY = sinus(x, amplitude, frequency);
                double filterY = filter(curY, lastFilterY);
                lastFilterY = filterY;
                filterDataSeries.add(new XYChart.Data<>(x,filterY));

            }
            int size = filterDataSeries.size();
            double filterMinY = filterDataSeries.get(size-1).getYValue().doubleValue();
            double filterMaxY = filterDataSeries.get(size-1).getYValue().doubleValue();
            for (int i = size-1;i>=size-size/4;i--)
            {
                double y = filterDataSeries.get(i).getYValue().doubleValue();
                if(y>filterMaxY)
                {
                    filterMaxY = y;
                }
                if(y<filterMinY)
                {
                    filterMinY = y;
                }
            }
            double filterAmplitude = (filterMaxY - filterMinY)/2;
            seriesACH.getData().add(new XYChart.Data<>(Math.log10(frequency),filterAmplitude/amplitude));

        }
    }
    private double sinus(double x,double amplitudeSinus, double frequencySinus)
    {
        return amplitudeSinus*Math.sin(frequencySinus*x);
    }
    private  LineChart<Number,Number> createChart()
    {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setAnimated(false);

        series = new XYChart.Series<>();
        series.setName("Входной сигнал");
        filterSeries = new XYChart.Series<>();
        filterSeries.setName("Выходной сигнал");
        seriesACH = new XYChart.Series<>();
        seriesACH.setName("АЧХ");

        redraw();
        lineChart.getData().add(series);
        lineChart.getData().add(filterSeries);
        lineChart.getData().add(seriesACH);
        lineChart.setCreateSymbols(false);
        lineChart.setPrefSize(1900,700);
        return lineChart;
    }
    Double filter(double inputY, double lastY)
    {
        double a = Math.pow(Math.E,-T0/tau);
        double b = T0/tau;
        return a*lastY+b*inputY;
    }

}
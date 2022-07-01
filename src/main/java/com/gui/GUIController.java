package com.gui;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

import static java.lang.Thread.sleep;

public class GUIController {
    @FXML
    public Button submitOrderButton;
    @FXML
    public AnchorPane pane;
    public static AnchorPane _pane;
    @FXML
    private Label welcomeText;
    @FXML
    private Canvas canvas;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    public static AgentNode bigBossNode;
    public static Map<AgentNode, ArrayList<AgentNode>> managersMachines;

    private int agentNum;
    private int machinesNum;

    @FXML
    public void initialize(){
        var gc = canvas.getGraphicsContext2D();
        gc.setFont(new Font(gc.getFont().toString(), 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        _pane = pane;
        agentNum = 6;
        machinesNum = 4;

        //Drawing big boss node
        bigBossNode = new AgentNode(canvas.getWidth()/2, 100, 100, 50,
                                        "Big Boss", Color.rgb(122, 21, 21));
        bigBossNode.draw(gc);

        //Drawing manager nodes
        managersMachines = new HashMap<>();
        for (int i = 0; i < agentNum; i++){
            AgentNode managerNode = new AgentNode((canvas.getWidth()/(agentNum + 1)) * (i+1), 200, 100,
                    50, "Manager#" + (i+1), Color.rgb(21, 122, 41));
            managersMachines.put(managerNode, null);
            managerNode.draw(gc);
        }

        //Drawing machine nodes
        managersMachines.keySet().forEach(manager -> {
            ArrayList<AgentNode> machineNodeList = new ArrayList<>();
            for (int i = 0; i < machinesNum; i++) {
                AgentNode machineNode = new AgentNode(manager.getX(), manager.getY() + (i + 1) * 100, 100,
                        50, "Machine#" + (i + 1), Color.rgb(21, 93, 122));
                machineNodeList.add(machineNode);
                machineNode.draw(gc);
            }
            managersMachines.put(manager, machineNodeList);
        });

        //Creating dialog form for orders submission
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Submit your Order");
        dialog.setHeaderText("Order details");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox productsComboBox = new ComboBox();
        productsComboBox.getItems().addAll(
                "A",
                "B",
                "C"
        );
        productsComboBox.setValue("A");
        TextField productQuantityInput = new TextField();
        formatToNumeric(productQuantityInput);
        TextField deadlineInput = new TextField();
        formatToNumeric(deadlineInput);


        grid.add(new Label("Product:"), 0, 0);
        grid.add(productsComboBox, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(productQuantityInput, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlineInput, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                List<String> orderDetails = new ArrayList<>();
                orderDetails.add(productsComboBox.getValue().toString());
                orderDetails.add(productQuantityInput.getText());
                orderDetails.add(deadlineInput.getText());
                return orderDetails;
            }
            return null;
        });

        submitOrderButton.setOnAction(event -> {
            createOrder(dialog);
        });

        startAgentPlatform();
    }

    private void formatToNumeric (TextField input){
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                input.setText(newValue.replaceAll("\\D", ""));
            }
        });
    }

    private void createOrder(Dialog<List<String>> dialog){
        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(order -> {
            System.out.println("Product: " + order.get(0) + ", Quantity: " + order.get(1) + ", Deadline: " + order.get(2));
        });
    }

    private void startAgentPlatform(){
        Runtime myRuntime = Runtime.instance();

        // prepare the settings for the platform that we're going to start
        Profile myProfile = new ProfileImpl();

        // create the com container
        ContainerController myContainer = myRuntime.createMainContainer(myProfile);

        try {
            AgentController simulationAgent = myContainer.createNewAgent(
                    "SimulationAgent",
                    "com.agents.SimulationAgent",
                    new Object[]{}
            );
            simulationAgent.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }

    }

    public static void moveAtoB(Pair<Double, Double> A, Pair<Double, Double> B){
        final Circle rectPath = new Circle(A.getKey(), A.getValue(), 20);
        rectPath.setFill(Color.RED);
        Path path = new Path();
        path.getElements().add(new MoveTo(A.getKey(),A.getValue()));
        path.getElements().add(new LineTo(B.getKey(),B.getValue()));
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(1000));
        pathTransition.setPath(path);
        pathTransition.setNode(rectPath);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(1);
        pathTransition.setInterpolator(Interpolator.LINEAR);
        pathTransition.setOnFinished(e -> _pane.getChildren().remove(rectPath));
        pathTransition.play();
        _pane.getChildren().add(rectPath);
    }

    public static void bigBossMsg(Agent A, String receiverType, AID receiverID){
        String receiverName = receiverID.getLocalName();
        switch (receiverType){
            case "manager":
                for (var manager : managersMachines.keySet()){
                    if (manager.text.equals(receiverName))
                        moveAtoB(new Pair<>(bigBossNode.getX(), bigBossNode.getY()),
                                 new Pair<>(manager.getX(), manager.getY()));
                }
                break;
            case "client":

                break;
        }

    }

    public static void managerMsg(Agent A, String receiverType, AID receiverID){
        switch (receiverType){
            case "bigBoss":
                for (var manager : managersMachines.keySet()){
                    if (manager.text.equals(A.getLocalName()))
                        moveAtoB(new Pair<>(manager.getX(), manager.getY()),
                                new Pair<>(bigBossNode.getX(), bigBossNode.getY()));
                }
                break;
            case "machine":

                break;
        }

    }


}
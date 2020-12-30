import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.control.*;


public class GUI extends Application{

    public void initStage(String dbChoice){
        //pass the db parameter to gui
        launch(dbChoice);
    }

    @Override
    public void start (Stage stage){
        //read the db parameter and initialize db connection based on it
        Parameters params = getParameters();
        String dbChoice = params.getUnnamed().get(0);
        DatabaseConnection dbConn = new DatabaseConnection(dbChoice);

        //building some of the ui elements
        Group mainGroup = new Group();
        HBox menuHBox = createMenuHBox();
        VBox leftMenu = new VBox(5f);
        ListView<String> resultsList = new ListView<>();
        resultsList.setPrefSize(250f, 510f);
        Label dbLabel = new Label("Using " + dbChoice + " connection.");
        leftMenu.getChildren().addAll(resultsList, dbLabel);
        leftMenu.setLayoutX(15f);
        leftMenu.setLayoutY(60f);

        Line vertLine = new Line(275f,57f,275f,578f);

        //call methods that create VBoxes
        VBox labelsVBox = createLabelsVbox();
        VBox fieldsVBox = createFieldsVbox();

        //set VBox positions
        labelsVBox.setLayoutX(300f);
        labelsVBox.setLayoutY(80f);
        fieldsVBox.setLayoutX(390f);
        fieldsVBox.setLayoutY(74f);

        //assign some of the controls to variables for use in events
        Node viewAllButton = menuHBox.getChildren().get(0);
        Node searchButton = menuHBox.getChildren().get(1);
        Node addButton = menuHBox.getChildren().get(2);
        Node exitButton = menuHBox.getChildren().get(3);
        HBox lowerButtons = (HBox)fieldsVBox.getChildren().get(8);
        HBox saveCancel = (HBox)lowerButtons.getChildren().get(0);
        HBox saveChangesDelete = (HBox)lowerButtons.getChildren().get(1);
        Node saveButton = saveCancel.getChildren().get(0);
        Node cancelButton = saveCancel.getChildren().get(1);
        Node saveChangesButton = saveChangesDelete.getChildren().get(0);
        Node deleteButton = saveChangesDelete.getChildren().get(1);

        //gather text input fields to a separate list
        List<TextInputControl> textFields = new ArrayList<>();
        fieldsVBox.getChildren().forEach(node -> {
            if (node instanceof TextInputControl) {
                textFields.add((TextInputControl) node);
            }
        });

        //event: call viewall() method, fetch everything from db
        //some ui elements visibility is toggled depending on result
        viewAllButton.setOnMouseClicked((mouseEvent -> saveChangesDelete.setVisible(viewAll(resultsList, dbConn, textFields))));

        //event: when clicking results list, id is separated from string and by that id results are fetched from db
        //text fields are filled by fillTextFields() method
        resultsList.setOnMouseClicked((mouseEvent -> {
            if (resultsList.getSelectionModel().getSelectedItem() != null) {
                Object selectedPerson = resultsList.getSelectionModel().getSelectedItem();
                String[] selectedIdArr = selectedPerson.toString().split(":");
                Person resultPerson = dbConn.searchById(Integer.parseInt(selectedIdArr[0]));
                if (resultPerson != null) {
                    fillTextFields(textFields, resultPerson);
                }
            }
        }));

        //event: search dialog is shown, and a db search is made based on the search string from user input
        //if there are results, first result is selected and text fields are filled by fillTextFields() method
        //some ui elements visibility is toggled depending on result
        searchButton.setOnMouseClicked(mouseEvent -> {
            TextInputDialog searchDialog = new TextInputDialog();
            List<Person> personsList;
            searchDialog.setGraphic(null);
            searchDialog.setTitle("Search");
            searchDialog.setHeaderText("Enter name or part of a name:");
            Optional<String> searchTerm = searchDialog.showAndWait();
            if (searchTerm.isPresent()){
                personsList = dbConn.readDatabase(searchTerm.get());
                ObservableList<String> resultNames = FXCollections.observableArrayList();
                for (Person person: personsList){
                    resultNames.add(person.getPerson_id() + ": " + person.getName());
                }
                resultsList.setItems(resultNames);
                saveChangesDelete.setVisible(true);
            } else {
                return;
            }
            if (!resultsList.getItems().isEmpty()) {
                resultsList.getSelectionModel().selectFirst();
                fillTextFields(textFields, personsList.get(0));
            } else {
                clearTextFields(textFields);
                saveChangesDelete.setVisible(false);
            }
        });

        //event: some ui elements are disabled when beginning to add a new person
        //some ui elements visibility is toggled
        //resultsList is emptied
        addButton.setOnMouseClicked(mouseEvent -> {
            saveChangesDelete.setVisible(false);
            saveCancel.setVisible(true);
            viewAllButton.setDisable(true);
            searchButton.setDisable(true);
            addButton.setDisable(true);
            fieldsVBox.getChildren().get(9).setVisible(true);
            clearTextFields(textFields);
            resultsList.setItems(null);
            textFields.get(0).setText("ID will be assigned by database");
            textFields.get(1).requestFocus();
        });

        //event: discards all user input and ui elements go back to "normal" state
        cancelButton.setOnMouseClicked(mouseEvent -> {
            saveCancel.setVisible(false);
            viewAllButton.setDisable(false);
            searchButton.setDisable(false);
            addButton.setDisable(false);
            fieldsVBox.getChildren().get(9).setVisible(false);
            saveChangesDelete.setVisible(viewAll(resultsList, dbConn, textFields));
        });

        //event: user input (name and year) are validated by validateNameYearInput() method
        //if everything is ok, try to insert the new person into db
        //ui elements are set back to "normal" state
        saveButton.setOnMouseClicked(mouseEvent -> {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION,
                    "New person successfully added.", ButtonType.OK);
            Alert failAlert = new Alert(Alert.AlertType.ERROR,
                    "Adding person failed.", ButtonType.OK);

            if (validateNameYearInput(textFields)) {
                Person newPerson = new Person(textFields.get(1).getText(), Integer.parseInt(textFields.get(4).getText()),
                        textFields.get(2).getText(), textFields.get(3).getText(),
                        textFields.get(5).getText(), textFields.get(6).getText(),
                        textFields.get(7).getText());
                if (dbConn.insertDatabase(newPerson)) {
                    successAlert.showAndWait();
                } else {
                    failAlert.showAndWait();
                    return;
                }
                saveCancel.setVisible(false);
                viewAllButton.setDisable(false);
                searchButton.setDisable(false);
                addButton.setDisable(false);
                fieldsVBox.getChildren().get(9).setVisible(false);
                saveChangesDelete.setVisible(viewAll(resultsList, dbConn, textFields));
            }
        });

        //event: user input (name and year) are validated by validateNameYearInput() method
        //if everything is ok, try to update the persons information in db
        //ui elements are set back to "normal" state and current person in resultsList is refreshed by refreshCurrent()
        saveChangesButton.setOnMouseClicked(mouseEvent -> {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION,
                    "Person successfully updated.", ButtonType.OK);
            Alert failAlert = new Alert(Alert.AlertType.ERROR,
                    "Updating person failed.", ButtonType.OK);

            if (validateNameYearInput(textFields)){
                Person updatedPerson = new Person(textFields.get(1).getText(), Integer.parseInt(textFields.get(4).getText()),
                        textFields.get(2).getText(), textFields.get(3).getText(),
                        textFields.get(5).getText(), textFields.get(6).getText(),
                        textFields.get(7).getText(), Integer.parseInt(textFields.get(0).getText()));
                if (dbConn.updateDatabase(updatedPerson)){
                    successAlert.showAndWait();
                } else {
                    failAlert.showAndWait();
                    return;
                }
                saveCancel.setVisible(false);
                viewAllButton.setDisable(false);
                searchButton.setDisable(false);
                addButton.setDisable(false);
                fieldsVBox.getChildren().get(9).setVisible(false);
                refreshCurrent(resultsList, textFields,0);
            }
        });

        //event: confirmation dialog about deleting is shown to user
        //if OK is pressed, try to delete the person from db based on id number of selected person
        //resultsList is refreshed based on if person was deleted
        deleteButton.setOnMouseClicked(mouseEvent -> {
            Alert areYouSure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION,
                    "Person successfully deleted.", ButtonType.OK);
            Alert failAlert = new Alert(Alert.AlertType.ERROR,
                    "Deleting person failed.", ButtonType.OK);

            int idToDelete = Integer.parseInt(textFields.get(0).getText());
            Optional<ButtonType> result = areYouSure.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES){
                if (dbConn.deleteDatabase(idToDelete)){
                    successAlert.showAndWait();
                } else {
                    failAlert.showAndWait();
                }
            } else {
                return;
            }
            refreshCurrent(resultsList,textFields,1);
            clearTextFields(textFields);
            saveChangesDelete.setVisible(false);
            resultsList.getSelectionModel().clearSelection();
        });

        //event: exit with status 0
        exitButton.setOnMouseClicked(mouseEvent -> System.exit(0));

        //event: length of input in text fields is limited by limitField()
        textFields.get(0).setOnKeyPressed(keyEvent -> limitField(textFields.get(0), 10));
        textFields.get(1).setOnKeyPressed(keyEvent -> limitField(textFields.get(1), 30));
        textFields.get(2).setOnKeyPressed(keyEvent -> limitField(textFields.get(2), 50));
        textFields.get(3).setOnKeyPressed(keyEvent -> limitField(textFields.get(3), 30));
        textFields.get(4).setOnKeyPressed(keyEvent -> limitField(textFields.get(4), 4));
        textFields.get(5).setOnKeyPressed(keyEvent -> limitField(textFields.get(5), 255));
        textFields.get(6).setOnKeyPressed(keyEvent -> limitField(textFields.get(6), 255));
        textFields.get(7).setOnKeyPressed(keyEvent -> limitField(textFields.get(7), 255));

        //create the scene and show the stage
        Scene scene1 = new Scene(mainGroup, 800, 600);
        mainGroup.getChildren().addAll(menuHBox, leftMenu, vertLine, labelsVBox, fieldsVBox);
        stage.setScene(scene1);
        stage.setTitle("PersonsDB");
        stage.show();
    }

    //refreshing the resultsList after modifying or deleting a person
    //0 = update name to listview, 1 = delete line
    private void refreshCurrent(ListView<String> resultsList, List<TextInputControl> textFields, int updateOrDelete){
        ObservableList<String> resultNames = resultsList.getItems();
        int currentId = Integer.parseInt(textFields.get(0).getText());
        int indexToUpdate = -1;
        for (int i = 0; i < resultNames.size(); i++) {
            String[] resultArr = resultNames.get(i).split(":");
            if (Integer.parseInt(resultArr[0]) == currentId){
                indexToUpdate = i;
            }
        }
        if (indexToUpdate > -1 && updateOrDelete == 0) {
            resultNames.set(indexToUpdate, currentId + ": " + textFields.get(1).getText());
        } else if (indexToUpdate > -1 && updateOrDelete == 1) {
            resultNames.remove(indexToUpdate);
        }
        resultsList.setItems(resultNames);
    }

    //validation of name and year input fields
    private boolean validateNameYearInput(List<TextInputControl> textFields){
        int year;
        Alert yearAlert = new Alert(Alert.AlertType.ERROR,
                "Please enter a valid year between 1900 and 2100", ButtonType.OK);
        Alert nameAlert = new Alert(Alert.AlertType.ERROR, "Please enter a valid name", ButtonType.OK);

        if (textFields.get(1).getText().equals("") || textFields.get(1).getText() == null){
            nameAlert.showAndWait();
            return false;
        } else {
            try {
                year = Integer.parseInt(textFields.get(4).getText());
            } catch (Exception error) {
                yearAlert.showAndWait();
                return false;
            }
            if (!(year <= 2100 && year >= 1900)) {
                yearAlert.showAndWait();
                return false;
            }
        }
        return true;
    }

    //view all persons in db (= search with "")
    private boolean viewAll(ListView<String> resultsList, DatabaseConnection dbConn, List<TextInputControl> textFields){
        boolean notEmpty = true;
        ObservableList<String> resultNames = FXCollections.observableArrayList();
        List<Person> personsList = dbConn.readDatabase("");
        for (Person person: personsList){
            resultNames.add(person.getPerson_id() + ": " + person.getName());
        }
        resultsList.setItems(resultNames);
        if (!resultsList.getItems().isEmpty()) {
            resultsList.getSelectionModel().selectFirst();
            fillTextFields(textFields, personsList.get(0));
        } else {
            clearTextFields(textFields);
            notEmpty = false;
        }
        return notEmpty;
    }

    //clear text fields
    private void clearTextFields(List<TextInputControl> textFields){
        textFields.forEach(field -> field.setText(""));
    }

    //fill text fields based on the person given
    private void fillTextFields(List<TextInputControl> textFields, Person resultPerson){
        textFields.get(0).setText(String.valueOf(resultPerson.getPerson_id()));
        textFields.get(1).setText(resultPerson.getName());
        textFields.get(2).setText(resultPerson.getAddress());
        textFields.get(3).setText(resultPerson.getPhone());
        textFields.get(4).setText(String.valueOf(resultPerson.getBirthYear()));
        textFields.get(5).setText(resultPerson.getInfo1());
        textFields.get(6).setText(resultPerson.getInfo2());
        textFields.get(7).setText(resultPerson.getInfo3());
    }

    //text field input length limitation
    private void limitField(TextInputControl control, int limit){
        if (control.getText() != null) {
            if (control.getText().length() > limit) {
                control.setText(control.getText().substring(0, limit - 1));
                control.positionCaret(control.getText().length());
            }
        }
    }

    //creates the upper menu HBox
    private HBox createMenuHBox() {
        HBox menuHBox = new HBox(10f);
        menuHBox.setPadding(new Insets(10f));
        List<Button> buttonsList = new ArrayList<>();
        Button viewAllButton = new Button("View all persons");
        buttonsList.add(viewAllButton);
        Button searchButton = new Button("Search by name");
        buttonsList.add(searchButton);
        Button addButton = new Button("Add a new person");
        buttonsList.add(addButton);
        Button exitButton = new Button("Exit");
        buttonsList.add(exitButton);

        buttonsList.forEach(button -> {
            button.setPrefSize(176f, 25f);
            menuHBox.getChildren().add(button);
        });
        return menuHBox;
    }

    //creates the labels VBox
    private VBox createLabelsVbox() {
        VBox labelsVBox = new VBox(10f);
        Text idLabel = new Text("ID:\n");
        Text nameLabel = new Text("Name:\n");
        Text addressLabel = new Text("Address:\n");
        Text phoneLabel = new Text("Phone:\n");
        Text birthYearLabel = new Text("Year of birth:\n");
        Text info1label = new Text("Info 1:\n\n\n");
        Text info2label = new Text("Info 2:\n\n\n");
        Text info3label = new Text("Info 3:\n\n\n");
        labelsVBox.getChildren().addAll(idLabel, nameLabel, addressLabel, phoneLabel, birthYearLabel, info1label,
                info2label, info3label);
        return labelsVBox;
    }

    //creates the text fields VBox
    //also includes save, cancel, save changes and delete buttons in their HBoxes
    private VBox createFieldsVbox() {
        VBox fieldsVBox = new VBox(16f);
        List<Control> textFields = new ArrayList<>();
        TextField idField = new TextField();
        idField.setEditable(false);
        textFields.add(idField);
        TextField nameField = new TextField();
        textFields.add(nameField);
        TextField addressField = new TextField();
        textFields.add(addressField);
        TextField phoneField = new TextField();
        textFields.add(phoneField);
        TextField birthYearField = new TextField();
        textFields.add(birthYearField);
        TextArea info1Area = new TextArea();
        textFields.add(info1Area);
        TextArea info2Area = new TextArea();
        textFields.add(info2Area);
        TextArea info3Area = new TextArea();
        textFields.add(info3Area);

        HBox lowerButtons = new HBox();

        HBox saveCancel = new HBox(10f);
        saveCancel.setVisible(false);
        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(75f);
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(75f);

        HBox saveChangesDelete = new HBox(10f);
        saveChangesDelete.setVisible(false);
        Button saveChangesButton = new Button("Save changes");
        saveChangesButton.setPrefWidth(100f);
        Button deleteButton = new Button("Delete");
        deleteButton.setPrefWidth(75f);
        saveChangesDelete.setPadding(new Insets(0f,0f,0f,10f));

        saveCancel.getChildren().addAll(saveButton,cancelButton);
        saveChangesDelete.getChildren().addAll(saveChangesButton, deleteButton);
        lowerButtons.getChildren().addAll(saveCancel, saveChangesDelete);

        Label saveLabel = new Label("Name and year of birth are mandatory");
        saveLabel.setVisible(false);

        textFields.forEach(control -> {
            if (control instanceof TextField || control instanceof TextArea){
                control.setPrefWidth(350f);
            }
            if (control instanceof TextArea){
                control.setPrefHeight(57f);
                ((TextArea) control).setWrapText(true);
            }
            fieldsVBox.getChildren().add(control);
        });

        fieldsVBox.getChildren().addAll(lowerButtons,saveLabel);
        return fieldsVBox;
    }
}

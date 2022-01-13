package thorpe.luke.password.ui;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import thorpe.luke.password.PasswordEntry;
import thorpe.luke.password.PasswordLibrary;
import thorpe.luke.password.PasswordMismatchException;
import thorpe.luke.password.ui.model.Options;
import thorpe.luke.password.ui.model.Style;
import thorpe.luke.password.ui.service.MediaService;
import thorpe.luke.password.ui.service.OptionsService;
import thorpe.luke.util.Mutable;

public class PasswordManagerApplication extends Application {

  public static final String WINDOW_TITLE = "Steadfast Password Manager";
  public static final int WINDOW_WIDTH = 1280;
  public static final int WINDOW_HEIGHT = 720;

  private static final char CONSOLE_TICK_SYMBOL = '✔';
  private static final char CONSOLE_TIME_SYMBOL = '!';
  private static final char CONSOLE_CROSS_SYMBOL = '✘';
  private static final ImmutableMap<Character, String> CONSOLE_SYMBOL_TO_CSS =
      ImmutableMap.<Character, String>builder()
          .put(
              CONSOLE_TICK_SYMBOL,
              Style.css(Style.TEXT_COLOUR.setTo("green"), Style.TEXT_WEIGHT.setTo("bold")))
          .put(
              CONSOLE_TIME_SYMBOL,
              Style.css(Style.TEXT_COLOUR.setTo("darkblue"), Style.TEXT_WEIGHT.setTo("bold")))
          .put(
              CONSOLE_CROSS_SYMBOL,
              Style.css(Style.TEXT_COLOUR.setTo("red"), Style.TEXT_WEIGHT.setTo("bold")))
          .build();

  private static final String PASSWORD_MANAGER_ASSETS_PATH = "./.assets/";
  private static final String PASSWORD_MANAGER_CONFIG_PATH = "./.config/";

  private final MediaService mediaService = new MediaService(PASSWORD_MANAGER_ASSETS_PATH);
  private final OptionsService optionsService = new OptionsService(PASSWORD_MANAGER_CONFIG_PATH);

  private final Image titleLogo = mediaService.loadImage("title_logo.png");
  private final Image optionsLogo = mediaService.loadImage("options_logo_small.png");
  private final Image icon = mediaService.loadImage("icon.png");

  private final Pane root;
  private final Scene scene;
  private final ListView<String> console;
  private final ListView<File> passwordLibraryData;
  private Mutable<Options> optionsMutable;
  private Stage launcherStage;

  public PasswordManagerApplication() {
    this.root = new StackPane();
    this.scene = new Scene(root);
    this.console = new ListView<>();
    this.passwordLibraryData = new ListView<>();
    this.optionsMutable = new Mutable<>(optionsService.loadDefaultOptions());
  }

  @Override
  public void start(Stage stage) throws Exception {
    this.launcherStage = stage;

    setTitleLogo();
    setConsole();
    setPasswordLibraryData();
    setOptionsButton();

    stage.setScene(scene);
    stage.setTitle(WINDOW_TITLE);
    stage.setWidth(WINDOW_WIDTH);
    stage.setHeight(WINDOW_HEIGHT);
    stage.setResizable(false);
    stage.getIcons().add(icon);

    consoleGreen("Welcome to the Steadfast Password Manager!");
    loadOptionsFromDisk();
    loadPasswordLibraryDataFromDisk();
    optionsMutable.addUpdateListener(options -> loadPasswordLibraryDataFromDisk());

    showLauncher();
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    try {
      optionsService.writeOptionsFileToDisk(optionsMutable.get());
    } catch (IOException ignored) {
    }
  }

  private void showLauncher() {
    launcherStage.show();
  }

  private void hideLauncher() {
    launcherStage.hide();
  }

  public static void main(String[] args) {
    launch();
  }

  private boolean areYouSureDialog(String question) {
    Alert warningAlert = new Alert(Alert.AlertType.WARNING);

    Stage stage = (Stage) warningAlert.getDialogPane().getScene().getWindow();

    stage.getIcons().add(titleLogo);
    warningAlert.setTitle("Are you sure?");

    warningAlert.getDialogPane().getButtonTypes().clear();
    warningAlert.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setAlignment(Pos.CENTER);

    pane.add(new Label(question), 0, 0);

    warningAlert.getDialogPane().setContent(pane);

    return warningAlert.showAndWait().map(buttonType -> buttonType == ButtonType.YES).orElse(false);
  }

  private void setTitleLogo() {
    ImageView imageView = new ImageView(titleLogo);
    StackPane.setAlignment(imageView, Pos.TOP_CENTER);
    StackPane.setMargin(imageView, new Insets(100));
    root.getChildren().add(imageView);
  }

  private void setConsole() {
    console.setCellFactory(
        console ->
            new ListCell<>() {
              @Override
              protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                setText(empty ? null : message);

                if (!empty) {
                  String css = CONSOLE_SYMBOL_TO_CSS.get(message.charAt(0));
                  styleProperty().setValue(css);
                }
              }
            });

    console.setMaxWidth(WINDOW_WIDTH >> 1);
    console.setMaxHeight(WINDOW_HEIGHT >> 2);
    console.setEditable(false);
    StackPane.setAlignment(console, Pos.BOTTOM_CENTER);
    StackPane.setMargin(console, new Insets(50));
    root.getChildren().add(console);
  }

  private void setPasswordLibraryData() {
    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setMaxWidth(WINDOW_WIDTH >> 1);
    pane.setMaxHeight(WINDOW_HEIGHT >> 2);

    passwordLibraryData.setCellFactory(
        passwordLibraryData ->
            new ListCell<>() {
              @Override
              protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                setText(empty ? null : file.getName());
              }
            });
    passwordLibraryData.setPrefWidth(WINDOW_WIDTH >> 1);
    passwordLibraryData.setPrefHeight(WINDOW_HEIGHT >> 2);
    passwordLibraryData.setEditable(false);
    pane.add(passwordLibraryData, 0, 0);

    GridPane buttonPane = new GridPane();
    buttonPane.setHgap(5);
    buttonPane.setVgap(5);
    buttonPane.setAlignment(Pos.CENTER);
    int buttonWidth = WINDOW_WIDTH >> 2;

    Consumer<PasswordLibrary> savePasswordLibraryToFile =
        passwordLibrary -> {
          String fileName =
              LocalDateTime.now().toString().replaceAll("[-:.]", "_")
                  + "___"
                  + passwordLibrary.getName().replaceAll("[^a-zA-Z0-9]", "")
                  + PasswordLibrary.FILE_SUFFIX;
          File file = new File(new File(optionsMutable.get().getDataPath()), fileName);
          try {
            if (!file.createNewFile()) {
              throw new IOException("Couldn't create file " + file.getName());
            }
          } catch (IOException e) {
            consoleError("Failed to write password library to file. Reason: %s.", e.getMessage());
            return;
          }
          try {
            passwordLibrary.writeToEncryptedFile(file, optionsMutable.get().getSalt());
          } catch (IOException e) {
            consoleError("Failed to write password library to file. Reason: %s.", e.getMessage());
            return;
          }
          loadPasswordLibraryDataFromDisk();
        };

    Button addButton = new Button("Add");
    addButton.setPrefWidth(buttonWidth);
    addButton.setOnAction(
        actionEvent ->
            loadPasswordLibraryFromDialog("", "", "", new LinkedList<>())
                .ifPresent(savePasswordLibraryToFile));
    buttonPane.add(addButton, 0, 0);

    Button editButton = new Button("Edit");
    editButton.setPrefWidth(buttonWidth);
    editButton.setOnAction(
        actionEvent -> {
          File file = passwordLibraryData.getSelectionModel().getSelectedItem();
          if (file == null) {
            return;
          }

          loadPasswordFromDialog("Please enter the password to unlock " + file.getName() + ":")
              .ifPresent(
                  password -> {
                    PasswordLibrary passwordLibrary;
                    try {
                      consoleLog("Reading password library %s from disk.", file.getName());
                      passwordLibrary =
                          PasswordLibrary.readFromEncryptedFile(
                              file, password, optionsMutable.get().getSalt());
                    } catch (IOException e) {
                      consoleError(
                          "Failed to read password library from disk. Reason: %s.", e.getMessage());
                      return;
                    } catch (PasswordMismatchException e) {
                      consoleError("Incorrect password.");
                      return;
                    }
                    loadPasswordLibraryFromDialog(
                            passwordLibrary.getName(),
                            passwordLibrary.getDescription(),
                            password,
                            passwordLibrary.getEntries())
                        .ifPresent(savePasswordLibraryToFile);
                  });
        });
    buttonPane.add(editButton, 1, 0);

    pane.add(buttonPane, 0, 1);

    pane.setAlignment(Pos.CENTER);
    root.getChildren().add(pane);
  }

  private Optional<PasswordLibrary> loadPasswordLibraryFromDialog(
      String name, String description, String password, Collection<PasswordEntry> passwordEntries) {
    Dialog<PasswordLibrary> passwordLibraryDialog = new Dialog<>();

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    Stage stage = (Stage) passwordLibraryDialog.getDialogPane().getScene().getWindow();
    passwordLibraryDialog.setTitle("Password Library Editor");
    stage.getIcons().add(titleLogo);

    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setAlignment(Pos.CENTER);

    GridPane textPane = new GridPane();
    textPane.setHgap(10);
    textPane.setVgap(10);

    TextField nameTextField = new TextField(name);
    textPane.add(new Label("Password Library Name:"), 0, 0);
    textPane.add(nameTextField, 1, 0);

    TextArea descriptionTextArea = new TextArea(description);
    descriptionTextArea.setPrefRowCount(3);
    textPane.add(new Label("Description:"), 0, 1);
    textPane.add(descriptionTextArea, 1, 1);

    TextField passwordTextField = new PasswordField();
    passwordTextField.setText(password);
    textPane.add(new Label("Password:"), 0, 2);
    textPane.add(passwordTextField, 1, 2);

    pane.add(textPane, 0, 0);

    ListView<PasswordEntry> passwordEntryListView = new ListView<>();
    passwordEntryListView.setCellFactory(
        listView ->
            new ListCell<>() {
              @Override
              protected void updateItem(PasswordEntry passwordEntry, boolean empty) {
                super.updateItem(passwordEntry, empty);
                setText(empty ? null : passwordEntry.getName());

                if (!empty) {
                  styleProperty().setValue(Style.TEXT_ALIGN.setTo("center"));
                }
              }
            });
    passwordEntryListView.getItems().addAll(passwordEntries);
    pane.add(passwordEntryListView, 0, 1);

    GridPane buttonPane = new GridPane();
    buttonPane.setHgap(10);
    buttonPane.setVgap(10);
    buttonPane.setAlignment(Pos.CENTER);
    int buttonWidth = 240;
    Supplier<PasswordEntry> currentPasswordEntrySupplier =
        () -> passwordEntryListView.getSelectionModel().getSelectedItem();

    Button addButton = new Button("Add");
    addButton.setPrefWidth(buttonWidth);
    addButton.setOnAction(
        actionEvent ->
            loadPasswordEntryFromDialog("", "", null, LocalDateTime.now(), new HashMap<>())
                .ifPresent(
                    passwordEntry ->
                        Platform.runLater(
                            () -> passwordEntryListView.getItems().add(passwordEntry))));
    buttonPane.add(addButton, 0, 0);

    Button editButton = new Button("Edit");
    editButton.setPrefWidth(buttonWidth);
    editButton.setOnAction(
        actionEvent -> {
          PasswordEntry oldPasswordEntry = currentPasswordEntrySupplier.get();
          if (oldPasswordEntry != null) {
            loadPasswordEntryFromDialog(
                    oldPasswordEntry.getName(),
                    oldPasswordEntry.getDescription(),
                    oldPasswordEntry.getPassword(),
                    oldPasswordEntry.getDateTimeOfCreation(),
                    oldPasswordEntry.getOldPasswords())
                .ifPresent(
                    passwordEntry ->
                        Platform.runLater(
                            () -> {
                              passwordEntryListView.getItems().remove(oldPasswordEntry);
                              passwordEntryListView.getItems().add(passwordEntry);
                            }));
          }
        });
    buttonPane.add(editButton, 1, 0);

    Button deleteButton = new Button("Delete");
    deleteButton.setPrefWidth(buttonWidth);
    deleteButton.setOnAction(
        actionEvent -> {
          PasswordEntry currentPasswordEntry = currentPasswordEntrySupplier.get();
          if (currentPasswordEntry != null
              && areYouSureDialog(
                  "Are you sure you want to delete the password entry called "
                      + currentPasswordEntry.getName()
                      + "?")) {
            Platform.runLater(() -> passwordEntryListView.getItems().remove(currentPasswordEntry));
          }
        });
    buttonPane.add(deleteButton, 2, 0);

    pane.add(buttonPane, 0, 2);
    pane.add(new Separator(), 0, 3);

    passwordLibraryDialog
        .getDialogPane()
        .getButtonTypes()
        .addAll(saveButtonType, ButtonType.CANCEL);

    passwordLibraryDialog.setResultConverter(
        buttonType -> {
          if (buttonType == saveButtonType) {
            return PasswordLibrary.fromPassword(
                nameTextField.getText(),
                descriptionTextArea.getText(),
                passwordTextField.getText(),
                optionsMutable.get().getSalt(),
                passwordEntryListView.getItems());
          }
          return null;
        });

    passwordLibraryDialog.getDialogPane().setContent(pane);

    return passwordLibraryDialog.showAndWait();
  }

  private Optional<String> loadPasswordFromDialog(String title) {
    Dialog<String> passwordDialog = new Dialog<>();

    ButtonType enterButtonType = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);

    Stage stage = (Stage) passwordDialog.getDialogPane().getScene().getWindow();
    passwordDialog.setTitle("Password");
    stage.getIcons().add(titleLogo);

    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setAlignment(Pos.CENTER);

    TextField passwordTextField = new PasswordField();
    pane.add(new Label(title), 0, 0);
    pane.add(passwordTextField, 0, 1);

    passwordDialog.getDialogPane().getButtonTypes().addAll(enterButtonType, ButtonType.CANCEL);

    passwordDialog.setResultConverter(
        buttonType -> {
          if (buttonType == enterButtonType) {
            return passwordTextField.getText();
          }
          return null;
        });

    passwordDialog.getDialogPane().setContent(pane);

    return passwordDialog.showAndWait();
  }

  private Optional<PasswordEntry> loadPasswordEntryFromDialog(
      String name,
      String description,
      String password,
      LocalDateTime dateTimeOfCreation,
      Map<LocalDateTime, String> oldPasswords) {
    Dialog<PasswordEntry> passwordEntryDialog = new Dialog<>();

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    Stage stage = (Stage) passwordEntryDialog.getDialogPane().getScene().getWindow();
    passwordEntryDialog.setTitle("Edit Entry");
    stage.getIcons().add(titleLogo);

    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setAlignment(Pos.CENTER);

    GridPane textPane = new GridPane();
    textPane.setHgap(10);
    textPane.setVgap(10);

    TextField nameTextField = new TextField(name);
    textPane.add(new Label("Password Entry Name:"), 0, 0);
    textPane.add(nameTextField, 1, 0);

    TextArea descriptionTextArea = new TextArea(description);
    descriptionTextArea.setPrefRowCount(3);
    textPane.add(new Label("Description:"), 0, 1);
    textPane.add(descriptionTextArea, 1, 1);

    TextField passwordTextField = new TextField(password);
    textPane.add(new Label("Password:"), 0, 2);
    textPane.add(passwordTextField, 1, 2);

    pane.add(textPane, 0, 0);

    ListView<String> oldPasswordListView = new ListView<>();
    //    oldPasswordListView.setCellFactory(
    //            listView ->
    //                    new ListCell<>() {
    //                      @Override
    //                      protected void updateItem(String message, boolean empty) {
    //                        super.updateItem(message, empty);
    //                        setText(empty ? null : message);
    //
    //                        if (!empty) {
    //                          String css = CONSOLE_SYMBOL_TO_CSS.get(message.charAt(0));
    //                          styleProperty().setValue(css);
    //                        }
    //                      }
    //                    });
    oldPasswordListView
        .getItems()
        .addAll(
            oldPasswords
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(
                    creationTimePasswordEntry ->
                        String.format(
                            "Password %s was created at %s",
                            creationTimePasswordEntry.getValue(),
                            creationTimePasswordEntry
                                .getKey()
                                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))))
                .collect(Collectors.toList()));
    pane.add(oldPasswordListView, 0, 1);

    passwordEntryDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    passwordEntryDialog.setResultConverter(
        buttonType -> {
          if (buttonType == saveButtonType
              && !Objects.equals(passwordTextField.getText(), password)) {
            if (password != null) {
              oldPasswords.put(dateTimeOfCreation, password);
            }
            return new PasswordEntry(
                nameTextField.getText(),
                descriptionTextArea.getText(),
                passwordTextField.getText(),
                LocalDateTime.now(),
                oldPasswords);
          }
          return null;
        });

    passwordEntryDialog.getDialogPane().setContent(pane);

    return passwordEntryDialog.showAndWait();
  }

  private void setOptionsButton() {
    Button optionsButton = new Button();
    optionsButton.setGraphic(new ImageView(optionsLogo));
    StackPane.setAlignment(optionsButton, Pos.TOP_RIGHT);
    StackPane.setMargin(optionsButton, new Insets(10));

    optionsButton.setOnAction(
        actionEvent -> loadOptionsFromDialog().ifPresent(optionsMutable::set));

    root.getChildren().add(optionsButton);
  }

  private void loadOptionsFromDisk() {
    consoleLog("Loading options from disk.");
    try {
      optionsMutable.set(optionsService.loadOptionsFromDisk());
      consoleGreen("Successfully loaded options from disk!");
      return;
    } catch (IOException e) {
      consoleError("Failed to load options from disk. Reason: %s.", e.getMessage());
    }
    consoleLog("Writing new options file to disk.");
    try {
      optionsMutable.set(optionsService.writeNewOptionsFileToDisk());
      consoleGreen("Successfully setup new options file!");
    } catch (IOException e) {
      consoleError("Failed to write new options file to disk. Reason: %s.", e.getMessage());
      consoleLog(
          "Loading a default set of options. Any changes you make this session may not be saved.");
      optionsMutable.set(optionsService.loadDefaultOptions());
      consoleGreen("Successfully loaded default options!");
    }
  }

  private Optional<Options> loadOptionsFromDialog() {
    Dialog<Options> optionsDialog = new Dialog<>();

    ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);

    Stage stage = (Stage) optionsDialog.getDialogPane().getScene().getWindow();
    optionsDialog.setTitle("Options");
    stage.getIcons().add(optionsLogo);

    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);

    DirectoryChooser directoryChooser = new DirectoryChooser();
    String dataPath = optionsMutable.get().getDataPath();
    Button directoryChooserButton = new Button("Choose New Location");
    Label dataPathLabel = new Label(dataPath);
    directoryChooserButton.setOnAction(
        actionEvent -> {
          File chosenDirectory = directoryChooser.showDialog(stage);
          String updatedDataPath = chosenDirectory.getPath();
          dataPathLabel.setText(!updatedDataPath.isBlank() ? updatedDataPath : dataPath);
        });

    pane.add(new Label("Password Library Store:"), 0, 0);
    pane.add(new Label(dataPath), 1, 0);
    pane.add(directoryChooserButton, 2, 0);

    TextField saltTextField = new TextField(optionsMutable.get().getSalt());
    pane.add(new Label("Salt:"), 0, 1);
    pane.add(saltTextField, 2, 1);

    optionsDialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

    optionsDialog.setResultConverter(
        buttonType -> {
          if (buttonType == applyButtonType) {
            return Options.where()
                .dataPathIsSetTo(dataPathLabel.getText())
                .saltIsSetTo(saltTextField.getText())
                .build();
          }
          return null;
        });

    optionsDialog.getDialogPane().setContent(pane);

    return optionsDialog.showAndWait();
  }

  private void loadPasswordLibraryDataFromDisk() {
    consoleLog("Loading password library data from disk.");
    File passwordLibraryDataDirectory = new File(optionsMutable.get().getDataPath());
    if (!passwordLibraryDataDirectory.exists()) {
      consoleError(
          "Failed to load password library data from disk. Reason: no directory at %s.",
          passwordLibraryDataDirectory);
      consoleLog("Writing new password library directory to disk.");
      if (!passwordLibraryDataDirectory.mkdirs()) {
        consoleError(
            "Failed to write new password library directory to disk. Please update the data path using the options menu.");
        return;
      }
      consoleGreen("Successfully setup new password library directory!");
    }

    if (!passwordLibraryDataDirectory.isDirectory()) {
      consoleError(
          "Password library data path does not point to a directory. Please update the data path using the options menu.");
      return;
    }

    List<File> passwordLibraryData;
    try {
      passwordLibraryData =
          Files.walk(passwordLibraryDataDirectory.toPath())
              .map(Path::toFile)
              .filter(File::isFile)
              .filter(file -> file.getName().endsWith(PasswordLibrary.FILE_SUFFIX))
              .sorted(Comparator.comparing(File::getName))
              .collect(Collectors.toList());
    } catch (IOException e) {
      consoleError("Failed to load password library data from disk. Reason: %s.", e.getMessage());
      return;
    }
    consoleGreen("Successfully loaded password library data!");

    Platform.runLater(
        () -> {
          this.passwordLibraryData.getItems().clear();
          this.passwordLibraryData.getItems().addAll(passwordLibraryData);
        });
  }

  private void consoleWrite(char symbol, String message, Object... arguments) {
    Platform.runLater(
        () -> console.getItems().add(String.format(symbol + " " + message, arguments)));
  }

  private void consoleGreen(String greenMessage, Object... arguments) {
    consoleWrite(CONSOLE_TICK_SYMBOL, greenMessage, arguments);
  }

  private void consoleLog(String logMessage, Object... arguments) {
    consoleWrite(CONSOLE_TIME_SYMBOL, logMessage, arguments);
  }

  private void consoleError(String errorMessage, Object... arguments) {
    consoleWrite(CONSOLE_CROSS_SYMBOL, errorMessage, arguments);
  }
}

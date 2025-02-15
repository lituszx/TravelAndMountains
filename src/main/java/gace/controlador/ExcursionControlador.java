package gace.controlador;

import gace.modelo.Excursion;
import gace.modelo.Inscripcion;
import gace.modelo.Socio;
import gace.modelo.dao.DAOFactory;
import gace.modelo.dao.ExcursionDao;
import gace.vista.DatosUtil;
import gace.vista.VistaExcursion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.jfr.Percentage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javafx.geometry.Pos.CENTER;


/**
 * Falta la part del botó de buscar.
 * Canviar estil de Gent inscita perque es noti que es pot fer doble click.
 * Canviar height de les columns de mostrar detalle.
 * -
 * Insertar, modificar, eliminar fet.
 * Cancelar fet.
 */
public class ExcursionControlador {
    private DatosUtil datosUtil;
    private VistaExcursion vistaExcursion;
    private ExcursionDao excursionDao;
    @FXML
    private TableView<Excursion> tableView;
    @FXML
    private TableColumn<Excursion, Integer> columnId;
    @FXML
    private TableColumn<Excursion, String> columnCodigo;
    @FXML
    private TableColumn<Excursion, String> columnDescripcion;
    @FXML
    private TableColumn<Excursion, Date> columnFecha;
    @FXML
    private TableColumn<Excursion, Integer> columnCantidad;
    @FXML
    private TableColumn<Excursion, Double> columnPrecio;


    public ExcursionControlador() {
        this.vistaExcursion = new VistaExcursion();
        this.excursionDao = DAOFactory.getExcursionDao();
        this.datosUtil = new DatosUtil();
    }


    public void novaExc(){
        novaExcursio(null);
    }

    public void novaExcursio(Excursion excMod) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Ingresar Excursión");

        Label codigoLabel = new Label("Código:");
        TextField codigoField = new TextField();
        codigoField.setPromptText("Ingrese el Código");

        Label descripcionLabel = new Label("Descripción:");
        TextField descripcionField = new TextField();
        descripcionField.setPromptText("Ingrese descripción");

        Label fechaLabel = new Label("Fecha:");
        DatePicker fechaPicker = new DatePicker();

        Label diasLabel = new Label("Número de días:");
        TextField diasField = new TextField();
        diasField.setPromptText("Ingrese número de días");

        Label precioLabel = new Label("Precio de inscripción:");
        TextField precioField = new TextField();
        precioField.setPromptText("Ingrese precio");

        if(excMod != null){
            codigoField.setText(excMod.getCodigo());
            descripcionField.setText(excMod.getDescripcion());
            diasField.setText(String.valueOf(excMod.getNoDias()));
            fechaPicker.setValue(excMod.getFecha().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            precioField.setText(String.valueOf(excMod.getPrecio()));
        }

        Button modificarButton = new Button("Modificar");
        modificarButton.setOnAction(event -> {
            String codigo = codigoField.getText();
            String descripcion = descripcionField.getText();
            LocalDate fecha = (fechaPicker.getValue() != null) ? fechaPicker.getValue() : null;
            String dias = diasField.getText();
            String precio = precioField.getText();

            if (descripcion.isEmpty() || fecha == null || dias.isEmpty() || precio.isEmpty()) {
                mostrarAlerta("Por favor, complete todos los campos.");
                return;
            }
            try {
                int numeroDias = Integer.parseInt(dias);
                double precioInscripcion = Double.parseDouble(precio);
                Date fechaDate = validarFecha(fecha);
                modificarExc(excMod, codigo, descripcion, fechaDate, numeroDias, precioInscripcion);
                vistaExcursion.detalleExcursion(excMod.toString());
                mostrarExcursiones();
                modalStage.close();
            } catch (NumberFormatException e) {
                mostrarAlerta("Número de días y precio deben ser valores numéricos.");
            } catch (ParseException e) {
                mostrarAlerta("Fecha no válida.");
            }
        });
        Button aceptarButton = new Button("Aceptar");
        Button cancelarButton = new Button("Cancelar");
        aceptarButton.setOnAction(event -> {
            String codigo = codigoField.getText();
            String descripcion = descripcionField.getText();
            LocalDate fecha = (fechaPicker.getValue() != null) ? fechaPicker.getValue() : null;
            String dias = diasField.getText();
            String precio = precioField.getText();

            if (descripcion.isEmpty() || fecha == null || dias.isEmpty() || precio.isEmpty()) {
                mostrarAlerta("Por favor, complete todos los campos.");
                return;
            }

            try {
                int numeroDias = Integer.parseInt(dias);
                double precioInscripcion = Double.parseDouble(precio);
                Date fechaDate = validarFecha(fecha);
                Excursion exc = new Excursion(codigo, descripcion, fechaDate, numeroDias, precioInscripcion);
                excursionDao.insertar(exc);
                vistaExcursion.detalleExcursion(exc.toString());
                mostrarExcursiones();
                modalStage.close(); 
            } catch (NumberFormatException e) {
                mostrarAlerta("Número de días y precio deben ser valores numéricos.");
            } catch (ParseException e) {
                mostrarAlerta("Fecha no válida.");
            }
        });
        cancelarButton.setOnAction(event -> modalStage.close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(codigoLabel, 0, 0);
        grid.add(codigoField, 1, 0);

        grid.add(descripcionLabel, 0, 1);
        grid.add(descripcionField, 1, 1);

        grid.add(fechaLabel, 0, 2);
        grid.add(fechaPicker, 1, 2);

        grid.add(diasLabel, 0, 3);
        grid.add(diasField, 1, 3);

        grid.add(precioLabel, 0, 4);
        grid.add(precioField, 1, 4);

        HBox buttonBox = null;
        if(excMod != null){
            buttonBox = new HBox(10, modificarButton, cancelarButton);
        } else {
            buttonBox = new HBox(10, aceptarButton, cancelarButton);
        }
        buttonBox.setPadding(new Insets(10));
        grid.add(buttonBox, 1, 5);

        Scene scene = new Scene(grid, 400, 250);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    public void modificarExc(Excursion exc, String codigo, String descripcion, Date fecha, int noDias, Double precio){
        exc.setCodigo(codigo);
        exc.setDescripcion(descripcion);
        exc.setFecha(fecha);
        exc.setNoDias(noDias);
        exc.setPrecio(precio);
        DAOFactory.getExcursionDao().modificar(exc);
    }


    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Advertencia");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }


//        String strExcursio = this.vistaExcursion.formExcursion();
//        String[] datosExc = strExcursio.split(",");
//        if (datosExc.length < 4) {
//            datosUtil.mostrarError("Datos de la excursión incompletos");
//            return false;
//        }
//        Date data = null;
//        try{
//            data = validarFecha(datosExc[2]);
//        } catch (ParseException e) {
//            datosUtil.mostrarError("Fecha no válida");
//            return false;
//        }
//        if(data == null){
//            datosUtil.mostrarError("Fecha no válida");
//            return false;
//        }
//        Excursion exc = new Excursion(datosExc[0], datosExc[1], data, Integer.parseInt(datosExc[3]), Double.parseDouble(datosExc[4]));
//        excursionDao.insertar(exc);
//        vistaExcursion.detalleExcursion(exc.toString());
//        return true;
//    }

    private Date validarFecha(LocalDate fecha) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormato = fecha.format(formatter);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        return dateFormat.parse(fechaFormato);
    }

//    public Excursion pedirExcursion(){
//        String codigo = vistaExcursion.pedirExc();
//        Excursion exc = buscarExcursion(codigo);
//        if(exc == null){
//            datosUtil.mostrarError("Excursion no encontrada");
//            return null;
//        }
//        vistaExcursion.detalleExcursion(exc.toString());
//        return exc;
//    }
//
//    public Excursion buscarExcursion(int id_excursion){
//        return DAOFactory.getExcursionDao().buscar(id_excursion);
//    }
//    public Excursion buscarExcursion(String codigo){
//        return DAOFactory.getExcursionDao().buscar(codigo);
//    }
    @FXML
    private void buscarExcursiones(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Excursión");
        dialog.setHeaderText("Por favor, ingrese el ID de la excursión a buscar:");
        dialog.setContentText("ID de la Excursión:");

        dialog.showAndWait().ifPresent(idInput -> {
            try {
                // Convertir el ID ingresado a un número
                int idExcursion = Integer.parseInt(idInput);


                Excursion excursionEncontrada = excursionDao.buscar(idExcursion);


                if (excursionEncontrada != null) {
                    mostrarDetalle(excursionEncontrada);
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("Excursión Encontrada");
//                    alert.setHeaderText("Detalles de la Excursión:");
//                    alert.setContentText(
//                            "ID: " + excursionEncontrada.getId() + "\n" +
//                                    "Código: " + excursionEncontrada.getCodigo() + "\n" +
//                                    "Descripción: " + excursionEncontrada.getDescripcion() + "\n" +
//                                    "Fecha: " + excursionEncontrada.getFecha() + "\n" +
//                                    "Número de Días: " + excursionEncontrada.getNoDias() + "\n" +
//                                    "Precio: " + excursionEncontrada.getPrecio()
//                    );
//                    alert.showAndWait();
                } else {

                    datosUtil.mostrarError(
                            "No se encontró una excursión con el ID " + idExcursion + ".");
                }

            } catch (NumberFormatException e) {

                datosUtil.mostrarError(
                        "El ID ingresado no es válido. Por favor, ingrese un número.");
            }
        });
    }

//    public boolean eliminarExcursion(){
//        ArrayList<Excursion> excursiones = DAOFactory.getExcursionDao().listar();
//        if(excursiones== null){
//            datosUtil.mostrarError("No hay excursiones para eliminar");
//            return false;
//        }
//        for(Excursion excursion : excursiones){
//            vistaExcursion.detalleExcursion(excursion.toString());
//        }
//        if(seleccionarExc(excursiones)){
//            return true;
//        }
//        return false;
//    }

//    public boolean seleccionarExc(ArrayList<Excursion> excursiones){
//        String codigo = vistaExcursion.pedirExc();
//        for(Excursion excur : excursiones){
//            if(excur.getCodigo().equals(codigo)) {
//                List<Inscripcion> insc = DAOFactory.getInscripcionDao().listarXExc(excur);
//                if(insc != null){
//                    datosUtil.mostrarError("No se puede eliminar la excursión porque tiene inscripciones");
//                    return false;
//                }
//                int opcion = datosUtil.pedirOpcion("Es esta la excursion que desea eliminar", "Sí", "No");
//                if (opcion == 1) {
//                    DAOFactory.getExcursionDao().eliminar(excur.getId());
//                    return true;
//                }
//                return false;
//            }
//        }
//        datosUtil.mostrarError("Excursion no encontrada");
//        return false;
//    }

    public void cancelarExcursion(Excursion exc){
        if(exc == null){
            datosUtil.mostrarError("No hay excursiones para cancelar");
            return;
        }

        int cantidad = DAOFactory.getExcursionDao().cancelar(exc);
        datosUtil.mostrarInfo("Se han cancelado "+cantidad+" inscripciones");
    }

    public void mostrarDetalle(Excursion exc){
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Detalles Excursión");
        Label idExc = new Label("ID: " + exc.getId());

        Label codiExc = new Label("Codi: " + exc.getCodigo());

        Label nomExc = new Label("Descripción: " + exc.getDescripcion());

        Label dataExc = new Label("Data: " + new SimpleDateFormat("dd/MM/yyyy").format(exc.getFecha()));

        Label preuExc = new Label("Preu: " + exc.getPrecio());

        Button cancelarExcursio = new Button("Cancelar Excursio");
        cancelarExcursio.setOnAction(event -> {
            cancelarExcursion(exc);
            mostrarExcursiones();
            modalStage.close();
        });

        Button crearInscripcion = new Button("Crear Inscripción");
        crearInscripcion.setOnAction(event -> {
            crearInscripcion(exc);
            modalStage.close();
        });

        Button modificarExcursio = new Button("Modificar Excursio");
        modificarExcursio.setOnAction(event -> {
            novaExcursio(exc);
            modalStage.close();
            mostrarExcursiones();
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        col2.setPercentWidth(33.33);
        col3.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col1, col2, col3);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row1coma5 =  new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();

        row1.setPercentHeight(10);
        row1coma5.setPercentHeight(10);
        row2.setPercentHeight(20);
        row3.setPercentHeight(20);
        row4.setPercentHeight(20);
        row5.setPercentHeight(20);

        grid.getRowConstraints().addAll(row1, row2, row3, row4, row5);
        Label nom = new Label();
        nom.setText("Detalles de la excursión:" + exc.getCodigo());
        grid.add(nom, 0,0,3,1);

        setLabelStyle(idExc, codiExc, dataExc);
        grid.add(idExc, 0, 1);
        grid.add(codiExc, 1, 1);
        grid.add(dataExc, 2, 1);

        nomExc.setMaxWidth(Double.MAX_VALUE);
        nomExc.setMaxHeight(Double.MAX_VALUE);
        nomExc.setAlignment(CENTER);

        grid.add(nomExc, 0, 2, 3, 2);
        Label gentLabel = null;
        List<Inscripcion> insc = DAOFactory.getInscripcionDao().listarXExc(exc);
        if(insc == null){
            gentLabel = new Label("Gent Inscrita: 0");
        }else{
            gentLabel = new Label("Gent Inscrita: " + insc.size());
        }
        gentLabel.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        gentLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                mostraInsc(insc, exc.getCodigo());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hbox = new HBox(10, gentLabel, spacer, preuExc);
        hbox.setAlignment(CENTER);
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setMaxHeight(Double.MAX_VALUE);

        grid.add(hbox, 0, 4, 3, 1);

        cancelarExcursio.setMaxWidth(Double.MAX_VALUE);
        crearInscripcion.setMaxWidth(Double.MAX_VALUE);
        modificarExcursio.setMaxWidth(Double.MAX_VALUE);

        HBox buttonsBox = new HBox(10, crearInscripcion, modificarExcursio, cancelarExcursio);
        buttonsBox.setAlignment(CENTER);
        HBox.setHgrow(cancelarExcursio, Priority.ALWAYS);
        HBox.setHgrow(crearInscripcion, Priority.ALWAYS);
        HBox.setHgrow(modificarExcursio, Priority.ALWAYS);
        buttonsBox.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        grid.add(buttonsBox, 0, 5, 3, 1);

        StackPane root = new StackPane(grid);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Scene scene = new Scene(root,500,250);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void setLabelStyle(Label... labels) {
        for (Label label : labels) {
            label.setStyle("-fx-background-color: lightgreen; -fx-padding: 10; -fx-alignment: center;");
            label.setMaxHeight(Double.MAX_VALUE);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(CENTER);
            //Pos.CENTER
        }
    }

    private void mostraInsc(List<Inscripcion> insc, String exc) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Inscripciones para la excursión "+ exc);

        ListView<String> listView = new ListView<>();
        listView.setPrefSize(400, 300);
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                        }
                    }
                };
            }
        });

        for (Inscripcion ins : insc) {
            listView.getItems().add("INSC- " + ins.getCodigo() + " - Socio: " + ins.getSocio().getIdSocio()+" "+ins.getSocio().getNombre()+ " "+ins.getSocio().getApellido());
        }

        Button cerrarButton = new Button("Cerrar");
        cerrarButton.setOnAction(event -> modalStage.close());

        VBox layout = new VBox(10, listView, cerrarButton);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    public void crearInscripcion(Excursion exc){
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Crear Inscripción");


        List<Socio> socios = DAOFactory.getSocioDao().listar();
        ArrayList<String> nombres = new ArrayList<>();
        for (Socio socio : socios){
            nombres.add(socio.getIdSocio()+ "- "+ socio.getNombre()+ " "+ socio.getApellido()+ ", " + socio.getTipoSocio());
        }
        ObservableList<String> sociosData = FXCollections.observableArrayList(nombres);

        ComboBox<String> socioComboBox = new ComboBox<>(sociosData);

        Button aceptarButton = new Button("Aceptar");
        Button cancelarButton = new Button("Cancelar");
        aceptarButton.setOnAction(event -> {
            String selec = socioComboBox.getValue();
            if(selec == null){
                datosUtil.mostrarError("Seleccione un socio");
                return;
            }
            int id = Integer.parseInt(selec.split("-")[0].trim());
            Socio selectedSocio = DAOFactory.getSocioDao().buscar(id);
            if(selectedSocio == null){
                datosUtil.mostrarError("Socio no encontrado");
                return;
            }
            String codi = InscripcionControlador.getCodigoExcursion(selectedSocio.getIdSocio(), exc.getCodigo());
            Inscripcion insc = new Inscripcion(codi, selectedSocio, exc);
            DAOFactory.getInscripcionDao().insertar(insc);
            modalStage.close();
        });
        cancelarButton.setOnAction(event -> modalStage.close());
        VBox layout = new VBox(10, socioComboBox, aceptarButton, cancelarButton);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 400, 300);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }


    public void mostrarExcursiones(){
        ArrayList<Excursion> excs = DAOFactory.getExcursionDao().listar();
        if(excs == null){
            datosUtil.mostrarError("No hay excursiones para mostrar");
            return;
        }

        ObservableList<Excursion> datos = FXCollections.observableArrayList(excs);

        // Establecer los datos en la ListView
        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        columnFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnCantidad.setCellValueFactory(new PropertyValueFactory<>("noDias"));
        columnPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnFecha.setCellFactory(column -> {
            return new TableCell<Excursion, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(new SimpleDateFormat("dd/MM/yyyy").format(item));
                    }
                }
            };
        });
        columnPrecio.setCellFactory(column -> {
            return new TableCell<Excursion, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                    }
                }
            };
        });

        tableView.setItems(datos);
        tableView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Excursion exc = tableView.getSelectionModel().getSelectedItem();
                mostrarDetalle(exc);
            }
        });
//        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//            if (newSelection != null) {
//                mostrarDetalle(newSelection);
//            }
//        });
    }

}

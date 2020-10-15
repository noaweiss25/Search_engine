package View;

import Model.Document;
import Model.Indexer;
import Model.Query;
import Model.Searcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class Controller {
    public Button browsecop;
    public Button runQuerry;
    public Button browseposting;
    public Button browseQuerryFile;
    public Button resetdic;
    public Button showdic;
    public Button uploaddic;
    public TextField textcor;
    public TextField textposting;
    public TextField browseQ;
    public TextField typeQ;
    public CheckBox withstem;
    public Text itnomanoa;
    public String corpusPath;
    public String postingPath;
    public String queryFilePAth;
    public Indexer indexer;
    public CheckBox semantic;
    public Button chooseFile;
    public Button runQuerryFile;
    public Button resultFolder;
    private String resultPath;
    public ObservableList<String> items;
    public ChoiceBox<String> Entitilist;
    public boolean uplode = false;
    public boolean dictionary = false;
    public CheckBox internet;

    public void takePath(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        String title;
        dirChooser.setTitle("Choose Your Directory");

        File f = dirChooser.showDialog(null);

        if (null != f) {
            String filePath = f.getAbsolutePath();
            if (event.getSource() == browsecop) {
                textcor.setText(filePath);
                corpusPath = filePath;
            } else if (event.getSource() == browseposting) {
                textposting.setText(filePath);
                postingPath = filePath;
            }
        }
    }

    public void run(ActionEvent event) throws IOException, ParseException {
        if (corpusPath == null || postingPath == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please make sure you select both corpus and posting paths!");
            alert.show();
        } else {
            long start = System.currentTimeMillis();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please wait while creating indexer , that might take a while");
            alert.show();
            indexer = new Indexer(corpusPath, postingPath, withstem.isSelected());
            indexer.fillDictionary();
            long end = System.currentTimeMillis();
            long runtime_ms = end - start;
            long runtime_sec = runtime_ms / 1000L;
            alert.close();
            String runtime_minutes = (String.valueOf(((runtime_ms / 1000.0))));
            //System.out.println("Full program runtime: " + (runtime_ms / 1000.0) + "[sec] or " + runtime_ms + "[ms] or " + runtime_minutes + "[minutes]");
            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION, "Total number of documents : " + Document.docCollection.size() + "\nTotal number of unique terms : " + indexer.dictionary.size() + "\nTotal time for indexer is " + (runtime_minutes) + " seconds");
            alert2.show();
            dictionary = true;
            browseQ.setVisible(true);
            typeQ.setVisible(true);
        }
    }

    public void reset(ActionEvent event) {
        if (dictionary || uplode) {
            System.gc();
            if (corpusPath == null || postingPath == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You cannot reset if there is no Dictionary!");
                alert.show();
            } else {
                File[] posting = new File(postingPath).listFiles();
                for (File file : posting) {
                    file.delete();
                }
                indexer.clearRam();
                corpusPath = null;
                postingPath = null;
                textcor.setText("");
                textcor.setPromptText("Enter your Corpus path");
                textposting.setText("");
                textposting.setPromptText("Enter your Posting path");
            }
            dictionary = false;
            uplode = false;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You cannot reset if there is no Dictionary!");
            alert.show();
        }
    }

    public void show(ActionEvent event) throws FileNotFoundException {
        if (!dictionary && !uplode) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You cannot show Dictionary if there is no Dictionary!");
            alert.show();
        } else {
            File dic = new File(postingPath + "//dicToShow2.txt");
            BufferedReader fullPost = new BufferedReader(new FileReader(dic));
            System.gc();

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(dic);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException, ParseException {
        if (!withstem.isSelected()) {
            if (new File(postingPath + "\\Entitties.txt").isFile() && new File(postingPath + "\\dictionary.txt").isFile()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please wait while uploading indexer , that might take a few seconds");
                alert.show();
                indexer = new Indexer(corpusPath, postingPath, withstem.isSelected());
                indexer.upload(withstem.isSelected());
                uplode = true;
                alert.close();
                //browseQ.se;
                //typeQ.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Some files are missing to complete this process");
                alert.show();
            }
        } else {
            if (new File(postingPath + "\\Entitties.txt").isFile() && new File(postingPath + "\\stemDictionary.txt").isFile()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please wait while uploading indexer , that might take a few seconds");
                alert.show();
                indexer = new Indexer(corpusPath, postingPath, withstem.isSelected());
                indexer.upload(withstem.isSelected());
                uplode = true;
                alert.close();
                //browseQ.se;
                //typeQ.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Some files are missing to complete this process");
                alert.show();
            }

        }
    }

    public void EnterQuerryFile(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose Your Directory for resaults file");

        File f = dirChooser.showDialog(null);

        if (null != f) {
            resultPath = f.getAbsolutePath();
        }
    }

    public void runQerry(ActionEvent actionEvent) throws IOException, ParseException {
        if (resultPath != null) {
            if (!uplode && !dictionary) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please make an upload to your dictionary or make a new dictionary!");
                alert.show();
            } else {
                Searcher searcher = new Searcher("", corpusPath, withstem.isSelected(), semantic.isSelected(), internet.isSelected());
                String qur = typeQ.getText();
                Query query = new Query(500, qur, "", "");
                ListView<String> list = new ListView();
                items = FXCollections.observableArrayList(
                        searcher.calculate(query).keySet());
                Entitilist.setItems(items);
                list.setItems(items);
                list.setCellFactory(ComboBoxListCell.forListView(items));
                StackPane root = new StackPane();
                root.getChildren().add(list);
                Stage stage = new Stage();
                stage.setScene(new Scene(root, 200, 250));
                stage.show();
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Total num of docs retrived: "+items.size());
                alert.show();
                File f = new File(resultPath + "\\result.txt");
                PrintWriter writer = new PrintWriter(f, "UTF-8");
                Set<String> keyset = searcher.calculate(query).keySet();
                for (String a : keyset) {
                    writer.println(query.getID() + " 0 " + a + " 1" + " 42.38" + " mt");
                }


                writer.close();
                //items.clear();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please enter result path first!");
            alert.show();
        }
    }

    public void takePathQ(ActionEvent event) {

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose Your Directory");

        File f = dirChooser.showDialog(null);
        if (null != f) {
            String filePath = f.getAbsolutePath();
            if (event.getSource() == browseQuerryFile) {
                browseQ.setText(filePath);
                queryFilePAth = filePath;

            }
        }
    }

    public void runFileQ(ActionEvent event) throws IOException {
        if (resultPath != null) {
            if (!uplode && !dictionary) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please make an upload to your dictionary or make a new dictionary!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please wait while calculating, that might take a few seconds");
                alert.show();
                Searcher searcher = new Searcher(queryFilePAth, corpusPath, withstem.isSelected(), semantic.isSelected(), internet.isSelected());
                List<Query> Q = searcher.readQuery(queryFilePAth);
                ListView<String> list = new ListView();
                ArrayList<String> set = new ArrayList<>();
                int  minus = 0;
                try {
                    File f = new File(resultPath + "\\result.txt");
                    PrintWriter writer = new PrintWriter(f, "UTF-8");


                    for (Query qu : Q) {
                        Set<String> keyset = searcher.calculate(qu).keySet();
                        for (String a : keyset) {
                            writer.println(qu.getID() + " 0 " + a + " 1" + " 42.38" + " mt");
                        }
                        minus++;
                        set.add("-------- " + qu.getID() + " --------");
                        set.addAll(keyset);
                    }
                    writer.close();
                } catch (Exception e1) {

                }
                items = FXCollections.observableArrayList(set);
                Entitilist.setItems(items);
                list.setItems(items);
                list.setCellFactory(ComboBoxListCell.forListView(items));
                StackPane root = new StackPane();
                root.getChildren().add(list);
                Stage stage = new Stage();
                stage.setScene(new Scene(root, 200, 250));
                stage.show();
                alert.close();
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION,"Total num of docs retrived: "+(items.size()-minus));
                alert2.show();
                //items.clear();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please enter result path first!");
            alert.show();
        }
    }

    public void top5ForDoc(ActionEvent event) throws IOException {
        String choose = Entitilist.getValue();
        Document d = Document.docCollection.get(Document.docIdToDocNumber.get(choose));
        ListView<String> list = new ListView();
        ObservableList<String> ent;
        try {
            ent = FXCollections.observableArrayList(d.returnTop5());
        } catch (Exception e) {
            ent = FXCollections.observableArrayList("No entities in this doc!");
        }
        list.setItems(ent);
        list.setCellFactory(ComboBoxListCell.forListView(ent));
        StackPane root = new StackPane();
        root.getChildren().add(list);
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 200, 250));
        stage.show();

    }
}

/*
 * The MIT License
 *
 * Copyright 2017 Noukkis.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package discordfs;

import discordfs.beans.DirectoryItem;
import discordfs.beans.FileItem;
import discordfs.beans.FileTreeCell;
import discordfs.helpers.Statics;
import discordfs.wrk.DiscordWrk;
import discordfs.wrk.FilesWrk;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.DirectoryChooser;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 *
 * @author Jordan Vesy
 */
public class Ctrl implements Initializable {

    @FXML
    private TreeView<String> tree;

    private FilesWrk wrk;

    private ContextMenu cutContext;
    private ContextMenu linkContext;

    private TreeItem clipboard;

    @FXML
    private ContextMenu context;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            DiscordWrk bot = new DiscordWrk(new JDABuilder(AccountType.BOT).setToken(Statics.BOT_TOKEN).buildBlocking());
            wrk = new FilesWrk(bot);
            refresh();
            tree.setCellFactory((TreeView<String> list) -> new FileTreeCell(this));
            createContexts();
            wrk.getUpdater().show();
            clipboard = null;
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException ex) {
            ex.printStackTrace();
            Platform.exit();
        }
    }

    @FXML
    private void onDownload(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        File f = dc.showDialog(tree.getScene().getWindow());
        if (f != null) {
            wrk.download(f, (FileItem) tree.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        FileItem selected = (FileItem) tree.getSelectionModel().getSelectedItem();
        DirectoryItem parent = selected.getParentDir();
        wrk.delete(selected);
        if (parent != null) {
            tree.getSelectionModel().select(parent);
        }
    }

    @FXML
    private void onNewFolder(ActionEvent event) {
        FileItem selected = (FileItem) tree.getSelectionModel().getSelectedItem();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Name");
        dialog.setContentText("Please enter your new folder name :");
        dialog.showAndWait().ifPresent((name) -> {
            if (name.contains("?") || name.contains("/")) {
                displayError("The name can't contain \"?\" or \"/\"");
            } else {
                wrk.createFolder(selected, name);
                selected.setExpanded(true);
            }
        });
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        refresh();
    }

    @FXML
    private void onSymlink(ActionEvent event) {
        clipboard = tree.getSelectionModel().getSelectedItem();
        if (clipboard != null) {
            clipboard.getGraphic().setOpacity(.5);
            tree.setContextMenu(linkContext);
        }
    }

    @FXML
    private void onCut(ActionEvent event) {
        clipboard = tree.getSelectionModel().getSelectedItem();
        if (clipboard != null) {
            clipboard.getGraphic().setOpacity(.5);
            tree.setContextMenu(cutContext);
        }
    }

    private void onCancel(ActionEvent event) {
        clipboard.getGraphic().setOpacity(1);
        clipboard = null;
        tree.setContextMenu(context);
    }

    private void onPaste(ActionEvent event) {
        TreeItem selected = tree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            clipboard.getGraphic().setOpacity(1);
            wrk.cutPaste((FileItem) clipboard, (FileItem) selected);
            tree.setContextMenu(context);
            clipboard = null;
        }
    }

    private void onCreateLink(ActionEvent event) {
        TreeItem selected = tree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            clipboard.getGraphic().setOpacity(1);
            wrk.symlink((FileItem) clipboard, (FileItem) selected);
            tree.setContextMenu(context);
            clipboard = null;
        }
    }

    private void refresh() {
        tree.setRoot(wrk.getRoot());
    }

    public FilesWrk getWrk() {
        return wrk;
    }

    public void displayError(String error) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(error);
        a.showAndWait();
    }

    private void createContexts() {
        MenuItem cancelPaste = new MenuItem("Cancel");
        cancelPaste.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE, KeyCodeCombination.SHORTCUT_ANY));
        cancelPaste.setOnAction((event) -> onCancel(event));
        MenuItem cancelLink = new MenuItem("Cancel");
        cancelLink.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE, KeyCodeCombination.SHORTCUT_ANY));
        cancelLink.setOnAction((event) -> onCancel(event));
        MenuItem paste = new MenuItem("Paste");
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCodeCombination.SHORTCUT_DOWN));
        paste.setOnAction((event) -> onPaste(event));
        MenuItem createLink = new MenuItem("Create Symlink");
        createLink.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCodeCombination.SHORTCUT_DOWN));
        createLink.setOnAction((event) -> onCreateLink(event));
        cutContext = new ContextMenu(paste, cancelPaste);
        linkContext = new ContextMenu(createLink, cancelLink);
    }

}

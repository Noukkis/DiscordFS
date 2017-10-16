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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            DiscordWrk bot = new DiscordWrk(new JDABuilder(AccountType.BOT).setToken(Statics.BOT_TOKEN).buildBlocking());
            wrk = new FilesWrk(bot);
            refresh();
            tree.setCellFactory((TreeView<String> list) -> new FileTreeCell(this));
            wrk.getUpdater().show();
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
        dialog.setTitle("Nouveau dossier");
        dialog.setHeaderText("Nom du nouveau dossier");
        dialog.setContentText("Veuillez entrer le nom du dossier :");
        dialog.showAndWait().ifPresent((name) -> {
            wrk.createFolder(selected, name);
            selected.setExpanded(true);
        });
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        refresh();
    }

    private void refresh() {
        tree.setRoot(wrk.getRoot());
    }

    public FilesWrk getWrk() {
        return wrk;
    }

}

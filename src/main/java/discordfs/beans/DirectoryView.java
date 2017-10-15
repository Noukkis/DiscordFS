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
package discordfs.beans;

import java.util.ArrayList;

/**
 *
 * @author Jordan Vesy
 */
public class DirectoryView extends FileView {

    private ArrayList<FileView> children;

    public DirectoryView(DirectoryView parent, String name, long id) {
        this(parent, name, id, false);
    }

    public DirectoryView(DirectoryView parent, String name, long id, boolean root) {
        super(parent, name, id, root);
        this.children = new ArrayList<>();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
    
    @Override
    public int getSize() {
        int n = 1;
        for (FileView child : children) {
            if(child.isDirectory()) {
                n += child.getSize();
            }
        }
        return n;
    }

    public ArrayList<FileView> getChildren() {
        return children;
    }
}
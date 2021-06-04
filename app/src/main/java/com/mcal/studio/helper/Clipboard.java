package com.mcal.studio.helper;

import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

public class Clipboard {

    private static final Clipboard CLIPBOARD = new Clipboard();
    private File currentFile = null;
    private Type type = Type.COPY;
    private TreeNode currentNode = null;

    public static Clipboard getInstance() {
        return CLIPBOARD;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TreeNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(TreeNode currentNode) {
        this.currentNode = currentNode;
    }

    public enum Type {
        COPY, CUT
    }
}

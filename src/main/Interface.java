package main;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Interface extends JFrame {

    JTree tree = new JTree();
    JPopupMenu menuFile = new JPopupMenu();
    JPopupMenu menuChunk = new JPopupMenu();
    private DefaultMutableTreeNode selectedNode;

    public Interface() {


        File filesFolder = new File("data/files");
        File[] listOfFiles = filesFolder.listFiles();
        File chunksFolder = new File("data/chunks");
        File[] listOfChunks = chunksFolder.listFiles();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Data");

        DefaultMutableTreeNode filesNode = new DefaultMutableTreeNode("Files");
        for (int i = 0; i < listOfFiles.length; i++)
            filesNode.add(new DefaultMutableTreeNode(listOfFiles[i]));

        DefaultMutableTreeNode chunksNode = new DefaultMutableTreeNode("Chunks");
        for (int i = 0; i < listOfChunks.length; i++)
            chunksNode.add(new DefaultMutableTreeNode(listOfChunks[i]));

        root.add(filesNode);
        root.add(chunksNode);

        //create the tree by passing in the root node
        tree = new JTree(root);


        JMenuItem item = new JMenuItem("Delete");
        item.addActionListener(getDeleteActionListener());
        menuFile.add(item);

        JMenuItem item2 = new JMenuItem("add");
        item2.addActionListener(getAddActionListener());
        menuFile.add(item2);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = getContentPane();
        content.add(tree, BorderLayout.CENTER);
        tree.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON3) {

                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (selectedNode.getParent().toString().equals("Files")) {
                        menuFile.show(tree, arg0.getX(), arg0.getY());
                    }
                }
                super.mousePressed(arg0);
            }
        });
        setSize(300, 300);
        setVisible(true);
    }

    private ActionListener getAddActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null) {
                    System.out.println("pressed" + selectedNode);
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("added");
                    selectedNode.add(n);
                    tree.repaint();
                    tree.updateUI();
                }
            }
        };
    }

    private ActionListener getDeleteActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null) {
                    try {
                        Config.deleteFile(selectedNode.toString());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("pressed" + selectedNode);
                }
            }
        };
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        new Interface();
    }
}
package main;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Interface extends JPanel {

    JTree tree = new JTree();
    private DefaultMutableTreeNode selectedNode;
    public DefaultMutableTreeNode root = new DefaultMutableTreeNode("Data");
    public DefaultMutableTreeNode filesNode = new DefaultMutableTreeNode("Files");
    public DefaultMutableTreeNode chunksNode = new DefaultMutableTreeNode("Chunks");
    public JPopupMenu menuFile = new JPopupMenu();
    public JPopupMenu menuChunk = new JPopupMenu();

    public Interface() {

        super(new BorderLayout());

        updateTree();

        root.add(filesNode);
        root.add(chunksNode);

        tree = new JTree(root);

        JMenuItem item = new JMenuItem("Delete");
        item.addActionListener(getDeleteActionListener());
        menuFile.add(item);

        JMenuItem item2 = new JMenuItem("Restore");
        item2.addActionListener(getRestoreActionListener());
        menuFile.add(item2);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){
                    TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    if(pathForLocation != null){
                        selectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getParent().toString().equals("Files"))
                            menuFile.show(tree, arg0.getX(), arg0.getY());
                    } else{
                        selectedNode = null;
                    }
                }
                super.mousePressed(arg0);
            }
        });

        JButton button1 = new JButton("Botao 1");
        //button1.setActionCommand();
        //button1.addActionListener(this);

        JButton button2 = new JButton("Botao 2");
        //button2.setActionCommand();
        //button2.addActionListener(this);

        JButton button3 = new JButton("Botao 3");
        //button3.setActionCommand();
        //button3.addActionListener(this);

        tree.setPreferredSize(new Dimension(300, 150));
        add(tree, BorderLayout.CENTER);

        JPanel jPanel = new JPanel(new GridLayout(0, 3));
        jPanel.add(button1);
        jPanel.add(button2);
        jPanel.add(button3);
        add(jPanel, BorderLayout.SOUTH);
    }

    public void updateTree(){
        filesNode.removeAllChildren();
        chunksNode.removeAllChildren();
        File filesFolder = new File("data/files");
        File[] listOfFiles = filesFolder.listFiles();
        File chunksFolder = new File("data/chunks");
        File[] listOfChunks = chunksFolder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++)
            if(listOfFiles[i].isFile())
                filesNode.add(new DefaultMutableTreeNode(listOfFiles[i].getName()));

        for (int i = 0; i < listOfChunks.length; i++)
            if(listOfChunks[i].isFile())
                chunksNode.add(new DefaultMutableTreeNode(listOfChunks[i].getName()));

        tree.repaint();
        tree.updateUI();

    }


    private ActionListener getDeleteActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null)
                    try {
                        Config.deleteFile(selectedNode.toString());
                        updateTree();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };
    }


    private ActionListener getRestoreActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null)
                    try {
                        Config.restoreFile(selectedNode.toString());
                        updateTree();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };
    }


    private static void runInterface() {
        JFrame jFrame = new JFrame("Project");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Interface contentPane = new Interface();
        contentPane.setOpaque(true);
        jFrame.setContentPane(contentPane);

        jFrame.pack();
        jFrame.setVisible(true);
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        Config.newFileInPath();
        runInterface();
    }
}

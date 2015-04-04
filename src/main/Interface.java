package main;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;


public class Interface extends JFrame
{
    private JTree tree;
    private JLabel selectedLabel;

    public Interface()
    {
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

        //ImageIcon imageIcon = new ImageIcon(TreeExample.class.getResource("file.png"));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
       // renderer.setLeafIcon(imageIcon);

        tree.setCellRenderer(renderer);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        add(new JScrollPane(tree));

        selectedLabel = new JLabel();
        add(selectedLabel, BorderLayout.SOUTH);

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                selectedLabel.setText(selectedNode.getUserObject().toString());
                if(selectedNode.getParent().toString().equals("Chunks"))
                    System.out.println("jjjk");
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Files");
        this.setSize(200, 200);
        this.setVisible(true);
    }

    public static void main(String[] args)
    {
new Interface();

    }
}
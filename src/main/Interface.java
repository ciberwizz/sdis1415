package main;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

public class Interface extends JPanel {

    public static JFrame jframe;
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

        JMenuItem itemf = new JMenuItem("Delete");
        itemf.addActionListener(getDeleteActionListener());
        menuFile.add(itemf);

        JMenuItem itemf2 = new JMenuItem("Restore");
        itemf2.addActionListener(getRestoreActionListener());
        menuFile.add(itemf2);

        JMenuItem itemc = new JMenuItem("Delete");
        itemc.addActionListener(getDeleteChunkActionListener());
        menuChunk.add(itemc);


        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){
                    TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    if(pathForLocation != null){
                        selectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getParent().toString().equals("Files"))
                            menuFile.show(tree, arg0.getX(), arg0.getY());
                        else if(selectedNode.getParent().toString().equals("Chunks"))
                            menuChunk.show(tree, arg0.getX(), arg0.getY());
                    } else{
                        selectedNode = null;
                    }
                }
                super.mousePressed(arg0);
            }
        });

        JButton newFiles = new JButton("Look For New Files");
        newFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Config.newFileInPath();
                updateTree();
            }
        });

        /*
        JButton button2 = new JButton("Botao 2");
        button2.setActionCommand();
        button2.addActionListener(this);

        JButton button3 = new JButton("Botao 3");
        button3.setActionCommand();
        button3.addActionListener(this);
*/
        tree.setPreferredSize(new Dimension(300, 150));
        add(tree, BorderLayout.CENTER);

        JPanel jPanel = new JPanel(new GridLayout(0, 3));
        jPanel.add(newFiles);
        add(jPanel, BorderLayout.SOUTH);
    }

    public void updateTree(){
        filesNode.removeAllChildren();
        chunksNode.removeAllChildren();

        Set set = Config.numberOfChunks.keySet();
        Set set2 = Config.chunksOfOurFiles.keySet();

        Iterator iterator = set.iterator();
        Iterator iterator2 = set2.iterator();

        while (iterator.hasNext())
        {
            Object o = iterator.next();
            filesNode.add(new DefaultMutableTreeNode(o.toString()));
        }

        while (iterator2.hasNext())
        {
            Object o = iterator2.next();
            chunksNode.add(new DefaultMutableTreeNode(o.toString()));
        }

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
                        if(!Config.missingChunks.isEmpty()){
                            JOptionPane.showMessageDialog(jframe, "There are some chunks missing. Wait a moment and try again, please.");
                        }
                        updateTree();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };
    }


    private ActionListener getDeleteChunkActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null)
                    try {
                        Config.deleteChunk(selectedNode.toString());
                        updateTree();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };
    }


    public static Interface runInterface() {
        jframe = new JFrame("Project");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Interface contentPane = new Interface();
        contentPane.setOpaque(true);
        jframe.setContentPane(contentPane);

        jframe.pack();
        jframe.setVisible(true);

        return contentPane;
    }

}

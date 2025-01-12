package ru.gr09262;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.gr09262.gui.AreaSelector;
import ru.gr09262.gui.FractalPainter;
import ru.gr09262.gui.UndoManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private final UndoManager undoManager = new UndoManager();
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.0, 1.0);
    private final JPanel mainPanel = new JPanel(){
        @Override
        public void paint(Graphics g){
            fPainter.paint(g);
        }
    };
    private final AreaSelector selector = new AreaSelector();
    private Point dragStartPoint = null;
    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem openItem = getOpenItem();

        JMenuItem saveItem = getSaveItem();

        JMenuItem photoMenu = getPhotoMenu();

        JMenuItem resetItem = new JMenuItem("Сбросить масштаб");
        resetItem.addActionListener(e -> {
            fPainter.resetCoordinates();
            undoManager.addOperation(fPainter::resetCoordinates);
            repaint();
        });

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(photoMenu);

        menuBar.add(fileMenu);
        menuBar.add(resetItem);
        menuBar.add(exitItem);

        setJMenuBar(menuBar);

        setSize(300, 200);
        return menuBar;
    }

    private JMenuItem getPhotoMenu() {
        JMenuItem photoMenu = new JMenuItem("Сохранить фото");
        photoMenu.addActionListener(e -> {
            JFileChooser savePhotoDialog = new JFileChooser();
            savePhotoDialog.setDialogTitle("Сохранение фото");
            savePhotoDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Photo (.png)", "png");
            savePhotoDialog.setFileFilter(filter);

            int result = savePhotoDialog.showSaveDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    savePanelAsImage(mainPanel, savePhotoDialog.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Фото успешно сохранено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при сохранении фото: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return photoMenu;
    }

    private JMenuItem getSaveItem() {
        JMenuItem saveItem = new JMenuItem("Сохранить");
        saveItem.addActionListener(e -> {
            JFileChooser saveFileDialog = new JFileChooser();
            saveFileDialog.setDialogTitle("Сохранение JSON конфига");
            saveFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "JSON (.json", "json");
            saveFileDialog.setFileFilter(filter);

            int result = saveFileDialog.showSaveDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    saveConfig(saveFileDialog.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Файл успешно сохранен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при сохранении конфига: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

        });
        return saveItem;
    }

    private JMenuItem getOpenItem() {
        JMenuItem openItem = new JMenuItem("Открыть");
        openItem.addActionListener(e -> {
            JFileChooser openFileDialog = new JFileChooser();
            openFileDialog.setDialogTitle("Открытие JSON конфига");
            openFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "JSON (.json", "json");
            openFileDialog.setFileFilter(filter);

            int result = openFileDialog.showOpenDialog(MainFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    openConfig(openFileDialog.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Файл успешно открыт!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при открытии конфига: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Ошибка при открытии конфига: " + ex.getMessage());
                }
            }

        });
        return openItem;
    }

    private void savePanelAsImage(JPanel panel, String filePath) throws Exception {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d);

        // Добавляем расширение ".png", если его нет
        if (!filePath.toLowerCase().endsWith(".png")) {
            filePath += ".png";
        }

        // Сохраняем изображение
        File outputFile = new File(filePath);
        ImageIO.write(image, "png", outputFile);
    }

    private void saveConfig(String filePath) throws IOException {
        if (!filePath.toLowerCase().endsWith(".json")) {
            filePath += ".json";
        }

        ObjectMapper mapper = new ObjectMapper();

        File outputFile = new File(filePath);
        mapper.writeValue(outputFile, fPainter);
    }

    private void openConfig(String filePath) throws IOException {
        if (!filePath.toLowerCase().endsWith(".json")) {
            throw new IOException("Не json файл");
        }

        ObjectMapper mapper = new ObjectMapper();
        File inputFile = new File(filePath);
        FractalPainter jsonPainter = mapper.readValue(inputFile, fPainter.getClass());
        fPainter.updateCoordinates(jsonPainter.getConverter().getXMin(), jsonPainter.getConverter().getXMax(),
                jsonPainter.getConverter().getYMin(), jsonPainter.getConverter().getYMax(), mainPanel.getWidth(), mainPanel.getHeight());
        mainPanel.repaint();
    }

    public MainFrame(){

        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        add(mainPanel);

        selector.setColor(Color.BLUE);

        setJMenuBar(createMenu());

        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!undoManager.canUndo()) fPainter.updateCoordinates(-2.0, 1.0, -1.0, 1.0, mainPanel.getWidth(), mainPanel.getHeight());
                undoManager.undo();
                undoManager.undo();
                System.out.println(undoManager.canUndo());
                mainPanel.repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
                undoManager.redo();
                System.out.println(undoManager.canRedo());
                mainPanel.repaint();
            }
        });

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                selector.setGraphics(mainPanel.getGraphics());
                fPainter.setWidth(mainPanel.getWidth());
                fPainter.setHeight(mainPanel.getHeight());
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(SwingUtilities.isLeftMouseButton(e)) {
                    selector.addPoint(e.getPoint());
                }
                if(SwingUtilities.isRightMouseButton(e)) {
                    dragStartPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(SwingUtilities.isLeftMouseButton(e)) {
                    selector.paint();
                    var rect = selector.getRect();
                    Point startPoint = rect.getStartPoint();
                    int width = rect.getWidth();
                    int height = rect.getHeigth();
                    if (startPoint != null && width > 0 && height > 0) {
                        var converter = fPainter.getConverter();
                        double xMin = converter.xScr2Crt(startPoint.x);
                        double yMax = converter.yScr2Crt(startPoint.y);
                        double xMax = converter.xScr2Crt(startPoint.x + width);
                        double yMin = converter.yScr2Crt(startPoint.y + height);
                        undoManager.addOperation(() -> fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight()));
                        fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
                        mainPanel.repaint();
                    }
                    selector.clearSelection();
                }
                if(SwingUtilities.isRightMouseButton(e)) {
                    Point dragEndPoint = e.getPoint();

                    int dx = dragStartPoint.x - dragEndPoint.x;
                    int dy = dragStartPoint.y - dragEndPoint.y;

                    double xShift = fPainter.getConverter().xScr2Crt(0) - fPainter.getConverter().xScr2Crt(dx);
                    double yShift = fPainter.getConverter().yScr2Crt(0) - fPainter.getConverter().yScr2Crt(dy);

                    double xMin = fPainter.getConverter().getXMin() - xShift;
                    double xMax = fPainter.getConverter().getXMax() - xShift;
                    double yMin = fPainter.getConverter().getYMin() - yShift;
                    double yMax = fPainter.getConverter().getYMax() - yShift;
                    undoManager.addOperation(() -> {
                        fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
                    });
                    fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
                    mainPanel.repaint();
                    dragStartPoint = null;
                }
            }
        });
        mainPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if(SwingUtilities.isLeftMouseButton(e)) {
                    selector.paint();
                    selector.addPoint(e.getPoint());
                    selector.paint();
                }
            }
        });
    }
}

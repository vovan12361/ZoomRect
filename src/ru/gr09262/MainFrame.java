package ru.gr09262;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jcodec.api.awt.AWTSequenceEncoder;
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
import java.util.ArrayList;
import java.util.Objects;


/**
 * Основной класс графического интерфейса приложения.
 */
public class MainFrame extends JFrame {
    private final UndoManager undoManager = new UndoManager();
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.0, 1.0);
    private final JPanel mainPanel = new JPanel(){
        @Override
        public void paint(Graphics g){
            fPainter.adjustCoordinatesToAspectRatio(mainPanel.getWidth(), mainPanel.getHeight());
            fPainter.paint(g);
        }
    };
    private final AreaSelector selector = new AreaSelector();
    private Point dragStartPoint = null;
    /**
     * Создает меню для окна приложения.
     *
     * @return объект JMenuBar, содержащий элементы меню.
     */
    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem openItem = getOpenItem();

        JMenuItem saveItem = getSaveItem();

        JMenuItem photoMenu = getPhotoMenu();

        JMenu colorMenu = new JMenu("Цветовая схема");

        JMenuItem videoItem = new JMenuItem("Видео");
        videoItem.addActionListener(e -> {
            try {
                startVideo();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        });

        JMenuItem backgroundColorDialog = new JMenuItem("Цвет, входящих в множество точек");
        backgroundColorDialog.addActionListener(e -> {
            Color inSetColor = JColorChooser.showDialog(MainFrame.this, "Выберите цвет", Color.black);
            fPainter.setInSetColor(inSetColor);
            repaint();
        });

        JMenuItem mainColorDialog = new JMenuItem("Дополнительный цвет");
        mainColorDialog.addActionListener(e -> {
            Color mainColor = JColorChooser.showDialog(MainFrame.this, "Выберите цвет", Color.RED);
            fPainter.setMainColor(mainColor);
            repaint();
        });

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

        colorMenu.add(backgroundColorDialog);
        colorMenu.addSeparator();
        colorMenu.add(mainColorDialog);

        menuBar.add(fileMenu);
        menuBar.add(resetItem);
        menuBar.add(colorMenu);
        menuBar.add(videoItem);
        menuBar.add(exitItem);

        setJMenuBar(menuBar);

        setSize(300, 200);
        return menuBar;
    }

    public void startVideo() {
        double xMinEnd = fPainter.getConverter().getXMin();
        double xMaxEnd = fPainter.getConverter().getXMax();
        double yMinEnd = fPainter.getConverter().getYMin();
        double yMaxEnd = fPainter.getConverter().getYMax();
        ArrayList<BufferedImage> listBI = new ArrayList<>();
        fPainter.resetCoordinates();
            new Thread(() -> {
                double xMin = fPainter.getConverter().getXMin();
                double xMax = fPainter.getConverter().getXMax();
                double yMin = fPainter.getConverter().getYMin();
                double yMax = fPainter.getConverter().getYMax();
//                double xMinEnd = -1.787770546568697, xMaxEnd = -1.7877660961343191, yMinEnd = -2.5517939243822196E-6, yMaxEnd = 3.1123952838655016E-6;
                int frames = 1200;
                double xMinStep = (xMinEnd - xMin) / frames;
                double xMaxStep = (xMaxEnd - xMax) / frames;
                double yMinStep = (yMinEnd - yMin) / frames;
                double yMaxStep = (yMaxEnd - yMax) / frames;
                for (int frame = 0; frame < frames; frame++) {
                    xMin += xMinStep;
                    xMax += xMaxStep;
                    yMin += yMinStep;
                    yMax += yMaxStep;
                    fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
                    BufferedImage image = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = image.createGraphics();
                    mainPanel.paint(g2d);
                    listBI.add(image);
                    mainPanel.repaint();
                    try {
                        Thread.sleep(250); // Задержка между кадрами (можно настроить)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AWTSequenceEncoder encoder = null;
                try {
                    encoder = AWTSequenceEncoder.createSequenceEncoder(new File("4.mp4"), 30);
                    for (BufferedImage image : listBI) {
                        encoder.encodeImage(image);
                    }
                    System.out.println("Video generated");
                } catch (Exception e) {
                    System.out.println("Fail to generate video!");

                }
                try {
                    Objects.requireNonNull(encoder).finish();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
    }

    /**
     * Создает пункт меню для сохранения изображения.
     *
     * @return объект JMenuItem для сохранения изображения.
     */
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

    /**
     * Создает пункт меню для сохранения конфигурации в формате JSON.
     *
     * @return объект JMenuItem для сохранения конфигурации.
     */
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

    /**
     * Создает пункт меню для открытия конфигурации из файла JSON.
     *
     * @return объект JMenuItem для открытия конфигурации.
     */
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
                    try {
                        openConfig(openFileDialog.getSelectedFile().getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Файл успешно открыт!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Ошибка при открытии конфига: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Ошибка при открытии конфига: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при открытии конфига: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Ошибка при открытии конфига: " + ex.getMessage());
                }
            }

        });
        return openItem;
    }

    /**
     * Метод для сохранения содержимого панели в виде изображения.
     *
     * @param panel    Панель, содержимое которой нужно сохранить.
     * @param filePath Путь к файлу, в который будет сохранено изображение.
     * @throws Exception Если возникает ошибка при сохранении изображения.
     */
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

    /**
     * Метод для сохранения текущей конфигурации фрактала в файл JSON.
     *
     * @param filePath Путь к файлу, в который будет сохранена конфигурация.
     * @throws IOException Если возникает ошибка при записи файла.
     */
    private void saveConfig(String filePath) throws IOException {
        if (!filePath.toLowerCase().endsWith(".json")) {
            filePath += ".json";
        }

        ObjectMapper mapper = new ObjectMapper();

        File outputFile = new File(filePath);
        mapper.writeValue(outputFile, fPainter);
    }

    /**
     * Метод для загрузки конфигурации фрактала из файла JSON.
     *
     * @param filePath Путь к файлу, из которого будет загружена конфигурация.
     * @throws IOException Если возникает ошибка при чтении файла или формат файла неверный.
     */
    private void openConfig(String filePath) throws IOException {
        if (!filePath.toLowerCase().endsWith(".json")) {
            throw new IOException("Не json файл");
        }

        ObjectMapper mapper = new ObjectMapper();
        File inputFile = new File(filePath);
        FractalPainter jsonPainter = mapper.readValue(inputFile, fPainter.getClass());

        double xMin = jsonPainter.getConverter().getXMin();
        double xMax = jsonPainter.getConverter().getXMax();
        double yMin = jsonPainter.getConverter().getYMin();
        double yMax = jsonPainter.getConverter().getYMax();

        if (xMin == 0.0 || xMax == 0.0 || yMin == 0.0 || yMax == 0.0) {
            throw new IOException("JSON файл содержит некорректные или неполные данные о координатах.");
        }
        fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
        mainPanel.repaint();
    }

    /**
     * Обрабатывает выделенную область для масштабирования.
     */
    private void processSelection() {
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

    /**
     * Обрабатывает перемещение области просмотра при правом клике и перетаскивании.
     *
     * @param e Событие мыши, связанное с перемещением.
     */
    private void processDrag(MouseEvent e) {
        Point dragEndPoint = e.getPoint();
        int dx = dragStartPoint.x - dragEndPoint.x;
        int dy = dragStartPoint.y - dragEndPoint.y;

        double xShift = fPainter.getConverter().xScr2Crt(0) - fPainter.getConverter().xScr2Crt(dx);
        double yShift = fPainter.getConverter().yScr2Crt(0) - fPainter.getConverter().yScr2Crt(dy);

        if (xShift != 0 && yShift != 0) {
            double xMin = fPainter.getConverter().getXMin() - xShift;
            double xMax = fPainter.getConverter().getXMax() - xShift;
            double yMin = fPainter.getConverter().getYMin() - yShift;
            double yMax = fPainter.getConverter().getYMax() - yShift;
            undoManager.addOperation(() -> fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight()));
            fPainter.updateCoordinates(xMin, xMax, yMin, yMax, mainPanel.getWidth(), mainPanel.getHeight());
            mainPanel.repaint();
        }
        dragStartPoint = null;
    }

    /**
     * Конструктор основного окна приложения.
     * Устанавливает параметры окна, настраивает меню и обработчики событий.
     */
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
                    processSelection();
                }
                if(SwingUtilities.isRightMouseButton(e)) {
                    processDrag(e);
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
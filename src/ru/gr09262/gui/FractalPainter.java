package ru.gr09262.gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gr09262.fractals.Mondelbrot;
import ru.gr09262.math.Complex;
import ru.gr09262.math.Converter;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Класс для отрисовки множества Мандельброта.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FractalPainter implements Painter{

    private final Mondelbrot mondelbrot = new Mondelbrot();
    private final Converter converter;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private Color inSetColor = Color.BLACK;
    private float[] mainColor = Color.RGBtoHSB(255,0,0,null);
    /**
     * Конструктор с указанием начальных границ области отображения фрактала.
     *
     * @param xMin Минимальное значение по оси X.
     * @param xMax Максимальное значение по оси X.
     * @param yMin Минимальное значение по оси Y.
     * @param yMax Максимальное значение по оси Y.
     */
    @JsonCreator
    public FractalPainter(@JsonProperty("xmin") double xMin,
                          @JsonProperty("xmax") double xMax,
                          @JsonProperty("ymin") double yMin,
                          @JsonProperty("ymax") double yMax){
        converter = new Converter(xMin,xMax,yMin,yMax,0,0);
    }

    /**
     * Возвращает объект конвертера координат.
     *
     * @return Экземпляр {@link Converter}.
     */
    public Converter getConverter() {
        return converter;
    }

    /**
     * Обновляет координаты области отображения фрактала.
     *
     * @param xMin       Минимальное значение по оси X.
     * @param xMax       Максимальное значение по оси X.
     * @param yMin       Минимальное значение по оси Y.
     * @param yMax       Максимальное значение по оси Y.
     * @param panelWidth Ширина панели.
     * @param panelHeight Высота панели.
     */
    public void updateCoordinates(double xMin, double xMax,
                                  double yMin, double yMax, int panelWidth, int panelHeight){
        converter.setXShape(xMin,xMax);
        converter.setYShape(yMin,yMax);
        adjustCoordinatesToAspectRatio(panelWidth, panelHeight);
    }

    /**
     * Корректирует координаты для сохранения пропорций изображения.
     *
     * @param panelWidth  Ширина панели.
     * @param panelHeight Высота панели.
     */
    public void adjustCoordinatesToAspectRatio(int panelWidth, int panelHeight) {
        double panelAspect = (double) panelWidth / panelHeight;
        double fractalWidth = converter.getXMax() - converter.getXMin();
        double fractalHeight = converter.getYMax() - converter.getYMin();
        double fractalAspect = fractalWidth / fractalHeight;

        if (panelAspect > fractalAspect) {
            double newWidth = fractalHeight * panelAspect;

            double centerX = (converter.getXMin() + converter.getXMax()) / 2;
            converter.setXShape(centerX - newWidth / 2, centerX + newWidth / 2);
        } else {
            double newHeight = fractalWidth / panelAspect;
            double centerY = (converter.getYMin() + converter.getYMax()) / 2;
            converter.setYShape(centerY - newHeight / 2, centerY + newHeight / 2);
        }
    }

    /**
     * Отрисовывает фрактал на графическом контексте.
     *
     * @param g Графический контекст.
     */
    @Override
    public void paint(Graphics g) {
        double fractalWidth = converter.getXMax() - converter.getXMin();
        double fractalHeight = converter.getYMax() - converter.getYMin();
        int maxIter = (int) Math.sqrt(15 / (fractalWidth * fractalHeight));
        mondelbrot.setIter(maxIter);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            int height = getHeight();
            int width = getWidth();
            int chunkSize = height / THREAD_COUNT;

            Future<?>[] tasks = new Future[THREAD_COUNT];

            for (int t = 0; t < THREAD_COUNT; t++) {
                final int startY = t * chunkSize;
                final int endY = (t == THREAD_COUNT - 1) ? height : (t + 1) * chunkSize;

                int finalMaxIter = mondelbrot.getIter();
                tasks[t] = executor.submit(() -> {
                    for (int i = 0; i < width; i++) {
                        for (int j = startY; j < endY; j++) {
                            var x = converter.xScr2Crt(i);
                            var y = converter.yScr2Crt(j);
                            var iterations = mondelbrot.isInSet(new Complex(x, y));
                            Color color;
                            if (iterations == finalMaxIter) {
                                color = inSetColor;
                            } else {
                                float hue = mainColor[0] + (float) iterations / finalMaxIter;
                                if (hue > 1.0f) hue -= 1.0f;
                                color = Color.getHSBColor(hue, mainColor[1], mainColor[2]);
                            }
                            synchronized (g) {
                                g.setColor(color);
                                g.fillRect(i, j, 1, 1);
                            }
                        }
                    }
                });
            }

            for (Future<?> task : tasks) {
                try {
                    task.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
        }
    }

    /**
     * Возвращает ширину панели.
     *
     * @return Ширина панели.
     */
    @Override
    @JsonIgnore
    public int getWidth() {
        return converter.getWidth();
    }

    /**
     * Устанавливает ширину панели.
     *
     * @param width Новая ширина панели.
     */
    @Override
    public void setWidth(int width) {
        converter.setWidth(width);
    }

    /**
     * Возвращает высоту панели.
     *
     * @return Высота панели.
     */
    @Override
    @JsonIgnore
    public int getHeight() {
        return converter.getHeight();
    }

    /**
     * Устанавливает высоту панели.
     */
    @Override
    public void setHeight(int height) {
        converter.setHeight(height);
    }

    /**
     * Сбрасывает координаты области отображения к стандартным значениям.
     */
    public void resetCoordinates() {
        updateCoordinates(-2.0, 1.0, -1.0, 1.0, getWidth(), getHeight());
    }

    /**
     * Устанавливает цвет для точек, принадлежащих множеству.
     *
     * @param inSetColor Новый цвет.
     */
    public void setInSetColor(Color inSetColor) {
        this.inSetColor = inSetColor;
    }

    /**
     * Устанавливает основной цвет для точек, не принадлежащих множеству.
     *
     * @param mainColor Новый основной цвет.
     */
    public void setMainColor(Color mainColor) {
        this.mainColor = Color.RGBtoHSB(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), null);
    }
}
